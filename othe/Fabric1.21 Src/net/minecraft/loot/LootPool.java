/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.loot.v2.FabricLootPoolBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootChoice;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionConsumingBuilder;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.entry.LootPoolEntryTypes;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionConsumingBuilder;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.mutable.MutableInt;

public class LootPool {
    public static final Codec<LootPool> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)LootPoolEntryTypes.CODEC.listOf().fieldOf("entries")).forGetter(pool -> pool.entries), LootCondition.CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(pool -> pool.conditions), LootFunctionTypes.CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter(pool -> pool.functions), ((MapCodec)LootNumberProviderTypes.CODEC.fieldOf("rolls")).forGetter(pool -> pool.rolls), ((MapCodec)LootNumberProviderTypes.CODEC.fieldOf("bonus_rolls")).orElse(ConstantLootNumberProvider.create(0.0f)).forGetter(pool -> pool.bonusRolls)).apply((Applicative<LootPool, ?>)instance, LootPool::new));
    public final List<LootPoolEntry> entries;
    public final List<LootCondition> conditions;
    private final Predicate<LootContext> predicate;
    public final List<LootFunction> functions;
    private final BiFunction<ItemStack, LootContext, ItemStack> javaFunctions;
    public final LootNumberProvider rolls;
    public final LootNumberProvider bonusRolls;

    LootPool(List<LootPoolEntry> entries, List<LootCondition> conditions, List<LootFunction> functions, LootNumberProvider rolls, LootNumberProvider bonusRolls) {
        this.entries = entries;
        this.conditions = conditions;
        this.predicate = Util.allOf(conditions);
        this.functions = functions;
        this.javaFunctions = LootFunctionTypes.join(functions);
        this.rolls = rolls;
        this.bonusRolls = bonusRolls;
    }

    private void supplyOnce(Consumer<ItemStack> lootConsumer, LootContext context) {
        Random random = context.getRandom();
        ArrayList<LootChoice> list = Lists.newArrayList();
        MutableInt mutableInt = new MutableInt();
        for (LootPoolEntry lootPoolEntry : this.entries) {
            lootPoolEntry.expand(context, choice -> {
                int i = choice.getWeight(context.getLuck());
                if (i > 0) {
                    list.add(choice);
                    mutableInt.add(i);
                }
            });
        }
        int i = list.size();
        if (mutableInt.intValue() == 0 || i == 0) {
            return;
        }
        if (i == 1) {
            ((LootChoice)list.get(0)).generateLoot(lootConsumer, context);
            return;
        }
        int j = random.nextInt(mutableInt.intValue());
        for (LootChoice lootChoice : list) {
            if ((j -= lootChoice.getWeight(context.getLuck())) >= 0) continue;
            lootChoice.generateLoot(lootConsumer, context);
            return;
        }
    }

    public void addGeneratedLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
        if (!this.predicate.test(context)) {
            return;
        }
        Consumer<ItemStack> consumer = LootFunction.apply(this.javaFunctions, lootConsumer, context);
        int i = this.rolls.nextInt(context) + MathHelper.floor(this.bonusRolls.nextFloat(context) * context.getLuck());
        for (int j = 0; j < i; ++j) {
            this.supplyOnce(consumer, context);
        }
    }

    public void validate(LootTableReporter reporter) {
        int i;
        for (i = 0; i < this.conditions.size(); ++i) {
            this.conditions.get(i).validate(reporter.makeChild(".condition[" + i + "]"));
        }
        for (i = 0; i < this.functions.size(); ++i) {
            this.functions.get(i).validate(reporter.makeChild(".functions[" + i + "]"));
        }
        for (i = 0; i < this.entries.size(); ++i) {
            this.entries.get(i).validate(reporter.makeChild(".entries[" + i + "]"));
        }
        this.rolls.validate(reporter.makeChild(".rolls"));
        this.bonusRolls.validate(reporter.makeChild(".bonusRolls"));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder
    implements LootFunctionConsumingBuilder<Builder>,
    LootConditionConsumingBuilder<Builder>,
    FabricLootPoolBuilder {
        private final ImmutableList.Builder<LootPoolEntry> entries = ImmutableList.builder();
        private final ImmutableList.Builder<LootCondition> conditions = ImmutableList.builder();
        private final ImmutableList.Builder<LootFunction> functions = ImmutableList.builder();
        private LootNumberProvider rolls = ConstantLootNumberProvider.create(1.0f);
        private LootNumberProvider bonusRollsRange = ConstantLootNumberProvider.create(0.0f);

        public Builder rolls(LootNumberProvider rolls) {
            this.rolls = rolls;
            return this;
        }

        @Override
        public Builder getThisFunctionConsumingBuilder() {
            return this;
        }

        public Builder bonusRolls(LootNumberProvider bonusRolls) {
            this.bonusRollsRange = bonusRolls;
            return this;
        }

        public Builder with(LootPoolEntry.Builder<?> entry) {
            this.entries.add((Object)entry.build());
            return this;
        }

        @Override
        public Builder conditionally(LootCondition.Builder builder) {
            this.conditions.add((Object)builder.build());
            return this;
        }

        @Override
        public Builder apply(LootFunction.Builder builder) {
            this.functions.add((Object)builder.build());
            return this;
        }

        public LootPool build() {
            return new LootPool((List<LootPoolEntry>)((Object)this.entries.build()), (List<LootCondition>)((Object)this.conditions.build()), (List<LootFunction>)((Object)this.functions.build()), this.rolls, this.bonusRollsRange);
        }

        @Override
        public /* synthetic */ LootFunctionConsumingBuilder getThisFunctionConsumingBuilder() {
            return this.getThisFunctionConsumingBuilder();
        }

        @Override
        public /* synthetic */ LootFunctionConsumingBuilder apply(LootFunction.Builder function) {
            return this.apply(function);
        }

        @Override
        public /* synthetic */ LootConditionConsumingBuilder getThisConditionConsumingBuilder() {
            return this.getThisFunctionConsumingBuilder();
        }

        @Override
        public /* synthetic */ LootConditionConsumingBuilder conditionally(LootCondition.Builder condition) {
            return this.conditionally(condition);
        }
    }
}

