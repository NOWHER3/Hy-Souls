package com.nowhere.SoulWarp.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nowhere.SoulWarp.WarpManager;
import javax.annotation.Nonnull;

public class DelWarpCommand extends AbstractPlayerCommand {
    private final WarpManager warpManager;
    private final RequiredArg<String> nameArg;

    public DelWarpCommand(WarpManager warpManager) {
        super("delwarp", "Delete a warp");
        this.requirePermission("soul.command.delwarp");
        this.warpManager = warpManager;
        this.nameArg = this.withRequiredArg("name", "Warp name", ArgTypes.STRING);
    }

    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef player, @Nonnull World world) {
        String name = (String)context.get(this.nameArg);
        if (this.warpManager.getWarp(name) == null) {
            player.sendMessage(Message.raw("Warp " + name + " not found!"));
        } else {
            this.warpManager.deleteWarp(name);
            player.sendMessage(Message.raw("Warp " + name + " deleted!"));
        }
    }
}
