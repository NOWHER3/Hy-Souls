package com.nowhere.hysouls.currency.humanity;

import com.hypixel.hytale.logger.HytaleLogger;
import com.nowhere.hysouls.config.PlayerData;
import com.nowhere.hysouls.config.PlayerDataManager;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages humanity currency for players.
 * Uses PlayerDataManager (data/ folder) for persistence.
 */
public final class HumanityManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final ConcurrentHashMap<UUID, AtomicInteger> humanityCounts = new ConcurrentHashMap<>();
    private static final int STARTING_HUMANITY = 0; // Players start hollow with 0 humanity
    private static final int MAX_HUMANITY = 99; // Maximum humanity cap

    private HumanityManager() {}

    public static int getHumanity(UUID playerId) {
        AtomicInteger counter = humanityCounts.get(playerId);
        return counter != null ? counter.get() : 0;
    }

    public static void addHumanity(UUID playerId, int amount) {
        if (amount == 0) return;
        AtomicInteger counter = humanityCounts.computeIfAbsent(playerId, k -> new AtomicInteger(0));
        // Cap at MAX_HUMANITY
        counter.updateAndGet(current -> Math.min(MAX_HUMANITY, current + amount));
        save(playerId);
    }

    public static void removeHumanity(UUID playerId, int amount) {
        if (amount <= 0) return;
        AtomicInteger counter = humanityCounts.get(playerId);
        if (counter == null) return;

        // Ensure humanity doesn't go below 0
        counter.updateAndGet(current -> Math.max(0, current - amount));
        save(playerId);
    }

    public static void load(UUID playerId) {
        PlayerData data = PlayerDataManager.getPlayerData(playerId);
        // Cap at MAX_HUMANITY when loading
        int cappedCount = Math.min(MAX_HUMANITY, data.humanity);
        humanityCounts.put(playerId, new AtomicInteger(cappedCount));
        LOGGER.atFine().log("Loaded %d humanity for %s", cappedCount, playerId);
    }

    public static void save(UUID playerId) {
        AtomicInteger counter = humanityCounts.get(playerId);
        if (counter == null) return;

        PlayerData data = PlayerDataManager.getPlayerData(playerId);
        data.humanity = counter.get();
        PlayerDataManager.savePlayerData(playerId);
        LOGGER.atFine().log("Saved %d humanity for %s", counter.get(), playerId);
    }

    public static void unload(UUID playerId) {
        save(playerId);
        humanityCounts.remove(playerId);
    }

    public static void shutdown() {
        for (UUID playerId : humanityCounts.keySet()) {
            save(playerId);
        }
        humanityCounts.clear();
    }
}
