package com.nowhere.hysouls.display.humanity.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nowhere.hysouls.Main;
import com.nowhere.hysouls.appearance.HollowEventHandler;
import com.nowhere.hysouls.currency.humanity.HumanityManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

public class HumanityCountCommand extends AbstractPlayerCommand {
    public HumanityCountCommand() {
        super("humanitycount", "Show how much humanity you have", false);
        this.setPermissionGroups(new String[0]);
        this.addSubCommand(new SetHumanityCommand());
        this.addSubCommand(new AddHumanityCommand());
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

    // Subcommand: /humanitycount set <amount>
    static class SetHumanityCommand extends AbstractPlayerCommand {
        @Nonnull
        private final RequiredArg<Integer> amountArg = this.withRequiredArg("amount", "Amount to set", ArgTypes.INTEGER);

        SetHumanityCommand() {
            super("set", "Set humanity to a specific amount", false);
        }

        @Override
        protected void execute(@Nonnull CommandContext commandContext,
                               @Nonnull Store<EntityStore> store,
                               @Nonnull Ref<EntityStore> ref,
                               @Nonnull PlayerRef playerRef,
                               @Nonnull World world) {
            int amount = this.amountArg.get(commandContext);
            if (amount < 0) {
                commandContext.sendMessage(Message.raw("Amount must be 0 or greater."));
                return;
            }

            // Set humanity by first getting current, then adding/removing difference
            int current = HumanityManager.getHumanity(playerRef.getUuid());
            int difference = amount - current;

            if (difference > 0) {
                HumanityManager.addHumanity(playerRef.getUuid(), difference);
            } else if (difference < 0) {
                HumanityManager.removeHumanity(playerRef.getUuid(), -difference);
            }

            // Get actual resulting value (may be capped at max)
            int actual = HumanityManager.getHumanity(playerRef.getUuid());
            commandContext.sendMessage(Message.raw(String.format("Humanity set to %d.", actual)));

            // Update hollow state after changing humanity
            HollowEventHandler.checkAndUpdateHollowState(store, ref, playerRef.getUuid());

            // Update HUD
            if (Main.get().getHudManager() != null) {
                Main.get().getHudManager().updateHumanityDisplay(playerRef.getUuid());
            }
        }
    }

    // Subcommand: /humanitycount add <amount>
    static class AddHumanityCommand extends AbstractPlayerCommand {
        @Nonnull
        private final RequiredArg<Integer> amountArg = this.withRequiredArg("amount", "Amount to add", ArgTypes.INTEGER);

        AddHumanityCommand() {
            super("add", "Add humanity", false);
        }

        @Override
        protected void execute(@Nonnull CommandContext commandContext,
                               @Nonnull Store<EntityStore> store,
                               @Nonnull Ref<EntityStore> ref,
                               @Nonnull PlayerRef playerRef,
                               @Nonnull World world) {
            int amount = this.amountArg.get(commandContext);
            if (amount <= 0) {
                commandContext.sendMessage(Message.raw("Amount must be greater than 0."));
                return;
            }

            HumanityManager.addHumanity(playerRef.getUuid(), amount);
            int newTotal = HumanityManager.getHumanity(playerRef.getUuid());
            commandContext.sendMessage(Message.raw(String.format("Added %d humanity. Total: %d", amount, newTotal)));

            // Update hollow state after adding humanity
            HollowEventHandler.checkAndUpdateHollowState(store, ref, playerRef.getUuid());

            // Update HUD
            if (Main.get().getHudManager() != null) {
                Main.get().getHudManager().updateHumanityDisplay(playerRef.getUuid());
            }
        }
    }
}
