package com.nowhere.SoulWarp.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nowhere.SoulWarp.WarpManager;
import javax.annotation.Nonnull;
import java.util.UUID;

public class ListWarpsCommand extends AbstractPlayerCommand {
    private final WarpManager warpManager;

    public ListWarpsCommand(WarpManager warpManager) {
        super("warps", "List all warps");
        this.requirePermission("soul.command.warp");
        this.warpManager = warpManager;
    }

    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef player, @Nonnull World world) {
        UUID playerUuid = player.getUuid();
        if (this.warpManager.getWarpNames(playerUuid).isEmpty()) {
            player.sendMessage(Message.raw("No warps set."));
        } else {
            player.sendMessage(Message.raw("Warps: " + String.join(", ", this.warpManager.getWarpNames(playerUuid))));
        }
    }
}