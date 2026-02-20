package com.nowhere.hysouls.display.humanity.config;

import com.hypixel.hytale.logger.HytaleLogger;
import com.nowhere.hysouls.config.UserPreferences;
import com.nowhere.hysouls.config.UserPreferencesManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages humanity HUD position configuration for players.
 * Uses UserPreferencesManager (user/ folder) for persistence.
 */
public final class HumanityHudConfigManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Map<UUID, HumanityHudPositionConfig> configs = new HashMap<>();

    private HumanityHudConfigManager() {}

    public static HumanityHudPositionConfig load(UUID playerId) {
        if (configs.containsKey(playerId)) {
            return configs.get(playerId);
        }

        UserPreferences prefs = UserPreferencesManager.getUserPreferences(playerId);
        HumanityHudPositionConfig config = new HumanityHudPositionConfig();
        config.side = prefs.humanityHud.side;
        config.offset = prefs.humanityHud.offset;
        config.bottom = prefs.humanityHud.bottom;
        config.width = prefs.humanityHud.width;
        config.height = prefs.humanityHud.height;

        configs.put(playerId, config);
        LOGGER.atFine().log("Loaded humanity HUD config for %s", playerId);
        return config;
    }

    public static void save(UUID playerId) {
        HumanityHudPositionConfig config = configs.get(playerId);
        if (config == null) return;

        UserPreferences prefs = UserPreferencesManager.getUserPreferences(playerId);
        prefs.humanityHud.side = config.side;
        prefs.humanityHud.offset = config.offset;
        prefs.humanityHud.bottom = config.bottom;
        prefs.humanityHud.width = config.width;
        prefs.humanityHud.height = config.height;
        UserPreferencesManager.saveUserPreferences(playerId);

        LOGGER.atFine().log("Saved humanity HUD config for %s", playerId);
    }

    public static void unload(UUID playerId) {
        if (configs.containsKey(playerId)) {
            save(playerId);
            configs.remove(playerId);
        }
    }

    public static void shutdown() {
        for (UUID playerId : configs.keySet()) {
            save(playerId);
        }
        configs.clear();
    }
}
