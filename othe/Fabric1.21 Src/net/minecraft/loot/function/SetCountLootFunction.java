/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;

public class SetCountLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<SetCountLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetCountLootFunction.addConditionsField(instance).and(instance.group(((MapCodec)LootNumberProviderTypes.CODEC.fieldOf("count")).forGetter(function -> function.countRange), ((MapCodec)Codec.BOOL.fieldOf("add")).orElse(false).forGetter(function -> function.add))).apply((Applicative<SetCountLootFunction, ?>)instance, SetCountLootFunction::new));
    private final LootNumberProvider countRange;
    private final boolean add;

    private SetCountLootFunction(List<LootCondition> conditions, LootNumberProvider countRange, boolean add) {
        super(conditions);
        this.countRange = countRange;
        this.add = add;
    }

    public LootFunctionType<SetCountLootFunction> getType() {
        return LootFunctionTypes.SET_COUNT;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return this.countRange.getRequiredParameters();
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        int i = this.add ? stack.getCount() : 0;
        stack.setCount(i + this.countRange.nextInt(context));
        return stack;
    }

    public static ConditionalLootFunction.Builder<?> builder(LootNumberProvider countRange) {
        return SetCountLootFunction.builder((List<LootCondition> list) -> new SetCountLootFunction((List<LootCondition>)list, countRange, false));
    }

    public static ConditionalLootFunction.Builder<?> builder(LootNumberProvider countRange, boolean add) {
        return SetCountLootFunction.builder((List<LootCondition> list) -> new SetCountLootFunction((List<LootCondition>)list, countRange, add));
    }
}

