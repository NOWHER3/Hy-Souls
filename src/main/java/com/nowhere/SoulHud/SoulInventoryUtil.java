package com.nowhere.SoulHud;

import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;

import javax.annotation.Nullable;

public final class SoulInventoryUtil {
    private SoulInventoryUtil() {
    }

    public static int countSouls(@Nullable Inventory inventory) {
        if (inventory == null) {
            return 0;
        } else {
            int total = 0;
            ItemContainer[] sections = new ItemContainer[]{inventory.getHotbar(), inventory.getStorage(), inventory.getBackpack(), inventory.getUtility(), inventory.getTools()};

            for(ItemContainer section : sections) {
                if (section != null) {
                    short capacity = section.getCapacity();

                    for(short slot = 0; slot < capacity; ++slot) {
                        ItemStack stack = section.getItemStack(slot);
                        if (stack != null) {
                            try {
                                if (ItemStack.isEmpty(stack)) {
                                    continue;
                                }
                            } catch (NoSuchMethodError var11) {
                                if (stack.getQuantity() <= 0) {
                                    continue;
                                }
                            }

                            String id = stack.getItemId();
                            if (id != null && looksLikeSoul(id)) {
                                total += stack.getQuantity();
                            }
                        }
                    }
                }
            }

            return total;
        }
    }

    private static boolean looksLikeSoul(String itemId) {
        return itemId.toLowerCase().contains("soul_essence");
    }
}
