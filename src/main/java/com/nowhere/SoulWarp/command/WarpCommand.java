package com.nowhere.SoulWarp.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.nowhere.SoulWarp.WarpManager;
import com.nowhere.SoulWarp.WarpModel;
import com.nowhere.SoulWarp.util.PermissionUtil;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class WarpCommand extends AbstractPlayerCommand {
    private final WarpManager warpManager;
    private final RequiredArg<String> nameArg;
    private static final String WARMUP_BYPASS_PERMISSION = "soul.warps.bypass.warmup";
    private static final String COOLDOWN_BYPASS_PERMISSION = "soul.warps.bypass.cooldown";
    private static final double MOVEMENT_THRESHOLD = (double)0.5F;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public WarpCommand(WarpManager warpManager) {
        super("warp", "Teleport to a warp");
        this.requirePermission("soul.command.warp");
        this.warpManager = warpManager;
        this.nameArg = this.withRequiredArg("name", "Warp name", ArgTypes.STRING);
    }

    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef player, @Nonnull World world) {
        UUID playerUuid = player.getUuid();
        String name = (String)context.get(this.nameArg);
        WarpModel warp = this.warpManager.getWarp(playerUuid, name);
        if (warp == null) {
            player.sendMessage(Message.raw("Warp not found: " + name));
        } else if (!warp.worldUuid.equals(player.getWorldUuid())) {
            player.sendMessage(Message.raw("Warp is in another world! Cross-world teleportation is not supported yet."));
        } else {
            boolean bypassCooldown = PermissionUtil.hasPermission(playerUuid, "soul.warps.bypass.cooldown");
            if (!bypassCooldown) {
                long remaining = this.warpManager.getRemainingCooldown(player.getUuid());
                if (remaining > 0L) {
                    long seconds = remaining / 1000L + 1L;
                    player.sendMessage(Message.raw("You must wait " + seconds + " seconds before warping again."));
                    return;
                }
            }

            if (this.warpManager.isWarmingUp(player.getUuid())) {
                player.sendMessage(Message.raw("Teleportation already in progress!"));
            } else {
                int warmup = this.warpManager.getWarmup();
                boolean bypassWarmup = PermissionUtil.hasPermission(playerUuid, "soul.warps.bypass.warmup");
                if (warmup > 0 && !bypassWarmup) {
                    this.startWarmupCountdown(player, playerUuid, store, ref, world, warp, name, warmup);
                } else {
                    this.executeTeleport(player, playerUuid, store, ref, world, warp, name);
                }

            }
        }
    }

    private void startWarmupCountdown(PlayerRef player, UUID playerUuid, Store<EntityStore> store, Ref<EntityStore> ref, World world, WarpModel warp, String warpName, int warmupSeconds) {
        this.warpManager.setWarmingUp(playerUuid, true);
        Vector3d pos = player.getTransform().getPosition();
        double startX = pos.x;
        double startY = pos.y;
        double startZ = pos.z;
        int[] secondsRemaining = new int[]{warmupSeconds};
        sendWarmupNotification(player, warmupSeconds);
        ScheduledFuture<?>[] futureHolder = new ScheduledFuture[1];
        futureHolder[0] = scheduler.scheduleAtFixedRate(() -> world.execute(() -> {
            if (!this.warpManager.isWarmingUp(playerUuid)) {
                futureHolder[0].cancel(true);
            } else {
                Vector3d currentPosition = player.getTransform().getPosition();
                if (this.hasPlayerMoved(startX, startY, startZ, currentPosition)) {
                    this.cancelWarmup(player, playerUuid, futureHolder[0]);
                } else {
                    int var10002 = secondsRemaining[0]--;
                    if (secondsRemaining[0] <= 0) {
                        futureHolder[0].cancel(true);
                        if (this.warpManager.isWarmingUp(playerUuid)) {
                            this.executeTeleport(player, playerUuid, store, ref, world, warp, warpName);
                        }
                    }

                }
            }
        }), 1L, 1L, TimeUnit.SECONDS);
    }

    private boolean hasPlayerMoved(double startX, double startY, double startZ, Vector3d current) {
        double dx = current.x - startX;
        double dy = current.y - startY;
        double dz = current.z - startZ;
        return Math.sqrt(dx * dx + dy * dy + dz * dz) > (double)0.5F;
    }

    private void cancelWarmup(PlayerRef player, UUID playerUuid, ScheduledFuture<?> future) {
        this.warpManager.setWarmingUp(playerUuid, false);
        future.cancel(true);
        player.sendMessage(Message.raw("Teleportation cancelled - you moved!"));
    }

    private void executeTeleport(PlayerRef player, UUID playerUuid, Store<EntityStore> store, Ref<EntityStore> ref, World world, WarpModel warp, String warpName) {
        this.warpManager.setWarmingUp(playerUuid, false);
        Vector3f rot = new Vector3f();
        rot.setYaw(warp.yaw);
        rot.setPitch(0.0F);
        Teleport teleport = new Teleport(world, new Vector3d(warp.x, warp.y, warp.z), rot);
        store.addComponent(ref, Teleport.getComponentType(), teleport);
        player.sendMessage(Message.raw("Teleported to " + warpName));
        this.warpManager.setCooldown(playerUuid);
    }

    private static void sendWarmupNotification(PlayerRef player, int secondsLeft) {
        PacketHandler packetHandler = player.getPacketHandler();
        Message primaryMessage = Message.raw("TELEPORTING").color("#00FF00");
        Message secondaryMessage = Message.raw("Do not move until " + secondsLeft + " seconds.").color("#228B22");
        ItemWithAllMetadata icon = (new ItemStack("Ingredient_Void_Essence", 1)).toPacket();
        NotificationUtil.sendNotification(packetHandler, primaryMessage, secondaryMessage, icon);
    }
}