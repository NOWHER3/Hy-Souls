package me.nowhere;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import me.nowhere.HySouls.SoulHud.SoulCountCommand;
import me.nowhere.HySouls.SoulHud.SoulHudManager;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class main extends JavaPlugin {
    private static main instance;
    private SoulHudManager hudManager;

    public main(@Nonnull JavaPluginInit init) {
        super(init);
    }

    public static main get() {
        return instance;
    }

    protected void setup() {
        instance = this;
        this.getLogger().at(Level.INFO).log("Setup Complete!");
        this.getCommandRegistry().registerCommand(new SoulCountCommand());
        this.hudManager = new SoulHudManager(this);
        this.getLogger().at(Level.INFO).log("SoulHudManager started.");
    }

    protected void start() {
        this.getLogger().at(Level.INFO).log("Plugin Started!");
    }

    protected void shutdown() {
        if (this.hudManager != null) {
            this.hudManager.shutdown();
            this.hudManager = null;
        }

        this.getLogger().at(Level.INFO).log("Plugin Shutting Down!");
    }
}
