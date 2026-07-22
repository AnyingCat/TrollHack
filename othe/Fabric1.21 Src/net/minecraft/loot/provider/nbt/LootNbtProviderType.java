/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.provider.nbt;

import com.mojang.serialization.MapCodec;
import net.minecraft.loot.provider.nbt.LootNbtProvider;

public record LootNbtProviderType(MapCodec<? extends LootNbtProvider> codec) {
}

