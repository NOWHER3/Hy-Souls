package com.nowhere.Estus;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class EstusManager {
    private static final String ESTUS_ITEM_ID = "Potion_Hysouls_Estus";
    private static final String ESTUS_EMPTY_ITEM_ID = "Potion_Hysouls_Estus_Empty";
    private final JavaPlugin plugin;
    private final Set<UUID> processing = ConcurrentHashMap.newKeySet();

    public EstusManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.registerListeners();
    }

    private void registerListeners() {
        this.plugin.getEventRegistry().registerGlobal(PlayerReadyEvent.class, (event) -> {
            Player player = event.getPlayer();
            Ref<EntityStore> ref = event.getPlayerRef();

            if (player != null && ref != null) {
                Store<EntityStore> store = ref.getStore();
                PlayerRef playerRef = (PlayerRef) store.getComponent(ref, PlayerRef.getComponentType());
                if (playerRef != null && playerRef.isValid()) {
                    this.handlePlayerReady(playerRef, player);
                }
            }
        });

        this.plugin.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, (event) -> {
            PlayerRef pr = event.getPlayerRef();
            if (pr != null) {
                EstusConfigManager.unload(pr.getUuid());
            }
        });

        this.plugin.getEventRegistry().registerGlobal(LivingEntityInventoryChangeEvent.class, (event) -> {
            LivingEntity entity = (LivingEntity) event.getEntity();
            if (entity instanceof Player player) {
                Ref<EntityStore> ref = player.getReference();
                if (ref != null) {
                    Store<EntityStore> store = ref.getStore();
                    PlayerRef playerRef = (PlayerRef) store.getComponent(ref, PlayerRef.getComponentType());
                    if (playerRef != null && playerRef.isValid()) {
                        this.handleInventoryChange(playerRef, player);
                    }
                }
            }
        });
    }

    private void handlePlayerReady(PlayerRef playerRef, Player player) {
        UUID uuid = playerRef.getUuid();
        EstusConfig config = EstusConfigManager.load(uuid);
        short targetSlot = (short) (config.slot - 1);

        if (!processing.add(uuid)) return;
        try {
            Inventory inventory = player.getInventory();
            ItemContainer hotbar = inventory.getHotbar();

            // Scan hotbar for existing estus (real or empty)
            short existingSlot = findAnyEstusInContainer(hotbar);

            if (existingSlot >= 0) {
                // Estus found in hotbar
                if (existingSlot != targetSlot) {
                    swapSlots(hotbar, existingSlot, targetSlot);
                }
            } else {
                // Check all other sections
                short foundSlot = findAndRemoveEstusFromSections(inventory);
                if (foundSlot >= 0) {
                    // Found in another section, place in hotbar target slot
                    ItemStack occupant = hotbar.getItemStack(targetSlot);
                    hotbar.setItemStackForSlot(targetSlot, new ItemStack(ESTUS_ITEM_ID, 1));
                    // If something was in the target slot, move it to storage
                    if (occupant != null && !isEstus(occupant)) {
                        inventory.getStorage().addItemStack(occupant);
                    }
                } else {
                    // No estus anywhere — give default amount
                    ItemStack occupant = hotbar.getItemStack(targetSlot);
                    if (occupant != null && !ItemStack.isEmpty(occupant)) {
                        // Move occupant out of the way
                        hotbar.removeItemStackFromSlot(targetSlot);
                        inventory.getStorage().addItemStack(occupant);
                    }
                    hotbar.setItemStackForSlot(targetSlot, new ItemStack(ESTUS_ITEM_ID, 5));
                }
            }
        } catch (Exception e) {
            this.plugin.getLogger().at(Level.WARNING).log("Error giving estus to " + uuid + ": " + e.getMessage());
        } finally {
            processing.remove(uuid);
        }
    }

    private void handleInventoryChange(PlayerRef playerRef, Player player) {
        UUID uuid = playerRef.getUuid();

        if (!processing.add(uuid)) return;
        try {
            EstusConfig config = EstusConfigManager.load(uuid);
            short targetSlot = (short) (config.slot - 1);

            Inventory inventory = player.getInventory();
            ItemContainer hotbar = inventory.getHotbar();

            // Check if estus is already in the correct slot
            ItemStack targetItem = hotbar.getItemStack(targetSlot);
            if (targetItem != null && isEstus(targetItem)) {
                return;
            }

            // Estus is NOT in the correct slot — find it
            // Check hotbar first
            short estusSlot = findEstusInContainer(hotbar);
            if (estusSlot >= 0) {
                // Estus is in hotbar but wrong slot — swap
                swapSlots(hotbar, estusSlot, targetSlot);
                return;
            }

            // Check other inventory sections
            ItemContainer[] sections = {inventory.getStorage(), inventory.getBackpack(), inventory.getUtility(), inventory.getTools()};
            for (ItemContainer section : sections) {
                if (section == null) continue;
                short capacity = section.getCapacity();
                for (short slot = 0; slot < capacity; slot++) {
                    ItemStack stack = section.getItemStack(slot);
                    if (stack != null && isEstus(stack)) {
                        // Remove from this section
                        section.removeItemStackFromSlot(slot);
                        // Move current occupant of target slot if needed
                        ItemStack occupant = hotbar.getItemStack(targetSlot);
                        hotbar.setItemStackForSlot(targetSlot, new ItemStack(ESTUS_ITEM_ID, 1));
                        if (occupant != null && !ItemStack.isEmpty(occupant) && !isAnyEstus(occupant)) {
                            section.setItemStackForSlot(slot, occupant);
                        }
                        return;
                    }
                }
            }

            // No real estus found anywhere — place empty estus if not already there
            ItemStack currentTarget = hotbar.getItemStack(targetSlot);
            if (currentTarget == null || !isEmptyEstus(currentTarget)) {
                if (currentTarget != null && !ItemStack.isEmpty(currentTarget)) {
                    hotbar.removeItemStackFromSlot(targetSlot);
                    inventory.getStorage().addItemStack(currentTarget);
                }
                hotbar.setItemStackForSlot(targetSlot, new ItemStack(ESTUS_EMPTY_ITEM_ID, 1));
            }
        } catch (Exception e) {
            this.plugin.getLogger().at(Level.WARNING).log("Error enforcing estus slot for " + uuid + ": " + e.getMessage());
        } finally {
            processing.remove(uuid);
        }
    }

    public void moveEstusToSlot(Player player, UUID uuid, int newSlot) {
        if (!processing.add(uuid)) return;
        try {
            short targetSlot = (short) newSlot;
            Inventory inventory = player.getInventory();
            ItemContainer hotbar = inventory.getHotbar();

            short estusSlot = findAnyEstusInContainer(hotbar);
            if (estusSlot >= 0) {
                if (estusSlot != targetSlot) {
                    swapSlots(hotbar, estusSlot, targetSlot);
                }
            } else {
                // No estus in hotbar, place empty
                ItemStack occupant = hotbar.getItemStack(targetSlot);
                hotbar.setItemStackForSlot(targetSlot, new ItemStack(ESTUS_EMPTY_ITEM_ID, 1));
                if (occupant != null && !ItemStack.isEmpty(occupant)) {
                    inventory.getStorage().addItemStack(occupant);
                }
            }
        } finally {
            processing.remove(uuid);
        }
    }

    private short findEstusInContainer(ItemContainer container) {
        if (container == null) return -1;
        short capacity = container.getCapacity();
        for (short slot = 0; slot < capacity; slot++) {
            ItemStack stack = container.getItemStack(slot);
            if (stack != null && isEstus(stack)) {
                return slot;
            }
        }
        return -1;
    }

    private short findAnyEstusInContainer(ItemContainer container) {
        if (container == null) return -1;
        short capacity = container.getCapacity();
        for (short slot = 0; slot < capacity; slot++) {
            ItemStack stack = container.getItemStack(slot);
            if (stack != null && isAnyEstus(stack)) {
                return slot;
            }
        }
        return -1;
    }

    /**
     * Finds and removes estus from non-hotbar sections.
     * Returns the slot it was found in (>= 0) or -1 if not found.
     */
    private short findAndRemoveEstusFromSections(Inventory inventory) {
        ItemContainer[] sections = {inventory.getStorage(), inventory.getBackpack(), inventory.getUtility(), inventory.getTools()};
        for (ItemContainer section : sections) {
            if (section == null) continue;
            short capacity = section.getCapacity();
            for (short slot = 0; slot < capacity; slot++) {
                ItemStack stack = section.getItemStack(slot);
                if (stack != null && isEstus(stack)) {
                    section.removeItemStackFromSlot(slot);
                    return slot;
                }
            }
        }
        return -1;
    }

    private void swapSlots(ItemContainer hotbar, short fromSlot, short toSlot) {
        ItemStack estusStack = hotbar.getItemStack(fromSlot);
        ItemStack occupant = hotbar.getItemStack(toSlot);
        hotbar.removeItemStackFromSlot(fromSlot);
        hotbar.removeItemStackFromSlot(toSlot);
        hotbar.setItemStackForSlot(toSlot, estusStack);
        if (occupant != null && !ItemStack.isEmpty(occupant)) {
            hotbar.setItemStackForSlot(fromSlot, occupant);
        }
    }

    private static boolean isEstus(ItemStack stack) {
        if (stack == null) return false;
        try {
            if (ItemStack.isEmpty(stack)) return false;
        } catch (NoSuchMethodError e) {
            if (stack.getQuantity() <= 0) return false;
        }
        String id = stack.getItemId();
        return ESTUS_ITEM_ID.equals(id);
    }

    private static boolean isEmptyEstus(ItemStack stack) {
        if (stack == null) return false;
        return ESTUS_EMPTY_ITEM_ID.equals(stack.getItemId());
    }

    private static boolean isAnyEstus(ItemStack stack) {
        return isEstus(stack) || isEmptyEstus(stack);
    }

    public void shutdown() {
        processing.clear();
    }
}
