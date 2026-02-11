package com.nowhere.Humanity.command;

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
import com.nowhere.Main;
import com.nowhere.Humanity.config.HumanityHudConfigManager;
import com.nowhere.Humanity.config.HumanityHudPositionConfig;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class HumanityHudPosCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> sideArg;
    private final RequiredArg<Integer> offsetArg;
    private final RequiredArg<Integer> bottomArg;

    public HumanityHudPosCommand() {
        super("humanityhudpos", "Set humanity HUD position", false);
        this.setPermissionGroups(new String[0]);
        this.sideArg = this.withRequiredArg("side", "left or right", ArgTypes.STRING);
        this.offsetArg = this.withRequiredArg("offset", "Pixel offset from side edge", ArgTypes.INTEGER);
        this.bottomArg = this.withRequiredArg("bottom", "Pixel offset from bottom edge", ArgTypes.INTEGER);
    }

    protected void execute(@NonNullDecl CommandContext commandContext,
                           @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl Ref<EntityStore> ref,
                           @NonNullDecl PlayerRef playerRef,
                           @NonNullDecl World world) {
        String side = (String) commandContext.get(this.sideArg);
        int offset = (Integer) commandContext.get(this.offsetArg);
        int bottom = (Integer) commandContext.get(this.bottomArg);

        if (!"left".equals(side) && !"right".equals(side)) {
            commandContext.sendMessage(Message.raw("Invalid side. Use 'left' or 'right'."));
            return;
        }

        HumanityHudPositionConfig config = HumanityHudConfigManager.load(playerRef.getUuid());
        config.side = side;
        config.offset = offset;
        config.bottom = bottom;
        HumanityHudConfigManager.save(playerRef.getUuid());

        Main.get().getHumanityHudManager().applyConfig(playerRef.getUuid());
        commandContext.sendMessage(Message.raw(String.format("Humanity HUD moved to %s, offset %d, bottom %d.", side, offset, bottom)));
    }
}
