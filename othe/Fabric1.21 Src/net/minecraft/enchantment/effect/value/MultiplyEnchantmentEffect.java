/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect.value;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;
import net.minecraft.util.math.random.Random;

public record MultiplyEnchantmentEffect(EnchantmentLevelBasedValue factor) implements EnchantmentValueEffect
{
    public static final MapCodec<MultiplyEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)EnchantmentLevelBasedValue.CODEC.fieldOf("factor")).forGetter(MultiplyEnchantmentEffect::factor)).apply((Applicative<MultiplyEnchantmentEffect, ?>)instance, MultiplyEnchantmentEffect::new));

    @Override
    public float apply(int level, Random random, float inputValue) {
        return inputValue * this.factor.getValue(level);
    }

    public MapCodec<MultiplyEnchantmentEffect> getCodec() {
        return CODEC;
    }
}

