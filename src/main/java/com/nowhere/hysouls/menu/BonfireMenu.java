package com.nowhere.hysouls.menu;

import com.hypixel.hytale.builtin.crafting.state.BenchState;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.windows.Window;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nowhere.hysouls.currency.soul.SoulCraftingWindow;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class BonfireMenu extends InteractiveCustomUIPage<BonfireMenu.BindingData> {

    @Nullable
    private final Vector3i blockPosition;

    public BonfireMenu(@Nonnull PlayerRef playerRef) {
        this(playerRef, null);
    }

    public BonfireMenu(@Nonnull PlayerRef playerRef, @Nullable Vector3i blockPosition) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, BindingData.CODEC);
        this.blockPosition = blockPosition;
    }

    public static class BindingData {
        public static final BuilderCodec<BindingData> CODEC = BuilderCodec.builder(BindingData.class, BindingData::new)
                .append(new KeyedCodec<>("Type", Codec.STRING), (d, v) -> d.type = v, d -> d.type).add()
                .build();

        private String type;

        public String getType() {
            return type;
        }
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/BonfireMenu.ui");

        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TextButton53d0dc1c", EventData.of("Type", "LevelUp"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#AttuneMagic", EventData.of("Type", "AttuneMagic"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TextButton9f6b71fa", EventData.of("Type", "ReverseHollowing"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TextButton640bb5a9", EventData.of("Type", "Kindle"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CraftButton", EventData.of("Type", "Craft"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CancelTextButton23e43fbe", EventData.of("Type", "Leave"), false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull BindingData data) {
        if (data.getType() == null) {
            return;
        }
        switch (data.getType()) {
            case "Craft":
                openBenchCraftingUI(ref, store);
                break;
            case "Leave":
                this.close();
                break;
            case "LevelUp":
            case "AttuneMagic":
            case "ReverseHollowing":
            case "Kindle":
                // TODO: implement these features
                break;
        }
    }

    @SuppressWarnings({"removal", "deprecation"})
    private void openBenchCraftingUI(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        if (blockPosition == null) {
            this.close();
            return;
        }

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            this.close();
            return;
        }

        World world = store.getExternalData().getWorld();
        BlockState blockState = world.getState(blockPosition.x, blockPosition.y, blockPosition.z, true);
        if (!(blockState instanceof BenchState benchState)) {
            this.close();
            return;
        }

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        UUID uuid = playerRef.getUuid();

        // Close the custom page first so the client resets its input/cursor state
        this.close();

        Window[] windows = new Window[]{ new SoulCraftingWindow(benchState, uuid) };
        player.getPageManager().setPageWithWindows(ref, store, Page.Bench, true, windows);
    }
}
