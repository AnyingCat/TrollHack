/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.explosion;

import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;

public class AdvancedExplosionBehavior
extends ExplosionBehavior {
    private final boolean destroyBlocks;
    private final boolean damageEntities;
    private final Optional<Float> knockbackModifier;
    private final Optional<RegistryEntryList<Block>> immuneBlocks;

    public AdvancedExplosionBehavior(boolean destroyBlocks, boolean damageEntities, Optional<Float> knockbackModifier, Optional<RegistryEntryList<Block>> immuneBlocks) {
        this.destroyBlocks = destroyBlocks;
        this.damageEntities = damageEntities;
        this.knockbackModifier = knockbackModifier;
        this.immuneBlocks = immuneBlocks;
    }

    @Override
    public Optional<Float> getBlastResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
        if (this.immuneBlocks.isPresent()) {
            if (blockState.isIn(this.immuneBlocks.get())) {
                return Optional.of(Float.valueOf(3600000.0f));
            }
            return Optional.empty();
        }
        return super.getBlastResistance(explosion, world, pos, blockState, fluidState);
    }

    @Override
    public boolean canDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power) {
        return this.destroyBlocks;
    }

    @Override
    public boolean shouldDamage(Explosion explosion, Entity entity) {
        return this.damageEntities;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public float getKnockbackModifier(Entity entity) {
        boolean bl;
        if (entity instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity)entity;
            if (playerEntity.getAbilities().flying) {
                return 0.0f;
            }
        }
        boolean bl2 = bl = false;
        if (bl) {
            return 0.0f;
        }
        float f = this.knockbackModifier.orElseGet(() -> Float.valueOf(super.getKnockbackModifier(entity))).floatValue();
        return f;
    }
}

