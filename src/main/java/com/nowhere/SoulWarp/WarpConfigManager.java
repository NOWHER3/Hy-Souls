package com.nowhere.SoulWarp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
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

public final class WarpConfigManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String GLOBAL_CONFIG_FILE_NAME = "config.json";
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static SoulWarpsConfig globalConfig;
    private static Path dataDirectory;
    private static Path globalConfigPath;
    private static Path usersDirectory;
    private static final Map<UUID, UserWarpsConfig> userConfigs = new HashMap<>();

    private WarpConfigManager() {
    }

    public static void init(JavaPlugin pluginInstance, Path dataDir) {
        dataDirectory = dataDir.resolve("warps");
        globalConfigPath = dataDirectory.resolve("config.json");
        usersDirectory = dataDirectory.resolve("users");

        // Migrate old config if exists
        Path oldConfigPath = dataDir.resolve("warps.json");
        ConfigMigration.migrate(oldConfigPath);

        loadGlobalConfig();
    }

    private static void loadGlobalConfig() {
        if (globalConfigPath == null) {
            ((HytaleLogger.Api)LOGGER.atWarning()).log("WarpConfigManager not initialized, using default config");
            globalConfig = new SoulWarpsConfig();
        } else if (!Files.exists(globalConfigPath, new LinkOption[0])) {
            ((HytaleLogger.Api)LOGGER.atInfo()).log("Global warps config file not found, creating default config");
            globalConfig = new SoulWarpsConfig();
            saveGlobalConfig();
        } else {
            try (
                    InputStream is = Files.newInputStream(globalConfigPath);
                    Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            ) {
                globalConfig = (SoulWarpsConfig)GSON.fromJson(reader, SoulWarpsConfig.class);
                if (globalConfig == null) {
                    ((HytaleLogger.Api)LOGGER.atWarning()).log("Global warps config file was empty, using default config");
                    globalConfig = new SoulWarpsConfig();
                }

                ((HytaleLogger.Api)LOGGER.atInfo()).log("Global warps configuration loaded successfully");
            } catch (Exception e) {
                ((HytaleLogger.Api)LOGGER.atSevere()).log("Failed to load global warps configuration: " + e.getMessage());
                globalConfig = new SoulWarpsConfig();
            }
        }
    }

    private static void saveGlobalConfig() {
        if (globalConfigPath != null && globalConfig != null) {
            try {
                Files.createDirectories(globalConfigPath.getParent());
                if (Files.exists(globalConfigPath, new LinkOption[0])) {
                    Path backupPath = globalConfigPath.resolveSibling("config.json.bak");
                    Files.copy(globalConfigPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
                }

                try (Writer writer = Files.newBufferedWriter(globalConfigPath, StandardCharsets.UTF_8)) {
                    GSON.toJson(globalConfig, writer);
                }
            } catch (IOException e) {
                ((HytaleLogger.Api)LOGGER.atSevere()).log("Failed to save global warps configuration: " + e.getMessage());
            }
        } else {
            ((HytaleLogger.Api)LOGGER.atWarning()).log("Cannot save global config: WarpConfigManager not initialized");
        }
    }

    public static UserWarpsConfig loadUserConfig(UUID userUuid) {
        // Check if already loaded
        if (userConfigs.containsKey(userUuid)) {
            return userConfigs.get(userUuid);
        }

        if (usersDirectory == null) {
            ((HytaleLogger.Api)LOGGER.atWarning()).log("WarpConfigManager not initialized, using default user config");
            UserWarpsConfig config = new UserWarpsConfig();
            userConfigs.put(userUuid, config);
            return config;
        }

        Path userConfigPath = usersDirectory.resolve(userUuid.toString() + ".json");

        if (!Files.exists(userConfigPath, new LinkOption[0])) {
            ((HytaleLogger.Api)LOGGER.atInfo()).log("User warps config file not found for %s, creating new", userUuid);
            UserWarpsConfig config = new UserWarpsConfig();
            userConfigs.put(userUuid, config);
            saveUserConfig(userUuid);
            return config;
        }

        try (
                InputStream is = Files.newInputStream(userConfigPath);
                Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
        ) {
            UserWarpsConfig config = (UserWarpsConfig)GSON.fromJson(reader, UserWarpsConfig.class);
            if (config == null) {
                ((HytaleLogger.Api)LOGGER.atWarning()).log("User warps config file was empty for %s, using default config", userUuid);
                config = new UserWarpsConfig();
            }

            userConfigs.put(userUuid, config);
            ((HytaleLogger.Api)LOGGER.atInfo()).log("User warps configuration loaded successfully for %s", userUuid);
            return config;
        } catch (Exception e) {
            ((HytaleLogger.Api)LOGGER.atSevere()).log("Failed to load user warps configuration for %s: %s", userUuid, e.getMessage());
            UserWarpsConfig config = new UserWarpsConfig();
            userConfigs.put(userUuid, config);
            return config;
        }
    }

    public static void saveUserConfig(UUID userUuid) {
        UserWarpsConfig config = userConfigs.get(userUuid);
        if (usersDirectory == null || config == null) {
            ((HytaleLogger.Api)LOGGER.atWarning()).log("Cannot save user config for %s: WarpConfigManager not initialized or config not loaded", userUuid);
            return;
        }

        try {
            Files.createDirectories(usersDirectory);
            Path userConfigPath = usersDirectory.resolve(userUuid.toString() + ".json");

            if (Files.exists(userConfigPath, new LinkOption[0])) {
                Path backupPath = userConfigPath.resolveSibling(userUuid.toString() + ".json.bak");
                Files.copy(userConfigPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            }

            try (Writer writer = Files.newBufferedWriter(userConfigPath, StandardCharsets.UTF_8)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            ((HytaleLogger.Api)LOGGER.atSevere()).log("Failed to save user warps configuration for %s: %s", userUuid, e.getMessage());
        }
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
        dataDirectory = null;
        usersDirectory = null;
    }
}