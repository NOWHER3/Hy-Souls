package com.nowhere.SoulWarp;

import com.hypixel.hytale.logger.HytaleLogger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

public final class ConfigMigration {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Path OLD_CONFIG_PATH = Path.of("mods", "com.nowhere.SoulWarps", "SoulWarps.json");
    private static final Path OLD_CONFIG_PATH2 = Path.of("mods", "com.nowhere.SoulWarps", "SoulWarps.json.bak");
    private static final Path OLD_CONFIG_DIR = Path.of("mods", "com.nowhere.SoulWarps");

    private ConfigMigration() {
    }

    public static boolean migrate(Path newConfigPath) {
        if (!Files.exists(OLD_CONFIG_PATH, new LinkOption[0])) {
            return false;
        } else if (Files.exists(newConfigPath, new LinkOption[0])) {
            ((HytaleLogger.Api)LOGGER.atInfo()).log("New config already exists, skipping migration. Old config at: %s", OLD_CONFIG_PATH);
            return false;
        } else {
            try {
                Files.createDirectories(newConfigPath.getParent());
                Files.copy(OLD_CONFIG_PATH, newConfigPath, StandardCopyOption.COPY_ATTRIBUTES);
                ((HytaleLogger.Api)LOGGER.atInfo()).log("Migrated config from %s to %s", OLD_CONFIG_PATH, newConfigPath);
                Path backupPath = OLD_CONFIG_PATH.resolveSibling(String.valueOf(OLD_CONFIG_PATH.getFileName()) + ".bak");
                Files.move(OLD_CONFIG_PATH, backupPath, StandardCopyOption.REPLACE_EXISTING);
                ((HytaleLogger.Api)LOGGER.atInfo()).log("Renamed old config file to: %s", backupPath);
                deleteDirectoryIfEmpty(OLD_CONFIG_DIR);
                return true;
            } catch (IOException e) {
                ((HytaleLogger.Api)LOGGER.atSevere()).log("Failed to migrate config: %s", e.getMessage());
                return false;
            }
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