package com.nowhere.SoulWarp.event;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nowhere.SoulWarp.WarpManager;
import com.nowhere.SoulWarp.WarpModel;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class BreakBlockSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {
    private final WarpManager warpManager;

    public BreakBlockSystem(WarpManager warpManager) {
        super(BreakBlockEvent.class);
        this.warpManager = warpManager;
    }

    @Override
    public void handle(int id,
                       @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
                       @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
                       @NonNullDecl BreakBlockEvent breakBlockEvent) {
        BlockType blockType = breakBlockEvent.getBlockType();
        if (blockType == null || !blockType.getId().equals("Bench_Hysouls_Bonfire")) {
            return;
        }

        var reference = archetypeChunk.getReferenceTo(id);
        PlayerRef playerRef = store.getComponent(reference, PlayerRef.getComponentType());

        // Get the block position that is being broken
        Vector3i blockPos = breakBlockEvent.getTargetBlock();
        int blockX = blockPos.x;
        int blockY = blockPos.y;
        int blockZ = blockPos.z;

        // Find and delete the warp associated with this specific block position
        WarpModel deletedWarp = this.warpManager.deleteWarpByBlockPosition(
                playerRef.getUuid(),
                blockX, blockY, blockZ
        );

        if (deletedWarp != null) {
            playerRef.sendMessage(Message.raw("Bonfire warp '" + deletedWarp.name + "' deleted."));
        }
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }
}