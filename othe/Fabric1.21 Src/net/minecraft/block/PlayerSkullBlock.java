/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.SkullBlock;

public class PlayerSkullBlock
extends SkullBlock {
    public static final MapCodec<PlayerSkullBlock> CODEC = PlayerSkullBlock.createCodec(PlayerSkullBlock::new);

    public MapCodec<PlayerSkullBlock> getCodec() {
        return CODEC;
    }

    public PlayerSkullBlock(AbstractBlock.Settings settings) {
        super(SkullBlock.Type.PLAYER, settings);
    }
}

