/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;

public class SetCustomDataLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<SetCustomDataLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetCustomDataLootFunction.addConditionsField(instance).and(((MapCodec)StringNbtReader.NBT_COMPOUND_CODEC.fieldOf("tag")).forGetter(function -> function.nbt)).apply((Applicative<SetCustomDataLootFunction, ?>)instance, SetCustomDataLootFunction::new));
    private final NbtCompound nbt;

    private SetCustomDataLootFunction(List<LootCondition> conditions, NbtCompound nbt) {
        super(conditions);
        this.nbt = nbt;
    }

    public LootFunctionType<SetCustomDataLootFunction> getType() {
        return LootFunctionTypes.SET_CUSTOM_DATA;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> nbt.copyFrom(this.nbt));
        return stack;
    }

    @Deprecated
    public static ConditionalLootFunction.Builder<?> builder(NbtCompound nbt) {
        return SetCustomDataLootFunction.builder((List<LootCondition> conditions) -> new SetCustomDataLootFunction((List<LootCondition>)conditions, nbt));
    }
}

