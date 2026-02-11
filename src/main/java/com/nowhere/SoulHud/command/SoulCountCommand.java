package com.nowhere.SoulHud.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nowhere.SoulCurrency.SoulManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class SoulCountCommand extends AbstractPlayerCommand {
    public SoulCountCommand() {
        super("soulcount", "Show how many souls you have", false);
        this.setPermissionGroups(new String[0]);
    }

    protected void execute(@NonNullDecl CommandContext commandContext,
                           @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl Ref<EntityStore> ref,
                           @NonNullDecl PlayerRef playerRef,
                           @NonNullDecl World world
    ) {
        int count = SoulManager.getSouls(playerRef.getUuid());
        commandContext.sendMessage(Message.raw(String.format("You have %d souls.", count)));
    }
}

