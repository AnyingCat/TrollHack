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
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public record DamageItemEnchantmentEffect(EnchantmentLevelBasedValue amount) implements EnchantmentEntityEffect
{
    public static final MapCodec<DamageItemEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)EnchantmentLevelBasedValue.CODEC.fieldOf("amount")).forGetter(damageItemEnchantmentEffect -> damageItemEnchantmentEffect.amount)).apply((Applicative<DamageItemEnchantmentEffect, ?>)instance, DamageItemEnchantmentEffect::new));

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        ServerPlayerEntity serverPlayerEntity;
        LivingEntity livingEntity = context.owner();
        ServerPlayerEntity serverPlayerEntity2 = livingEntity instanceof ServerPlayerEntity ? (serverPlayerEntity = (ServerPlayerEntity)livingEntity) : null;
        context.stack().damage((int)this.amount.getValue(level), world, serverPlayerEntity2, context.onBreak());
    }

    public MapCodec<DamageItemEnchantmentEffect> getCodec() {
        return CODEC;
    }
}

