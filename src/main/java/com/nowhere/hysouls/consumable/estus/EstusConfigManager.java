package com.nowhere.hysouls.consumable.estus;

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

public final class EstusConfigManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static Path dataDirectory;
    private static final Map<UUID, EstusConfig> configs = new HashMap<>();

    private EstusConfigManager() {}

    public static void init(Path baseDir) {
        dataDirectory = baseDir.resolve("estus");
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            LOGGER.atSevere().log("Failed to create estus directory: %s", e.getMessage());
        }
    }

    public static EstusConfig load(UUID playerId) {
        if (configs.containsKey(playerId)) {
            return configs.get(playerId);
        }

        if (dataDirectory == null) {
            EstusConfig config = new EstusConfig();
            configs.put(playerId, config);
            return config;
        }

        Path filePath = dataDirectory.resolve(playerId.toString() + ".json");
        if (!Files.exists(filePath, new LinkOption[0])) {
            EstusConfig config = new EstusConfig();
            configs.put(playerId, config);
            return config;
        }

        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            EstusConfig config = GSON.fromJson(reader, EstusConfig.class);
            if (config == null) {
                config = new EstusConfig();
            }
            configs.put(playerId, config);
            return config;
        } catch (Exception e) {
            LOGGER.atSevere().log("Failed to load estus config for %s: %s", playerId, e.getMessage());
            EstusConfig config = new EstusConfig();
            configs.put(playerId, config);
            return config;
        }
    }

    public static void save(UUID playerId) {
        if (dataDirectory == null) return;
        EstusConfig config = configs.get(playerId);
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
            LOGGER.atSevere().log("Failed to save estus config for %s: %s", playerId, e.getMessage());
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
