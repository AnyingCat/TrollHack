/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.condition;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.loot.condition.AllOfLootCondition;
import net.minecraft.loot.condition.AnyOfLootCondition;
import net.minecraft.loot.condition.InvertedLootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextAware;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;

/**
 * Loot conditions, officially {@index predicate}s, are JSON-based conditions to test
 * against in world. It's used in loot tables, advancements, and commands, and can be
 * defined by data packs.
 */
public interface LootCondition
extends LootContextAware,
Predicate<LootContext> {
    public static final Codec<LootCondition> BASE_CODEC = Registries.LOOT_CONDITION_TYPE.getCodec().dispatch("condition", LootCondition::getType, LootConditionType::codec);
    public static final Codec<LootCondition> CODEC = Codec.lazyInitialized(() -> Codec.withAlternative(BASE_CODEC, AllOfLootCondition.INLINE_CODEC));
    public static final Codec<RegistryEntry<LootCondition>> ENTRY_CODEC = RegistryElementCodec.of(RegistryKeys.PREDICATE, CODEC);

    public LootConditionType getType();

    @FunctionalInterface
    public static interface Builder {
        public LootCondition build();

        default public Builder invert() {
            return InvertedLootCondition.builder(this);
        }

        default public AnyOfLootCondition.Builder or(Builder condition) {
            return AnyOfLootCondition.builder(this, condition);
        }

        default public AllOfLootCondition.Builder and(Builder condition) {
            return AllOfLootCondition.builder(this, condition);
        }
    }
}

