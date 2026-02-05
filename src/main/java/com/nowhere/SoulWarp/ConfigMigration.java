package com.nowhere.SoulWarp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.logger.HytaleLogger;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.stream.Stream;

public final class ConfigMigration {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Path OLD_CONFIG_PATH = Path.of("mods", "com.nowhere.SoulWarps", "SoulWarps.json");
    private static final Path OLD_CONFIG_DIR = Path.of("mods", "com.nowhere.SoulWarps");
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    private ConfigMigration() {
    }

    // Old config structure for migration
    private static class OldSoulWarpsConfig {
        public Map<String, WarpModel> warps;
        public int warmup = 3;
        public int cooldown = 5;
    }

    public static boolean migrate(Path oldConfigPath) {
        // Check both the old location and the passed path
        if (!Files.exists(OLD_CONFIG_PATH, new LinkOption[0]) && !Files.exists(oldConfigPath, new LinkOption[0])) {
            return false;
        }

        Path configToMigrate = Files.exists(OLD_CONFIG_PATH, new LinkOption[0]) ? OLD_CONFIG_PATH : oldConfigPath;

        try {
            // Load old config
            OldSoulWarpsConfig oldConfig;
            try (
                    InputStream is = Files.newInputStream(configToMigrate);
                    Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            ) {
                oldConfig = GSON.fromJson(reader, OldSoulWarpsConfig.class);
            }

            if (oldConfig == null) {
                ((HytaleLogger.Api)LOGGER.atWarning()).log("Old config file was empty, skipping migration");
                return false;
            }

            ((HytaleLogger.Api)LOGGER.atInfo()).log("Migrating old warp configuration...");

            // If there were warps in the old config, we'll need to notify the user
            // since we don't know which user they belonged to
            if (oldConfig.warps != null && !oldConfig.warps.isEmpty()) {
                ((HytaleLogger.Api)LOGGER.atWarning()).log("Found %d warps in old config. These warps cannot be automatically migrated to specific users.", oldConfig.warps.size());
                ((HytaleLogger.Api)LOGGER.atWarning()).log("Old warps data has been preserved in backup file. Users will need to recreate their warps.");
            }

            // Backup the old config
            Path backupPath = configToMigrate.resolveSibling(configToMigrate.getFileName() + ".bak");
            Files.copy(configToMigrate, backupPath, StandardCopyOption.REPLACE_EXISTING);
            ((HytaleLogger.Api)LOGGER.atInfo()).log("Backed up old config to: %s", backupPath);

            // Delete the old config file
            Files.delete(configToMigrate);
            ((HytaleLogger.Api)LOGGER.atInfo()).log("Deleted old config file: %s", configToMigrate);

            // Clean up old directory if empty
            deleteDirectoryIfEmpty(OLD_CONFIG_DIR);

            return true;
        } catch (IOException e) {
            ((HytaleLogger.Api)LOGGER.atSevere()).log("Failed to migrate config: %s", e.getMessage());
            return false;
        }
    }

    private static void deleteDirectoryIfEmpty(Path directory) {
        try {
            if (Files.isDirectory(directory, new LinkOption[0]) && isDirectoryEmpty(directory)) {
                Files.delete(directory);
                ((HytaleLogger.Api)LOGGER.atInfo()).log("Deleted empty old config directory: %s", directory);
            }
        } catch (IOException e) {
            ((HytaleLogger.Api)LOGGER.atWarning()).log("Could not delete old config directory: %s", e.getMessage());
        }
    }

    private static boolean isDirectoryEmpty(Path directory) throws IOException {
        try (Stream<Path> entries = Files.list(directory)) {
            return entries.findFirst().isEmpty();
        }
    }
}