/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ContainerComponentModifier;
import net.minecraft.loot.ContainerComponentModifiers;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.entry.LootPoolEntryTypes;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;

public class SetContentsLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<SetContentsLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetContentsLootFunction.addConditionsField(instance).and(instance.group(((MapCodec)ContainerComponentModifiers.MODIFIER_CODEC.fieldOf("component")).forGetter(function -> function.component), ((MapCodec)LootPoolEntryTypes.CODEC.listOf().fieldOf("entries")).forGetter(function -> function.entries))).apply((Applicative<SetContentsLootFunction, ?>)instance, SetContentsLootFunction::new));
    private final ContainerComponentModifier<?> component;
    private final List<LootPoolEntry> entries;

    SetContentsLootFunction(List<LootCondition> conditions, ContainerComponentModifier<?> component, List<LootPoolEntry> entries) {
        super(conditions);
        this.component = component;
        this.entries = List.copyOf(entries);
    }

    public LootFunctionType<SetContentsLootFunction> getType() {
        return LootFunctionTypes.SET_CONTENTS;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        if (stack.isEmpty()) {
            return stack;
        }
        Stream.Builder builder = Stream.builder();
        this.entries.forEach(entry -> entry.expand(context, choice -> choice.generateLoot(LootTable.processStacks(context.getWorld(), builder::add), context)));
        this.component.apply(stack, builder.build());
        return stack;
    }

    @Override
    public void validate(LootTableReporter reporter) {
        super.validate(reporter);
        for (int i = 0; i < this.entries.size(); ++i) {
            this.entries.get(i).validate(reporter.makeChild(".entry[" + i + "]"));
        }
    }

    public static Builder builder(ContainerComponentModifier<?> componentModifier) {
        return new Builder(componentModifier);
    }

    public static class Builder
    extends ConditionalLootFunction.Builder<Builder> {
        private final ImmutableList.Builder<LootPoolEntry> entries = ImmutableList.builder();
        private final ContainerComponentModifier<?> componentModifier;

        public Builder(ContainerComponentModifier<?> componentModifier) {
            this.componentModifier = componentModifier;
        }

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        public Builder withEntry(LootPoolEntry.Builder<?> entryBuilder) {
            this.entries.add((Object)entryBuilder.build());
            return this;
        }

        @Override
        public LootFunction build() {
            return new SetContentsLootFunction(this.getConditions(), this.componentModifier, (List<LootPoolEntry>)((Object)this.entries.build()));
        }

        @Override
        protected /* synthetic */ ConditionalLootFunction.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }
}

