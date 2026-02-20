package com.nowhere.hysouls.loot;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ECS system that populates dungeon/prefab chests with soul items when chunks are loaded.
 * Uses runtime detection and population to avoid requiring prefab modifications.
 *
 * System Lifecycle:
 * 1. Queries for ItemContainerState components (chests) in loaded chunks
 * 2. For each chest, determines tier from block type
 * 3. Rolls weighted random chance and populates with soul essence items
 * 4. Tracks processed chests by position to avoid repopulation
 */
public class ChestLootSystem extends EntityTickingSystem<ChunkStore> {

    private static final ComponentType<ChunkStore, ItemContainerState> COMPONENT_TYPE =
            BlockStateModule.get().getComponentType(ItemContainerState.class);

    private final ChestLootPopulator populator;
    private final Set<String> processedChests = ConcurrentHashMap.newKeySet();

    public ChestLootSystem() {
        this.populator = new ChestLootPopulator();
    }

    @Nonnull
    @Override
    public Query<ChunkStore> getQuery() {
        return COMPONENT_TYPE;
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
                     @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
        // Check if system is enabled
        ChestLootConfig config = ChestLootConfigManager.getConfig();
        if (config == null || !config.isEnabled()) {
            return;
        }

        // Get the ItemContainerState component for this entity
        ItemContainerState container = archetypeChunk.getComponent(index, COMPONENT_TYPE);
        if (container == null) {
            return;
        }

        // Generate unique chest ID based on position
        Vector3i pos = container.getBlockPosition();
        if (pos == null) {
            return;
        }
        String chestId = pos.x + "," + pos.y + "," + pos.z;

        // Skip if already processed
        if (processedChests.contains(chestId)) {
            return;
        }

        // Skip if this chest was placed by a player
        if (PlayerPlacedChestTracker.isPlayerPlaced(chestId)) {
            processedChests.add(chestId); // Mark as processed to avoid checking again
            return;
        }

        // Get chunk to access block type
        WorldChunk chunk = container.getChunk();
        if (chunk == null) {
            return;
        }

        // Get block type
        BlockType blockType = chunk.getBlockType(pos.x, pos.y, pos.z);
        if (blockType == null) {
            return;
        }

        String blockTypeId = blockType.getId();

        // Skip non-chest blocks (safety check)
        if (!blockTypeId.toLowerCase().contains("chest")) {
            return;
        }

        // Get item container
        ItemContainer itemContainer = container.getItemContainer();
        if (itemContainer == null) {
            return;
        }

        // Determine tier and try to populate
        String tierName = populator.determineTier(blockTypeId);
        boolean populated = populator.tryPopulateChest(itemContainer, tierName, blockTypeId);

        // Mark chest as processed (whether populated or not, to avoid re-rolling)
        processedChests.add(chestId);

        // Ensure chest state is persisted if populated
        if (populated) {
            container.markNeedsSave();
        }
    }
}
