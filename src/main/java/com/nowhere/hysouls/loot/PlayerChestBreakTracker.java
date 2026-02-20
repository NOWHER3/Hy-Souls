package com.nowhere.hysouls.loot;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * Removes broken chests from the player-placed blacklist.
 * This allows world-gen chests at the same coordinates to be populated later.
 */
public class PlayerChestBreakTracker extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    public PlayerChestBreakTracker() {
        super(BreakBlockEvent.class);
    }

    @Override
    public void handle(int id,
                       @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
                       @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
                       @NonNullDecl BreakBlockEvent breakBlockEvent) {
        BlockType blockType = breakBlockEvent.getBlockType();
        if (blockType == null) {
            return;
        }

        // Check if the broken block is a chest (any type of chest)
        String blockTypeId = blockType.getId();
        if (!blockTypeId.toLowerCase().contains("chest")) {
            return;
        }

        // Remove the chest from the player-placed blacklist
        Vector3i blockPos = breakBlockEvent.getTargetBlock();
        String chestId = blockPos.x + "," + blockPos.y + "," + blockPos.z;
        PlayerPlacedChestTracker.removeFromBlacklist(chestId);
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }
}
