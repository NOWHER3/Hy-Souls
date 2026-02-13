package com.nowhere.hysouls.config;

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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages per-player gameplay data (souls, humanity, hollow state, kindling).
 * Stored in data/ folder.
 */
public final class PlayerDataManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final ConcurrentHashMap<UUID, PlayerData> dataMap = new ConcurrentHashMap<>();
    private static Path dataDirectory;

    private PlayerDataManager() {}

    public static void init(Path baseDir) {
        dataDirectory = baseDir.resolve("data");
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            LOGGER.atSevere().log("Failed to create data directory: %s", e.getMessage());
        }
        LOGGER.atInfo().log("PlayerDataManager initialized at: %s", dataDirectory);
    }

    public static PlayerData getPlayerData(UUID playerId) {
        return dataMap.computeIfAbsent(playerId, PlayerDataManager::loadFromDisk);
    }

    private static PlayerData loadFromDisk(UUID playerId) {
        if (dataDirectory == null) {
            LOGGER.atWarning().log("PlayerDataManager not initialized, returning default data for %s", playerId);
            return new PlayerData();
        }

        Path filePath = dataDirectory.resolve(playerId.toString() + ".json");
        if (!Files.exists(filePath, new LinkOption[0])) {
            LOGGER.atInfo().log("No data file found for %s, creating default", playerId);
            PlayerData data = new PlayerData();
            savePlayerData(playerId, data);
            return data;
        }

        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            PlayerData data = GSON.fromJson(reader, PlayerData.class);
            if (data == null) {
                LOGGER.atWarning().log("Data file for %s was null, creating default", playerId);
                data = new PlayerData();
            }
            LOGGER.atInfo().log("Loaded player data for %s", playerId);
            return data;
        } catch (Exception e) {
            LOGGER.atSevere().log("Failed to load player data for %s: %s", playerId, e.getMessage());
            return new PlayerData();
        }
    }

    public static void savePlayerData(UUID playerId) {
        PlayerData data = dataMap.get(playerId);
        if (data == null) {
            LOGGER.atWarning().log("Attempted to save null data for %s", playerId);
            return;
        }
        savePlayerData(playerId, data);
    }

    private static void savePlayerData(UUID playerId, PlayerData data) {
        if (dataDirectory == null) {
            LOGGER.atWarning().log("PlayerDataManager not initialized, cannot save data for %s", playerId);
            return;
        }

        Path filePath = dataDirectory.resolve(playerId.toString() + ".json");
        try {
            Files.createDirectories(dataDirectory);

            if (Files.exists(filePath, new LinkOption[0])) {
                Path backup = filePath.resolveSibling(playerId.toString() + ".json.bak");
                Files.copy(filePath, backup, StandardCopyOption.REPLACE_EXISTING);
            }

            try (Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
            LOGGER.atFine().log("Saved player data for %s", playerId);
        } catch (IOException e) {
            LOGGER.atSevere().log("Failed to save player data for %s: %s", playerId, e.getMessage());
        }
    }

    public static void unload(UUID playerId) {
        PlayerData data = dataMap.remove(playerId);
        if (data != null) {
            savePlayerData(playerId, data);
            LOGGER.atInfo().log("Unloaded player data for %s", playerId);
        }
    }

    public static void shutdown() {
        LOGGER.atInfo().log("Shutting down PlayerDataManager, saving %d data files", dataMap.size());
        for (UUID playerId : dataMap.keySet()) {
            savePlayerData(playerId);
        }
        dataMap.clear();
        dataDirectory = null;
    }

    public static int getLoadedDataCount() {
        return dataMap.size();
    }
}
