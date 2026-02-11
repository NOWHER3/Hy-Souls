package com.nowhere.hysouls.drops;

import java.util.Map;

public class SoulDropConfig {
    private boolean enabled = true;
    private Map<String, CategoryDrops> categories;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, CategoryDrops> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, CategoryDrops> categories) {
        this.categories = categories;
    }

    public CategoryDrops getCategoryDrops(String category) {
        if (categories == null) return null;

        // Try exact match first
        CategoryDrops drops = categories.get(category);
        if (drops != null) return drops;

        // Fallback to default
        return categories.get("default");
    }

    public static class CategoryDrops {
        private String[] tags;
        private Map<String, ItemDrop> drops;

        public String[] getTags() {
            return tags;
        }

        public void setTags(String[] tags) {
            this.tags = tags;
        }

        public Map<String, ItemDrop> getDrops() {
            return drops;
        }

        public void setDrops(Map<String, ItemDrop> drops) {
            this.drops = drops;
        }
    }

    public static class ItemDrop {
        private String itemId;
        private int amount;
        private double chance = 1.0;

        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public double getChance() {
            return chance;
        }

        public void setChance(double chance) {
            this.chance = chance;
        }
    }
}
