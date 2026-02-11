package com.nowhere.hysouls.consumable.estus;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.DropItemEvent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class EstusDropRequestSystem extends EntityEventSystem<EntityStore, DropItemEvent.PlayerRequest> {
    private static final String ESTUS_ITEM_ID = "Potion_Hysouls_Estus";
    private static final String ESTUS_EMPTY_ITEM_ID = "Potion_Hysouls_Estus_Empty";

    public EstusDropRequestSystem() {
        super(DropItemEvent.PlayerRequest.class);
    }

    @Override
    public void handle(int id,
                       @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
                       @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
                       @NonNullDecl DropItemEvent.PlayerRequest event) {
        var reference = archetypeChunk.getReferenceTo(id);
        Player player = store.getComponent(reference, Player.getComponentType());
        if (player == null) return;

        Inventory inventory = player.getInventory();
        int sectionId = event.getInventorySectionId();
        short slotId = event.getSlotId();

        ItemContainer section = inventory.getSectionById(sectionId);
        if (section == null) return;

        ItemStack stack = section.getItemStack(slotId);
        String itemId = stack != null ? stack.getItemId() : null;
        if (ESTUS_ITEM_ID.equals(itemId) || ESTUS_EMPTY_ITEM_ID.equals(itemId)) {
            event.setCancelled(true);
        }
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }
}
