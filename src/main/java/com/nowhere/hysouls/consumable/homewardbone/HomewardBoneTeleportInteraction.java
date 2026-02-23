package com.nowhere.hysouls.consumable.homewardbone;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.Message;
import com.nowhere.hysouls.config.PlayerData;
import com.nowhere.hysouls.config.PlayerDataManager;
import com.nowhere.hysouls.warp.SpawnPointManager;
import com.nowhere.hysouls.warp.WarpModel;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Instant interaction that teleports the player to the last bonfire rested at.
 * Consumes the item on use (via JSON interaction chain with ModifyInventory).
 * If no bonfire has been rested at, sends a message and the interaction chain
 * stops (item not consumed) because firstRun returns without calling Next.
 */
public class HomewardBoneTeleportInteraction extends SimpleInstantInteraction {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @SuppressWarnings("unchecked")
    public static final BuilderCodec<HomewardBoneTeleportInteraction> CODEC =
            ((BuilderCodec.Builder<HomewardBoneTeleportInteraction>)
                    BuilderCodec.builder(HomewardBoneTeleportInteraction.class,
                            HomewardBoneTeleportInteraction::new,
                            SimpleInstantInteraction.CODEC)
            ).build();

    @Override
    @Nonnull
    public WaitForDataFrom getWaitForDataFrom() {
        return WaitForDataFrom.Server;
    }

    @Override
    protected void firstRun(@Nonnull InteractionType type,
                            @Nonnull InteractionContext context,
                            @Nonnull CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        assert commandBuffer != null;
        Ref<EntityStore> ref = context.getEntity();
        PlayerRef playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) {
            return;
        }

        UUID playerId = playerRef.getUuid();

        // Check if player has a respawn bonfire set
        String bonfireWarpName = SpawnPointManager.getRespawnBonfire(playerId);
        if (bonfireWarpName == null) {
            playerRef.sendMessage(Message.raw("You have not rested at a bonfire."));
            return;
        }

        // Look up the warp data
        PlayerData data = PlayerDataManager.getPlayerData(playerId);
        WarpModel warp = data.warps.get(bonfireWarpName);
        if (warp == null) {
            playerRef.sendMessage(Message.raw("Your bonfire has been destroyed."));
            SpawnPointManager.clearRespawnBonfire(playerId);
            return;
        }

        // Cross-world check
        UUID playerWorldUuid = playerRef.getWorldUuid();
        if (!warp.worldUuid.equals(playerWorldUuid)) {
            playerRef.sendMessage(Message.raw("Your bonfire is in another world."));
            return;
        }

        // Play sound effect
        SoundUtil.playSoundEvent2dToPlayer(
                playerRef,
                SoundEvent.getAssetMap().getIndex("SFX_Deployable_Totem_Heal_Spawn"),
                SoundCategory.SFX
        );

        // Defer teleport to after store processing completes — calling store.addComponent()
        // during an interaction tick crashes with "Store is currently processing!"
        commandBuffer.run(deferredStore -> {
            try {
                World world = deferredStore.getExternalData().getWorld();
                if (world == null) {
                    LOGGER.atWarning().log("World is null during Homeward Bone teleport for player %s", playerId);
                    return;
                }

                Vector3f rotation = new Vector3f();
                rotation.setYaw(warp.yaw);
                rotation.setPitch(0.0F);
                Teleport teleport = new Teleport(world, new Vector3d(warp.x, warp.y, warp.z), rotation);
                deferredStore.addComponent(ref, Teleport.getComponentType(), teleport);

                LOGGER.atInfo().log("Player %s used Homeward Bone to teleport to bonfire %s", playerId, bonfireWarpName);
            } catch (Exception e) {
                LOGGER.atSevere().log("Error during Homeward Bone teleport for player %s: %s", playerId, e.getMessage());
            }
        });

        playerRef.sendMessage(Message.raw("Returned to bonfire."));
    }
}
