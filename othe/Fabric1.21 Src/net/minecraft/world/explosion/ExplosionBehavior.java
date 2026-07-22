/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.explosion;

import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.explosion.Explosion;

public class ExplosionBehavior {
    public Optional<Float> getBlastResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
        if (blockState.isAir() && fluidState.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Float.valueOf(Math.max(blockState.getBlock().getBlastResistance(), fluidState.getBlastResistance())));
    }

    public boolean canDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power) {
        return true;
    }

    public boolean shouldDamage(Explosion explosion, Entity entity) {
        return true;
    }

    public float getKnockbackModifier(Entity entity) {
        return 1.0f;
    }

    public float calculateDamage(Explosion explosion, Entity entity) {
        float f = explosion.getPower() * 2.0f;
        Vec3d vec3d = explosion.getPosition();
        double d = Math.sqrt(entity.squaredDistanceTo(vec3d)) / (double)f;
        double e = (1.0 - d) * (double)Explosion.getExposure(vec3d, entity);
        return (float)((e * e + e) / 2.0 * 7.0 * (double)f + 1.0);
    }
}

