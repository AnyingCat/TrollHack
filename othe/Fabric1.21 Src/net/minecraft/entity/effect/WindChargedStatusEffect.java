/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.effect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.projectile.AbstractWindChargeEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

class WindChargedStatusEffect
extends StatusEffect {
    protected WindChargedStatusEffect(StatusEffectCategory statusEffectCategory, int i) {
        super(statusEffectCategory, i, ParticleTypes.SMALL_GUST);
    }

    @Override
    public void onEntityRemoval(LivingEntity entity, int amplifier, Entity.RemovalReason reason) {
        World world;
        if (reason == Entity.RemovalReason.KILLED && (world = entity.getWorld()) instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            double d = entity.getX();
            double e = entity.getY() + (double)(entity.getHeight() / 2.0f);
            double f = entity.getZ();
            float g = 3.0f + entity.getRandom().nextFloat() * 2.0f;
            serverWorld.createExplosion(entity, null, AbstractWindChargeEntity.EXPLOSION_BEHAVIOR, d, e, f, g, false, World.ExplosionSourceType.TRIGGER, ParticleTypes.GUST_EMITTER_SMALL, ParticleTypes.GUST_EMITTER_LARGE, SoundEvents.ENTITY_BREEZE_WIND_BURST);
        }
    }
}

