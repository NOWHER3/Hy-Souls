package com.nowhere.hysouls.warp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.nowhere.hysouls.config.PlayerData;
import com.nowhere.hysouls.config.PlayerDataManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages warp configuration.
 * Global config stored in server/warpconfig.json.
 * Per-player warps stored in PlayerData (data/ folder).
 */
public final class WarpConfigManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static SoulWarpsConfig globalConfig;
    private static Path globalConfigPath;
    private static final Map<UUID, UserWarpsConfig> userConfigs = new HashMap<>();

    private WarpConfigManager() {}

    public static void init(JavaPlugin pluginInstance, Path dataDir) {
        // Global config now in server/ folder
        globalConfigPath = dataDir.resolve("server").resolve("warpconfig.json");

        // Migrate old config if exists
        Path oldConfigPath = dataDir.resolve("warps.json");
        com.nowhere.hysouls.warp.ConfigMigration.migrate(oldConfigPath);

        loadGlobalConfig();
    }

    private static void loadGlobalConfig() {
        if (globalConfigPath == null) {
            LOGGER.atWarning().log("WarpConfigManager not initialized, using default config");
            globalConfig = new SoulWarpsConfig();
        } else if (!Files.exists(globalConfigPath, new LinkOption[0])) {
            LOGGER.atInfo().log("Global warps config file not found, creating default config");
            globalConfig = new SoulWarpsConfig();
            saveGlobalConfig();
        } else {
            try (
                InputStream is = Files.newInputStream(globalConfigPath);
                Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            ) {
                globalConfig = GSON.fromJson(reader, SoulWarpsConfig.class);
                if (globalConfig == null) {
                    LOGGER.atWarning().log("Global warps config file was empty, using default config");
                    globalConfig = new SoulWarpsConfig();
                }

                LOGGER.atInfo().log("Global warps configuration loaded from server/warpconfig.json");
            } catch (Exception e) {
                LOGGER.atSevere().log("Failed to load global warps configuration: " + e.getMessage());
                globalConfig = new SoulWarpsConfig();
            }
        }
    }

    private static void saveGlobalConfig() {
        if (globalConfigPath != null && globalConfig != null) {
            try {
                Files.createDirectories(globalConfigPath.getParent());
                if (Files.exists(globalConfigPath, new LinkOption[0])) {
                    Path backupPath = globalConfigPath.resolveSibling("warpconfig.json.bak");
                    Files.copy(globalConfigPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
                }

                try (Writer writer = Files.newBufferedWriter(globalConfigPath, StandardCharsets.UTF_8)) {
                    GSON.toJson(globalConfig, writer);
                }
                LOGGER.atFine().log("Saved global warps configuration to server/warpconfig.json");
            } catch (IOException e) {
                LOGGER.atSevere().log("Failed to save global warps configuration: " + e.getMessage());
            }
        } else {
            LOGGER.atWarning().log("Cannot save global config: WarpConfigManager not initialized");
        }
    }

    /**
     * Loads user warps from PlayerData (data/ folder).
     */
    public static UserWarpsConfig loadUserConfig(UUID userUuid) {
        // Check if already loaded
        if (userConfigs.containsKey(userUuid)) {
            return userConfigs.get(userUuid);
        }

        // Load from PlayerData
        PlayerData data = PlayerDataManager.getPlayerData(userUuid);
        UserWarpsConfig config = new UserWarpsConfig();
        config.warps = new HashMap<>(data.warps);

        userConfigs.put(userUuid, config);
        LOGGER.atFine().log("Loaded user warps for %s from PlayerData: %d warps", userUuid, config.warps.size());
        return config;
    }

    /**
     * Saves user warps to PlayerData (data/ folder).
     */
    public static void saveUserConfig(UUID userUuid) {
        UserWarpsConfig config = userConfigs.get(userUuid);
        if (config == null) {
            LOGGER.atWarning().log("Cannot save user warps for %s: config not loaded", userUuid);
            return;
        }

        PlayerData data = PlayerDataManager.getPlayerData(userUuid);
        data.warps = new HashMap<>(config.warps);
        PlayerDataManager.savePlayerData(userUuid);

        LOGGER.atFine().log("Saved user warps for %s to PlayerData: %d warps", userUuid, config.warps.size());
    }

    public static void unloadUserConfig(UUID userUuid) {
        if (userConfigs.containsKey(userUuid)) {
            saveUserConfig(userUuid);
            userConfigs.remove(userUuid);
        }
    }

    public static SoulWarpsConfig getGlobalConfig() {
        if (globalConfig == null) {
            globalConfig = new SoulWarpsConfig();
        }
        return globalConfig;
    }

    public static UserWarpsConfig getUserConfig(UUID userUuid) {
        return loadUserConfig(userUuid);
    }

    public static void shutdown() {
        // Save all loaded user configs
        for (UUID userUuid : userConfigs.keySet()) {
            saveUserConfig(userUuid);
        }
        userConfigs.clear();

        if (globalConfig != null && globalConfigPath != null) {
            saveGlobalConfig();
        }

        globalConfig = null;
        globalConfigPath = null;
    }
}