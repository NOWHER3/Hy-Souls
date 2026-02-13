package com.nowhere.hysouls.death;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nowhere.hysouls.Main;
import com.nowhere.hysouls.appearance.PlayerAppearanceManager;
import com.nowhere.hysouls.currency.humanity.HumanityManager;
import com.nowhere.hysouls.currency.soul.SoulManager;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ECS System that handles Dark Souls-style death penalties when DeathComponent is added to players.
 * - Drop all souls as items
 * - Drop all humanity as items
 * - Become hollow (lose human form)
 */
public class DeathPenaltySystem extends DeathSystems.OnDeathSystem {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String SOUL_ESSENCE_ID = "Ingredient_Hysouls_Soul_Essence";
    private static final String HUMANITY_ESSENCE_ID = "Ingredient_Hysouls_Humanity_Essence"; // Soft version that auto-increments counter

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

            // Get death position
            Vector3d position = transform.getPosition();
            Vector3f rotation = Vector3f.ZERO;

            // Generate item drops
            List<ItemStack> itemsToDrop = new ArrayList<>();

            // Drop souls as items at death location
            if (souls > 0) {
                // Split into stacks of max 64
                int remaining = souls;
                while (remaining > 0) {
                    int stackSize = Math.min(remaining, 64);
                    itemsToDrop.add(new ItemStack(SOUL_ESSENCE_ID, stackSize));
                    remaining -= stackSize;
                }
                SoulManager.setSouls(playerId, 0); // Clear soul counter
                LOGGER.atInfo().log("Player %s dropped %d souls on death", playerId, souls);
            }

            // Drop humanity as items at death location
            if (humanity > 0) {
                // Split into stacks of max 64
                int remaining = humanity;
                while (remaining > 0) {
                    int stackSize = Math.min(remaining, 64);
                    itemsToDrop.add(new ItemStack(HUMANITY_ESSENCE_ID, stackSize));
                    remaining -= stackSize;
                }
                HumanityManager.removeHumanity(playerId, humanity); // Clear humanity counter
                LOGGER.atInfo().log("Player %s dropped %d humanity on death", playerId, humanity);
            }

            // Spawn item entities at death location
            if (!itemsToDrop.isEmpty()) {
                try {
                    Holder<EntityStore>[] dropEntities = ItemComponent.generateItemDrops(
                            store,
                            itemsToDrop,
                            position,
                            rotation
                    );

                    if (dropEntities != null && dropEntities.length > 0) {
                        commandBuffer.addEntities(dropEntities, AddReason.SPAWN);
                    }
                } catch (Exception e) {
                    LOGGER.atSevere().log("Error spawning death drops: %s", e.getMessage());
                }
            }

            // Make player hollow
            PlayerAppearanceManager.makeHollow(store, ref, playerId);

            // Update HUDs
            if (Main.get().getHudManager() != null) {
                Main.get().getHudManager().updateSoulDisplay(playerId);
                Main.get().getHudManager().updateHumanityDisplay(playerId);
            }

            // Notify player
            if (souls > 0 || humanity > 0) {
                player.sendMessage(Message.raw("You died and lost " + souls + " souls and " + humanity + " humanity.").color("#8B0000"));
            }

        } catch (Exception e) {
            LOGGER.atSevere().log("Error handling death penalty: %s", e.getMessage());
        }
    }
}
