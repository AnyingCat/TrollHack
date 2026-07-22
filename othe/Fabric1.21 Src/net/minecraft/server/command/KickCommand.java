/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class KickCommand {
    private static final SimpleCommandExceptionType CANNOT_KICK_OWNER_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.kick.owner.failed"));
    private static final SimpleCommandExceptionType CANNOT_KICK_SINGLEPLAYER_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.kick.singleplayer.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("kick").requires(source -> source.hasPermissionLevel(3))).then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).executes(context -> KickCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), Text.translatable("multiplayer.disconnect.kicked")))).then(CommandManager.argument("reason", MessageArgumentType.message()).executes(context -> KickCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), MessageArgumentType.getMessage(context, "reason"))))));
    }

    private static int execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Text reason) throws CommandSyntaxException {
        if (!source.getServer().isRemote()) {
            throw CANNOT_KICK_SINGLEPLAYER_EXCEPTION.create();
        }
        int i = 0;
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            if (source.getServer().isHost(serverPlayerEntity.getGameProfile())) continue;
            serverPlayerEntity.networkHandler.disconnect(reason);
            source.sendFeedback(() -> Text.translatable("commands.kick.success", serverPlayerEntity.getDisplayName(), reason), true);
            ++i;
        }
        if (i == 0) {
            throw CANNOT_KICK_OWNER_EXCEPTION.create();
        }
        return i;
    }
}

