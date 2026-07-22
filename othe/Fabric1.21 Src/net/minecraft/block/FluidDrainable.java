/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public interface FluidDrainable {
    public ItemStack tryDrainFluid(@Nullable PlayerEntity var1, WorldAccess var2, BlockPos var3, BlockState var4);

    /**
     * {@return the sound played when filling a bucket with the fluid contained in this block}
     * 
     * @see net.minecraft.fluid.Fluid#getBucketFillSound()
     */
    public Optional<SoundEvent> getBucketFillSound();
}

