package com.nowhere.SoulHud;

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
    private boolean useMultipleHud = false;
    private Object multipleHudInstance = null;

    public SoulHudManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.activeHuds = new ConcurrentHashMap<>();
        this.detectMultipleHud();
        this.registerListeners();
    }

    /**
     * Detect if MultipleHUD is available at runtime
     */
    private void detectMultipleHud() {
        try {
            Class<?> multipleHudClass = Class.forName("com.buuz135.multiplehud.MultipleHUD");
            java.lang.reflect.Method getInstance = multipleHudClass.getMethod("getInstance");
            this.multipleHudInstance = getInstance.invoke(null);
            this.useMultipleHud = true;
            this.plugin.getLogger().at(Level.INFO).log("MultipleHUD detected! Using multi-HUD mode.");
        } catch (ClassNotFoundException e) {
            this.plugin.getLogger().at(Level.INFO).log("MultipleHUD not found. Using standard HUD mode (may conflict with other HUD mods).");
        } catch (Exception e) {
            this.plugin.getLogger().at(Level.WARNING).log("Error detecting MultipleHUD: " + e.getMessage());
        }
    }

    private void registerListeners() {
        this.plugin.getEventRegistry().registerGlobal(PlayerReadyEvent.class, (event) -> {
            Player player = event.getPlayer();
            Ref<EntityStore> ref = event.getPlayerRef();

            if (player != null && ref != null) {
                Store<EntityStore> store = ref.getStore();
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
                Ref<EntityStore> ref = player.getReference();
                if (ref != null) {
                    Store<EntityStore> store = ref.getStore();
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

            if (useMultipleHud) {
                // Use MultipleHUD to register our HUD
                setHudViaMultipleHud(player, playerRef, hud);
            } else {
                // Fallback to standard HUD (may conflict with other mods)
                player.getHudManager().setCustomHud(playerRef, hud);
            }

            this.activeHuds.put(playerRef.getUuid(), hud);
            this.updateHudForPlayer(playerRef, player);
        }
    }

    /**
     * Use reflection to call MultipleHUD.getInstance().setCustomHud()
     * This avoids compile-time dependency issues
     */
    private void setHudViaMultipleHud(Player player, PlayerRef playerRef, SoulHud hud) {
        try {
            Class<?> multipleHudClass = multipleHudInstance.getClass();
            java.lang.reflect.Method setCustomHud = multipleHudClass.getMethod(
                    "setCustomHud",
                    Player.class,
                    PlayerRef.class,
                    String.class,
                    com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud.class
            );
            setCustomHud.invoke(multipleHudInstance, player, playerRef, "SoulHud", hud);
        } catch (Exception e) {
            this.plugin.getLogger().at(Level.SEVERE).log("Failed to set HUD via MultipleHUD: " + e.getMessage());
            // Fallback to standard method
            player.getHudManager().setCustomHud(playerRef, hud);
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
            ((HytaleLogger.Api)this.plugin.getLogger().at(Level.WARNING).withCause(ex))
                    .log("Error updating HUD for player " + String.valueOf(playerRef.getUuid()));
        }
    }

    public void shutdown() {
        this.activeHuds.clear();
    }
}