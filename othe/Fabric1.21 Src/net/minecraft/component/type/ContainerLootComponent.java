/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public record ContainerLootComponent(RegistryKey<LootTable> lootTable, long seed) {
    public static final Codec<ContainerLootComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)RegistryKey.createCodec(RegistryKeys.LOOT_TABLE).fieldOf("loot_table")).forGetter(ContainerLootComponent::lootTable), Codec.LONG.optionalFieldOf("seed", 0L).forGetter(ContainerLootComponent::seed)).apply((Applicative<ContainerLootComponent, ?>)instance, ContainerLootComponent::new));
}

