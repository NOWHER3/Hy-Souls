package com.nowhere.hysouls.menu.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nowhere.hysouls.Main;
import com.nowhere.hysouls.menu.BonfireMenu;
import com.nowhere.hysouls.menu.BonfireRestService;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("removal")
public class OpenBonfireMenuInteraction extends SimpleBlockInteraction {

    public static final BuilderCodec<OpenBonfireMenuInteraction> CODEC =
            BuilderCodec.builder(OpenBonfireMenuInteraction.class, OpenBonfireMenuInteraction::new,
                            SimpleBlockInteraction.CODEC)
                    .build();

    @Override
    protected void interactWithBlock(@Nonnull World world,
                                     @Nonnull CommandBuffer<EntityStore> commandBuffer,
                                     @Nonnull InteractionType type,
                                     @Nonnull InteractionContext context,
                                     @Nullable ItemStack itemInHand,
                                     @Nonnull Vector3i targetBlock,
                                     @Nonnull CooldownHandler cooldownHandler) {
        Ref<EntityStore> ref = context.getEntity();
        Player player = commandBuffer.getComponent(ref, Player.getComponentType());
        if (player == null) {
            return;
        }

        // Perform rest operations BEFORE opening menu (authentic Dark Souls behavior)
        BonfireRestService restService = Main.get().getBonfireRestService();
        if (restService != null) {
            int estusRefilled = restService.restAtBonfire(ref, commandBuffer.getStore(), targetBlock);

            // Track this bonfire as the player's respawn point
            String warpName = com.nowhere.hysouls.warp.WarpManager.generateBonfireName(
                    targetBlock.x, targetBlock.y, targetBlock.z);
            com.nowhere.hysouls.warp.SpawnPointManager.setRespawnBonfire(player.getPlayerRef().getUuid(), warpName);

            // Send estus refill message (will be visible when menu closes)
            if (estusRefilled > 0) {
                player.sendMessage(Message.raw(estusRefilled + " Estus refilled.").color("#00FF00"));
            }
        }

        // Play a sound when the bonfire menu opens
        SoundUtil.playSoundEvent2d(
                SoundEvent.getAssetMap().getIndex("SFX_Workbench_Open"),
                SoundCategory.SFX,
                commandBuffer
        );

        // Open the bonfire menu with block position for crafting
        player.getPageManager().openCustomPage(ref, commandBuffer.getStore(),
                new BonfireMenu(player.getPlayerRef(), targetBlock));
    }

    @Override
    protected void simulateInteractWithBlock(@Nonnull InteractionType type,
                                             @Nonnull InteractionContext context,
                                             @Nullable ItemStack itemInHand,
                                             @Nonnull World world,
                                             @Nonnull Vector3i targetBlock) {
        // No-op: simulation not needed for menu opening
    }
}
