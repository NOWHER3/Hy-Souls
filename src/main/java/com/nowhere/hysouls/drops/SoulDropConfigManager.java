package com.nowhere.hysouls.drops;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SoulDropConfigManager {
    private static final String CONFIG_DIR = "mods/Hysouls/server";
    private static final String CONFIG_FILE = "soul_drops_config.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static SoulDropConfig config;

    public static void loadConfig() {
        Path configPath = Paths.get(CONFIG_DIR, CONFIG_FILE);

        try {
            // Create config directory if it doesn't exist
            Files.createDirectories(configPath.getParent());

            // If config file doesn't exist, copy default from resources
            if (!Files.exists(configPath)) {
                copyDefaultConfig(configPath);
            }

            // Load config from file
            try (Reader reader = Files.newBufferedReader(configPath)) {
                config = GSON.fromJson(reader, SoulDropConfig.class);
            }

        } catch (Exception e) {
            config = createDefaultConfig();
        }
    }

    private static void copyDefaultConfig(Path targetPath) throws IOException {
        // Try to load bundled config from resources
        InputStream resourceStream = SoulDropConfigManager.class
                .getResourceAsStream("/config/soul_drops_config.json");

        if (resourceStream != null) {
            try (InputStream in = resourceStream;
                 OutputStream out = Files.newOutputStream(targetPath)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        } else {
            // No bundled config, create programmatic default
            config = createDefaultConfig();
            saveConfig(targetPath);
        }
    }

    private static void saveConfig(Path configPath) throws IOException {
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            GSON.toJson(config, writer);
        }
    }

    private static SoulDropConfig createDefaultConfig() {
        SoulDropConfig defaultConfig = new SoulDropConfig();
        defaultConfig.setEnabled(true);

        // Create default category drops programmatically
        java.util.Map<String, SoulDropConfig.CategoryDrops> categories = new java.util.HashMap<>();

        // Elite category (tags: "Elite", "Champion", "Veteran")
        SoulDropConfig.CategoryDrops eliteDrops = new SoulDropConfig.CategoryDrops();
        eliteDrops.setTags(new String[]{"Elite", "Champion", "Veteran"});
        java.util.Map<String, SoulDropConfig.ItemDrop> eliteItems = new java.util.HashMap<>();

        SoulDropConfig.ItemDrop eliteSoul = new SoulDropConfig.ItemDrop();
        eliteSoul.setItemId("Ingredient_Hysouls_Soul_Essence_Hard_5");
        eliteSoul.setAmount(1);
        eliteSoul.setChance(1.0);
        eliteItems.put("soul_essence", eliteSoul);

        SoulDropConfig.ItemDrop eliteHumanity = new SoulDropConfig.ItemDrop();
        eliteHumanity.setItemId("Ingredient_Hysouls_Humanity_Essence_Concentrated");
        eliteHumanity.setAmount(1);
        eliteHumanity.setChance(0.25); // 25% chance for elite NPCs
        eliteItems.put("humanity_essence", eliteHumanity);

        eliteDrops.setDrops(eliteItems);
        categories.put("elite", eliteDrops);

        // Boss category (tags: "Boss", "MiniBoss", "BossMinion")
        SoulDropConfig.CategoryDrops bossDrops = new SoulDropConfig.CategoryDrops();
        bossDrops.setTags(new String[]{"Boss", "MiniBoss", "BossMinion"});
        java.util.Map<String, SoulDropConfig.ItemDrop> bossItems = new java.util.HashMap<>();

        SoulDropConfig.ItemDrop bossSoul = new SoulDropConfig.ItemDrop();
        bossSoul.setItemId("Ingredient_Hysouls_Soul_Essence_Hard_10");
        bossSoul.setAmount(1);
        bossSoul.setChance(1.0);
        bossItems.put("soul_essence", bossSoul);

        SoulDropConfig.ItemDrop bossHumanity = new SoulDropConfig.ItemDrop();
        bossHumanity.setItemId("Ingredient_Hysouls_Humanity_Essence_Concentrated");
        bossHumanity.setAmount(1);
        bossHumanity.setChance(0.5); // 50% chance for boss NPCs
        bossItems.put("humanity_essence", bossHumanity);

        bossDrops.setDrops(bossItems);
        categories.put("boss", bossDrops);

        // Default category (common NPCs)
        SoulDropConfig.CategoryDrops defaultDrops = new SoulDropConfig.CategoryDrops();
        java.util.Map<String, SoulDropConfig.ItemDrop> defaultItems = new java.util.HashMap<>();

        // Soul essence drop (always)
        SoulDropConfig.ItemDrop defaultSoul = new SoulDropConfig.ItemDrop();
        defaultSoul.setItemId("Ingredient_Hysouls_Soul_Essence");
        defaultSoul.setAmount(100);
        defaultSoul.setChance(1.0);
        defaultItems.put("soul_essence", defaultSoul);

        // Humanity essence drop (5% chance for common NPCs)
        SoulDropConfig.ItemDrop humanityDrop = new SoulDropConfig.ItemDrop();
        humanityDrop.setItemId("Ingredient_Hysouls_Humanity_Essence_Concentrated");
        humanityDrop.setAmount(1);
        humanityDrop.setChance(0.05);
        defaultItems.put("humanity_essence", humanityDrop);

        defaultDrops.setDrops(defaultItems);
        categories.put("default", defaultDrops);

        defaultConfig.setCategories(categories);
        return defaultConfig;
    }

    public static SoulDropConfig getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    public static void reloadConfig() {
        loadConfig();
    }
}
