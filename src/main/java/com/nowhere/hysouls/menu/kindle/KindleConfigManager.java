package com.nowhere.hysouls.menu.kindle;

import com.hypixel.hytale.logger.HytaleLogger;
import com.nowhere.hysouls.config.PlayerData;
import com.nowhere.hysouls.config.PlayerDataManager;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages loading and saving per-player kindle configurations.
 * Uses PlayerDataManager (data/ folder) for persistence.
 */
public final class KindleConfigManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final ConcurrentHashMap<UUID, KindleConfig> configs = new ConcurrentHashMap<>();

    private KindleConfigManager() {}

    public static KindleConfig getUserConfig(UUID playerId) {
        return configs.computeIfAbsent(playerId, KindleConfigManager::load);
    }

    private static KindleConfig load(UUID playerId) {
        PlayerData data = PlayerDataManager.getPlayerData(playerId);
        KindleConfig config = new KindleConfig();
        config.kindling = new HashMap<>(data.kindling);

        LOGGER.atFine().log("Loaded kindle config for %s: %d bonfires", playerId, config.kindling.size());
        return config;
    }

    public static void saveUserConfig(UUID playerId) {
        KindleConfig config = configs.get(playerId);
        if (config == null) return;

        PlayerData data = PlayerDataManager.getPlayerData(playerId);
        data.kindling = new HashMap<>(config.kindling);
        PlayerDataManager.savePlayerData(playerId);

        LOGGER.atFine().log("Saved kindle config for %s: %d bonfires", playerId, config.kindling.size());
    }

    public static void unload(UUID playerId) {
        saveUserConfig(playerId);
        configs.remove(playerId);
    }

    public static void shutdown() {
        for (UUID playerId : configs.keySet()) {
            saveUserConfig(playerId);
        }
        configs.clear();
    }
}
