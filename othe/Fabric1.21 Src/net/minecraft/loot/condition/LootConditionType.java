/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.loot.condition.LootCondition;

public record LootConditionType(MapCodec<? extends LootCondition> codec) {
}

