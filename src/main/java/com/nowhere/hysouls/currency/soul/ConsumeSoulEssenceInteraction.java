package com.nowhere.hysouls.currency.soul;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
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
import javax.annotation.Nonnull;

/**
 * Instant interaction that adds souls to the player's soul counter.
 * The amount is configurable via the "SoulAmount" codec field in item JSON.
 * Used by all Ingredient_Hysouls_Soul_Essence_Hard variants on secondary use.
 */
public class ConsumeSoulEssenceInteraction extends SimpleInstantInteraction {

    private static final int DEFAULT_SOUL_AMOUNT = 200;

    protected int soulAmount = DEFAULT_SOUL_AMOUNT;

    @SuppressWarnings("unchecked")
    public static final BuilderCodec<ConsumeSoulEssenceInteraction> CODEC =
            ((BuilderCodec.Builder<ConsumeSoulEssenceInteraction>)
                    BuilderCodec.builder(ConsumeSoulEssenceInteraction.class,
                                    ConsumeSoulEssenceInteraction::new,
                                    SimpleInstantInteraction.CODEC)
                            .appendInherited(
                                    new KeyedCodec<>("SoulAmount", Codec.INTEGER),
                                    (interaction, amount) -> interaction.soulAmount = amount,
                                    interaction -> interaction.soulAmount,
                                    (interaction, parent) -> interaction.soulAmount = parent.soulAmount
                            ).add()
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

        SoulManager.addSouls(playerRef.getUuid(), soulAmount);

        SoundUtil.playSoundEvent2dToPlayer(
                playerRef,
                SoundEvent.getAssetMap().getIndex("SFX_Deployable_Totem_Heal_Spawn"),
                SoundCategory.SFX
        );
    }
}
