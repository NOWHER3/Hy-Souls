package com.nowhere;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.nowhere.SoulHud.command.SoulCountCommand;
import com.nowhere.SoulMenu.command.BonfireMenuCommand;
import com.nowhere.SoulHud.SoulHudManager;
import com.nowhere.SoulWarp.WarpConfigManager;
import com.nowhere.SoulWarp.WarpManager;
import com.nowhere.SoulWarp.command.DelWarpCommand;
import com.nowhere.SoulWarp.command.ListWarpsCommand;
import com.nowhere.SoulWarp.command.SetWarpCommand;
import com.nowhere.SoulWarp.command.WarpCommand;
import com.nowhere.SoulWarp.event.PlaceBlockSystem;
import com.nowhere.SoulWarp.event.BreakBlockSystem;
import com.nowhere.SoulMenu.interaction.OpenBonfireMenuInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;

import java.nio.file.Path;
import java.util.logging.Level;
import javax.annotation.Nonnull;

@SuppressWarnings("unused") // Instantiated by Hytale plugin framework via manifest.json
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

        this.getEntityStoreRegistry().registerSystem(new PlaceBlockSystem(warpManager));
        this.getEntityStoreRegistry().registerSystem(new BreakBlockSystem(warpManager));

        // Register bonfire menu interaction
        this.getCodecRegistry(Interaction.CODEC)
                .register("OpenBonfireMenu", OpenBonfireMenuInteraction.class, OpenBonfireMenuInteraction.CODEC);

        // Initialize SoulHud components
        this.getLogger().at(Level.INFO).log("Setup Complete!");
        this.getCommandRegistry().registerCommand(new SoulCountCommand());

        try {
            this.hudManager = new SoulHudManager(this);
            this.getLogger().at(Level.INFO).log("SoulHudManager started.");
        } catch (Exception e) {
            this.getLogger().at(Level.SEVERE).log("Failed to initialize SoulHudManager: " + e.getMessage(), e);
        }

        this.getCommandRegistry().registerCommand(new BonfireMenuCommand());
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

        // Shutdown WarpConfigManager to save all user configs
        WarpConfigManager.shutdown();

        this.getLogger().at(Level.INFO).log("Plugin Shutting Down!");
    }
}