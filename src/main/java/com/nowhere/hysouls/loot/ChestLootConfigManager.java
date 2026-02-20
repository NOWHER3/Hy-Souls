package com.nowhere.hysouls.loot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ChestLootConfigManager {
    private static final String CONFIG_DIR = "mods/Hysouls/server";
    private static final String CONFIG_FILE = "chest_loot_config.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static ChestLootConfig config;

    public static void loadConfig() {
        Path configPath = Paths.get(CONFIG_DIR, CONFIG_FILE);

        try {
            // Create config directory if it doesn't exist
            Files.createDirectories(configPath.getParent());

            // If config file doesn't exist, create default
            if (!Files.exists(configPath)) {
                config = createDefaultConfig();
                saveConfig(configPath);
            } else {
                // Load config from file
                try (Reader reader = Files.newBufferedReader(configPath)) {
                    config = GSON.fromJson(reader, ChestLootConfig.class);
                    System.out.println("[ChestLoot] Config loaded successfully");
                }
            }

        } catch (Exception e) {
            System.err.println("[ChestLoot] Failed to load config, using defaults: " + e.getMessage());
            config = createDefaultConfig();
        }
    }

    private static void saveConfig(Path configPath) throws IOException {
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            GSON.toJson(config, writer);
            System.out.println("[ChestLoot] Created default config file");
        }
    }

    private static ChestLootConfig createDefaultConfig() {
        ChestLootConfig defaultConfig = new ChestLootConfig();
        defaultConfig.setEnabled(true);

        // Create tier configurations with tag-based matching
        Map<String, ChestLootConfig.ChestTierConfig> tiers = new HashMap<>();

        // Common tier - matches village, kweebec, outpost, camp, and basic chests (fallback for unmatched)
        ChestLootConfig.ChestTierConfig commonTier = new ChestLootConfig.ChestTierConfig();
        commonTier.setBaseChance(1.0);
        commonTier.setTags(new String[]{"village", "kweebec", "outpost", "camp", "cabin", "house", "small", "chest"}); // Basic chests + fallback
        Map<String, Double> commonWeights = new HashMap<>();
        commonWeights.put("Ingredient_Hysouls_Soul_Essence_Hard", 40.0);
        commonWeights.put("Ingredient_Hysouls_Soul_Essence_Hard_2", 30.0);
        commonWeights.put("Ingredient_Hysouls_Soul_Essence_Hard_3", 20.0);
        commonWeights.put("Ingredient_Hysouls_Soul_Essence_Hard_4", 10.0);
        commonWeights.put("Ingredient_Hysouls_Humanity_Essence_Concentrated", 5.0); // Small chance for humanity
        commonTier.setItemWeights(commonWeights);
        tiers.put("common", commonTier);

        // Rare tier - matches temple, ruins, fortress, tower, and other mid-tier chests
        ChestLootConfig.ChestTierConfig rareTier = new ChestLootConfig.ChestTierConfig();
        rareTier.setBaseChance(1.0);
        rareTier.setTags(new String[]{"temple", "emerald", "ruins", "fortress", "tower", "shrine", "crypt", "rare", "uncommon", "medium"}); // Mid-tier structures
        Map<String, Double> rareWeights = new HashMap<>();
        rareWeights.put("Ingredient_Hysouls_Soul_Essence_Hard_5", 40.0);
        rareWeights.put("Ingredient_Hysouls_Soul_Essence_Hard_6", 30.0);
        rareWeights.put("Ingredient_Hysouls_Soul_Essence_Hard_7", 20.0);
        rareWeights.put("Ingredient_Hysouls_Soul_Essence_Hard_8", 10.0);
        rareWeights.put("Ingredient_Hysouls_Humanity_Essence_Concentrated", 8.0); // Slightly higher chance for humanity
        rareTier.setItemWeights(rareWeights);
        tiers.put("rare", rareTier);

        // Epic tier - matches epic, boss, dungeon, vault, dragon, elite chests
        ChestLootConfig.ChestTierConfig epicTier = new ChestLootConfig.ChestTierConfig();
        epicTier.setBaseChance(1.0);
        epicTier.setTags(new String[]{"epic", "boss", "dungeon", "vault", "dragon", "elite"});
        Map<String, Double> epicWeights = new HashMap<>();
        epicWeights.put("Ingredient_Hysouls_Soul_Essence_Hard_7", 35.0);
        epicWeights.put("Ingredient_Hysouls_Soul_Essence_Hard_8", 30.0);
        epicWeights.put("Ingredient_Hysouls_Soul_Essence_Hard_9", 25.0);
        epicWeights.put("Ingredient_Hysouls_Humanity_Essence_Concentrated", 9.0);
        epicTier.setItemWeights(epicWeights);
        tiers.put("epic", epicTier);

        // Legendary tier - matches legendary chests only
        ChestLootConfig.ChestTierConfig legendaryTier = new ChestLootConfig.ChestTierConfig();
        legendaryTier.setBaseChance(1.0);
        legendaryTier.setTags(new String[]{"legendary", "legend", "treasure"});
        Map<String, Double> legendaryWeights = new HashMap<>();
        legendaryWeights.put("Ingredient_Hysouls_Soul_Essence_Hard_9", 60.0);
        legendaryWeights.put("Ingredient_Hysouls_Soul_Essence_Hard_10", 40.0);
        legendaryWeights.put("Ingredient_Hysouls_Humanity_Essence_Concentrated", 10.0); // Higher chance in legendary chests
        legendaryTier.setItemWeights(legendaryWeights);
        tiers.put("legendary", legendaryTier);

        defaultConfig.setTiers(tiers);

        // Keep legacy mappings for backwards compatibility (optional, will be ignored if tiers have tags)
        Map<String, String> blockTypeMappings = new HashMap<>();
        blockTypeMappings.put("Chest", "common");
        blockTypeMappings.put("Chest_Rare", "rare");
        blockTypeMappings.put("Chest_Epic", "epic");
        blockTypeMappings.put("Chest_Legendary", "legendary");
        defaultConfig.setChestBlockTypeMappings(blockTypeMappings);

        return defaultConfig;
    }

    public static ChestLootConfig getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    public static void reloadConfig() {
        loadConfig();
    }
}
