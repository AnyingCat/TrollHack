/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;

public class SetItemLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<SetItemLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetItemLootFunction.addConditionsField(instance).and(((MapCodec)RegistryFixedCodec.of(RegistryKeys.ITEM).fieldOf("item")).forGetter(lootFunction -> lootFunction.item)).apply((Applicative<SetItemLootFunction, ?>)instance, SetItemLootFunction::new));
    private final RegistryEntry<Item> item;

    private SetItemLootFunction(List<LootCondition> conditions, RegistryEntry<Item> item) {
        super(conditions);
        this.item = item;
    }

    public LootFunctionType<SetItemLootFunction> getType() {
        return LootFunctionTypes.SET_ITEM;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        return stack.withItem(this.item.value());
    }
}

