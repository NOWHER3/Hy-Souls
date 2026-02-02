package com.nowhere.SoulHud;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class SoulDisplayPlugin extends JavaPlugin {
    private SoulHudManager hudManager;

    public SoulDisplayPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    protected void setup() {
        super.setup();
        this.getCommandRegistry().registerCommand(new SoulCountCommand());
        this.hudManager = new SoulHudManager(this);
        this.getLogger().at(Level.INFO).log("SoulDisplay plugin loaded! SoulHUD enabled.");
    }

    protected void shutdown() {
        super.shutdown();
        if (this.hudManager != null) {
            this.hudManager.shutdown();
            this.getLogger().at(Level.INFO).log("SoulDisplay plugin shut down.");
        }

    }
}
