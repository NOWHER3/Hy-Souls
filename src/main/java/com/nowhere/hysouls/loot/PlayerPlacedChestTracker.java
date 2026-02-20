package com.nowhere.hysouls.loot;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks player-placed chests to prevent the loot system from populating them.
 * Maintains a blacklist of chest positions that were placed by players.
 */
public class PlayerPlacedChestTracker extends EntityEventSystem<EntityStore, PlaceBlockEvent> {

    private static final Set<String> playerPlacedChests = ConcurrentHashMap.newKeySet();

    public PlayerPlacedChestTracker() {
        super(PlaceBlockEvent.class);
    }

    @Override
    public void handle(int id,
                       @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
                       @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
                       @NonNullDecl PlaceBlockEvent placeBlockEvent) {
        ItemStack itemInHand = placeBlockEvent.getItemInHand();
        if (itemInHand == null) {
            return;
        }

        // Check if the placed block is a chest (any type of chest)
        String itemId = itemInHand.getItemId();
        if (!itemId.toLowerCase().contains("chest")) {
            return;
        }

        // Track the position of the player-placed chest
        Vector3i blockPos = placeBlockEvent.getTargetBlock();
        String chestId = blockPos.x + "," + blockPos.y + "," + blockPos.z;
        playerPlacedChests.add(chestId);
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }

    /**
     * Check if a chest at the given position was placed by a player.
     */
    public static boolean isPlayerPlaced(String chestId) {
        return playerPlacedChests.contains(chestId);
    }

    /**
     * Remove a chest from the player-placed blacklist (e.g., when broken).
     */
    public static void removeFromBlacklist(String chestId) {
        playerPlacedChests.remove(chestId);
    }
}
