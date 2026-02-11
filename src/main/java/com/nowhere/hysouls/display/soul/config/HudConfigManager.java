package com.nowhere.hysouls.display.soul.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.logger.HytaleLogger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class HudConfigManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static Path dataDirectory;
    private static final Map<UUID, HudPositionConfig> configs = new HashMap<>();

    private HudConfigManager() {}

    public static void init(Path baseDir) {
        dataDirectory = baseDir.resolve("menus");
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            LOGGER.atSevere().log("Failed to create menus directory: %s", e.getMessage());
        }
    }

    public static HudPositionConfig load(UUID playerId) {
        if (configs.containsKey(playerId)) {
            return configs.get(playerId);
        }

        if (dataDirectory == null) {
            HudPositionConfig config = new HudPositionConfig();
            configs.put(playerId, config);
            return config;
        }

        Path filePath = dataDirectory.resolve(playerId.toString() + ".json");
        if (!Files.exists(filePath, new LinkOption[0])) {
            HudPositionConfig config = new HudPositionConfig();
            configs.put(playerId, config);
            return config;
        }

        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            HudPositionConfig config = GSON.fromJson(reader, HudPositionConfig.class);
            if (config == null) {
                config = new HudPositionConfig();
            }
            configs.put(playerId, config);
            return config;
        } catch (Exception e) {
            LOGGER.atSevere().log("Failed to load HUD config for %s: %s", playerId, e.getMessage());
            HudPositionConfig config = new HudPositionConfig();
            configs.put(playerId, config);
            return config;
        }
    }

    public static void save(UUID playerId) {
        if (dataDirectory == null) return;
        HudPositionConfig config = configs.get(playerId);
        if (config == null) return;

        Path filePath = dataDirectory.resolve(playerId.toString() + ".json");
        try {
            Files.createDirectories(dataDirectory);
            if (Files.exists(filePath, new LinkOption[0])) {
                Path backup = filePath.resolveSibling(playerId.toString() + ".json.bak");
                Files.copy(filePath, backup, StandardCopyOption.REPLACE_EXISTING);
            }
            try (Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            LOGGER.atSevere().log("Failed to save HUD config for %s: %s", playerId, e.getMessage());
        }
    }

    public static void unload(UUID playerId) {
        if (configs.containsKey(playerId)) {
            save(playerId);
            configs.remove(playerId);
        }
    }

    public static void shutdown() {
        for (UUID playerId : configs.keySet()) {
            save(playerId);
        }
        configs.clear();
        dataDirectory = null;
    }
}
