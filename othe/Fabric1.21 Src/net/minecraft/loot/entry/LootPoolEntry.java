/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.entry;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionConsumingBuilder;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.AlternativeEntry;
import net.minecraft.loot.entry.EntryCombiner;
import net.minecraft.loot.entry.GroupEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.entry.SequenceEntry;
import net.minecraft.util.Util;

public abstract class LootPoolEntry
implements EntryCombiner {
    protected final List<LootCondition> conditions;
    private final Predicate<LootContext> conditionPredicate;

    protected LootPoolEntry(List<LootCondition> conditions) {
        this.conditions = conditions;
        this.conditionPredicate = Util.allOf(conditions);
    }

    protected static <T extends LootPoolEntry> Products.P1<RecordCodecBuilder.Mu<T>, List<LootCondition>> addConditionsField(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(LootCondition.CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(entry -> entry.conditions));
    }

    public void validate(LootTableReporter reporter) {
        for (int i = 0; i < this.conditions.size(); ++i) {
            this.conditions.get(i).validate(reporter.makeChild(".condition[" + i + "]"));
        }
    }

    protected final boolean test(LootContext context) {
        return this.conditionPredicate.test(context);
    }

    public abstract LootPoolEntryType getType();

    public static abstract class Builder<T extends Builder<T>>
    implements LootConditionConsumingBuilder<T> {
        private final ImmutableList.Builder<LootCondition> conditions = ImmutableList.builder();

        protected abstract T getThisBuilder();

        @Override
        public T conditionally(LootCondition.Builder builder) {
            this.conditions.add((Object)builder.build());
            return this.getThisBuilder();
        }

        @Override
        public final T getThisConditionConsumingBuilder() {
            return this.getThisBuilder();
        }

        protected List<LootCondition> getConditions() {
            return this.conditions.build();
        }

        public AlternativeEntry.Builder alternatively(Builder<?> builder) {
            return new AlternativeEntry.Builder(this, builder);
        }

        public GroupEntry.Builder sequenceEntry(Builder<?> entry) {
            return new GroupEntry.Builder(this, entry);
        }

        public SequenceEntry.Builder groupEntry(Builder<?> entry) {
            return new SequenceEntry.Builder(this, entry);
        }

        public abstract LootPoolEntry build();

        @Override
        public /* synthetic */ LootConditionConsumingBuilder getThisConditionConsumingBuilder() {
            return this.getThisConditionConsumingBuilder();
        }

        @Override
        public /* synthetic */ LootConditionConsumingBuilder conditionally(LootCondition.Builder condition) {
            return this.conditionally(condition);
        }
    }
}

