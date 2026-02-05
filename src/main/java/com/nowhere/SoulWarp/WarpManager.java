package com.nowhere.SoulWarp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WarpManager {
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Set<UUID> warmingUp = new HashSet<>();

    public boolean isWarmingUp(UUID uuid) {
        return this.warmingUp.contains(uuid);
    }

    public void setWarmingUp(UUID uuid, boolean isWarmingUp) {
        if (isWarmingUp) {
            this.warmingUp.add(uuid);
        } else {
            this.warmingUp.remove(uuid);
        }
    }

    public int getWarmup() {
        return WarpConfigManager.getGlobalConfig().warmup;
    }

    public int getCooldown() {
        return WarpConfigManager.getGlobalConfig().cooldown;
    }

    public long getRemainingCooldown(UUID uuid) {
        if (!this.cooldowns.containsKey(uuid)) {
            return 0L;
        } else {
            long remaining = (Long)this.cooldowns.get(uuid) - System.currentTimeMillis();
            return Math.max(0L, remaining);
        }
    }

    public void setCooldown(UUID uuid) {
        if (this.getCooldown() > 0) {
            this.cooldowns.put(uuid, System.currentTimeMillis() + (long)this.getCooldown() * 1000L);
        }
    }

    public void createWarp(UUID userUuid, String name, UUID worldUuid, double x, double y, double z, float yaw, float pitch) {
        WarpModel warp = new WarpModel(name, worldUuid, x, y, z, yaw, pitch);
        UserWarpsConfig userConfig = WarpConfigManager.getUserConfig(userUuid);
        userConfig.warps.put(name, warp);
        WarpConfigManager.saveUserConfig(userUuid);
    }

    /**
     * Creates a warp with an associated block position.
     * Used for block-based warps like bonfires where we need to track the block location.
     */
    public void createWarp(UUID userUuid, String name, UUID worldUuid, double x, double y, double z, float yaw, float pitch,
                           int blockX, int blockY, int blockZ) {
        WarpModel warp = new WarpModel(name, worldUuid, x, y, z, yaw, pitch, blockX, blockY, blockZ);
        UserWarpsConfig userConfig = WarpConfigManager.getUserConfig(userUuid);
        userConfig.warps.put(name, warp);
        WarpConfigManager.saveUserConfig(userUuid);
    }

    public void deleteWarp(UUID userUuid, String name) {
        UserWarpsConfig userConfig = WarpConfigManager.getUserConfig(userUuid);
        userConfig.warps.remove(name);
        WarpConfigManager.saveUserConfig(userUuid);
    }

    public WarpModel getWarp(UUID userUuid, String name) {
        UserWarpsConfig userConfig = WarpConfigManager.getUserConfig(userUuid);
        return userConfig.warps.get(name);
    }

    public Collection<String> getWarpNames(UUID userUuid) {
        UserWarpsConfig userConfig = WarpConfigManager.getUserConfig(userUuid);
        return userConfig.warps.keySet();
    }

    /**
     * Finds a warp by its associated block position.
     * Returns null if no warp matches the given block coordinates.
     */
    public WarpModel findWarpByBlockPosition(UUID userUuid, int blockX, int blockY, int blockZ) {
        UserWarpsConfig userConfig = WarpConfigManager.getUserConfig(userUuid);
        for (WarpModel warp : userConfig.warps.values()) {
            if (warp.matchesBlockPosition(blockX, blockY, blockZ)) {
                return warp;
            }
        }
        return null;
    }

    /**
     * Deletes a warp that matches the given block position.
     * Returns the deleted warp, or null if no matching warp was found.
     */
    public WarpModel deleteWarpByBlockPosition(UUID userUuid, int blockX, int blockY, int blockZ) {
        WarpModel warp = findWarpByBlockPosition(userUuid, blockX, blockY, blockZ);
        if (warp != null) {
            deleteWarp(userUuid, warp.name);
        }
        return warp;
    }

    /**
     * Generates a unique bonfire warp name based on block coordinates.
     * Format: bonfire_x_y_z
     */
    public static String generateBonfireName(int blockX, int blockY, int blockZ) {
        return String.format("bonfire_%d_%d_%d", blockX, blockY, blockZ);
    }
}