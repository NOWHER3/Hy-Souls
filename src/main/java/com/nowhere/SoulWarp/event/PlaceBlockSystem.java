package com.nowhere.SoulWarp.event;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nowhere.SoulWarp.WarpManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class PlaceBlockSystem extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
    private final WarpManager warpManager;

    public PlaceBlockSystem(WarpManager warpManager) {
        super(PlaceBlockEvent.class);
        this.warpManager = warpManager;
    }

    @Override
    public void handle(int id,
                       @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
                       @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
                       @NonNullDecl PlaceBlockEvent placeBlockEvent) {
        ItemStack itemInHand = placeBlockEvent.getItemInHand();
        if (itemInHand == null || !itemInHand.getItemId().equals("Bench_Hysouls_Bonfire")) {
            return;
        }

        var reference = archetypeChunk.getReferenceTo(id);
        PlayerRef playerRef = store.getComponent(reference, PlayerRef.getComponentType());

        // Get the block position where the bonfire is being placed
        Vector3i blockPos = placeBlockEvent.getTargetBlock();
        int blockX = blockPos.x;
        int blockY = blockPos.y;
        int blockZ = blockPos.z;

        // Get player's position and rotation for the teleport destination
        Vector3d pos = playerRef.getTransform().getPosition();
        Vector3f rot = playerRef.getHeadRotation();

        // Generate a unique warp name based on block coordinates
        String warpName = WarpManager.generateBonfireName(blockX, blockY, blockZ);

        // Create the warp with both teleport position and block position
        this.warpManager.createWarp(
                playerRef.getUuid(),
                warpName,
                playerRef.getWorldUuid(),
                pos.x, pos.y, pos.z,
                rot.getYaw(), rot.getPitch(),
                blockX, blockY, blockZ
        );

        playerRef.sendMessage(Message.raw("Bonfire warp '" + warpName + "' set."));
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }
}