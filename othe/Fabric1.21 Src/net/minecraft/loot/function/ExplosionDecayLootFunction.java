/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.util.math.random.Random;

public class ExplosionDecayLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<ExplosionDecayLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> ExplosionDecayLootFunction.addConditionsField(instance).apply(instance, ExplosionDecayLootFunction::new));

    private ExplosionDecayLootFunction(List<LootCondition> conditions) {
        super(conditions);
    }

    public LootFunctionType<ExplosionDecayLootFunction> getType() {
        return LootFunctionTypes.EXPLOSION_DECAY;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        Float float_ = context.get(LootContextParameters.EXPLOSION_RADIUS);
        if (float_ != null) {
            Random random = context.getRandom();
            float f = 1.0f / float_.floatValue();
            int i = stack.getCount();
            int j = 0;
            for (int k = 0; k < i; ++k) {
                if (!(random.nextFloat() <= f)) continue;
                ++j;
            }
            stack.setCount(j);
        }
        return stack;
    }

    public static ConditionalLootFunction.Builder<?> builder() {
        return ExplosionDecayLootFunction.builder(ExplosionDecayLootFunction::new);
    }
}

