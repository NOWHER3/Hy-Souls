package com.nowhere.SoulMenu.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nowhere.SoulMenu.BonfireMenu;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class BonfireMenuCommand extends AbstractPlayerCommand {
    public BonfireMenuCommand() {
        super("bmenu", "Bonfire Menu");
        this.setPermissionGroups(new String[0]);
    }

    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
        Player player = commandContext.senderAs(Player.class);
        player.getPageManager().openCustomPage(ref,store, new BonfireMenu(playerRef));
    }
}
