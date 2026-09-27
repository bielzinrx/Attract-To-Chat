package com.bielzinrx.attracttochat.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import net.minecraft.commands.CommandSourceStack;
import org.junit.jupiter.api.Test;

class AtcCommandTreeTest {
    @Test
    void exposesOnlyPersonalParticleCommand() {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        AtcCommand.register(dispatcher);

        var atc = dispatcher.getRoot().getChild("atc");
        assertNotNull(atc.getChild("client").getChild("particles"));
        assertNull(atc.getChild("config").getChild("particles"));
    }

    @Test
    void playerArgumentsAcceptTheEveryoneWildcard() throws Exception {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        AtcCommand.register(dispatcher);

        var atc = dispatcher.getRoot().getChild("atc");
        for (String[] branch : new String[][] {
                { "ignore", "add" }, { "ignore", "remove" },
                { "trollmode", "add" }, { "trollmode", "remove" } }) {
            var parent = atc.getChild(branch[0]).getChild(branch[1]);
            assertNotNull(parent, () -> String.join(" ", branch));
            assertNotNull(parent.getChild("@a"), () -> String.join(" ", branch) + " -> @a");
            var name = parent.getChild("player_name");
            assertNotNull(name, () -> String.join(" ", branch) + " -> player_name");
            StringArgumentType type = (StringArgumentType)
                ((ArgumentCommandNode<CommandSourceStack, ?>) name).getType();
            assertEquals("Notch", type.parse(new StringReader("Notch")), () -> String.join(" ", branch));
        }
    }
}
