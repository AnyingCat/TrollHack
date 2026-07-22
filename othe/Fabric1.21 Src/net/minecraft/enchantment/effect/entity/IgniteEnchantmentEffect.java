/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect.entity;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public record IgniteEnchantmentEffect(EnchantmentLevelBasedValue duration) implements EnchantmentEntityEffect
{
    public static final MapCodec<IgniteEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)EnchantmentLevelBasedValue.CODEC.fieldOf("duration")).forGetter(igniteEnchantmentEffect -> igniteEnchantmentEffect.duration)).apply((Applicative<IgniteEnchantmentEffect, ?>)instance, IgniteEnchantmentEffect::new));

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        user.setOnFireFor(this.duration.getValue(level));
    }

    public MapCodec<IgniteEnchantmentEffect> getCodec() {
        return CODEC;
    }
}

