package com.nowhere.hysouls.loot;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ChestLootPopulator {

    /**
     * Weighted random selection from item ID → weight map.
     * Uses the same algorithm as SoulDropsSystem for consistency.
     */
    public String selectWeightedItem(Map<String, Double> itemWeights) {
        if (itemWeights == null || itemWeights.isEmpty()) {
            return null;
        }

        // Calculate total weight
        double totalWeight = itemWeights.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        if (totalWeight <= 0) {
            return null;
        }

        // Roll random value in range [0, totalWeight)
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double roll = random.nextDouble(totalWeight);

        // Select item based on weighted roll
        double currentWeight = 0.0;
        for (Map.Entry<String, Double> entry : itemWeights.entrySet()) {
            currentWeight += entry.getValue();
            if (roll < currentWeight) {
                return entry.getKey();
            }
        }

        // Fallback (should not happen unless rounding errors)
        return itemWeights.keySet().iterator().next();
    }

    /**
     * Determine tier from chest block type using config mappings.
     */
    public String determineTier(String blockTypeId) {
        ChestLootConfig config = ChestLootConfigManager.getConfig();
        if (config == null) {
            return "common";
        }

        return config.getTierForBlockType(blockTypeId);
    }

    /**
     * Roll chance and populate chest if successful.
     * Returns true if chest was populated, false otherwise.
     */
    public boolean tryPopulateChest(ItemContainer container, String tierName, String blockTypeId) {
        ChestLootConfig config = ChestLootConfigManager.getConfig();
        if (config == null || !config.isEnabled()) {
            return false;
        }

        // Get tier configuration
        ChestLootConfig.ChestTierConfig tierConfig = config.getTierConfig(tierName);
        if (tierConfig == null) {
            return false;
        }

        // Roll base chance
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double roll = random.nextDouble();


        if (roll > tierConfig.getBaseChance()) {
            return false; // Didn't pass chance check
        }

        // Select weighted item
        String selectedItemId = selectWeightedItem(tierConfig.getItemWeights());
        if (selectedItemId == null) {
            return false;
        }

        // Add item to chest in random empty slot (quantity 1 for soul essence items)
        try {
            ItemStack soulItem = new ItemStack(selectedItemId, 1);

            // Try random slot placement (attempt up to 10 times to find empty slot)
            for (int attempt = 0; attempt < 10; attempt++) {
                short randomSlot = (short) random.nextInt(27); // Standard chest: 27 slots
                try {
                    ItemStack existing = container.getItemStack(randomSlot);
                    if (existing == null || ItemStack.isEmpty(existing)) {
                        container.setItemStackForSlot(randomSlot, soulItem);
                        return true;
                    }
                } catch (Exception slotError) {
                    // Slot access failed, try next slot
                    continue;
                }
            }

            // If random placement failed after 10 attempts, fallback to addItemStack
            container.addItemStack(soulItem);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
