/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.condition;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.loot.condition.AlternativeLootCondition;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.util.Util;

public class AnyOfLootCondition
extends AlternativeLootCondition {
    public static final MapCodec<AnyOfLootCondition> CODEC = AnyOfLootCondition.createCodec(AnyOfLootCondition::new);

    AnyOfLootCondition(List<LootCondition> terms) {
        super(terms, Util.anyOf(terms));
    }

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.ANY_OF;
    }

    public static Builder builder(LootCondition.Builder ... terms) {
        return new Builder(terms);
    }

    public static class Builder
    extends AlternativeLootCondition.Builder {
        public Builder(LootCondition.Builder ... builders) {
            super(builders);
        }

        @Override
        public Builder or(LootCondition.Builder builder) {
            this.add(builder);
            return this;
        }

        @Override
        protected LootCondition build(List<LootCondition> terms) {
            return new AnyOfLootCondition(terms);
        }
    }
}

