package com.bielzinrx.attracttochat.compat;

import com.bielzinrx.attracttochat.config.AttractToChatConfig;
import com.bielzinrx.attracttochat.engine.AtcEngine;
import com.bielzinrx.attracttochat.engine.MessageScore;
import com.bielzinrx.attracttochat.i18n.ServerTranslations;
import com.bielzinrx.attracttochat.platform.Platform;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * Optional, reflection-only integration with Walkie-Chat (mod id "walkietalkie").
 *
 * Walkie-Chat replaces vanilla chat with its own mixin and cancels the vanilla
 * path, so ATC's normal chat pipeline never sees those messages. This layer:
 *   1. registers a PROXIMITY_CHAT callback so ATC can still attract mobs to
 *      proximity chat spoken through Walkie-Chat (the callback receives the
 *      effective range, which Walkie-Chat already resolved through
 *      {@code AttractToChat.getEffectiveWalkieProximityRange});
 *   2. exposes {@code getEffectiveWalkieProximityRange}, which Walkie-Chat
 *      reflects on to decide how far its own proximity chat reaches;
 *   3. lets hostile mobs destroy placed Walkie Blocks (see
 *      {@code AtcEngine.destroyWalkieBlock}).
 *
 * Everything is reflective: ATC never hard-depends on Walkie-Chat classes.
 */
public final class WalkieChatCompat {

    private static final Logger LOGGER = LoggerFactory.getLogger("AttractToChat-WCCompat");
    private static final String WALKIE_MOD_ID = "walkietalkie";
    private static boolean initialized;

    private WalkieChatCompat() {}

    public static void init() {
        if (initialized || !Platform.getHelper().isModLoaded(WALKIE_MOD_ID)) return;
        initialized = true;

        try {
            Class<?> callbackClass = Class.forName("com.Theus452.walkietalkie.api.WalkieChatCallback");
            Field field = callbackClass.getField("PROXIMITY_CHAT");
            Object event = field.get(null);
            Method register = event.getClass().getMethod("register", Object.class);
            register.invoke(event, (Consumer<Object>) WalkieChatCompat::onProximityChat);
            LOGGER.info("[ATC] Walkie-Chat proximity chat callback registered.");
        } catch (ReflectiveOperationException exception) {
            initialized = false;
            LOGGER.error("[ATC] Walkie-Chat is loaded, but its callback API could not be registered.", exception);
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Walkie-Chat invokes this for every chat message it handles. Radio and
     * block-station paths pass range 0 (mobs there are attracted by
     * Walkie-Chat itself through AtcEngine.attractMobsAtPosition); only real
     * proximity chat carries a positive, already-resolved range.
     */
    private static void onProximityChat(Object data) {
        try {
            ServerPlayer sender = (ServerPlayer) invoke(data, "sender");
            String message = (String) invoke(data, "message");
            double range = (Double) invoke(data, "range");
            if (range <= 0.0 || !AttractToChatConfig.COMMON.walkieChatCompat.get()) return;
            if (!AtcEngine.shouldProcessWalkieSound(sender, message)) return;
            if (!AtcEngine.tryAcceptScan(sender, AtcEngine.isTrollPlayer(sender))) return;

            MessageScore score = new MessageScore(message, sender.getUUID());
            if (AtcEngine.applyVocalFatigue(sender, score)) return;
            AtcEngine.recordAcceptedScan(sender.getUUID());

            if (AtcEngine.isTrollPlayer(sender)) range *= 4.0;
            int[] attracted = AtcEngine.attractMobsAtPosition(
                (ServerLevel) sender.level, sender.blockPosition(), range, score);

            if (AtcEngine.isDebugMode()) {
                sender.displayClientMessage(ServerTranslations.component(sender,
                    "message.attracttochat.debug_info", range, attracted.length, score.caps), true);
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            LOGGER.error("[ATC] Failed to process Walkie-Chat proximity chat.", exception);
        }
    }

    private static Object invoke(Object target, String methodName) throws ReflectiveOperationException {
        return target.getClass().getMethod(methodName).invoke(target);
    }

    /**
     * Reads the frequency of a placed Walkie Block, or null when the block
     * entity is not a Walkie Block. Used by AtcEngine.destroyWalkieBlock to
     * notify players tuned to the destroyed station.
     */
    public static String getFrequencyIfWalkie(BlockEntity blockEntity) {
        if (blockEntity == null || !Platform.getHelper().isModLoaded(WALKIE_MOD_ID)) return null;
        try {
            Class<?> blockEntityClass = Class.forName("com.Theus452.walkietalkie.block.WalkieTalkieBlockEntity");
            if (blockEntityClass.isInstance(blockEntity)) {
                return (String) blockEntityClass.getMethod("getFrequency").invoke(blockEntity);
            }
        } catch (ReflectiveOperationException | ClassCastException exception) {
            LOGGER.debug("[ATC] Could not read Walkie Block frequency.", exception);
        }
        return null;
    }

    /**
     * True when the player is holding an active handheld Walkie (main or off
     * hand). While holding one, Walkie-Chat routes chat to the radio channel,
     * so ATC's own chat pipeline must not double-process the message.
     */
    public static boolean isActiveHandheldWalkie(ServerPlayer player) {
        if (player == null || !Platform.getHelper().isModLoaded(WALKIE_MOD_ID)) return false;
        return isActiveWalkieStack(player.getMainHandItem())
            || isActiveWalkieStack(player.getOffhandItem());
    }

    private static boolean isActiveWalkieStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        try {
            Class<?> itemClass = Class.forName("com.Theus452.walkietalkie.item.WalkieTalkieItem");
            if (!itemClass.isInstance(stack.getItem())) return false;
            Method getFrequency = itemClass.getMethod("getFrequency", ItemStack.class);
            String frequency = (String) getFrequency.invoke(null, stack);
            return frequency != null && !frequency.isEmpty();
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Delivers a "signal lost" push notification to every player tuned to the
     * given frequency (handheld radio or connected block), using Walkie-Chat's
     * own push-message pipeline. Used when a mob destroys a placed Walkie
     * Block. The message is resolved per player in their own language.
     */
    public static void notifyWalkieBlockDestroyed(ServerLevel level, String frequency, String langKey) {
        if (level == null || frequency == null || frequency.isBlank()
                || !Platform.getHelper().isModLoaded(WALKIE_MOD_ID)) {
            return;
        }
        try {
            Class<?> helperClass = Class.forName("com.Theus452.walkietalkie.util.WalkieMessageHelper");
            Method hasRadio = helperClass.getMethod("hasWalkieTalkieWithFrequency",
                ServerPlayer.class, String.class);
            Class<?> registryClass = Class.forName("com.Theus452.walkietalkie.networking.WalkieBlockRegistry");
            Method isConnected = registryClass.getMethod("isPlayerConnectedToAnyBlock",
                java.util.UUID.class, String.class);
            Class<?> networkClass = Class.forName("com.Theus452.walkietalkie.networking.WalkieNetworkHandler");
            Method sendPush = networkClass.getMethod("sendPushMessage",
                ServerPlayer.class, String.class, String.class, String.class);

            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                boolean tuned = Boolean.TRUE.equals(hasRadio.invoke(null, player, frequency))
                    || Boolean.TRUE.equals(isConnected.invoke(null, player.getUUID(), frequency));
                if (tuned) {
                    String text = ServerTranslations.component(player, langKey).getString();
                    sendPush.invoke(null, player, frequency, "ATC", text);
                }
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            LOGGER.debug("[ATC] Could not deliver Walkie Block destruction notice.", exception);
        }
    }
}
