package com.nowhere.SoulMenu.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nowhere.SoulMenu.BonfireMenu;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

@SuppressWarnings("removal")
public class OpenBonfireMenuInteraction extends SimpleInteraction {

    public static final BuilderCodec<OpenBonfireMenuInteraction> CODEC =
            BuilderCodec.builder(OpenBonfireMenuInteraction.class, OpenBonfireMenuInteraction::new,
                            SimpleInteraction.CODEC)
                    .build();

    @Override
    protected void tick0(boolean firstRun,
                         float time,
                         @NonNullDecl InteractionType type,
                         @NonNullDecl InteractionContext context,
                         @NonNullDecl CooldownHandler cooldownHandler) {
        Ref<EntityStore> owningEntity = context.getOwningEntity();
        Store<EntityStore> store = owningEntity.getStore();
        Player player = store.getComponent(owningEntity, Player.getComponentType());
        if (player == null) {
            return;
        }

        // Play a sound when the bonfire menu opens
        if (context.getCommandBuffer() != null) {
            SoundUtil.playSoundEvent2d(
                    SoundEvent.getAssetMap().getIndex("SFX_Workbench_Open"),
                    SoundCategory.SFX,
                    context.getCommandBuffer()
            );
        }

        // Open the bonfire menu
        player.getPageManager().openCustomPage(owningEntity, store, new BonfireMenu(player.getPlayerRef()));
    }
}