package com.nowhere.hysouls.currency.humanity;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nowhere.hysouls.Main;
import javax.annotation.Nonnull;

public class ConsumeHumanityEssenceInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<ConsumeHumanityEssenceInteraction> CODEC =
            BuilderCodec.builder(ConsumeHumanityEssenceInteraction.class,
                    ConsumeHumanityEssenceInteraction::new,
                    SimpleInstantInteraction.CODEC)
                    .build();

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

        Main.get().getHumanityHudManager().incrementHumanity(playerRef.getUuid());

        SoundUtil.playSoundEvent2dToPlayer(
                playerRef,
                SoundEvent.getAssetMap().getIndex("SFX_Deployable_Totem_Heal_Spawn"),
                SoundCategory.SFX
        );
    }
}
