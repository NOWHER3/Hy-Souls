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

public final class WarpConfigManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String CONFIG_FILE_NAME = "warps.json";
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static SoulWarpsConfig config;
    private static Path configPath;

    private WarpConfigManager() {
    }

    public static void init(JavaPlugin pluginInstance, Path dataDirectory) {
        configPath = dataDirectory.resolve("warps.json");
        ConfigMigration.migrate(configPath);
        load();
    }

    public static void load() {
        if (configPath == null) {
            ((HytaleLogger.Api)LOGGER.atWarning()).log("WarpConfigManager not initialized, using default config");
            config = new SoulWarpsConfig();
        } else if (!Files.exists(configPath, new LinkOption[0])) {
            ((HytaleLogger.Api)LOGGER.atInfo()).log("Warps config file not found, creating default config");
            config = new SoulWarpsConfig();
            save();
        } else {
            try (
                    InputStream is = Files.newInputStream(configPath);
                    Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            ) {
                config = (SoulWarpsConfig)GSON.fromJson(reader, SoulWarpsConfig.class);
                if (config == null) {
                    ((HytaleLogger.Api)LOGGER.atWarning()).log("Warps config file was empty, using default config");
                    config = new SoulWarpsConfig();
                }

                ((HytaleLogger.Api)LOGGER.atInfo()).log("Warps configuration loaded successfully");
            } catch (Exception e) {
                ((HytaleLogger.Api)LOGGER.atSevere()).log("Failed to load warps configuration: " + e.getMessage());
                config = new SoulWarpsConfig();
            }

        }
    }

    public static void save() {
        if (configPath != null && config != null) {
            try {
                Files.createDirectories(configPath.getParent());
                if (Files.exists(configPath, new LinkOption[0])) {
                    Path backupPath = configPath.resolveSibling("warps.json.bak");
                    Files.copy(configPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
                }

                try (Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
                    GSON.toJson(config, writer);
                }
            } catch (IOException e) {
                ((HytaleLogger.Api)LOGGER.atSevere()).log("Failed to save warps configuration: " + e.getMessage());
            }

        } else {
            ((HytaleLogger.Api)LOGGER.atWarning()).log("Cannot save config: WarpConfigManager not initialized");
        }
    }

    public static SoulWarpsConfig get() {
        if (config == null) {
            config = new SoulWarpsConfig();
        }

        return config;
    }

    public static void shutdown() {
        if (config != null && configPath != null) {
            save();
        }

        config = null;
        configPath = null;
    }
}