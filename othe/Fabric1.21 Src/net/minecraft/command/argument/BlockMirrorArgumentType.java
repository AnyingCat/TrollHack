/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.BlockMirror;

public class BlockMirrorArgumentType
extends EnumArgumentType<BlockMirror> {
    private BlockMirrorArgumentType() {
        super(BlockMirror.CODEC, BlockMirror::values);
    }

    public static EnumArgumentType<BlockMirror> blockMirror() {
        return new BlockMirrorArgumentType();
    }

    public static BlockMirror getBlockMirror(CommandContext<ServerCommandSource> context, String id) {
        return context.getArgument(id, BlockMirror.class);
    }
}

