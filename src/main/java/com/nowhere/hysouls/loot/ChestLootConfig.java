package com.nowhere.hysouls.loot;

import java.util.Map;

public class ChestLootConfig {
    private boolean enabled = true;
    private Map<String, ChestTierConfig> tiers;
    private Map<String, String> chestBlockTypeMappings;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, ChestTierConfig> getTiers() {
        return tiers;
    }

    public void setTiers(Map<String, ChestTierConfig> tiers) {
        this.tiers = tiers;
    }

    public Map<String, String> getChestBlockTypeMappings() {
        return chestBlockTypeMappings;
    }

    public void setChestBlockTypeMappings(Map<String, String> chestBlockTypeMappings) {
        this.chestBlockTypeMappings = chestBlockTypeMappings;
    }

    public ChestTierConfig getTierConfig(String tierName) {
        if (tiers == null) return null;
        return tiers.get(tierName);
    }

    public String getTierForBlockType(String blockTypeId) {
        // Use new tag-based matching (preferred)
        if (tiers != null) {
            return com.nowhere.hysouls.loot.util.ChestTierUtil.getTier(blockTypeId, tiers);
        }

        // Legacy fallback: exact block type ID matching
        if (chestBlockTypeMappings != null) {
            return chestBlockTypeMappings.getOrDefault(blockTypeId, "common");
        }

        return "common";
    }

    public static class ChestTierConfig {
        private double baseChance;
        private Map<String, Double> itemWeights;
        private String[] tags;  // Tag patterns for matching block types

        public double getBaseChance() {
            return baseChance;
        }

        public void setBaseChance(double baseChance) {
            this.baseChance = baseChance;
        }

        public Map<String, Double> getItemWeights() {
            return itemWeights;
        }

        public void setItemWeights(Map<String, Double> itemWeights) {
            this.itemWeights = itemWeights;
        }

        public String[] getTags() {
            return tags;
        }

        public void setTags(String[] tags) {
            this.tags = tags;
        }
    }
}
