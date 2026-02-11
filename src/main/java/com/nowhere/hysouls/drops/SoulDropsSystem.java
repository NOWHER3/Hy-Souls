package com.nowhere.hysouls.drops;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.nowhere.hysouls.drops.util.NPCCategoryUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SoulDropsSystem extends DeathSystems.OnDeathSystem {

    private final Query<EntityStore> query = NPCEntity.getComponentType();

    @Override
    @Nonnull
    public Query<EntityStore> getQuery() {
        return query;
    }

    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component,
                                @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        // Check if system is enabled
        SoulDropConfig config = SoulDropConfigManager.getConfig();
        if (config == null || !config.isEnabled()) {
            return;
        }

        // Get NPC component
        NPCEntity npcComponent = commandBuffer.getComponent(ref, NPCEntity.getComponentType());
        if (npcComponent == null) {
            return;
        }

        // Determine NPC category from config-defined tags
        int roleIndex = npcComponent.getRoleIndex();
        String category = NPCCategoryUtil.getCategory(roleIndex, config.getCategories());

        // Get drops for this category
        SoulDropConfig.CategoryDrops categoryDrops = config.getCategoryDrops(category);
        if (categoryDrops == null || categoryDrops.getDrops() == null) {
            return;
        }

        System.out.println("[SoulDrops] NPC death: roleIndex=" + roleIndex + " category=" + category);

        // Get drop position (NPC location)
        TransformComponent transform = commandBuffer.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) {
            return;
        }

        Vector3d position = transform.getPosition();
        Vector3f rotation = Vector3f.ZERO;

        // Generate item drops
        List<ItemStack> itemsToDrop = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (SoulDropConfig.ItemDrop itemDrop : categoryDrops.getDrops().values()) {
            // Check drop chance
            if (random.nextDouble() > itemDrop.getChance()) {
                continue;
            }

            // Fixed amount (Dark Souls style)
            int quantity = itemDrop.getAmount();

            if (quantity > 0) {
                System.out.println("[SoulDrops]   -> dropping " + quantity + "x " + itemDrop.getItemId());
                itemsToDrop.add(new ItemStack(itemDrop.getItemId(), quantity));
            }
        }

        // Spawn item entities at NPC location
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
                System.err.println("[SoulDrops] Error spawning drops for category '" + category + "': " + e.getMessage());
            }
        }
    }
}
