package com.nowhere.hysouls.appearance;

import com.hypixel.hytale.logger.HytaleLogger;
import com.nowhere.hysouls.config.PlayerData;
import com.nowhere.hysouls.config.PlayerDataManager;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the hollow state of players (whether they appear as hollow/undead).
 * Players become hollow when they have 0 humanity and can reverse it at bonfires.
 * Uses PlayerDataManager (data/ folder) for persistence.
 */
public final class HollowManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final ConcurrentHashMap<UUID, Boolean> hollowStates = new ConcurrentHashMap<>();

    private HollowManager() {}

    /**
     * Check if a player is currently hollow.
     */
    public static boolean isHollow(UUID playerId) {
        return hollowStates.getOrDefault(playerId, false);
    }

    /**
     * Set a player's hollow state.
     */
    public static void setHollow(UUID playerId, boolean hollow) {
        hollowStates.put(playerId, hollow);
        save(playerId);
    }

    /**
     * Make a player hollow (undead appearance).
     */
    public static void makeHollow(UUID playerId) {
        setHollow(playerId, true);
    }

    /**
     * Reverse hollowing (restore human appearance).
     */
    public static void reverseHollowing(UUID playerId) {
        setHollow(playerId, false);
    }

    public static void load(UUID playerId) {
        PlayerData data = PlayerDataManager.getPlayerData(playerId);
        hollowStates.put(playerId, data.isHollow);
        LOGGER.atFine().log("Loaded hollow state for %s: %b", playerId, data.isHollow);
    }

    public static void save(UUID playerId) {
        Boolean hollow = hollowStates.get(playerId);
        if (hollow == null) return;

        PlayerData data = PlayerDataManager.getPlayerData(playerId);
        data.isHollow = hollow;
        PlayerDataManager.savePlayerData(playerId);
        LOGGER.atFine().log("Saved hollow state for %s: %b", playerId, hollow);
    }

    public static void unload(UUID playerId) {
        save(playerId);
        hollowStates.remove(playerId);
    }

    public static void shutdown() {
        for (UUID playerId : hollowStates.keySet()) {
            save(playerId);
        }
        hollowStates.clear();
    }
}
