/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PressurePlateBlock
extends AbstractPressurePlateBlock {
    public static final MapCodec<PressurePlateBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)BlockSetType.CODEC.fieldOf("block_set_type")).forGetter(block -> block.blockSetType), PressurePlateBlock.createSettingsCodec()).apply((Applicative<PressurePlateBlock, ?>)instance, PressurePlateBlock::new));
    public static final BooleanProperty POWERED = Properties.POWERED;

    public MapCodec<PressurePlateBlock> getCodec() {
        return CODEC;
    }

    public PressurePlateBlock(BlockSetType type, AbstractBlock.Settings settings) {
        super(settings, type);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(POWERED, false));
    }

    @Override
    protected int getRedstoneOutput(BlockState state) {
        return state.get(POWERED) != false ? 15 : 0;
    }

    @Override
    protected BlockState setRedstoneOutput(BlockState state, int rsOut) {
        return (BlockState)state.with(POWERED, rsOut > 0);
    }

    @Override
    protected int getRedstoneOutput(World world, BlockPos pos) {
        Class<Entity> class_ = switch (this.blockSetType.pressurePlateSensitivity()) {
            default -> throw new MatchException(null, null);
            case BlockSetType.ActivationRule.EVERYTHING -> Entity.class;
            case BlockSetType.ActivationRule.MOBS -> LivingEntity.class;
        };
        return PressurePlateBlock.getEntityCount(world, BOX.offset(pos), class_) > 0 ? 15 : 0;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }
}

