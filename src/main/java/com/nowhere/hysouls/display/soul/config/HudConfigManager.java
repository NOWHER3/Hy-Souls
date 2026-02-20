package com.nowhere.hysouls.display.soul.config;

import com.hypixel.hytale.logger.HytaleLogger;
import com.nowhere.hysouls.config.UserPreferences;
import com.nowhere.hysouls.config.UserPreferencesManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages soul HUD position configuration for players.
 * Uses UserPreferencesManager (user/ folder) for persistence.
 */
public final class HudConfigManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Map<UUID, HudPositionConfig> configs = new HashMap<>();

    private HudConfigManager() {}

    public static HudPositionConfig load(UUID playerId) {
        if (configs.containsKey(playerId)) {
            return configs.get(playerId);
        }

        UserPreferences prefs = UserPreferencesManager.getUserPreferences(playerId);
        HudPositionConfig config = new HudPositionConfig();
        config.side = prefs.soulHud.side;
        config.offset = prefs.soulHud.offset;
        config.bottom = prefs.soulHud.bottom;
        config.width = prefs.soulHud.width;
        config.height = prefs.soulHud.height;

        configs.put(playerId, config);
        LOGGER.atFine().log("Loaded soul HUD config for %s", playerId);
        return config;
    }

    public static void save(UUID playerId) {
        HudPositionConfig config = configs.get(playerId);
        if (config == null) return;

        UserPreferences prefs = UserPreferencesManager.getUserPreferences(playerId);
        prefs.soulHud.side = config.side;
        prefs.soulHud.offset = config.offset;
        prefs.soulHud.bottom = config.bottom;
        prefs.soulHud.width = config.width;
        prefs.soulHud.height = config.height;
        UserPreferencesManager.saveUserPreferences(playerId);

        LOGGER.atFine().log("Saved soul HUD config for %s", playerId);
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
