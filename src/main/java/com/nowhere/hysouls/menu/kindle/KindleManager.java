package com.nowhere.hysouls.menu.kindle;

import com.hypixel.hytale.math.vector.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Manages bonfire kindling levels and humanity costs.
 *
 * Kindling levels (Dark Souls Remastered style):
 * - Level 0: 5 estus (default, no kindling)
 * - Level 1: 10 estus (costs 1 humanity)
 * - Level 2: 15 estus (costs 1 humanity, requires Rite of Kindling)
 * - Level 3: 20 estus (costs 1 humanity, requires Rite of Kindling, max)
 */
public class KindleManager {
    private static final int MAX_KINDLE_LEVEL = 3;
    private static final int[] ESTUS_AMOUNTS = {5, 10, 15, 20};
    private static final int KINDLE_COST = 1; // Each kindling costs 1 humanity (Dark Souls style)

    /**
     * Gets the kindle level for a specific bonfire for a player.
     *
     * @param playerId Player UUID
     * @param bonfirePos Bonfire block position
     * @return Kindle level (0-3)
     */
    public static int getKindleLevel(@Nonnull UUID playerId, @Nonnull Vector3i bonfirePos) {
        String key = getBonfireKey(bonfirePos);
        KindleConfig config = KindleConfigManager.getUserConfig(playerId);
        return config.kindling.getOrDefault(key, 0);
    }

    /**
     * Gets the estus amount for a specific bonfire for a player.
     *
     * @param playerId Player UUID
     * @param bonfirePos Bonfire block position (null = default 5 estus)
     * @return Number of estus flasks (5, 10, 15, or 20)
     */
    public static int getEstusAmount(@Nonnull UUID playerId, @Nullable Vector3i bonfirePos) {
        if (bonfirePos == null) {
            return ESTUS_AMOUNTS[0]; // Default 5 estus
        }
        int level = getKindleLevel(playerId, bonfirePos);
        return ESTUS_AMOUNTS[level];
    }

    /**
     * Checks if a bonfire can be kindled (not at max level).
     *
     * @param playerId Player UUID
     * @param bonfirePos Bonfire block position
     * @return true if bonfire can be kindled further
     */
    public static boolean canKindle(@Nonnull UUID playerId, @Nonnull Vector3i bonfirePos) {
        int currentLevel = getKindleLevel(playerId, bonfirePos);
        return currentLevel < MAX_KINDLE_LEVEL;
    }

    /**
     * Gets the humanity cost to kindle a bonfire to the next level.
     * In Dark Souls style, each kindling costs 1 humanity.
     *
     * @param playerId Player UUID
     * @param bonfirePos Bonfire block position
     * @return Humanity cost (always 1), or 0 if already at max level
     */
    public static int getKindleCost(@Nonnull UUID playerId, @Nonnull Vector3i bonfirePos) {
        int currentLevel = getKindleLevel(playerId, bonfirePos);
        if (currentLevel >= MAX_KINDLE_LEVEL) {
            return 0;
        }
        return KINDLE_COST;
    }

    /**
     * Gets the next estus amount after kindling.
     *
     * @param playerId Player UUID
     * @param bonfirePos Bonfire block position
     * @return Next estus amount, or current amount if at max level
     */
    public static int getNextEstusAmount(@Nonnull UUID playerId, @Nonnull Vector3i bonfirePos) {
        int currentLevel = getKindleLevel(playerId, bonfirePos);
        if (currentLevel >= MAX_KINDLE_LEVEL) {
            return ESTUS_AMOUNTS[currentLevel];
        }
        return ESTUS_AMOUNTS[currentLevel + 1];
    }

    /**
     * Kindles a bonfire to the next level.
     * Does not check humanity or level limits - caller must verify first.
     *
     * @param playerId Player UUID
     * @param bonfirePos Bonfire block position
     * @return New kindle level
     */
    public static int kindleBonfire(@Nonnull UUID playerId, @Nonnull Vector3i bonfirePos) {
        String key = getBonfireKey(bonfirePos);
        KindleConfig config = KindleConfigManager.getUserConfig(playerId);
        int currentLevel = config.kindling.getOrDefault(key, 0);
        int newLevel = Math.min(currentLevel + 1, MAX_KINDLE_LEVEL);
        config.kindling.put(key, newLevel);
        KindleConfigManager.saveUserConfig(playerId);
        return newLevel;
    }

    /**
     * Generates a unique key for a bonfire based on its position.
     *
     * @param pos Bonfire block position
     * @return Key in format "x_y_z"
     */
    private static String getBonfireKey(@Nonnull Vector3i pos) {
        return String.format("%d_%d_%d", pos.x, pos.y, pos.z);
    }
}
