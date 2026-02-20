package com.nowhere.hysouls.appearance;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;

/**
 * Manages player appearance transformations between human and hollow states.
 * Uses PlayerModelChanger to replace the ModelComponent with hollow/original models.
 */
public final class PlayerAppearanceManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private PlayerAppearanceManager() {}

    /**
     * Apply hollow model only. Does not change hollow state.
     * Safe to call when player is already hollow and just needs the visual applied.
     */
    public static void applyHollowModel(Store<EntityStore> store, Ref<EntityStore> playerRef, UUID playerId) {
        boolean success = PlayerModelChanger.applyHollowModel(store, playerRef);
        if (success) {
            LOGGER.atInfo().log("Player %s hollow model applied", playerId);
        } else {
            LOGGER.atWarning().log("Player %s hollow model application failed", playerId);
        }
    }

    /**
     * Make a player hollow and update their appearance.
     */
    public static void makeHollow(Store<EntityStore> store, Ref<EntityStore> playerRef, UUID playerId) {
        // Set hollow state
        HollowManager.makeHollow(playerId);

        // Apply hollow model
        boolean modelSuccess = PlayerModelChanger.applyHollowModel(store, playerRef);

        // Send message to player
        Player player = store.getComponent(playerRef, Player.getComponentType());
        if (player != null) {
            if (modelSuccess) {
                player.sendMessage(Message.raw("You have become hollow...").color("#8B0000"));
            } else {
                player.sendMessage(Message.raw("You have become hollow... (visual change failed)").color("#8B0000"));
            }
        }

        LOGGER.atInfo().log("Player %s became hollow (model applied: %s)", playerId, modelSuccess);
    }

    /**
     * Reverse hollowing and restore human appearance.
     */
    public static void reverseHollowing(Store<EntityStore> store, Ref<EntityStore> playerRef, UUID playerId) {
        // Reverse hollow state
        HollowManager.reverseHollowing(playerId);

        // Restore original model
        boolean success = PlayerModelChanger.restoreOriginalModel(store, playerRef);
        if (success) {
            LOGGER.atInfo().log("Player %s reversed hollowing and restored original model", playerId);
        } else {
            LOGGER.atWarning().log("Player %s reversed hollowing but model restoration failed", playerId);
        }
    }
}
