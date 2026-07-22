/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.WallSkullBlock;

public class WallPlayerSkullBlock
extends WallSkullBlock {
    public static final MapCodec<WallPlayerSkullBlock> CODEC = WallPlayerSkullBlock.createCodec(WallPlayerSkullBlock::new);

    public MapCodec<WallPlayerSkullBlock> getCodec() {
        return CODEC;
    }

    public WallPlayerSkullBlock(AbstractBlock.Settings settings) {
        super(SkullBlock.Type.PLAYER, settings);
    }
}

