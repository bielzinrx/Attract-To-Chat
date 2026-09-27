package com.bielzinrx.attracttochat.compat;

import com.bielzinrx.attracttochat.AttractToChat;
import com.bielzinrx.attracttochat.config.AttractToChatConfig;
import com.bielzinrx.attracttochat.engine.AtcEngine;
import com.bielzinrx.attracttochat.engine.MessageScore;
import com.bielzinrx.attracttochat.i18n.ServerTranslations;
import com.bielzinrx.attracttochat.platform.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class WalkieChatCompat {

    private static final Logger LOGGER = LoggerFactory.getLogger("AttractToChat-WCCompat");
    private static final String WALKIE_MOD_ID = "walkietalkie";
    private static final String CALLBACK_API_CLASS = "com.Theus452.walkietalkie.api.WalkieChatCallback";
    private static final String BLOCK_RELAY_CLASS = "com.Theus452.walkietalkie.util.WalkieBlockMessageRelay";
    private static final String WALKIE_ITEM_CLASS = "com.Theus452.walkietalkie.item.WalkieTalkieItem";
    private static final String MESSAGE_HELPER_CLASS = "com.Theus452.walkietalkie.util.WalkieMessageHelper";
    private static final String WALKIE_BLOCK_ENTITY_CLASS = "com.Theus452.walkietalkie.block.WalkieTalkieBlockEntity";
    private static final String CONNECTION_MANAGER_CLASS = "com.Theus452.walkietalkie.util.ConnectionManager";
    private static final String WALKIE_CHAT_PREFIX = "\u00a7a[Walkie-Talkie] ";
    private static final ResourceLocation WALKIE_RECEIVER_SOUND = new ResourceLocation("walkietalkie", "msg_receiver");

    private static boolean initialized;
    private static boolean callbackApiAvailable;
    private static boolean blockRelayAvailable;

    private static Class<?> walkieItemClass;
    private static Method walkieGetFrequency;
    private static Class<?> walkieBlockEntityClass;
    private static Method walkieBlockEntityGetFrequency;
    private static Method helperFindNearbyActiveBlock;
    private static Method helperHasWalkieTalkieWithFrequency;
    private static Method connectionHasFrequency;
    private static SoundEvent receiverChime;

    private WalkieChatCompat() {}

    public static void init() {
        if (initialized || !Platform.getHelper().isModLoaded(WALKIE_MOD_ID)) return;
        initialized = true;

        walkieItemClass = findClass(WALKIE_ITEM_CLASS, "handheld walkie probe");
        if (walkieItemClass != null) {
            walkieGetFrequency = findMethod(walkieItemClass, "getFrequency", "handheld frequency lookup", ItemStack.class);
        }

        walkieBlockEntityClass = findClass(WALKIE_BLOCK_ENTITY_CLASS, "walkie block entity");
        if (walkieBlockEntityClass != null) {
            walkieBlockEntityGetFrequency = findMethod(walkieBlockEntityClass, "getFrequency",
                "walkie block frequency lookup");
        }

        Class<?> helperClass = findClass(MESSAGE_HELPER_CLASS, "walkie message helper");
        if (helperClass != null) {
            helperFindNearbyActiveBlock = findMethod(helperClass, "findNearbyActiveBlock",
                "near-station probe", ServerPlayer.class);
            helperHasWalkieTalkieWithFrequency = findMethod(helperClass, "hasWalkieTalkieWithFrequency",
                "tuned-player probe", ServerPlayer.class, String.class);
        }

        Class<?> connectionClass = findClass(CONNECTION_MANAGER_CLASS, "connection manager");
        if (connectionClass != null) {
            connectionHasFrequency = findMethod(connectionClass, "hasConnectionWithFrequency",
                "frequency connection probe", ServerPlayer.class, String.class);
        }
        if (BuiltInRegistries.SOUND_EVENT.containsKey(WALKIE_RECEIVER_SOUND)) {
            receiverChime = BuiltInRegistries.SOUND_EVENT.get(WALKIE_RECEIVER_SOUND);
        }

        blockRelayAvailable = findClass(BLOCK_RELAY_CLASS, "block relay") != null;

        try {
            Class<?> callbackClass = Class.forName(CALLBACK_API_CLASS);
            Field field = callbackClass.getField("PROXIMITY_CHAT");
            Object event = field.get(null);
            Method register = event.getClass().getMethod("register", Object.class);
            register.invoke(event, (Consumer<Object>) WalkieChatCompat::onProximityChat);
            callbackApiAvailable = true;
            LOGGER.info("[ATC] Walkie-Chat proximity chat callback registered.");
        } catch (ClassNotFoundException noCallbackApi) {

            LOGGER.info("[ATC] Walkie-Chat has no callback API on this build; "
                + "handheld chat keeps flowing through ATC's own pipeline.");
        } catch (ReflectiveOperationException exception) {
            LOGGER.warn("[ATC] Walkie-Chat is loaded, but its callback API could not be registered.", exception);
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static boolean integrationActive() {
        return Platform.getHelper().isModLoaded(WALKIE_MOD_ID)
            && AttractToChatConfig.COMMON.walkieChatCompat.get();
    }

    public static boolean isCallbackApiAvailable() {
        return callbackApiAvailable;
    }

    public static boolean isBlockRelayAvailable() {
        return blockRelayAvailable;
    }

    public static boolean suppressesHandheldChat(ServerPlayer player) {
        return integrationActive() && callbackApiAvailable && isActiveHandheldWalkie(player);
    }

    public static BlockPos stationRelayPosition(ServerPlayer player) {
        if (!integrationActive() || !blockRelayAvailable) return null;
        String held = heldWalkieFrequency(player);
        if (held == null) return null;
        BlockEntity block = nearbyActiveBlock(player);
        if (block == null) return null;
        String frequency = getFrequencyIfWalkie(block);
        return frequency != null && frequency.equalsIgnoreCase(held) ? block.getBlockPos() : null;
    }

    private static void onProximityChat(Object data) {
        try {
            ServerPlayer sender = (ServerPlayer) invoke(data, "sender");
            String message = (String) invoke(data, "message");
            double reported = (Double) invoke(data, "range");
            if (reported <= 0.0 || !integrationActive()) return;
            double range = AttractToChat.getEffectiveWalkieProximityRange(message, reported);
            if (!AtcEngine.shouldProcessWalkieSound(sender, message)) return;

            MessageScore score = new MessageScore(message, sender.getUUID());
            if (AtcEngine.isTrollPlayer(sender)) range *= 4.0;

            AtcEngine.attractMobsAtPosition(
                (ServerLevel) sender.level(), sender.blockPosition(), range, score);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            LOGGER.error("[ATC] Failed to process Walkie-Chat proximity chat.", exception);
        }
    }

    private static Object invoke(Object target, String methodName) throws ReflectiveOperationException {
        return target.getClass().getMethod(methodName).invoke(target);
    }

    private static Class<?> findClass(String className, String purpose) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException missing) {
            LOGGER.info("[ATC] Walkie-Chat build has no {} ({}).", className, purpose);
            return null;
        } catch (LinkageError broken) {
            LOGGER.warn("[ATC] Walkie-Chat class {} could not be linked ({}).", className, purpose, broken);
            return null;
        }
    }

    private static Method findMethod(Class<?> owner, String name, String purpose, Class<?>... params) {
        try {
            return owner.getMethod(name, params);
        } catch (NoSuchMethodException missing) {
            LOGGER.warn("[ATC] Walkie-Chat build is missing the method {} ({}).", name, purpose);
            return null;
        }
    }

    public static String getFrequencyIfWalkie(BlockEntity blockEntity) {
        if (blockEntity == null || walkieBlockEntityClass == null
                || walkieBlockEntityGetFrequency == null
                || !Platform.getHelper().isModLoaded(WALKIE_MOD_ID)) {
            return null;
        }
        try {
            if (walkieBlockEntityClass.isInstance(blockEntity)) {
                return (String) walkieBlockEntityGetFrequency.invoke(blockEntity);
            }
        } catch (ReflectiveOperationException | ClassCastException exception) {
            LOGGER.debug("[ATC] Could not read Walkie Block frequency.", exception);
        }
        return null;
    }

    public static boolean isActiveHandheldWalkie(ServerPlayer player) {
        return heldWalkieFrequency(player) != null;
    }

    private static String heldWalkieFrequency(ServerPlayer player) {
        if (player == null || walkieItemClass == null || walkieGetFrequency == null
                || !Platform.getHelper().isModLoaded(WALKIE_MOD_ID)) {
            return null;
        }
        String main = walkieStackFrequency(player.getMainHandItem());
        return main != null ? main : walkieStackFrequency(player.getOffhandItem());
    }

    private static String walkieStackFrequency(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !walkieItemClass.isInstance(stack.getItem())) return null;
        try {
            String frequency = (String) walkieGetFrequency.invoke(null, stack);
            return frequency == null || frequency.isEmpty() ? null : frequency;
        } catch (ReflectiveOperationException failure) {
            LOGGER.debug("[ATC] Could not read handheld walkie frequency.", failure);
            return null;
        }
    }

    private static BlockEntity nearbyActiveBlock(ServerPlayer player) {
        if (player == null || helperFindNearbyActiveBlock == null
                || !Platform.getHelper().isModLoaded(WALKIE_MOD_ID)) {
            return null;
        }
        try {
            return (BlockEntity) helperFindNearbyActiveBlock.invoke(null, player);
        } catch (ReflectiveOperationException | ClassCastException failure) {
            LOGGER.debug("[ATC] Could not probe nearby Walkie Block.", failure);
            return null;
        }
    }

    public static void notifyWalkieBlockDestroyed(ServerLevel level, String frequency, String langKey,
            Object... args) {
        if (level == null || frequency == null || frequency.isBlank()
                || helperHasWalkieTalkieWithFrequency == null) {
            return;
        }
        try {
            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                if (!isTunedTo(player, frequency)) continue;
                player.sendSystemMessage(Component.literal(WALKIE_CHAT_PREFIX)
                    .append(ServerTranslations.component(player, langKey, args)));
                if (receiverChime != null) {
                    player.playNotifySound(receiverChime, SoundSource.PLAYERS, 0.7F, 1.0F);
                }
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            LOGGER.debug("[ATC] Could not deliver Walkie Block destruction notice.", exception);
        }
    }

    private static boolean isTunedTo(ServerPlayer player, String frequency)
            throws ReflectiveOperationException {
        return Boolean.TRUE.equals(helperHasWalkieTalkieWithFrequency.invoke(null, player, frequency))
            || (connectionHasFrequency != null
                && Boolean.TRUE.equals(connectionHasFrequency.invoke(null, player, frequency)));
    }
}