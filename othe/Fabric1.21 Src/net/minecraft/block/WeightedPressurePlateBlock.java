/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class WeightedPressurePlateBlock
extends AbstractPressurePlateBlock {
    public static final MapCodec<WeightedPressurePlateBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.intRange(1, 1024).fieldOf("max_weight")).forGetter(block -> block.weight), ((MapCodec)BlockSetType.CODEC.fieldOf("block_set_type")).forGetter(block -> block.blockSetType), WeightedPressurePlateBlock.createSettingsCodec()).apply((Applicative<WeightedPressurePlateBlock, ?>)instance, WeightedPressurePlateBlock::new));
    public static final IntProperty POWER = Properties.POWER;
    private final int weight;

    public MapCodec<WeightedPressurePlateBlock> getCodec() {
        return CODEC;
    }

    public WeightedPressurePlateBlock(int weight, BlockSetType type, AbstractBlock.Settings settings) {
        super(settings, type);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(POWER, 0));
        this.weight = weight;
    }

    @Override
    protected int getRedstoneOutput(World world, BlockPos pos) {
        int i = Math.min(WeightedPressurePlateBlock.getEntityCount(world, BOX.offset(pos), Entity.class), this.weight);
        if (i > 0) {
            float f = (float)Math.min(this.weight, i) / (float)this.weight;
            return MathHelper.ceil(f * 15.0f);
        }
        return 0;
    }

    @Override
    protected int getRedstoneOutput(BlockState state) {
        return state.get(POWER);
    }

    @Override
    protected BlockState setRedstoneOutput(BlockState state, int rsOut) {
        return (BlockState)state.with(POWER, rsOut);
    }

    @Override
    protected int getTickRate() {
        return 10;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWER);
    }
}

