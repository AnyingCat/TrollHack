/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class AbstractChestBlock<E extends BlockEntity>
extends BlockWithEntity {
    protected final Supplier<BlockEntityType<? extends E>> entityTypeRetriever;

    protected AbstractChestBlock(AbstractBlock.Settings settings, Supplier<BlockEntityType<? extends E>> entityTypeSupplier) {
        super(settings);
        this.entityTypeRetriever = entityTypeSupplier;
    }

    protected abstract MapCodec<? extends AbstractChestBlock<E>> getCodec();

    public abstract DoubleBlockProperties.PropertySource<? extends ChestBlockEntity> getBlockEntitySource(BlockState var1, World var2, BlockPos var3, boolean var4);
}

