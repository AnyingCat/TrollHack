/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.HashSet;
import java.util.UUID;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DebugConfigCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("debugconfig").requires(source -> source.hasPermissionLevel(3))).then(CommandManager.literal("config").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("target", EntityArgumentType.player()).executes(context -> DebugConfigCommand.executeConfig((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayer(context, "target")))))).then(CommandManager.literal("unconfig").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("target", UuidArgumentType.uuid()).suggests((context, suggestionsBuilder) -> CommandSource.suggestMatching(DebugConfigCommand.collectConfiguringPlayers(((ServerCommandSource)context.getSource()).getServer()), suggestionsBuilder)).executes(context -> DebugConfigCommand.executeUnconfig((ServerCommandSource)context.getSource(), UuidArgumentType.getUuid(context, "target"))))));
    }

    private static Iterable<String> collectConfiguringPlayers(MinecraftServer server) {
        HashSet<String> set = new HashSet<String>();
        for (ClientConnection clientConnection : server.getNetworkIo().getConnections()) {
            PacketListener packetListener = clientConnection.getPacketListener();
            if (!(packetListener instanceof ServerConfigurationNetworkHandler)) continue;
            ServerConfigurationNetworkHandler serverConfigurationNetworkHandler = (ServerConfigurationNetworkHandler)packetListener;
            set.add(serverConfigurationNetworkHandler.getDebugProfile().getId().toString());
        }
        return set;
    }

    private static int executeConfig(ServerCommandSource source, ServerPlayerEntity player) {
        GameProfile gameProfile = player.getGameProfile();
        player.networkHandler.reconfigure();
        source.sendFeedback(() -> Text.literal("Switched player " + gameProfile.getName() + "(" + String.valueOf(gameProfile.getId()) + ") to config mode"), false);
        return 1;
    }

    private static int executeUnconfig(ServerCommandSource source, UUID uuid) {
        for (ClientConnection clientConnection : source.getServer().getNetworkIo().getConnections()) {
            ServerConfigurationNetworkHandler serverConfigurationNetworkHandler;
            PacketListener packetListener = clientConnection.getPacketListener();
            if (!(packetListener instanceof ServerConfigurationNetworkHandler) || !(serverConfigurationNetworkHandler = (ServerConfigurationNetworkHandler)packetListener).getDebugProfile().getId().equals(uuid)) continue;
            serverConfigurationNetworkHandler.endConfiguration();
        }
        source.sendError(Text.literal("Can't find player to unconfig"));
        return 0;
    }
}

