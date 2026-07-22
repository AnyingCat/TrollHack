/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Stainable;
import net.minecraft.block.TransparentBlock;
import net.minecraft.util.DyeColor;

public class StainedGlassBlock
extends TransparentBlock
implements Stainable {
    public static final MapCodec<StainedGlassBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)DyeColor.CODEC.fieldOf("color")).forGetter(StainedGlassBlock::getColor), StainedGlassBlock.createSettingsCodec()).apply((Applicative<StainedGlassBlock, ?>)instance, StainedGlassBlock::new));
    private final DyeColor color;

    public MapCodec<StainedGlassBlock> getCodec() {
        return CODEC;
    }

    public StainedGlassBlock(DyeColor color, AbstractBlock.Settings settings) {
        super(settings);
        this.color = color;
    }

    @Override
    public DyeColor getColor() {
        return this.color;
    }
}

