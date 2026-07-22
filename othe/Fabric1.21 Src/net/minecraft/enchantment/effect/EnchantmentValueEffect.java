/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.enchantment.effect.AllOfEnchantmentEffects;
import net.minecraft.enchantment.effect.value.AddEnchantmentEffect;
import net.minecraft.enchantment.effect.value.MultiplyEnchantmentEffect;
import net.minecraft.enchantment.effect.value.RemoveBinomialEnchantmentEffect;
import net.minecraft.enchantment.effect.value.SetEnchantmentEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.random.Random;

public interface EnchantmentValueEffect {
    public static final Codec<EnchantmentValueEffect> CODEC = Registries.ENCHANTMENT_VALUE_EFFECT_TYPE.getCodec().dispatch(EnchantmentValueEffect::getCodec, Function.identity());

    public static MapCodec<? extends EnchantmentValueEffect> registerAndGetDefault(Registry<MapCodec<? extends EnchantmentValueEffect>> registry) {
        Registry.register(registry, "add", AddEnchantmentEffect.CODEC);
        Registry.register(registry, "all_of", AllOfEnchantmentEffects.ValueEffects.CODEC);
        Registry.register(registry, "multiply", MultiplyEnchantmentEffect.CODEC);
        Registry.register(registry, "remove_binomial", RemoveBinomialEnchantmentEffect.CODEC);
        return Registry.register(registry, "set", SetEnchantmentEffect.CODEC);
    }

    public float apply(int var1, Random var2, float var3);

    public MapCodec<? extends EnchantmentValueEffect> getCodec();
}

