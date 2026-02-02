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
        return WarpConfigManager.get().warmup;
    }

    public int getCooldown() {
        return WarpConfigManager.get().cooldown;
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

    public void createWarp(String name, UUID worldUuid, double x, double y, double z, float yaw, float pitch) {
        WarpModel warp = new WarpModel(name, worldUuid, x, y, z, yaw, pitch);
        WarpConfigManager.get().warps.put(name, warp);
        WarpConfigManager.save();
    }

    public void deleteWarp(String name) {
        WarpConfigManager.get().warps.remove(name);
        WarpConfigManager.save();
    }

    public WarpModel getWarp(String name) {
        return (WarpModel)WarpConfigManager.get().warps.get(name);
    }

    public Collection<String> getWarpNames() {
        return WarpConfigManager.get().warps.keySet();
    }
}