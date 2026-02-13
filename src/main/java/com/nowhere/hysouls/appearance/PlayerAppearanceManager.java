package com.nowhere.hysouls.appearance;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;

/**
 * Manages player appearance transformations between human and hollow states.
 *
 * Note: Due to Hytale's current cosmetic system limitations, we cannot dynamically
 * change player skin textures. This is a placeholder for when the API supports it.
 *
 * TODO: Implement actual skin changes when Hytale adds PlayerSkinComponent modification API
 *       - Apply hollow texture from Common/Characters/ based on hollow state
 *       - Update makeHollow() to apply hollow skin texture
 *       - Update reverseHollowing() to restore original player skin
 */
public final class PlayerAppearanceManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private PlayerAppearanceManager() {}

    /**
     * Update a player's appearance based on their hollow state.
     *
     * Currently this just sends a message to the player since we cannot
     * modify skin textures at runtime with the current Hytale API.
     */
    public static void updateAppearance(Store<EntityStore> store, Ref<EntityStore> playerRef, UUID playerId) {
        boolean isHollow = HollowManager.isHollow(playerId);

        Player player = store.getComponent(playerRef, Player.getComponentType());
        if (player != null) {
            if (isHollow) {
                LOGGER.atInfo().log("Player %s is hollow (visual change not yet supported)", playerId);
            } else {
                LOGGER.atInfo().log("Player %s is human", playerId);
            }
        }
    }

    /**
     * Make a player hollow and update their appearance.
     */
    public static void makeHollow(Store<EntityStore> store, Ref<EntityStore> playerRef, UUID playerId) {
        HollowManager.makeHollow(playerId);

        Player player = store.getComponent(playerRef, Player.getComponentType());
        if (player != null) {
            player.sendMessage(Message.raw("You have become hollow...").color("#8B0000"));
        }

        LOGGER.atInfo().log("Player %s became hollow", playerId);
    }

    /**
     * Reverse hollowing and restore human appearance.
     */
    public static void reverseHollowing(Store<EntityStore> store, Ref<EntityStore> playerRef, UUID playerId) {
        HollowManager.reverseHollowing(playerId);
        LOGGER.atInfo().log("Player %s reversed hollowing", playerId);
    }
}
