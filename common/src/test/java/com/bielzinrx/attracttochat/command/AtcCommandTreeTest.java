package com.bielzinrx.attracttochat.command;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import org.junit.jupiter.api.Test;

class AtcCommandTreeTest {
    @Test
    void exposesReleaseCommandTreeAndOnlyPersonalParticleControls() {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        AtcCommand.register(dispatcher);

        var atc = dispatcher.getRoot().getChild("atc");
        assertNotNull(atc);
        assertNotNull(atc.getChild("help"));
        assertNotNull(atc.getChild("debug"));
        assertNotNull(atc.getChild("ignore"));
        assertNotNull(atc.getChild("trollmode"));
        assertNotNull(atc.getChild("preset"));
        assertNotNull(atc.getChild("feature").getChild("caps"));
        assertNotNull(atc.getChild("feature").getChild("fatigue"));
        assertNotNull(atc.getChild("feature").getChild("antispam"));
        assertNotNull(atc.getChild("client").getChild("particles").getChild("enable"));
        assertNotNull(atc.getChild("client").getChild("particles").getChild("disable"));
        assertNull(atc.getChild("config").getChild("particles"));
    }
}
