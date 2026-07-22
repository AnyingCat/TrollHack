/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.entry;

import com.mojang.serialization.MapCodec;
import net.minecraft.loot.entry.LootPoolEntry;

public record LootPoolEntryType(MapCodec<? extends LootPoolEntry> codec) {
}

