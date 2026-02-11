package com.nowhere.Humanity.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nowhere.Humanity.HumanityManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class HumanityCountCommand extends AbstractPlayerCommand {
    public HumanityCountCommand() {
        super("humanitycount", "Show how much humanity you have", false);
        this.setPermissionGroups(new String[0]);
    }

    protected void execute(@NonNullDecl CommandContext commandContext,
                           @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl Ref<EntityStore> ref,
                           @NonNullDecl PlayerRef playerRef,
                           @NonNullDecl World world
    ) {
        int count = HumanityManager.getHumanity(playerRef.getUuid());
        commandContext.sendMessage(Message.raw(String.format("You have %d humanity.", count)));
    }
}
