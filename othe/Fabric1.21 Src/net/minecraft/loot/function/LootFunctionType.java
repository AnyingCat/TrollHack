/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.mojang.serialization.MapCodec;
import net.minecraft.loot.function.LootFunction;

public record LootFunctionType<T extends LootFunction>(MapCodec<T> codec) {
}

