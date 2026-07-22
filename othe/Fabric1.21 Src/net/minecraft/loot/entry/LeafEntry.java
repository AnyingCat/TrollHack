/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.entry;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootChoice;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionConsumingBuilder;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.util.math.MathHelper;

public abstract class LeafEntry
extends LootPoolEntry {
    public static final int DEFAULT_WEIGHT = 1;
    public static final int DEFAULT_QUALITY = 0;
    protected final int weight;
    protected final int quality;
    protected final List<LootFunction> functions;
    final BiFunction<ItemStack, LootContext, ItemStack> compiledFunctions;
    private final LootChoice choice = new Choice(){

        @Override
        public void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
            LeafEntry.this.generateLoot(LootFunction.apply(LeafEntry.this.compiledFunctions, lootConsumer, context), context);
        }
    };

    protected LeafEntry(int weight, int quality, List<LootCondition> conditions, List<LootFunction> functions) {
        super(conditions);
        this.weight = weight;
        this.quality = quality;
        this.functions = functions;
        this.compiledFunctions = LootFunctionTypes.join(functions);
    }

    protected static <T extends LeafEntry> Products.P4<RecordCodecBuilder.Mu<T>, Integer, Integer, List<LootCondition>, List<LootFunction>> addLeafFields(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(Codec.INT.optionalFieldOf("weight", 1).forGetter(entry -> entry.weight), Codec.INT.optionalFieldOf("quality", 0).forGetter(entry -> entry.quality)).and(LeafEntry.addConditionsField(instance).t1()).and(LootFunctionTypes.CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter(entry -> entry.functions));
    }

    @Override
    public void validate(LootTableReporter reporter) {
        super.validate(reporter);
        for (int i = 0; i < this.functions.size(); ++i) {
            this.functions.get(i).validate(reporter.makeChild(".functions[" + i + "]"));
        }
    }

    protected abstract void generateLoot(Consumer<ItemStack> var1, LootContext var2);

    @Override
    public boolean expand(LootContext lootContext, Consumer<LootChoice> consumer) {
        if (this.test(lootContext)) {
            consumer.accept(this.choice);
            return true;
        }
        return false;
    }

    public static Builder<?> builder(Factory factory) {
        return new BasicBuilder(factory);
    }

    static class BasicBuilder
    extends Builder<BasicBuilder> {
        private final Factory factory;

        public BasicBuilder(Factory factory) {
            this.factory = factory;
        }

        @Override
        protected BasicBuilder getThisBuilder() {
            return this;
        }

        @Override
        public LootPoolEntry build() {
            return this.factory.build(this.weight, this.quality, this.getConditions(), this.getFunctions());
        }

        @Override
        protected /* synthetic */ LootPoolEntry.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }

    @FunctionalInterface
    protected static interface Factory {
        public LeafEntry build(int var1, int var2, List<LootCondition> var3, List<LootFunction> var4);
    }

    public static abstract class Builder<T extends Builder<T>>
    extends LootPoolEntry.Builder<T>
    implements LootFunctionConsumingBuilder<T> {
        protected int weight = 1;
        protected int quality = 0;
        private final ImmutableList.Builder<LootFunction> functions = ImmutableList.builder();

        @Override
        public T apply(LootFunction.Builder builder) {
            this.functions.add((Object)builder.build());
            return (T)((Builder)this.getThisBuilder());
        }

        protected List<LootFunction> getFunctions() {
            return this.functions.build();
        }

        public T weight(int weight) {
            this.weight = weight;
            return (T)((Builder)this.getThisBuilder());
        }

        public T quality(int quality) {
            this.quality = quality;
            return (T)((Builder)this.getThisBuilder());
        }

        @Override
        public /* synthetic */ LootFunctionConsumingBuilder getThisFunctionConsumingBuilder() {
            return (LootFunctionConsumingBuilder)((Object)super.getThisConditionConsumingBuilder());
        }

        @Override
        public /* synthetic */ LootFunctionConsumingBuilder apply(LootFunction.Builder function) {
            return this.apply(function);
        }
    }

    protected abstract class Choice
    implements LootChoice {
        protected Choice() {
        }

        @Override
        public int getWeight(float luck) {
            return Math.max(MathHelper.floor((float)LeafEntry.this.weight + (float)LeafEntry.this.quality * luck), 0);
        }
    }
}

