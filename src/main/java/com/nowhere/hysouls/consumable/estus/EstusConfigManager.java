package com.nowhere.hysouls.consumable.estus;

import com.hypixel.hytale.logger.HytaleLogger;
import com.nowhere.hysouls.config.UserPreferences;
import com.nowhere.hysouls.config.UserPreferencesManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages estus flask configuration for players.
 * Uses UserPreferencesManager (user/ folder) for persistence.
 */
public final class EstusConfigManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Map<UUID, EstusConfig> configs = new HashMap<>();

    private EstusConfigManager() {}

    public static EstusConfig load(UUID playerId) {
        if (configs.containsKey(playerId)) {
            return configs.get(playerId);
        }

        UserPreferences prefs = UserPreferencesManager.getUserPreferences(playerId);
        EstusConfig config = new EstusConfig();
        config.slot = prefs.estusSlot;
        configs.put(playerId, config);

        LOGGER.atFine().log("Loaded estus config for %s: slot=%d", playerId, config.slot);
        return config;
    }

    public static void save(UUID playerId) {
        EstusConfig config = configs.get(playerId);
        if (config == null) return;

        UserPreferences prefs = UserPreferencesManager.getUserPreferences(playerId);
        prefs.estusSlot = config.slot;
        UserPreferencesManager.saveUserPreferences(playerId);

        LOGGER.atFine().log("Saved estus config for %s: slot=%d", playerId, config.slot);
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
