package com.nowhere.hysouls.death;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nowhere.hysouls.Main;
import com.nowhere.hysouls.currency.humanity.HumanityManager;
import com.nowhere.hysouls.currency.soul.SoulManager;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ECS System that handles Dark Souls-style death penalties when DeathComponent is added to players.
 * - Drop all souls as items (queued for next tick to avoid breaking death animation)
 * - Drop all humanity as items (queued for next tick)
 * - Become hollow (lose human form)
 */
public class DeathPenaltySystem extends DeathSystems.OnDeathSystem {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String SOUL_ESSENCE_ID = "Ingredient_Hysouls_Soul_Essence";
    private static final String HUMANITY_ESSENCE_ID = "Ingredient_Hysouls_Humanity_Essence";

    private final Query<EntityStore> query = Query.and(Player.getComponentType());

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return query;
    }

    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component,
                                 @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        try {
            Player player = commandBuffer.getComponent(ref, Player.getComponentType());
            PlayerRef playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
            TransformComponent transform = commandBuffer.getComponent(ref, TransformComponent.getComponentType());

            if (player == null || playerRef == null || transform == null) {
                return;
            }

            UUID playerId = playerRef.getUuid();

            // Get current souls and humanity
            int souls = SoulManager.getSouls(playerId);
            int humanity = HumanityManager.getHumanity(playerId);

            // Clear counters
            if (souls > 0) {
                SoulManager.setSouls(playerId, 0);
                LOGGER.atInfo().log("Player %s dropped %d souls on death", playerId, souls);
            }
            if (humanity > 0) {
                HumanityManager.removeHumanity(playerId, humanity);
                LOGGER.atInfo().log("Player %s dropped %d humanity on death", playerId, humanity);
            }

            // Queue item drops for next tick (spawning during death processing breaks death animation)
            List<ItemStack> itemsToDrop = new ArrayList<>();
            if (souls > 0) {
                int remaining = souls;
                while (remaining > 0) {
                    int stackSize = Math.min(remaining, 64);
                    itemsToDrop.add(new ItemStack(SOUL_ESSENCE_ID, stackSize));
                    remaining -= stackSize;
                }
            }
            if (humanity > 0) {
                int remaining = humanity;
                while (remaining > 0) {
                    int stackSize = Math.min(remaining, 64);
                    itemsToDrop.add(new ItemStack(HUMANITY_ESSENCE_ID, stackSize));
                    remaining -= stackSize;
                }
            }
            if (!itemsToDrop.isEmpty()) {
                Vector3d position = transform.getPosition();
                DeathDropProcessor.queueDrops(playerId.toString(), itemsToDrop, position);
            }

            // Set hollow state
            com.nowhere.hysouls.appearance.HollowManager.setHollow(playerId, true);
            LOGGER.atInfo().log("Player %s marked as hollow (appearance will update after respawn)", playerId);

            // Update HUDs
            if (Main.get().getHudManager() != null) {
                Main.get().getHudManager().updateSoulDisplay(playerId);
                Main.get().getHudManager().updateHumanityDisplay(playerId);
            }

            // Notify player
            if (souls > 0 || humanity > 0) {
                player.sendMessage(Message.raw("You died and lost " + souls + " souls and " + humanity + " humanity.").color("#8B0000"));
            }
            player.sendMessage(Message.raw("You will become hollow...").color("#8B0000"));

        } catch (Exception e) {
            LOGGER.atSevere().log("Error handling death penalty: %s", e.getMessage());
        }
    }
}
