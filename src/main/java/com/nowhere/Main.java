package com.nowhere;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.nowhere.SoulHud.SoulCountCommand;
import com.nowhere.SoulHud.SoulHudManager;
import com.nowhere.SoulWarp.WarpConfigManager;
import com.nowhere.SoulWarp.WarpManager;
import com.nowhere.SoulWarp.command.DelWarpCommand;
import com.nowhere.SoulWarp.command.ListWarpsCommand;
import com.nowhere.SoulWarp.command.SetWarpCommand;
import com.nowhere.SoulWarp.command.WarpCommand;

import java.nio.file.Path;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class Main extends JavaPlugin {

    private static final Path DATA_DIR = Path.of("mods", "Hysouls");

    private static Main instance;
    private SoulHudManager hudManager;

    public Main(@Nonnull JavaPluginInit init) {
        super(init);
    }

    public static Main get() {
        return instance;
    }

    @Override
    protected void setup() {
        instance = this;

        // Initialize SoulWarp components
        WarpConfigManager.init(this, DATA_DIR);


        WarpManager warpManager = new WarpManager();
        this.getCommandRegistry().registerCommand(new WarpCommand(warpManager));
        this.getCommandRegistry().registerCommand(new SetWarpCommand(warpManager));
        this.getCommandRegistry().registerCommand(new ListWarpsCommand(warpManager));
        this.getCommandRegistry().registerCommand(new DelWarpCommand(warpManager));


        // Initialize SoulHud components
        this.getLogger().at(Level.INFO).log("Setup Complete!");
        this.getCommandRegistry().registerCommand(new SoulCountCommand());

        try {
            this.hudManager = new SoulHudManager(this);
            this.getLogger().at(Level.INFO).log("SoulHudManager started.");
        } catch (Exception e) {
            this.getLogger().at(Level.SEVERE).log("Failed to initialize SoulHudManager: " + e.getMessage(), e);
        }
    }

    @Override
    protected void start() {
        this.getLogger().at(Level.INFO).log("Plugin Started!");
    }

    @Override
    protected void shutdown() {
        if (this.hudManager != null) {
            this.hudManager.shutdown();
            this.hudManager = null;
        }

        this.getLogger().at(Level.INFO).log("Plugin Shutting Down!");
    }
}