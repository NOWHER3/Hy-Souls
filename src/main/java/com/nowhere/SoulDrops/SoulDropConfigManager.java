package com.nowhere.SoulDrops;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SoulDropConfigManager {
    private static final String CONFIG_DIR = "mods/Hysouls/drops";
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
                System.out.println("[SoulDrops] Config loaded successfully");
            }

        } catch (Exception e) {
            System.err.println("[SoulDrops] Failed to load config, using defaults: " + e.getMessage());
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
                System.out.println("[SoulDrops] Created default config file");
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

        // Default category
        SoulDropConfig.CategoryDrops defaultDrops = new SoulDropConfig.CategoryDrops();
        java.util.Map<String, SoulDropConfig.ItemDrop> defaultItems = new java.util.HashMap<>();
        SoulDropConfig.ItemDrop defaultSoul = new SoulDropConfig.ItemDrop();
        defaultSoul.setItemId("Ingredient_Hysouls_Soul_Essence");
        defaultSoul.setAmount(100);
        defaultSoul.setChance(1.0);
        defaultItems.put("soul_essence", defaultSoul);
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
