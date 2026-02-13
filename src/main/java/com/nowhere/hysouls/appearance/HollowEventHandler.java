package com.nowhere.hysouls.appearance;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nowhere.hysouls.currency.humanity.HumanityManager;

import java.util.UUID;

/**
 * Handles player lifecycle events for hollow state management.
 */
public class HollowEventHandler {
    private final JavaPlugin plugin;

    public HollowEventHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // Load hollow state when player joins
        this.plugin.getEventRegistry().registerGlobal(PlayerReadyEvent.class, (event) -> {
            Player player = event.getPlayer();
            Ref<EntityStore> ref = event.getPlayerRef();

            if (player != null && ref != null) {
                Store<EntityStore> store = ref.getStore();
                PlayerRef playerRef = (PlayerRef) store.getComponent(ref, PlayerRef.getComponentType());
                if (playerRef != null && playerRef.isValid()) {
                    UUID playerId = playerRef.getUuid();

                    HollowManager.load(playerId);

                    // Update appearance on join
                    checkAndUpdateHollowState(store, ref, playerId);
                }
            }
        });

        // Save hollow state when player disconnects
        this.plugin.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, (event) -> {
            PlayerRef playerRef = event.getPlayerRef();
            if (playerRef != null) {
                UUID playerId = playerRef.getUuid();
                HollowManager.unload(playerId);
            }
        });
    }

    /**
     * Check humanity and update hollow state if needed.
     * Players with 0 humanity become hollow automatically.
     */
    public static void checkAndUpdateHollowState(Store<EntityStore> store, Ref<EntityStore> playerRef, UUID playerId) {
        int humanity = HumanityManager.getHumanity(playerId);
        boolean isHollow = HollowManager.isHollow(playerId);

        // If humanity is 0 and not already hollow, make them hollow
        if (humanity == 0 && !isHollow) {
            PlayerAppearanceManager.makeHollow(store, playerRef, playerId);
        }
        // If they have humanity but are hollow, keep them hollow until they reverse it at bonfire
        else {
            // Just update appearance to match current state
            PlayerAppearanceManager.updateAppearance(store, playerRef, playerId);
        }
    }
}
