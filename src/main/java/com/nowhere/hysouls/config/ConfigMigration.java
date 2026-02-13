package com.nowhere.hysouls.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hypixel.hytale.logger.HytaleLogger;
import com.nowhere.hysouls.consumable.estus.EstusConfig;
import com.nowhere.hysouls.display.humanity.config.HumanityHudPositionConfig;
import com.nowhere.hysouls.display.soul.config.HudPositionConfig;
import com.nowhere.hysouls.menu.kindle.KindleConfig;
import com.nowhere.hysouls.warp.UserWarpsConfig;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * Migrates data from old separate config files to the new 3-folder structure:
 * - data/ (souls, humanity, hollow, kindling)
 * - user/ (estus slot, HUD positions)
 * - server/ (drops config - handled separately)
 */
public final class ConfigMigration {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Type INT_TYPE = new TypeToken<Integer>(){}.getType();
    private static final Type BOOLEAN_TYPE = new TypeToken<Boolean>(){}.getType();

    private ConfigMigration() {}

    /**
     * Performs migration from old config structure to new 3-folder structure.
     */
    public static void migrate(Path baseDir) {
        LOGGER.atInfo().log("Starting config migration to new 3-folder structure...");

        Set<UUID> playerIds = collectAllPlayerIds(baseDir);
        LOGGER.atInfo().log("Found %d unique player IDs across old configs", playerIds.size());

        int migrated = 0;
        int failed = 0;

        for (UUID playerId : playerIds) {
            try {
                migratePlayerConfig(baseDir, playerId);
                migrated++;
            } catch (Exception e) {
                LOGGER.atSevere().log("Failed to migrate config for %s: %s", playerId, e.getMessage());
                failed++;
            }
        }

        LOGGER.atInfo().log("Migration complete: %d migrated, %d failed", migrated, failed);
    }

    /**
     * Collects all unique player UUIDs from old config directories.
     */
    private static Set<UUID> collectAllPlayerIds(Path baseDir) {
        Set<UUID> playerIds = new HashSet<>();

        // Check old directories AND the old unified "users" directory
        String[] oldDirs = {"souls", "humanity", "hollow", "estus", "kindle", "menus", "humanity_menus", "users"};

        for (String dirName : oldDirs) {
            Path dir = baseDir.resolve(dirName);
            if (Files.exists(dir) && Files.isDirectory(dir)) {
                try (Stream<Path> files = Files.list(dir)) {
                    files.filter(p -> p.toString().endsWith(".json"))
                         .filter(p -> !p.toString().endsWith(".bak"))
                         .forEach(p -> {
                             String fileName = p.getFileName().toString();
                             String uuidStr = fileName.replace(".json", "");
                             try {
                                 playerIds.add(UUID.fromString(uuidStr));
                             } catch (IllegalArgumentException e) {
                                 LOGGER.atWarning().log("Invalid UUID in file name: %s", fileName);
                             }
                         });
                } catch (IOException e) {
                    LOGGER.atWarning().log("Failed to list directory %s: %s", dirName, e.getMessage());
                }
            }
        }

        // Also check warps/users/ directory for old warp files
        Path warpsUsersDir = baseDir.resolve("warps").resolve("users");
        if (Files.exists(warpsUsersDir) && Files.isDirectory(warpsUsersDir)) {
            try (Stream<Path> files = Files.list(warpsUsersDir)) {
                files.filter(p -> p.toString().endsWith(".json"))
                     .filter(p -> !p.toString().endsWith(".bak"))
                     .forEach(p -> {
                         String fileName = p.getFileName().toString();
                         String uuidStr = fileName.replace(".json", "");
                         try {
                             playerIds.add(UUID.fromString(uuidStr));
                         } catch (IllegalArgumentException e) {
                             LOGGER.atWarning().log("Invalid UUID in file name: %s", fileName);
                         }
                     });
            } catch (IOException e) {
                LOGGER.atWarning().log("Failed to list directory warps/users: %s", e.getMessage());
            }
        }

        return playerIds;
    }

    /**
     * Migrates a single player's config from old format to new 3-folder structure.
     */
    private static void migratePlayerConfig(Path baseDir, UUID playerId) {
        PlayerData playerData = new PlayerData();
        UserPreferences userPrefs = new UserPreferences();

        // Check if we're migrating from the old unified config
        UserConfig oldUnified = loadOldUnifiedConfig(baseDir.resolve("users"), playerId);
        if (oldUnified != null) {
            LOGGER.atInfo().log("Migrating from old unified config for %s", playerId);
            playerData.souls = oldUnified.souls;
            playerData.humanity = oldUnified.humanity;
            playerData.isHollow = oldUnified.isHollow;
            playerData.kindling = new HashMap<>(oldUnified.kindling);

            userPrefs.estusSlot = oldUnified.estusSlot;
            userPrefs.soulHud.side = oldUnified.soulHud.side;
            userPrefs.soulHud.offset = oldUnified.soulHud.offset;
            userPrefs.soulHud.bottom = oldUnified.soulHud.bottom;
            userPrefs.soulHud.width = oldUnified.soulHud.width;
            userPrefs.soulHud.height = oldUnified.soulHud.height;
            userPrefs.humanityHud.side = oldUnified.humanityHud.side;
            userPrefs.humanityHud.offset = oldUnified.humanityHud.offset;
            userPrefs.humanityHud.bottom = oldUnified.humanityHud.bottom;
            userPrefs.humanityHud.width = oldUnified.humanityHud.width;
            userPrefs.humanityHud.height = oldUnified.humanityHud.height;
        } else {
            // Migrate from separate old files
            LOGGER.atInfo().log("Migrating from old separate configs for %s", playerId);

            // Migrate to PlayerData (data/)
            Integer souls = loadOldInteger(baseDir.resolve("souls"), playerId);
            if (souls != null) playerData.souls = souls;

            Integer humanity = loadOldInteger(baseDir.resolve("humanity"), playerId);
            if (humanity != null) playerData.humanity = humanity;

            Boolean hollow = loadOldBoolean(baseDir.resolve("hollow"), playerId);
            if (hollow != null) playerData.isHollow = hollow;

            KindleConfig kindleConfig = loadOldKindleConfig(baseDir.resolve("kindle"), playerId);
            if (kindleConfig != null && kindleConfig.kindling != null) {
                playerData.kindling = new HashMap<>(kindleConfig.kindling);
            }

            // Migrate warps from old warps/users/ directory
            UserWarpsConfig warpsConfig = loadOldWarpsConfig(baseDir.resolve("warps").resolve("users"), playerId);
            if (warpsConfig != null && warpsConfig.warps != null) {
                playerData.warps = new HashMap<>(warpsConfig.warps);
                LOGGER.atFine().log("Migrated %d warps for %s", warpsConfig.warps.size(), playerId);
            }

            // Migrate to UserPreferences (user/)
            EstusConfig estusConfig = loadOldEstusConfig(baseDir.resolve("estus"), playerId);
            if (estusConfig != null) userPrefs.estusSlot = estusConfig.slot;

            HudPositionConfig soulHud = loadOldSoulHudConfig(baseDir.resolve("menus"), playerId);
            if (soulHud != null) {
                userPrefs.soulHud.side = soulHud.side;
                userPrefs.soulHud.offset = soulHud.offset;
                userPrefs.soulHud.bottom = soulHud.bottom;
                userPrefs.soulHud.width = soulHud.width;
                userPrefs.soulHud.height = soulHud.height;
            }

            HumanityHudPositionConfig humanityHud = loadOldHumanityHudConfig(baseDir.resolve("humanity_menus"), playerId);
            if (humanityHud != null) {
                userPrefs.humanityHud.side = humanityHud.side;
                userPrefs.humanityHud.offset = humanityHud.offset;
                userPrefs.humanityHud.bottom = humanityHud.bottom;
                userPrefs.humanityHud.width = humanityHud.width;
                userPrefs.humanityHud.height = humanityHud.height;
            }
        }

        // Save to new structure
        saveToNewStructure(baseDir, playerId, playerData, userPrefs);
    }

    private static void saveToNewStructure(Path baseDir, UUID playerId, PlayerData playerData, UserPreferences userPrefs) {
        try {
            // Save PlayerData to data/
            Path dataDir = baseDir.resolve("data");
            Files.createDirectories(dataDir);
            Path dataFile = dataDir.resolve(playerId.toString() + ".json");
            if (!Files.exists(dataFile, new LinkOption[0])) {
                try (var writer = Files.newBufferedWriter(dataFile, StandardCharsets.UTF_8)) {
                    GSON.toJson(playerData, writer);
                }
                LOGGER.atInfo().log("Migrated player data for %s", playerId);
            }

            // Save UserPreferences to user/
            Path userDir = baseDir.resolve("user");
            Files.createDirectories(userDir);
            Path userFile = userDir.resolve(playerId.toString() + ".json");
            if (!Files.exists(userFile, new LinkOption[0])) {
                try (var writer = Files.newBufferedWriter(userFile, StandardCharsets.UTF_8)) {
                    GSON.toJson(userPrefs, writer);
                }
                LOGGER.atInfo().log("Migrated user preferences for %s", playerId);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to save migrated configs for " + playerId, e);
        }
    }

    private static UserConfig loadOldUnifiedConfig(Path dir, UUID playerId) {
        Path file = dir.resolve(playerId.toString() + ".json");
        if (!Files.exists(file, new LinkOption[0])) return null;

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, UserConfig.class);
        } catch (Exception e) {
            LOGGER.atWarning().log("Failed to read old unified config from %s: %s", file, e.getMessage());
            return null;
        }
    }

    private static Integer loadOldInteger(Path dir, UUID playerId) {
        Path file = dir.resolve(playerId.toString() + ".json");
        if (!Files.exists(file, new LinkOption[0])) return null;

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, INT_TYPE);
        } catch (Exception e) {
            LOGGER.atWarning().log("Failed to read old integer config from %s: %s", file, e.getMessage());
            return null;
        }
    }

    private static Boolean loadOldBoolean(Path dir, UUID playerId) {
        Path file = dir.resolve(playerId.toString() + ".json");
        if (!Files.exists(file, new LinkOption[0])) return null;

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, BOOLEAN_TYPE);
        } catch (Exception e) {
            LOGGER.atWarning().log("Failed to read old boolean config from %s: %s", file, e.getMessage());
            return null;
        }
    }

    private static EstusConfig loadOldEstusConfig(Path dir, UUID playerId) {
        Path file = dir.resolve(playerId.toString() + ".json");
        if (!Files.exists(file, new LinkOption[0])) return null;

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, EstusConfig.class);
        } catch (Exception e) {
            LOGGER.atWarning().log("Failed to read old estus config from %s: %s", file, e.getMessage());
            return null;
        }
    }

    private static KindleConfig loadOldKindleConfig(Path dir, UUID playerId) {
        Path file = dir.resolve(playerId.toString() + ".json");
        if (!Files.exists(file, new LinkOption[0])) return null;

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, KindleConfig.class);
        } catch (Exception e) {
            LOGGER.atWarning().log("Failed to read old kindle config from %s: %s", file, e.getMessage());
            return null;
        }
    }

    private static HudPositionConfig loadOldSoulHudConfig(Path dir, UUID playerId) {
        Path file = dir.resolve(playerId.toString() + ".json");
        if (!Files.exists(file, new LinkOption[0])) return null;

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, HudPositionConfig.class);
        } catch (Exception e) {
            LOGGER.atWarning().log("Failed to read old soul HUD config from %s: %s", file, e.getMessage());
            return null;
        }
    }

    private static HumanityHudPositionConfig loadOldHumanityHudConfig(Path dir, UUID playerId) {
        Path file = dir.resolve(playerId.toString() + ".json");
        if (!Files.exists(file, new LinkOption[0])) return null;

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, HumanityHudPositionConfig.class);
        } catch (Exception e) {
            LOGGER.atWarning().log("Failed to read old humanity HUD config from %s: %s", file, e.getMessage());
            return null;
        }
    }

    private static UserWarpsConfig loadOldWarpsConfig(Path dir, UUID playerId) {
        Path file = dir.resolve(playerId.toString() + ".json");
        if (!Files.exists(file, new LinkOption[0])) return null;

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, UserWarpsConfig.class);
        } catch (Exception e) {
            LOGGER.atWarning().log("Failed to read old warps config from %s: %s", file, e.getMessage());
            return null;
        }
    }

    /**
     * Legacy unified config structure - only used for migration from old format.
     * This class mirrors the old UserConfig that was deprecated.
     */
    private static class UserConfig {
        public int souls = 0;
        public int humanity = 0;
        public boolean isHollow = false;
        public int estusSlot = -1;
        public Map<String, Integer> kindling = new HashMap<>();
        public HudPositionConfig soulHud = new HudPositionConfig();
        public HumanityHudPositionConfig humanityHud = new HumanityHudPositionConfig();
    }
}
