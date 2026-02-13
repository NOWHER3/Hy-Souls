package com.nowhere.hysouls.appearance;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nowhere.hysouls.Main;
import com.nowhere.hysouls.currency.humanity.HumanityManager;

import java.util.UUID;

/**
 * Service for reversing hollowing at bonfires.
 * Costs 1 humanity to reverse hollowing.
 */
public class ReverseHollowingService {
    private static final int HUMANITY_COST = 1;

    /**
     * Attempt to reverse hollowing for a player.
     * Returns true if successful, false if they don't have enough humanity or aren't hollow.
     */
    public static boolean reverseHollowing(Store<EntityStore> store, Ref<EntityStore> playerRef, UUID playerId) {
        // Check if already human
        if (!HollowManager.isHollow(playerId)) {
            Player player = store.getComponent(playerRef, Player.getComponentType());
            if (player != null) {
                player.sendMessage(Message.raw("You are already human!"));
            }
            return false;
        }

        // Check if player has enough humanity
        int currentHumanity = HumanityManager.getHumanity(playerId);
        if (currentHumanity < HUMANITY_COST) {
            Player player = store.getComponent(playerRef, Player.getComponentType());
            if (player != null) {
                player.sendMessage(Message.raw("You need at least " + HUMANITY_COST + " humanity to reverse hollowing."));
            }
            return false;
        }

        // Consume humanity and reverse hollowing
        HumanityManager.removeHumanity(playerId, HUMANITY_COST);
        PlayerAppearanceManager.reverseHollowing(store, playerRef, playerId);

        // Update HUD to show new humanity count
        if (Main.get().getHudManager() != null) {
            Main.get().getHudManager().updateHumanityDisplay(playerId);
        }

        Player player = store.getComponent(playerRef, Player.getComponentType());
        if (player != null) {
            int remaining = HumanityManager.getHumanity(playerId);
            player.sendMessage(Message.raw("Hollowing reversed! You are human once more. (" + remaining + " humanity remaining)").color("#00FF00"));
        }

        return true;
    }
}
