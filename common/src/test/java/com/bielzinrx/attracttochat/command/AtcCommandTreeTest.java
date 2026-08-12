package com.bielzinrx.attracttochat.command;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.mojang.brigadier.CommandDispatcher;
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
}
