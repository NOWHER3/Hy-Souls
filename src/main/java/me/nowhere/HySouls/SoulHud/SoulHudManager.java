package me.nowhere.HySouls.SoulHud;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class SoulHudManager {
    private final JavaPlugin plugin;
    private final Map<UUID, SoulHud> activeHuds;

    public SoulHudManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.activeHuds = new ConcurrentHashMap<>();
        this.registerListeners();
    }

    private void registerListeners() {
        this.plugin.getEventRegistry().registerGlobal(PlayerReadyEvent.class, (event) -> {
            Player player = event.getPlayer();
            Ref<EntityStore> ref = event.getPlayerRef();  // This returns Ref<EntityStore>

            if (player != null && ref != null) {
                Store<EntityStore> store = ref.getStore();
                // Get the PlayerRef component from the entity
                PlayerRef playerRef = (PlayerRef) store.getComponent(ref, PlayerRef.getComponentType());
                if (playerRef != null && playerRef.isValid()) {
                    this.handlePlayerJoin(playerRef, player);
                }
            }
        });

        this.plugin.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, (event) -> {
            PlayerRef pr = event.getPlayerRef();
            if (pr != null) {
                this.handlePlayerLeave(pr);
            }
        });

        this.plugin.getEventRegistry().registerGlobal(LivingEntityInventoryChangeEvent.class, (event) -> {
            LivingEntity entity = (LivingEntity)event.getEntity();
            if (entity instanceof Player player) {
                // Get the entity reference from the Player
                Ref<EntityStore> ref = player.getReference();
                if (ref != null) {
                    Store<EntityStore> store = ref.getStore();
                    // Get the PlayerRef component from the entity
                    PlayerRef playerRef = (PlayerRef) store.getComponent(ref, PlayerRef.getComponentType());
                    if (playerRef != null && playerRef.isValid()) {
                        this.updateHudForPlayer(playerRef, player);
                    }
                }
            }
        });
    }

    private void handlePlayerJoin(PlayerRef playerRef, Player player) {
        if (playerRef != null && playerRef.isValid()) {
            SoulHud hud = new SoulHud(playerRef);
            player.getHudManager().setCustomHud(playerRef, hud);
            this.activeHuds.put(playerRef.getUuid(), hud);
            this.updateHudForPlayer(playerRef, player);
        }
    }

    private void handlePlayerLeave(PlayerRef playerRef) {
        if (playerRef != null) {
            this.activeHuds.remove(playerRef.getUuid());
        }
    }

    private void updateHudForPlayer(PlayerRef playerRef, Player player) {
        try {
            Inventory inventory = player.getInventory();
            int soulCount = SoulInventoryUtil.countSouls(inventory);
            SoulHud hud = this.activeHuds.get(playerRef.getUuid());
            if (hud != null) {
                hud.updateSoulCount(soulCount);
            }
        } catch (Exception ex) {
            ((HytaleLogger.Api)this.plugin.getLogger().at(Level.WARNING).withCause(ex)).log("Error updating HUD for player " + String.valueOf(playerRef.getUuid()));
        }
    }

    public void shutdown() {
        this.activeHuds.clear();
    }
}