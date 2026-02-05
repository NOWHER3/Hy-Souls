package com.nowhere.SoulWarp.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
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

public class SetWarpCommand extends AbstractPlayerCommand {
    private final WarpManager warpManager;
    private final RequiredArg<String> nameArg;

    public SetWarpCommand(WarpManager warpManager) {
        super("setwarp", "Set a new warp");
        this.requirePermission("soul.command.setwarp");
        this.warpManager = warpManager;
        this.nameArg = this.withRequiredArg("name", "Warp name", ArgTypes.STRING);
    }

    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef player, @Nonnull World world) {
        String name = (String)context.get(this.nameArg);
        Vector3d pos = player.getTransform().getPosition();
        Vector3f rot = player.getHeadRotation();
        this.warpManager.createWarp(player.getUuid(), name, player.getWorldUuid(), pos.x, pos.y, pos.z, rot.getYaw(), rot.getPitch());
        player.sendMessage(Message.raw("Warp " + name + " set successfully."));
    }
}