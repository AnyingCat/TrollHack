/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect;

import com.mojang.serialization.Codec;

public record DamageImmunityEnchantmentEffect() {
    public static final DamageImmunityEnchantmentEffect INSTANCE = new DamageImmunityEnchantmentEffect();
    public static final Codec<DamageImmunityEnchantmentEffect> CODEC = Codec.unit(() -> INSTANCE);
}

