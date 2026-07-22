/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.Spawner;
import net.minecraft.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.block.enums.TrialSpawnerState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TrialSpawnerBlock
extends BlockWithEntity {
    public static final MapCodec<TrialSpawnerBlock> CODEC = TrialSpawnerBlock.createCodec(TrialSpawnerBlock::new);
    public static final EnumProperty<TrialSpawnerState> TRIAL_SPAWNER_STATE = Properties.TRIAL_SPAWNER_STATE;
    public static final BooleanProperty OMINOUS = Properties.OMINOUS;

    public MapCodec<TrialSpawnerBlock> getCodec() {
        return CODEC;
    }

    public TrialSpawnerBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(TRIAL_SPAWNER_STATE, TrialSpawnerState.INACTIVE)).with(OMINOUS, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(TRIAL_SPAWNER_STATE, OMINOUS);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TrialSpawnerBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world2, BlockState state2, BlockEntityType<T> type) {
        BlockEntityTicker<T> blockEntityTicker;
        if (world2 instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world2;
            blockEntityTicker = TrialSpawnerBlock.validateTicker(type, BlockEntityType.TRIAL_SPAWNER, (world, pos, state, blockEntity) -> blockEntity.getSpawner().tickServer(serverWorld, pos, state.getOrEmpty(Properties.OMINOUS).orElse(false)));
        } else {
            blockEntityTicker = TrialSpawnerBlock.validateTicker(type, BlockEntityType.TRIAL_SPAWNER, (world, pos, state, blockEntity) -> blockEntity.getSpawner().tickClient(world, pos, state.getOrEmpty(Properties.OMINOUS).orElse(false)));
        }
        return blockEntityTicker;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType options) {
        super.appendTooltip(stack, context, tooltip, options);
        Spawner.appendSpawnDataToTooltip(stack, tooltip, "spawn_data");
    }
}

