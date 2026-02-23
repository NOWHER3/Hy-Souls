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
import com.hypixel.hytale.server.core.Message;
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
import com.nowhere.hysouls.appearance.HollowManager;
import com.nowhere.hysouls.appearance.ReverseHollowingService;
import com.nowhere.hysouls.currency.humanity.HumanityManager;
import com.nowhere.hysouls.currency.soul.SoulCraftingWindow;
import com.nowhere.hysouls.menu.kindle.KindleManager;
import com.nowhere.hysouls.Main;
import com.nowhere.hysouls.display.soul.SoulHud;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class BonfireMenu extends InteractiveCustomUIPage<BonfireMenu.BindingData> {

    @Nullable
    private final Vector3i blockPosition;
    private final UUID playerUuid;

    public BonfireMenu(@Nonnull PlayerRef playerRef) {
        this(playerRef, null);
    }

    public BonfireMenu(@Nonnull PlayerRef playerRef, @Nullable Vector3i blockPosition) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, BindingData.CODEC);
        this.blockPosition = blockPosition;
        this.playerUuid = playerRef.getUuid();
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
        setBonfireMenuHudState(true);
        uiCommandBuilder.append("Pages/BonfireMenu.ui");

        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TextButton53d0dc1c", EventData.of("Type", "LevelUp"), false);
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
            case "ReverseHollowing":
                handleReverseHollowing(ref, store);
                break;
            case "Kindle":
                handleKindle(ref, store);
                break;
            case "LevelUp":
                // TODO: implement this feature
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

    private void handleReverseHollowing(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) {
            this.close();
            return;
        }

        UUID playerId = playerRef.getUuid();
        ReverseHollowingService.reverseHollowing(store, ref, playerId);
    }

    private void handleKindle(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        if (blockPosition == null) {
            this.close();
            return;
        }

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        Player player = store.getComponent(ref, Player.getComponentType());
        if (playerRef == null || player == null) {
            this.close();
            return;
        }

        UUID playerId = playerRef.getUuid();

        // Check if player is in human form (not hollow)
        if (HollowManager.isHollow(playerId)) {
            player.sendMessage(Message.raw("You must be in human form to kindle a bonfire. Reverse hollowing first.").color("#FF6347"));
            return;
        }

        // Check if bonfire can be kindled (not at max level)
        if (!KindleManager.canKindle(playerId, blockPosition)) {
            player.sendMessage(Message.raw("Bonfire is already kindled to maximum capacity (20 Estus).").color("#FFD700"));
            return;
        }

        // Get humanity cost for next kindle level
        int humanityCost = KindleManager.getKindleCost(playerId, blockPosition);
        int currentHumanity = HumanityManager.getHumanity(playerId);
        int nextEstusAmount = KindleManager.getNextEstusAmount(playerId, blockPosition);

        // Check if player has enough humanity
        if (currentHumanity < humanityCost) {
            player.sendMessage(Message.raw(String.format("You need %d humanity to kindle this bonfire.", humanityCost)).color("#FF6347"));
            return;
        }

        // Deduct humanity and kindle the bonfire
        HumanityManager.removeHumanity(playerId, humanityCost);
        KindleManager.kindleBonfire(playerId, blockPosition);

        // Update humanity HUD display
        if (Main.get().getHudManager() != null) {
            Main.get().getHudManager().updateHumanityDisplay(playerId);
        }

        // Send confirmation message
        player.sendMessage(Message.raw(String.format("Bonfire kindled! Estus flask capacity increased to %d.", nextEstusAmount)).color("#FFD700"));

        // Recharge estus immediately with new amount
        Main.get().getBonfireRestService().restAtBonfire(ref, store, blockPosition);
    }

    @Override
    public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        setBonfireMenuHudState(false);
        super.onDismiss(ref, store);
    }

    private void setBonfireMenuHudState(boolean open) {
        if (Main.get().getHudManager() == null) return;
        SoulHud hud = Main.get().getHudManager().getHud(playerUuid);
        if (hud != null) {
            hud.setBonfireMenuOpen(open);
        }
    }
}
