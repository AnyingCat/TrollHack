/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.util.math.MathHelper;

public class SetOminousBottleAmplifierLootFunction
extends ConditionalLootFunction {
    static final MapCodec<SetOminousBottleAmplifierLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetOminousBottleAmplifierLootFunction.addConditionsField(instance).and(((MapCodec)LootNumberProviderTypes.CODEC.fieldOf("amplifier")).forGetter(lootFunction -> lootFunction.amplifier)).apply((Applicative<SetOminousBottleAmplifierLootFunction, ?>)instance, SetOminousBottleAmplifierLootFunction::new));
    private final LootNumberProvider amplifier;

    private SetOminousBottleAmplifierLootFunction(List<LootCondition> conditions, LootNumberProvider amplifier) {
        super(conditions);
        this.amplifier = amplifier;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return this.amplifier.getRequiredParameters();
    }

    public LootFunctionType<SetOminousBottleAmplifierLootFunction> getType() {
        return LootFunctionTypes.SET_OMINOUS_BOTTLE_AMPLIFIER;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        int i = MathHelper.clamp(this.amplifier.nextInt(context), 0, 4);
        stack.set(DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER, i);
        return stack;
    }

    public LootNumberProvider getAmplifier() {
        return this.amplifier;
    }

    public static ConditionalLootFunction.Builder<?> builder(LootNumberProvider amplifier) {
        return SetOminousBottleAmplifierLootFunction.builder((List<LootCondition> conditions) -> new SetOminousBottleAmplifierLootFunction((List<LootCondition>)conditions, amplifier));
    }
}

