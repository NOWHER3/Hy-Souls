package com.nowhere.hysouls.currency.soul;

import com.hypixel.hytale.logger.HytaleLogger;
import com.nowhere.hysouls.config.PlayerData;
import com.nowhere.hysouls.config.PlayerDataManager;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages soul currency for players.
 * Uses PlayerDataManager (data/ folder) for persistence.
 */
public final class SoulManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final ConcurrentHashMap<UUID, AtomicInteger> soulCounts = new ConcurrentHashMap<>();

    private SoulManager() {}

    public static int getSouls(UUID playerId) {
        AtomicInteger counter = soulCounts.get(playerId);
        return counter != null ? counter.get() : 0;
    }

    public static void addSouls(UUID playerId, int amount) {
        if (amount <= 0) return;
        soulCounts.computeIfAbsent(playerId, k -> new AtomicInteger(0)).addAndGet(amount);
        save(playerId);
    }

    public static void setSouls(UUID playerId, int amount) {
        soulCounts.computeIfAbsent(playerId, k -> new AtomicInteger(0)).set(Math.max(0, amount));
        save(playerId);
    }

    public static void load(UUID playerId) {
        PlayerData data = PlayerDataManager.getPlayerData(playerId);
        soulCounts.put(playerId, new AtomicInteger(data.souls));
        LOGGER.atFine().log("Loaded %d souls for %s", data.souls, playerId);
    }

    public static void save(UUID playerId) {
        AtomicInteger counter = soulCounts.get(playerId);
        if (counter == null) return;

        PlayerData data = PlayerDataManager.getPlayerData(playerId);
        data.souls = counter.get();
        PlayerDataManager.savePlayerData(playerId);
        LOGGER.atFine().log("Saved %d souls for %s", counter.get(), playerId);
    }

    public static void unload(UUID playerId) {
        save(playerId);
        soulCounts.remove(playerId);
    }

    public static void shutdown() {
        for (UUID playerId : soulCounts.keySet()) {
            save(playerId);
        }
        soulCounts.clear();
    }
}
