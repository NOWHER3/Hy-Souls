package com.nowhere.hysouls;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.nowhere.hysouls.display.soul.command.SoulCountCommand;
import com.nowhere.hysouls.display.soul.command.SoulHudPosCommand;
import com.nowhere.hysouls.display.soul.command.ToggleSoulHudCommand;
import com.nowhere.hysouls.display.soul.config.HudConfigManager;
import com.nowhere.hysouls.menu.command.BonfireMenuCommand;
import com.nowhere.hysouls.help.HysoulsHelpCommand;
import com.nowhere.hysouls.display.soul.SoulHudManager;
import com.nowhere.hysouls.currency.soul.SoulManager;
import com.nowhere.hysouls.warp.WarpConfigManager;
import com.nowhere.hysouls.warp.WarpManager;
import com.nowhere.hysouls.warp.command.DelWarpCommand;
import com.nowhere.hysouls.warp.command.ListWarpsCommand;
import com.nowhere.hysouls.warp.command.SetWarpCommand;
import com.nowhere.hysouls.warp.command.WarpCommand;
import com.nowhere.hysouls.warp.event.PlaceBlockSystem;
import com.nowhere.hysouls.warp.event.BreakBlockSystem;
import com.nowhere.hysouls.menu.interaction.OpenBonfireMenuInteraction;
import com.nowhere.hysouls.currency.soul.ConsumeSoulEssenceInteraction;
import com.nowhere.hysouls.drops.SoulDropConfigManager;
import com.nowhere.hysouls.drops.SoulDropsSystem;
import com.nowhere.hysouls.drops.util.NPCCategoryUtil;
import com.nowhere.hysouls.loot.ChestLootConfigManager;
import com.nowhere.hysouls.loot.ChestLootSystem;
import com.nowhere.hysouls.loot.PlayerPlacedChestTracker;
import com.nowhere.hysouls.loot.PlayerChestBreakTracker;
import com.nowhere.hysouls.consumable.estus.EstusConfigManager;
import com.nowhere.hysouls.consumable.estus.EstusDropRequestSystem;
import com.nowhere.hysouls.consumable.estus.EstusDropSystem;
import com.nowhere.hysouls.consumable.estus.EstusManager;
import com.nowhere.hysouls.consumable.estus.command.EstusSlotCommand;
import com.nowhere.hysouls.currency.humanity.HumanityManager;
import com.nowhere.hysouls.display.humanity.HumanityHudManager;
import com.nowhere.hysouls.currency.humanity.ConsumeHumanityEssenceInteraction;
import com.nowhere.hysouls.display.humanity.config.HumanityHudConfigManager;
import com.nowhere.hysouls.display.humanity.command.HumanityCountCommand;
import com.nowhere.hysouls.display.humanity.command.HumanityHudPosCommand;
import com.nowhere.hysouls.display.humanity.command.ToggleHumanityHudCommand;
import com.nowhere.hysouls.menu.BonfireRestService;
import com.nowhere.hysouls.menu.kindle.KindleConfigManager;
import com.nowhere.hysouls.appearance.HollowManager;
import com.nowhere.hysouls.appearance.HollowEventHandler;
import com.nowhere.hysouls.appearance.PostRespawnAppearanceSystem;
import com.nowhere.hysouls.death.DeathPenaltySystem;
import com.nowhere.hysouls.death.DeathDropProcessor;
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
    private BonfireRestService bonfireRestService;
    private HollowEventHandler hollowEventHandler;
    private DeathPenaltySystem deathPenaltySystem;
    private PostRespawnAppearanceSystem postRespawnAppearanceSystem;

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

    public BonfireRestService getBonfireRestService() {
        return this.bonfireRestService;
    }

    @Override
    protected void setup() {
        instance = this;

        // Initialize 3-folder config system (must be first!)
        com.nowhere.hysouls.config.PlayerDataManager.init(DATA_DIR);
        com.nowhere.hysouls.config.UserPreferencesManager.init(DATA_DIR);
        this.getLogger().at(Level.INFO).log("PlayerDataManager and UserPreferencesManager initialized");

        // Run migration from old config structure to new 3-folder structure
        try {
            com.nowhere.hysouls.config.ConfigMigration.migrate(DATA_DIR);
            this.getLogger().at(Level.INFO).log("Config migration completed");
        } catch (Exception e) {
            this.getLogger().at(Level.SEVERE).log("Config migration failed: " + e.getMessage(), e);
        }

        // Initialize SoulWarp components (uses separate config - NOT migrated)
        WarpConfigManager.init(this, DATA_DIR);

        WarpManager warpManager = new WarpManager();
        this.getCommandRegistry().registerCommand(new WarpCommand(warpManager));
        this.getCommandRegistry().registerCommand(new SetWarpCommand(warpManager));
        this.getCommandRegistry().registerCommand(new ListWarpsCommand(warpManager));
        this.getCommandRegistry().registerCommand(new DelWarpCommand(warpManager));

        this.getEntityStoreRegistry().registerSystem(new PlaceBlockSystem(warpManager));
        this.getEntityStoreRegistry().registerSystem(new BreakBlockSystem(warpManager));

        // Initialize SoulDrops system (uses separate config - NOT migrated)
        SoulDropConfigManager.loadConfig();
        this.getEntityStoreRegistry().registerSystem(new SoulDropsSystem());
        this.getLogger().at(Level.INFO).log("SoulDrops system registered");

        // Initialize ChestLoot system (uses separate config - NOT migrated)
        ChestLootConfigManager.loadConfig();
        this.getChunkStoreRegistry().registerSystem(new ChestLootSystem());
        this.getEntityStoreRegistry().registerSystem(new PlayerPlacedChestTracker());
        this.getEntityStoreRegistry().registerSystem(new PlayerChestBreakTracker());
        this.getLogger().at(Level.INFO).log("ChestLoot system registered");

        // Register bonfire menu interaction
        this.getCodecRegistry(Interaction.CODEC)
                .register("OpenBonfireMenu", OpenBonfireMenuInteraction.class, OpenBonfireMenuInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC)
                .register("ConsumeSoulEssence", ConsumeSoulEssenceInteraction.class, ConsumeSoulEssenceInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC)
                .register("ConsumeHumanityEssence", ConsumeHumanityEssenceInteraction.class, ConsumeHumanityEssenceInteraction.CODEC);

        // Register Humanity commands
        this.getCommandRegistry().registerCommand(new HumanityCountCommand());
        this.getCommandRegistry().registerCommand(new HumanityHudPosCommand());
        this.getCommandRegistry().registerCommand(new ToggleHumanityHudCommand());

        this.humanityHudManager = new HumanityHudManager();

        // Initialize Hollow event handler
        this.hollowEventHandler = new HollowEventHandler(this);

        // Initialize and register Death Penalty ECS system
        this.deathPenaltySystem = new DeathPenaltySystem();
        this.getEntityStoreRegistry().registerSystem(this.deathPenaltySystem);
        this.getLogger().at(Level.INFO).log("DeathPenaltySystem registered.");

        // Death drop processor (spawns queued item drops on next tick)
        this.getEntityStoreRegistry().registerSystem(new DeathDropProcessor());
        this.getLogger().at(Level.INFO).log("DeathDropProcessor registered.");

        // Initialize and register Post-Respawn Appearance System
        this.postRespawnAppearanceSystem = new PostRespawnAppearanceSystem();
        this.getEntityStoreRegistry().registerSystem(this.postRespawnAppearanceSystem);
        this.getLogger().at(Level.INFO).log("PostRespawnAppearanceSystem registered.");

        // Register SoulHud commands
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
        this.getEntityStoreRegistry().registerSystem(new EstusDropSystem());
        this.getEntityStoreRegistry().registerSystem(new EstusDropRequestSystem());
        this.getCommandRegistry().registerCommand(new EstusSlotCommand());

        try {
            this.estusManager = new EstusManager(this);
            this.getLogger().at(Level.INFO).log("EstusManager started.");
        } catch (Exception e) {
            this.getLogger().at(Level.SEVERE).log("Failed to initialize EstusManager: " + e.getMessage(), e);
        }

        // Initialize BonfireRestService
        try {
            this.bonfireRestService = new BonfireRestService(this);
            this.getLogger().at(Level.INFO).log("BonfireRestService started.");
        } catch (Exception e) {
            this.getLogger().at(Level.SEVERE).log("Failed to initialize BonfireRestService: " + e.getMessage(), e);
        }

        this.getCommandRegistry().registerCommand(new BonfireMenuCommand());
        this.getCommandRegistry().registerCommand(new HysoulsHelpCommand());

        this.getLogger().at(Level.INFO).log("Setup Complete!");
    }

    @Override
    protected void start() {
        // Initialize NPC category detection (after NPCGroup assets are loaded)
        NPCCategoryUtil.initialize();

        // Register hollow event handlers (after setup)
        if (this.hollowEventHandler != null) {
            this.hollowEventHandler.register();
            this.getLogger().at(Level.INFO).log("HollowEventHandler registered.");
        }

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

        if (this.bonfireRestService != null) {
            this.bonfireRestService.shutdown();
            this.bonfireRestService = null;
        }

        // Shutdown individual managers (they now use 3-folder config internally)
        SoulManager.shutdown();
        HumanityManager.shutdown();
        HollowManager.shutdown();
        com.nowhere.hysouls.warp.SpawnPointManager.shutdown();

        EstusConfigManager.shutdown();
        KindleConfigManager.shutdown();
        HudConfigManager.shutdown();
        HumanityHudConfigManager.shutdown();

        // Shutdown warp configs (separate system)
        WarpConfigManager.shutdown();

        // Shutdown 3-folder config managers (must be last!)
        com.nowhere.hysouls.config.PlayerDataManager.shutdown();
        com.nowhere.hysouls.config.UserPreferencesManager.shutdown();
        this.getLogger().at(Level.INFO).log("Config managers shutdown complete");

        this.getLogger().at(Level.INFO).log("Plugin Shutting Down!");
    }
}