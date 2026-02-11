package com.nowhere.hysouls.display.soul;

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
import com.nowhere.hysouls.currency.soul.SoulManager;
import com.nowhere.hysouls.display.soul.config.HudConfigManager;
import com.nowhere.hysouls.display.soul.config.HudPositionConfig;
import com.nowhere.hysouls.currency.humanity.HumanityManager;
import com.nowhere.hysouls.display.humanity.config.HumanityHudConfigManager;
import com.nowhere.hysouls.display.humanity.config.HumanityHudPositionConfig;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class SoulHudManager {
    private final JavaPlugin plugin;
    private final Map<UUID, SoulHud> activeHuds;
    private final Set<UUID> processingPickup = ConcurrentHashMap.newKeySet();
    private boolean useMultipleHud = false;
    private Object multipleHudInstance = null;

    public SoulHudManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.activeHuds = new ConcurrentHashMap<>();
        this.detectMultipleHud();
        this.registerListeners();
    }

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
                        this.handleInventoryChange(playerRef, player);
                    }
                }
            }
        });
    }

    private void handlePlayerJoin(PlayerRef playerRef, Player player) {
        if (playerRef == null || !playerRef.isValid()) return;

        // Load soul counter for this player
        SoulManager.load(playerRef.getUuid());

        // Migrate any existing soul items in inventory to the counter
        Inventory inventory = player.getInventory();
        int existingSouls = SoulInventoryUtil.removeSouls(inventory);
        if (existingSouls > 0) {
            SoulManager.addSouls(playerRef.getUuid(), existingSouls);
        }

        // Load humanity counter for this player
        HumanityManager.load(playerRef.getUuid());

        SoulHud hud = new SoulHud(playerRef);

        // Load HUD position configs (applied during build(), not via update())
        HudPositionConfig posConfig = HudConfigManager.load(playerRef.getUuid());
        hud.setPositionConfig(posConfig);

        HumanityHudPositionConfig humanityPosConfig = HumanityHudConfigManager.load(playerRef.getUuid());
        hud.setHumanityPositionConfig(humanityPosConfig);

        if (useMultipleHud) {
            setHudViaMultipleHud(player, playerRef, hud);
        } else {
            player.getHudManager().setCustomHud(playerRef, hud);
        }

        this.activeHuds.put(playerRef.getUuid(), hud);
        this.updateHudForPlayer(playerRef);
        this.updateHumanityDisplay(playerRef.getUuid());
    }

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
            player.getHudManager().setCustomHud(playerRef, hud);
        }
    }

    private void handlePlayerLeave(PlayerRef playerRef) {
        if (playerRef != null) {
            SoulManager.unload(playerRef.getUuid());
            HudConfigManager.unload(playerRef.getUuid());
            HumanityManager.unload(playerRef.getUuid());
            HumanityHudConfigManager.unload(playerRef.getUuid());
            this.activeHuds.remove(playerRef.getUuid());
        }
    }

    private void handleInventoryChange(PlayerRef playerRef, Player player) {
        UUID uuid = playerRef.getUuid();

        // Guard against re-entrancy (removing items triggers another inventory change event)
        if (!processingPickup.add(uuid)) return;
        try {
            Inventory inventory = player.getInventory();
            int soulsInInventory = SoulInventoryUtil.countSouls(inventory);
            if (soulsInInventory > 0) {
                SoulInventoryUtil.removeSouls(inventory);
                SoulManager.addSouls(uuid, soulsInInventory);
            }
            this.updateHudForPlayer(playerRef);
        } catch (Exception ex) {
            ((HytaleLogger.Api) this.plugin.getLogger().at(Level.WARNING).withCause(ex))
                    .log("Error processing soul pickup for player " + uuid);
        } finally {
            processingPickup.remove(uuid);
        }
    }

    private void updateHudForPlayer(PlayerRef playerRef) {
        try {
            int soulCount = SoulManager.getSouls(playerRef.getUuid());
            SoulHud hud = this.activeHuds.get(playerRef.getUuid());
            if (hud != null) {
                hud.updateSoulCount(soulCount);
            }
        } catch (Exception ex) {
            ((HytaleLogger.Api) this.plugin.getLogger().at(Level.WARNING).withCause(ex))
                    .log("Error updating HUD for player " + playerRef.getUuid());
        }
    }

    public void applyConfig(UUID uuid) {
        SoulHud hud = this.activeHuds.get(uuid);
        if (hud != null) {
            HudPositionConfig config = HudConfigManager.load(uuid);
            hud.applyPosition(config);
        }
    }

    public void toggleHud(UUID uuid) {
        SoulHud hud = this.activeHuds.get(uuid);
        if (hud != null) {
            hud.setVisible(!hud.isVisible());
        }
    }

    public void updateHumanityDisplay(UUID playerId) {
        SoulHud hud = this.activeHuds.get(playerId);
        if (hud != null) {
            hud.updateHumanityCount(HumanityManager.getHumanity(playerId));
        }
    }

    public void applyHumanityConfig(UUID uuid) {
        SoulHud hud = this.activeHuds.get(uuid);
        if (hud != null) {
            HumanityHudPositionConfig config = HumanityHudConfigManager.load(uuid);
            hud.applyHumanityPosition(config);
        }
    }

    public void toggleHumanityHud(UUID uuid) {
        SoulHud hud = this.activeHuds.get(uuid);
        if (hud != null) {
            hud.setHumanityVisible(!hud.isHumanityVisible());
        }
    }

    public void shutdown() {
        this.activeHuds.clear();
    }
}
