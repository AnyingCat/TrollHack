/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionConsumingBuilder;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.Util;

public abstract class ConditionalLootFunction
implements LootFunction {
    protected final List<LootCondition> conditions;
    private final Predicate<LootContext> predicate;

    protected ConditionalLootFunction(List<LootCondition> conditions) {
        this.conditions = conditions;
        this.predicate = Util.allOf(conditions);
    }

    public abstract LootFunctionType<? extends ConditionalLootFunction> getType();

    protected static <T extends ConditionalLootFunction> Products.P1<RecordCodecBuilder.Mu<T>, List<LootCondition>> addConditionsField(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(LootCondition.CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(function -> function.conditions));
    }

    @Override
    public final ItemStack apply(ItemStack itemStack, LootContext lootContext) {
        return this.predicate.test(lootContext) ? this.process(itemStack, lootContext) : itemStack;
    }

    protected abstract ItemStack process(ItemStack var1, LootContext var2);

    @Override
    public void validate(LootTableReporter reporter) {
        LootFunction.super.validate(reporter);
        for (int i = 0; i < this.conditions.size(); ++i) {
            this.conditions.get(i).validate(reporter.makeChild(".conditions[" + i + "]"));
        }
    }

    protected static Builder<?> builder(Function<List<LootCondition>, LootFunction> joiner) {
        return new Joiner(joiner);
    }

    @Override
    public /* synthetic */ Object apply(Object itemStack, Object context) {
        return this.apply((ItemStack)itemStack, (LootContext)context);
    }

    static final class Joiner
    extends Builder<Joiner> {
        private final Function<List<LootCondition>, LootFunction> joiner;

        public Joiner(Function<List<LootCondition>, LootFunction> joiner) {
            this.joiner = joiner;
        }

        @Override
        protected Joiner getThisBuilder() {
            return this;
        }

        @Override
        public LootFunction build() {
            return this.joiner.apply(this.getConditions());
        }

        @Override
        protected /* synthetic */ Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }

    public static abstract class Builder<T extends Builder<T>>
    implements LootFunction.Builder,
    LootConditionConsumingBuilder<T> {
        private final ImmutableList.Builder<LootCondition> conditionList = ImmutableList.builder();

        @Override
        public T conditionally(LootCondition.Builder builder) {
            this.conditionList.add((Object)builder.build());
            return this.getThisBuilder();
        }

        @Override
        public final T getThisConditionConsumingBuilder() {
            return this.getThisBuilder();
        }

        protected abstract T getThisBuilder();

        protected List<LootCondition> getConditions() {
            return this.conditionList.build();
        }

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

