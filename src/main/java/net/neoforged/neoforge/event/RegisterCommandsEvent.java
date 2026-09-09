package net.neoforged.neoforge.event;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class RegisterCommandsEvent extends Event {

    private final CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher;
    private final CommandBuildContext buildContext;
    private final Commands.CommandSelection commandSelection;

    public RegisterCommandsEvent(
            CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher,
            CommandBuildContext buildContext,
            Commands.CommandSelection commandSelection
    ) {
        this.dispatcher = dispatcher;
        this.buildContext = buildContext;
        this.commandSelection = commandSelection;
    }

    public CommandDispatcher<net.minecraft.commands.CommandSourceStack> getDispatcher() {
        return this.dispatcher;
    }

    public CommandBuildContext getBuildContext() {
        return this.buildContext;
    }

    public Commands.CommandSelection getCommandSelection() {
        return this.commandSelection;
    }
}
