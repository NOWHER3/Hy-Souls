package com.nowhere.hysouls.consumable.estus;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.DropItemEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class EstusDropSystem extends EntityEventSystem<EntityStore, DropItemEvent.Drop> {
    private static final String ESTUS_ITEM_ID = "Potion_Hysouls_Estus";
    private static final String ESTUS_EMPTY_ITEM_ID = "Potion_Hysouls_Estus_Empty";

    public EstusDropSystem() {
        super(DropItemEvent.Drop.class);
    }

    @Override
    public void handle(int id,
                       @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
                       @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
                       @NonNullDecl DropItemEvent.Drop event) {
        ItemStack itemStack = event.getItemStack();
        String itemId = itemStack != null ? itemStack.getItemId() : null;
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
