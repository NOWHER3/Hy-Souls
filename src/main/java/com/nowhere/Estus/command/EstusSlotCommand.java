package com.nowhere.Estus.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nowhere.Estus.EstusConfig;
import com.nowhere.Estus.EstusConfigManager;
import com.nowhere.Main;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class EstusSlotCommand extends AbstractPlayerCommand {
    private final RequiredArg<Integer> slotArg;

    public EstusSlotCommand() {
        super("estusslot", "Set estus flask hotbar slot (1-9)", false);
        this.setPermissionGroups(new String[0]);
        this.slotArg = this.withRequiredArg("slot", "Hotbar slot number (1-9)", ArgTypes.INTEGER);
    }

    @Override
    protected void execute(@NonNullDecl CommandContext commandContext,
                           @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl Ref<EntityStore> ref,
                           @NonNullDecl PlayerRef playerRef,
                           @NonNullDecl World world) {
        int slot = (Integer) commandContext.get(this.slotArg);

        if (slot < 1 || slot > 9) {
            commandContext.sendMessage(Message.raw("Invalid slot. Must be between 1 and 9."));
            return;
        }

        EstusConfig config = EstusConfigManager.load(playerRef.getUuid());
        config.slot = slot;
        EstusConfigManager.save(playerRef.getUuid());

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            Main.get().getEstusManager().moveEstusToSlot(player, playerRef.getUuid(), slot - 1);
        }

        commandContext.sendMessage(Message.raw("Estus flask moved to slot " + slot + "."));
    }
}
