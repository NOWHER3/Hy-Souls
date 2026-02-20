package com.nowhere.hysouls.appearance;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.RespawnSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Applies hollow appearance after player respawns.
 * Extends the engine's OnRespawnSystem which fires when DeathComponent is removed
 * (i.e., when the player clicks Respawn). Using the engine's base class ensures
 * proper integration with the death/respawn lifecycle without interfering with
 * the death animation.
 */
public class PostRespawnAppearanceSystem extends RespawnSystems.OnRespawnSystem {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }

    @Override
    public void onComponentRemoved(@Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component,
                                   @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        try {
            PlayerRef playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
            if (playerRef == null || !playerRef.isValid()) {
                return;
            }

            UUID playerId = playerRef.getUuid();

            // Defer to after store processing unlocks (same tick) — calling putComponent() during
            // a RefChangeSystem callback would crash with "Store is currently processing!"
            commandBuffer.run(deferredStore -> {
                try {
                    // Apply hollow appearance (existing logic)
                    HollowEventHandler.checkAndUpdateHollowState(deferredStore, ref, playerId);

                    // Teleport to respawn bonfire if set
                    com.nowhere.hysouls.warp.SpawnPointManager.teleportToRespawnBonfire(deferredStore, ref, playerId);

                    LOGGER.atInfo().log("Applied post-respawn appearance and teleportation for player %s", playerId);
                } catch (Exception e) {
                    LOGGER.atSevere().log("Error in post-respawn processing for player %s: %s",
                            playerId, e.getMessage());
                }
            });

        } catch (Exception e) {
            LOGGER.atSevere().log("Error applying post-respawn appearance: %s", e.getMessage());
        }
    }
}
