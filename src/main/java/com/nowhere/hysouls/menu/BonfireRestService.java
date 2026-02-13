package com.nowhere.hysouls.menu;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nowhere.hysouls.consumable.estus.EstusConfig;
import com.nowhere.hysouls.consumable.estus.EstusConfigManager;
import com.nowhere.hysouls.menu.kindle.KindleManager;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Service that handles bonfire rest functionality:
 * - Recharges estus flask (amount based on kindle level)
 * - Restores health to 100%
 * - Restores stamina to 100%
 */
public class BonfireRestService {
    private static final String ESTUS_ITEM_ID = "Potion_Hysouls_Estus";

    private final JavaPlugin plugin;
    private final Set<UUID> processing = ConcurrentHashMap.newKeySet();

    public BonfireRestService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Performs rest operations on a player at a bonfire:
     * 1. Recharges estus flask (amount based on bonfire kindle level)
     * 2. Restores health to 100%
     * 3. Restores stamina to 100%
     *
     * @param ref   Player entity reference
     * @param store Entity store
     * @param bonfirePos Bonfire position (null = default 5 estus)
     * @return true if rest was successful, false if already processing or error occurred
     */
    public boolean restAtBonfire(Ref<EntityStore> ref, Store<EntityStore> store, @Nullable Vector3i bonfirePos) {
        // Validate player ref
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null || !playerRef.isValid()) {
            return false;
        }

        UUID uuid = playerRef.getUuid();

        // Prevent concurrent rest operations for same player
        if (!processing.add(uuid)) {
            return false;
        }

        try {
            // Get player component
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) {
                return false;
            }

            // Perform rest operations
            boolean estusRecharged = rechargeEstus(player, uuid, bonfirePos);
            boolean healthRestored = restoreHealth(ref, store);
            boolean staminaRestored = restoreStamina(ref, store);

            // Log results (for debugging, can be removed in production)
            if (estusRecharged || healthRestored || staminaRestored) {
                this.plugin.getLogger().at(Level.FINE).log(
                    "Player %s rested at bonfire (Estus: %s, Health: %s, Stamina: %s)",
                    uuid, estusRecharged, healthRestored, staminaRestored
                );
            }

            return true;
        } catch (Exception e) {
            this.plugin.getLogger().at(Level.WARNING).log(
                "Error during bonfire rest for player " + uuid + ": " + e.getMessage(), e
            );
            return false;
        } finally {
            processing.remove(uuid);
        }
    }

    /**
     * Recharges player's estus flask based on bonfire kindle level.
     * Places estus in the player's configured hotbar slot.
     *
     * @param player Player entity
     * @param uuid   Player UUID
     * @param bonfirePos Bonfire position (null = default 5 estus)
     * @return true if estus was recharged
     */
    private boolean rechargeEstus(Player player, UUID uuid, @Nullable Vector3i bonfirePos) {
        try {
            // Get player's configured estus slot (default slot 1 → index 0)
            EstusConfig config = EstusConfigManager.load(uuid);
            short targetSlot = (short) (config.slot - 1);

            // Get estus amount based on kindle level
            int estusAmount = KindleManager.getEstusAmount(uuid, bonfirePos);

            Inventory inventory = player.getInventory();
            if (inventory == null) {
                return false;
            }

            ItemContainer hotbar = inventory.getHotbar();
            if (hotbar == null) {
                return false;
            }

            // Set full estus flask
            hotbar.setItemStackForSlot(targetSlot, new ItemStack(ESTUS_ITEM_ID, estusAmount));
            return true;
        } catch (Exception e) {
            this.plugin.getLogger().at(Level.WARNING).log(
                "Failed to recharge estus for player " + uuid + ": " + e.getMessage()
            );
            return false;
        }
    }

    /**
     * Restores player's health to maximum.
     *
     * @param ref   Player entity reference
     * @param store Entity store
     * @return true if health was restored
     */
    private boolean restoreHealth(Ref<EntityStore> ref, Store<EntityStore> store) {
        try {
            // Get EntityStatMap component
            EntityStatMap statMap = store.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType());
            if (statMap == null) {
                return false;
            }

            // Maximize health
            statMap.maximizeStatValue(DefaultEntityStatTypes.getHealth());
            return true;
        } catch (Exception e) {
            this.plugin.getLogger().at(Level.WARNING).log(
                "Failed to restore health: " + e.getMessage()
            );
            return false;
        }
    }

    /**
     * Restores player's stamina to maximum.
     *
     * @param ref   Player entity reference
     * @param store Entity store
     * @return true if stamina was restored
     */
    private boolean restoreStamina(Ref<EntityStore> ref, Store<EntityStore> store) {
        try {
            // Get EntityStatMap component
            EntityStatMap statMap = store.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType());
            if (statMap == null) {
                return false;
            }

            // Maximize stamina
            statMap.maximizeStatValue(DefaultEntityStatTypes.getStamina());
            return true;
        } catch (Exception e) {
            this.plugin.getLogger().at(Level.WARNING).log(
                "Failed to restore stamina: " + e.getMessage()
            );
            return false;
        }
    }

    /**
     * Cleanup method called during plugin shutdown.
     */
    public void shutdown() {
        processing.clear();
    }
}
