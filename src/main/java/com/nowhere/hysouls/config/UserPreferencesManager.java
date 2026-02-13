package com.nowhere.hysouls.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.logger.HytaleLogger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages per-player user preferences (estus slot, HUD positions).
 * Stored in user/ folder.
 */
public final class UserPreferencesManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final ConcurrentHashMap<UUID, UserPreferences> prefsMap = new ConcurrentHashMap<>();
    private static Path userDirectory;

    private UserPreferencesManager() {}

    public static void init(Path baseDir) {
        userDirectory = baseDir.resolve("user");
        try {
            Files.createDirectories(userDirectory);
        } catch (IOException e) {
            LOGGER.atSevere().log("Failed to create user directory: %s", e.getMessage());
        }
        LOGGER.atInfo().log("UserPreferencesManager initialized at: %s", userDirectory);
    }

    public static UserPreferences getUserPreferences(UUID playerId) {
        return prefsMap.computeIfAbsent(playerId, UserPreferencesManager::loadFromDisk);
    }

    private static UserPreferences loadFromDisk(UUID playerId) {
        if (userDirectory == null) {
            LOGGER.atWarning().log("UserPreferencesManager not initialized, returning default prefs for %s", playerId);
            return new UserPreferences();
        }

        Path filePath = userDirectory.resolve(playerId.toString() + ".json");
        if (!Files.exists(filePath, new LinkOption[0])) {
            LOGGER.atInfo().log("No preferences file found for %s, creating default", playerId);
            UserPreferences prefs = new UserPreferences();
            saveUserPreferences(playerId, prefs);
            return prefs;
        }

        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            UserPreferences prefs = GSON.fromJson(reader, UserPreferences.class);
            if (prefs == null) {
                LOGGER.atWarning().log("Preferences file for %s was null, creating default", playerId);
                prefs = new UserPreferences();
            }
            LOGGER.atInfo().log("Loaded user preferences for %s", playerId);
            return prefs;
        } catch (Exception e) {
            LOGGER.atSevere().log("Failed to load user preferences for %s: %s", playerId, e.getMessage());
            return new UserPreferences();
        }
    }

    public static void saveUserPreferences(UUID playerId) {
        UserPreferences prefs = prefsMap.get(playerId);
        if (prefs == null) {
            LOGGER.atWarning().log("Attempted to save null preferences for %s", playerId);
            return;
        }
        saveUserPreferences(playerId, prefs);
    }

    private static void saveUserPreferences(UUID playerId, UserPreferences prefs) {
        if (userDirectory == null) {
            LOGGER.atWarning().log("UserPreferencesManager not initialized, cannot save preferences for %s", playerId);
            return;
        }

        Path filePath = userDirectory.resolve(playerId.toString() + ".json");
        try {
            Files.createDirectories(userDirectory);

            if (Files.exists(filePath, new LinkOption[0])) {
                Path backup = filePath.resolveSibling(playerId.toString() + ".json.bak");
                Files.copy(filePath, backup, StandardCopyOption.REPLACE_EXISTING);
            }

            try (Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                GSON.toJson(prefs, writer);
            }
            LOGGER.atFine().log("Saved user preferences for %s", playerId);
        } catch (IOException e) {
            LOGGER.atSevere().log("Failed to save user preferences for %s: %s", playerId, e.getMessage());
        }
    }

    public static void unload(UUID playerId) {
        UserPreferences prefs = prefsMap.remove(playerId);
        if (prefs != null) {
            saveUserPreferences(playerId, prefs);
            LOGGER.atInfo().log("Unloaded user preferences for %s", playerId);
        }
    }

    public static void shutdown() {
        LOGGER.atInfo().log("Shutting down UserPreferencesManager, saving %d preference files", prefsMap.size());
        for (UUID playerId : prefsMap.keySet()) {
            saveUserPreferences(playerId);
        }
        prefsMap.clear();
        userDirectory = null;
    }

    public static int getLoadedPreferencesCount() {
        return prefsMap.size();
    }
}
