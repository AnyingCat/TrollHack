/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.ResourcePackRemoveS2CPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class ServerPackCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("serverpack").requires(source -> source.hasPermissionLevel(2))).then(CommandManager.literal("push").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("url", StringArgumentType.string()).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("uuid", UuidArgumentType.uuid()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("hash", StringArgumentType.word()).executes(context -> ServerPackCommand.executePush((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "url"), Optional.of(UuidArgumentType.getUuid(context, "uuid")), Optional.of(StringArgumentType.getString(context, "hash")))))).executes(context -> ServerPackCommand.executePush((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "url"), Optional.of(UuidArgumentType.getUuid(context, "uuid")), Optional.empty())))).executes(context -> ServerPackCommand.executePush((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "url"), Optional.empty(), Optional.empty()))))).then(CommandManager.literal("pop").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("uuid", UuidArgumentType.uuid()).executes(context -> ServerPackCommand.executePop((ServerCommandSource)context.getSource(), UuidArgumentType.getUuid(context, "uuid"))))));
    }

    private static void sendToAll(ServerCommandSource source, Packet<?> packet) {
        source.getServer().getNetworkIo().getConnections().forEach(connection -> connection.send(packet));
    }

    private static int executePush(ServerCommandSource source, String url, Optional<UUID> uuid, Optional<String> hash) {
        UUID uUID = uuid.orElseGet(() -> UUID.nameUUIDFromBytes(url.getBytes(StandardCharsets.UTF_8)));
        String string = hash.orElse("");
        ResourcePackSendS2CPacket resourcePackSendS2CPacket = new ResourcePackSendS2CPacket(uUID, url, string, false, null);
        ServerPackCommand.sendToAll(source, resourcePackSendS2CPacket);
        return 0;
    }

    private static int executePop(ServerCommandSource source, UUID uuid) {
        ResourcePackRemoveS2CPacket resourcePackRemoveS2CPacket = new ResourcePackRemoveS2CPacket(Optional.of(uuid));
        ServerPackCommand.sendToAll(source, resourcePackRemoveS2CPacket);
        return 0;
    }
}

