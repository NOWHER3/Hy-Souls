package com.nowhere.SoulCurrency;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hypixel.hytale.logger.HytaleLogger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class SoulManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Type INT_TYPE = new TypeToken<Integer>(){}.getType();
    private static final ConcurrentHashMap<UUID, AtomicInteger> soulCounts = new ConcurrentHashMap<>();
    private static Path dataDirectory;

    private SoulManager() {}

    public static void init(Path baseDir) {
        dataDirectory = baseDir.resolve("souls");
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            LOGGER.atSevere().log("Failed to create souls directory: %s", e.getMessage());
        }
    }

    public static int getSouls(UUID playerId) {
        AtomicInteger counter = soulCounts.get(playerId);
        return counter != null ? counter.get() : 0;
    }

    public static void addSouls(UUID playerId, int amount) {
        if (amount <= 0) return;
        soulCounts.computeIfAbsent(playerId, k -> new AtomicInteger(0)).addAndGet(amount);
        save(playerId);
    }

    public static void setSouls(UUID playerId, int amount) {
        soulCounts.computeIfAbsent(playerId, k -> new AtomicInteger(0)).set(Math.max(0, amount));
        save(playerId);
    }

    public static void load(UUID playerId) {
        if (dataDirectory == null) {
            soulCounts.computeIfAbsent(playerId, k -> new AtomicInteger(0));
            return;
        }

        Path filePath = dataDirectory.resolve(playerId.toString() + ".json");
        if (!Files.exists(filePath, new LinkOption[0])) {
            soulCounts.put(playerId, new AtomicInteger(0));
            return;
        }

        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            Integer count = GSON.fromJson(reader, INT_TYPE);
            soulCounts.put(playerId, new AtomicInteger(count != null ? count : 0));
        } catch (Exception e) {
            LOGGER.atSevere().log("Failed to load soul data for %s: %s", playerId, e.getMessage());
            soulCounts.put(playerId, new AtomicInteger(0));
        }
    }

    public static void save(UUID playerId) {
        if (dataDirectory == null) return;
        AtomicInteger counter = soulCounts.get(playerId);
        if (counter == null) return;

        Path filePath = dataDirectory.resolve(playerId.toString() + ".json");
        try {
            Files.createDirectories(dataDirectory);
            if (Files.exists(filePath, new LinkOption[0])) {
                Path backup = filePath.resolveSibling(playerId.toString() + ".json.bak");
                Files.copy(filePath, backup, StandardCopyOption.REPLACE_EXISTING);
            }
            try (Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                GSON.toJson(counter.get(), writer);
            }
        } catch (IOException e) {
            LOGGER.atSevere().log("Failed to save soul data for %s: %s", playerId, e.getMessage());
        }
    }

    public static void unload(UUID playerId) {
        save(playerId);
        soulCounts.remove(playerId);
    }

    public static void shutdown() {
        for (UUID playerId : soulCounts.keySet()) {
            save(playerId);
        }
        soulCounts.clear();
        dataDirectory = null;
    }
}
