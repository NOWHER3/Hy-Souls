package com.nowhere.hysouls.death;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Processes queued item drops on the next server tick.
 * Death drops are queued by DeathPenaltySystem and spawned here to avoid
 * interfering with the engine's death animation processing.
 */
public class DeathDropProcessor extends EntityTickingSystem<EntityStore> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Queue<PendingDrop> pendingDrops = new ConcurrentLinkedQueue<>();

    private final Query<EntityStore> query = Player.getComponentType();

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return query;
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        // Only process on first entity index to avoid duplicate processing
        if (index != 0) return;

        PendingDrop drop;
        while ((drop = pendingDrops.poll()) != null) {
            try {
                Holder<EntityStore>[] dropEntities = ItemComponent.generateItemDrops(
                        store, drop.items, drop.position, drop.rotation);

                if (dropEntities != null && dropEntities.length > 0) {
                    commandBuffer.addEntities(dropEntities, AddReason.SPAWN);
                    LOGGER.atInfo().log("Spawned %d death drop stacks for player %s",
                            dropEntities.length, drop.playerId);
                }
            } catch (Exception e) {
                LOGGER.atSevere().log("Error spawning queued death drops for player %s: %s",
                        drop.playerId, e.getMessage());
            }
        }
    }

    /**
     * Queue item drops to be spawned on the next tick.
     */
    public static void queueDrops(String playerId, List<ItemStack> items, Vector3d position) {
        pendingDrops.add(new PendingDrop(playerId, items, position, Vector3f.ZERO));
    }

    private static class PendingDrop {
        final String playerId;
        final List<ItemStack> items;
        final Vector3d position;
        final Vector3f rotation;

        PendingDrop(String playerId, List<ItemStack> items, Vector3d position, Vector3f rotation) {
            this.playerId = playerId;
            this.items = items;
            this.position = position;
            this.rotation = rotation;
        }
    }
}
