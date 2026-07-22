/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.predicate.item.ItemPredicate;

public class FilteredLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<FilteredLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> FilteredLootFunction.addConditionsField(instance).and(instance.group(((MapCodec)ItemPredicate.CODEC.fieldOf("item_filter")).forGetter(lootFunction -> lootFunction.itemFilter), ((MapCodec)LootFunctionTypes.CODEC.fieldOf("modifier")).forGetter(lootFunction -> lootFunction.modifier))).apply((Applicative<FilteredLootFunction, ?>)instance, FilteredLootFunction::new));
    private final ItemPredicate itemFilter;
    private final LootFunction modifier;

    private FilteredLootFunction(List<LootCondition> conditions, ItemPredicate itemFilter, LootFunction modifier) {
        super(conditions);
        this.itemFilter = itemFilter;
        this.modifier = modifier;
    }

    public LootFunctionType<FilteredLootFunction> getType() {
        return LootFunctionTypes.FILTERED;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        if (this.itemFilter.test(stack)) {
            return (ItemStack)this.modifier.apply(stack, context);
        }
        return stack;
    }

    @Override
    public void validate(LootTableReporter reporter) {
        super.validate(reporter);
        this.modifier.validate(reporter.makeChild(".modifier"));
    }
}

