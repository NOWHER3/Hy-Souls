package com.nowhere.hysouls.currency.soul;

import com.hypixel.hytale.builtin.crafting.state.BenchState;
import com.hypixel.hytale.builtin.crafting.window.SimpleCraftingWindow;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.ExtraResources;
import com.hypixel.hytale.protocol.ItemQuantity;
import com.hypixel.hytale.protocol.packets.window.WindowAction;
import com.hypixel.hytale.server.core.entity.entities.player.windows.MaterialExtraResourcesSection;
import com.hypixel.hytale.server.core.entity.entities.player.windows.WindowManager;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.UUID;

/**
 * Crafting window that provides soul essence from the player's soul counter
 * via the extra resources section (same mechanism as nearby chests).
 *
 * Uses a tracking container to intercept item removal at the low level,
 * syncing consumed souls back to the counter in real time.
 */
public class SoulCraftingWindow extends SimpleCraftingWindow {

    private static final String SOUL_ITEM_ID = "Ingredient_Hysouls_Soul_Essence";

    private final UUID playerUuid;
    private TrackingSoulContainer soulContainer;
    private int lastSyncedRemoved = 0;
    private boolean soulsMerged = false;

    public SoulCraftingWindow(BenchState benchState, UUID playerUuid) {
        super(benchState);
        this.playerUuid = playerUuid;
    }

    @Override
    public void init(@Nonnull PlayerRef playerRef, @Nonnull WindowManager manager) {
        super.init(playerRef, manager);

        int souls = SoulManager.getSouls(playerUuid);
        this.soulContainer = new TrackingSoulContainer();
        if (souls > 0) {
            this.soulContainer.addItemStack(new ItemStack(SOUL_ITEM_ID, souls));
        }
    }

    @Override
    public void handleAction(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                             @Nonnull WindowAction action) {
        super.handleAction(ref, store, action);
        syncCounter();
    }

    @Override
    @Nonnull
    public MaterialExtraResourcesSection getExtraResourcesSection() {
        MaterialExtraResourcesSection section = super.getExtraResourcesSection();

        if (!soulsMerged) {
            section.setItemContainer(
                    new CombinedItemContainer(section.getItemContainer(), soulContainer)
            );

            int soulCount = soulContainer.getCurrentCount();
            if (soulCount > 0) {
                ExtraResources existing = section.toPacket();
                ItemQuantity soulQuantity = new ItemQuantity(SOUL_ITEM_ID, soulCount);

                if (existing.resources != null && existing.resources.length > 0) {
                    ItemQuantity[] merged = Arrays.copyOf(existing.resources, existing.resources.length + 1);
                    merged[merged.length - 1] = soulQuantity;
                    section.setExtraMaterials(merged);
                } else {
                    section.setExtraMaterials(new ItemQuantity[]{ soulQuantity });
                }
            }

            soulsMerged = true;
        }

        return section;
    }

    @Override
    public void invalidateExtraResources() {
        soulsMerged = false;
        syncCounter();
        super.invalidateExtraResources();
    }

    @Override
    public void onClose0() {
        syncCounter();
        super.onClose0();
    }

    /**
     * Deducts newly consumed souls from the player's counter.
     * Called after each craft action, invalidation, and on close.
     */
    private void syncCounter() {
        if (soulContainer == null) return;
        int totalRemoved = soulContainer.getTotalRemoved();
        int newlyConsumed = totalRemoved - lastSyncedRemoved;
        if (newlyConsumed > 0) {
            lastSyncedRemoved = totalRemoved;
            int current = SoulManager.getSouls(playerUuid);
            SoulManager.setSouls(playerUuid, Math.max(0, current - newlyConsumed));
        }
    }

    /**
     * A SimpleItemContainer that tracks how many items have been removed,
     * intercepting at the internal_setSlot / internal_removeSlot level
     * so we catch all removal paths (full slot clear, partial quantity decrease).
     */
    private static class TrackingSoulContainer extends SimpleItemContainer {
        private int totalRemoved = 0;
        private boolean insideSetSlot = false;

        TrackingSoulContainer() {
            super((short) 1);
        }

        @Override
        protected ItemStack internal_setSlot(short slot, ItemStack itemStack) {
            int prevQty = getSlotQuantity(slot);
            insideSetSlot = true;
            ItemStack result = super.internal_setSlot(slot, itemStack);
            insideSetSlot = false;
            int newQty = getSlotQuantity(slot);
            if (prevQty > newQty) {
                totalRemoved += (prevQty - newQty);
            }
            return result;
        }

        @Override
        protected ItemStack internal_removeSlot(short slot) {
            if (!insideSetSlot) {
                totalRemoved += getSlotQuantity(slot);
            }
            return super.internal_removeSlot(slot);
        }

        private int getSlotQuantity(short slot) {
            ItemStack stack = this.internal_getSlot(slot);
            return stack != null ? stack.getQuantity() : 0;
        }

        int getTotalRemoved() {
            return totalRemoved;
        }

        int getCurrentCount() {
            return getSlotQuantity((short) 0);
        }
    }
}
