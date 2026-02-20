package com.nowhere.hysouls.loot.util;

import com.nowhere.hysouls.loot.ChestLootConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility for determining chest tier based on block type ID pattern matching.
 * Uses flexible matching to support different chest variants.
 */
public class ChestTierUtil {
    private static final Map<String, String> tierCache = new ConcurrentHashMap<>();

    /**
     * Resolves the loot tier for a chest block type.
     * Checks against tag patterns defined in each tier config.
     *
     * Matching logic:
     * 1. If tier has "tags" array, check if blockTypeId contains any tag (case-insensitive)
     * 2. Otherwise, use tier name as the tag pattern
     * 3. First match wins (order matters)
     * 4. Falls back to "common" if no match
     */
    public static String getTier(String blockTypeId, Map<String, ChestLootConfig.ChestTierConfig> tiers) {
        if (blockTypeId == null || tiers == null) {
            return "common";
        }

        // Check cache first
        String cached = tierCache.get(blockTypeId);
        if (cached != null) {
            return cached;
        }

        // Resolve tier
        String tier = resolveTier(blockTypeId, tiers);
        tierCache.put(blockTypeId, tier);
        return tier;
    }

    private static String resolveTier(String blockTypeId, Map<String, ChestLootConfig.ChestTierConfig> tiers) {
        String blockTypeLower = blockTypeId.toLowerCase();

        // Check each tier in order
        for (Map.Entry<String, ChestLootConfig.ChestTierConfig> entry : tiers.entrySet()) {
            String tierName = entry.getKey();
            ChestLootConfig.ChestTierConfig tierConfig = entry.getValue();

            // Skip default tier in initial pass
            if ("common".equals(tierName)) {
                continue;
            }

            String[] tags = tierConfig.getTags();

            if (tags != null && tags.length > 0) {
                // Check if blockTypeId contains any of the tags
                for (String tag : tags) {
                    if (blockTypeLower.contains(tag.toLowerCase())) {
                        return tierName;
                    }
                }
            } else {
                // Use tier name as tag pattern
                if (blockTypeLower.contains(tierName.toLowerCase())) {
                    return tierName;
                }
            }
        }

        return "common";
    }

    public static void clearCache() {
        tierCache.clear();
    }
}
