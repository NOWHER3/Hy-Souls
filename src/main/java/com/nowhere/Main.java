package com.nowhere;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.nowhere.SoulHud.command.SoulCountCommand;
import com.nowhere.SoulHud.command.SoulHudPosCommand;
import com.nowhere.SoulHud.command.ToggleSoulHudCommand;
import com.nowhere.SoulHud.config.HudConfigManager;
import com.nowhere.SoulMenu.command.BonfireMenuCommand;
import com.nowhere.Help.HysoulsHelpCommand;
import com.nowhere.SoulHud.SoulHudManager;
import com.nowhere.SoulCurrency.SoulManager;
import com.nowhere.SoulWarp.WarpConfigManager;
import com.nowhere.SoulWarp.WarpManager;
import com.nowhere.SoulWarp.command.DelWarpCommand;
import com.nowhere.SoulWarp.command.ListWarpsCommand;
import com.nowhere.SoulWarp.command.SetWarpCommand;
import com.nowhere.SoulWarp.command.WarpCommand;
import com.nowhere.SoulWarp.event.PlaceBlockSystem;
import com.nowhere.SoulWarp.event.BreakBlockSystem;
import com.nowhere.SoulMenu.interaction.OpenBonfireMenuInteraction;
import com.nowhere.SoulCurrency.ConsumeSoulEssenceInteraction;
import com.nowhere.SoulDrops.SoulDropConfigManager;
import com.nowhere.SoulDrops.SoulDropsSystem;
import com.nowhere.SoulDrops.util.NPCCategoryUtil;
import com.nowhere.Estus.EstusConfigManager;
import com.nowhere.Estus.EstusDropRequestSystem;
import com.nowhere.Estus.EstusDropSystem;
import com.nowhere.Estus.EstusManager;
import com.nowhere.Estus.command.EstusSlotCommand;
import com.nowhere.Humanity.HumanityManager;
import com.nowhere.Humanity.HumanityHudManager;
import com.nowhere.Humanity.ConsumeHumanityEssenceInteraction;
import com.nowhere.Humanity.config.HumanityHudConfigManager;
import com.nowhere.Humanity.command.HumanityCountCommand;
import com.nowhere.Humanity.command.HumanityHudPosCommand;
import com.nowhere.Humanity.command.ToggleHumanityHudCommand;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;

import java.nio.file.Path;
import java.util.logging.Level;
import javax.annotation.Nonnull;

@SuppressWarnings("unused") // Instantiated by Hytale plugin framework via manifest.json
public class Main extends JavaPlugin {

    private static final Path DATA_DIR = Path.of("mods", "Hysouls");

    private static Main instance;
    private SoulHudManager hudManager;
    private EstusManager estusManager;
    private HumanityHudManager humanityHudManager;

    public Main(@Nonnull JavaPluginInit init) {
        super(init);
    }

    public static Main get() {
        return instance;
    }

    public SoulHudManager getHudManager() {
        return this.hudManager;
    }

    public EstusManager getEstusManager() {
        return this.estusManager;
    }

    public HumanityHudManager getHumanityHudManager() {
        return this.humanityHudManager;
    }

    @Override
    protected void setup() {
        instance = this;

        // Initialize soul counter
        SoulManager.init(DATA_DIR);

        // Initialize SoulWarp components
        WarpConfigManager.init(this, DATA_DIR);

        WarpManager warpManager = new WarpManager();
        this.getCommandRegistry().registerCommand(new WarpCommand(warpManager));
        this.getCommandRegistry().registerCommand(new SetWarpCommand(warpManager));
        this.getCommandRegistry().registerCommand(new ListWarpsCommand(warpManager));
        this.getCommandRegistry().registerCommand(new DelWarpCommand(warpManager));

        this.getEntityStoreRegistry().registerSystem(new PlaceBlockSystem(warpManager));
        this.getEntityStoreRegistry().registerSystem(new BreakBlockSystem(warpManager));

        // Initialize SoulDrops system
        SoulDropConfigManager.loadConfig();
        this.getEntityStoreRegistry().registerSystem(new SoulDropsSystem());
        this.getLogger().at(Level.INFO).log("SoulDrops system registered");

        // Register bonfire menu interaction
        this.getCodecRegistry(Interaction.CODEC)
                .register("OpenBonfireMenu", OpenBonfireMenuInteraction.class, OpenBonfireMenuInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC)
                .register("ConsumeSoulEssence", ConsumeSoulEssenceInteraction.class, ConsumeSoulEssenceInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC)
                .register("ConsumeHumanityEssence", ConsumeHumanityEssenceInteraction.class, ConsumeHumanityEssenceInteraction.CODEC);

        // Initialize Humanity components
        HumanityManager.init(DATA_DIR);
        HumanityHudConfigManager.init(DATA_DIR);
        this.getCommandRegistry().registerCommand(new HumanityCountCommand());
        this.getCommandRegistry().registerCommand(new HumanityHudPosCommand());
        this.getCommandRegistry().registerCommand(new ToggleHumanityHudCommand());

        this.humanityHudManager = new HumanityHudManager();

        // Initialize SoulHud components
        HudConfigManager.init(DATA_DIR);
        this.getLogger().at(Level.INFO).log("Setup Complete!");
        this.getCommandRegistry().registerCommand(new SoulCountCommand());
        this.getCommandRegistry().registerCommand(new SoulHudPosCommand());
        this.getCommandRegistry().registerCommand(new ToggleSoulHudCommand());

        try {
            this.hudManager = new SoulHudManager(this);
            this.getLogger().at(Level.INFO).log("SoulHudManager started.");
        } catch (Exception e) {
            this.getLogger().at(Level.SEVERE).log("Failed to initialize SoulHudManager: " + e.getMessage(), e);
        }

        // Initialize Estus Flask system
        EstusConfigManager.init(DATA_DIR);
        this.getEntityStoreRegistry().registerSystem(new EstusDropSystem());
        this.getEntityStoreRegistry().registerSystem(new EstusDropRequestSystem());
        this.getCommandRegistry().registerCommand(new EstusSlotCommand());

        try {
            this.estusManager = new EstusManager(this);
            this.getLogger().at(Level.INFO).log("EstusManager started.");
        } catch (Exception e) {
            this.getLogger().at(Level.SEVERE).log("Failed to initialize EstusManager: " + e.getMessage(), e);
        }

        this.getCommandRegistry().registerCommand(new BonfireMenuCommand());
        this.getCommandRegistry().registerCommand(new HysoulsHelpCommand());
    }

    @Override
    protected void start() {
        // Initialize NPC category detection (after NPCGroup assets are loaded)
        NPCCategoryUtil.initialize();
        this.getLogger().at(Level.INFO).log("Plugin Started!");
    }

    @Override
    protected void shutdown() {
        if (this.hudManager != null) {
            this.hudManager.shutdown();
            this.hudManager = null;
        }

        if (this.estusManager != null) {
            this.estusManager.shutdown();
            this.estusManager = null;
        }

        if (this.humanityHudManager != null) {
            this.humanityHudManager.shutdown();
            this.humanityHudManager = null;
        }

        // Shutdown soul counter, warp configs, HUD configs, estus configs, and humanity
        SoulManager.shutdown();
        WarpConfigManager.shutdown();
        HudConfigManager.shutdown();
        EstusConfigManager.shutdown();
        HumanityManager.shutdown();
        HumanityHudConfigManager.shutdown();

        this.getLogger().at(Level.INFO).log("Plugin Shutting Down!");
    }
}