/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;

public class SetPotionLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<SetPotionLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetPotionLootFunction.addConditionsField(instance).and(((MapCodec)Potion.CODEC.fieldOf("id")).forGetter(function -> function.potion)).apply((Applicative<SetPotionLootFunction, ?>)instance, SetPotionLootFunction::new));
    private final RegistryEntry<Potion> potion;

    private SetPotionLootFunction(List<LootCondition> conditions, RegistryEntry<Potion> potion) {
        super(conditions);
        this.potion = potion;
    }

    public LootFunctionType<SetPotionLootFunction> getType() {
        return LootFunctionTypes.SET_POTION;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        stack.apply(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT, this.potion, PotionContentsComponent::with);
        return stack;
    }

    public static ConditionalLootFunction.Builder<?> builder(RegistryEntry<Potion> potion) {
        return SetPotionLootFunction.builder((List<LootCondition> conditions) -> new SetPotionLootFunction((List<LootCondition>)conditions, potion));
    }
}

