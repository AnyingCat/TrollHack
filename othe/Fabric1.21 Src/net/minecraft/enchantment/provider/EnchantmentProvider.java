/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.provider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;

public interface EnchantmentProvider {
    public static final Codec<EnchantmentProvider> CODEC = Registries.ENCHANTMENT_PROVIDER_TYPE.getCodec().dispatch(EnchantmentProvider::getCodec, Function.identity());

    public void provideEnchantments(ItemStack var1, ItemEnchantmentsComponent.Builder var2, Random var3, LocalDifficulty var4);

    public MapCodec<? extends EnchantmentProvider> getCodec();
}

