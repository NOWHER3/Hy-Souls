package com.nowhere.hysouls.warp;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nowhere.hysouls.config.PlayerData;
import com.nowhere.hysouls.config.PlayerDataManager;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player respawn points at bonfires.
 * Tracks which bonfire a player last rested at and handles teleportation on respawn.
 * Uses PlayerDataManager (data/ folder) for persistence.
 */
public final class SpawnPointManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final ConcurrentHashMap<UUID, String> respawnBonfires = new ConcurrentHashMap<>();

    private SpawnPointManager() {}

    /**
     * Sets the respawn bonfire for a player.
     * @param playerId Player UUID
     * @param bonfireWarpName Warp name of the bonfire (e.g., "bonfire_123_64_456")
     */
    public static void setRespawnBonfire(UUID playerId, String bonfireWarpName) {
        respawnBonfires.put(playerId, bonfireWarpName);
        save(playerId);
        LOGGER.atFine().log("Set respawn bonfire for player %s to %s", playerId, bonfireWarpName);
    }

    /**
     * Gets the respawn bonfire for a player.
     * @param playerId Player UUID
     * @return Bonfire warp name, or null if none set
     */
    public static String getRespawnBonfire(UUID playerId) {
        return respawnBonfires.get(playerId);
    }

    /**
     * Clears the respawn bonfire for a player.
     * Used when a bonfire is destroyed or for admin cleanup.
     * @param playerId Player UUID
     */
    public static void clearRespawnBonfire(UUID playerId) {
        respawnBonfires.remove(playerId);
        save(playerId);
        LOGGER.atFine().log("Cleared respawn bonfire for player %s", playerId);
    }

    /**
     * Teleports a player to their respawn bonfire if set.
     * Handles edge cases gracefully (no bonfire set, bonfire destroyed, cross-world mismatch).
     * Should be called during respawn processing in a deferred callback to avoid store lock conflicts.
     *
     * @param store Entity store
     * @param ref Player entity reference
     * @param playerId Player UUID
     */
    public static void teleportToRespawnBonfire(Store<EntityStore> store, Ref<EntityStore> ref, UUID playerId) {
        try {
            // Get respawn bonfire name
            String bonfireWarpName = getRespawnBonfire(playerId);
            if (bonfireWarpName == null) {
                // No bonfire set - use default spawn (Hytale handles this)
                LOGGER.atFine().log("Player %s has no respawn bonfire set, using default spawn", playerId);
                return;
            }

            // Look up warp from PlayerData
            PlayerData data = PlayerDataManager.getPlayerData(playerId);
            WarpModel warp = data.warps.get(bonfireWarpName);

            if (warp == null) {
                // Bonfire destroyed - clear respawn bonfire and use default spawn
                LOGGER.atWarning().log("Respawn bonfire %s not found for player %s (bonfire destroyed?), clearing and using default spawn",
                        bonfireWarpName, playerId);
                clearRespawnBonfire(playerId);
                return;
            }

            // Get player world UUID for cross-world check
            PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (playerRef == null || !playerRef.isValid()) {
                LOGGER.atWarning().log("Invalid player reference during respawn teleport for %s", playerId);
                return;
            }

            UUID playerWorldUuid = playerRef.getWorldUuid();
            if (!warp.worldUuid.equals(playerWorldUuid)) {
                // Cross-world mismatch - skip teleport for safety
                LOGGER.atWarning().log("Respawn bonfire %s for player %s is in different world (bonfire: %s, player: %s), using default spawn",
                        bonfireWarpName, playerId, warp.worldUuid, playerWorldUuid);
                return;
            }

            // Get world for teleport component
            World world = store.getExternalData().getWorld();
            if (world == null) {
                LOGGER.atWarning().log("World is null during respawn teleport for player %s", playerId);
                return;
            }

            // Create and apply teleport component
            Vector3f rotation = new Vector3f();
            rotation.setYaw(warp.yaw);
            rotation.setPitch(0.0F);
            Teleport teleport = new Teleport(world, new Vector3d(warp.x, warp.y, warp.z), rotation);
            store.addComponent(ref, Teleport.getComponentType(), teleport);

            LOGGER.atInfo().log("Teleporting player %s to respawn bonfire %s at (%.2f, %.2f, %.2f)",
                    playerId, bonfireWarpName, warp.x, warp.y, warp.z);

        } catch (Exception e) {
            LOGGER.atSevere().log("Error teleporting player %s to respawn bonfire: %s",
                    playerId, e.getMessage());
        }
    }

    /**
     * Loads respawn bonfire from PlayerData for a player.
     * Called when player joins server.
     * @param playerId Player UUID
     */
    public static void load(UUID playerId) {
        PlayerData data = PlayerDataManager.getPlayerData(playerId);
        if (data.respawnBonfire != null) {
            respawnBonfires.put(playerId, data.respawnBonfire);
            LOGGER.atFine().log("Loaded respawn bonfire for %s: %s", playerId, data.respawnBonfire);
        }
    }

    /**
     * Saves respawn bonfire to PlayerData for a player.
     * @param playerId Player UUID
     */
    public static void save(UUID playerId) {
        String bonfire = respawnBonfires.get(playerId);
        PlayerData data = PlayerDataManager.getPlayerData(playerId);
        data.respawnBonfire = bonfire;
        PlayerDataManager.savePlayerData(playerId);
        LOGGER.atFine().log("Saved respawn bonfire for %s: %s", playerId, bonfire);
    }

    /**
     * Unloads a player's respawn bonfire from memory.
     * Called when player disconnects.
     * @param playerId Player UUID
     */
    public static void unload(UUID playerId) {
        save(playerId);
        respawnBonfires.remove(playerId);
    }

    /**
     * Shuts down the manager, saving all data.
     * Called during plugin shutdown.
     */
    public static void shutdown() {
        for (UUID playerId : respawnBonfires.keySet()) {
            save(playerId);
        }
        respawnBonfires.clear();
        LOGGER.atInfo().log("SpawnPointManager shutdown complete");
    }
}
