/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
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
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;

public class SetDamageLootFunction
extends ConditionalLootFunction {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<SetDamageLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetDamageLootFunction.addConditionsField(instance).and(instance.group(((MapCodec)LootNumberProviderTypes.CODEC.fieldOf("damage")).forGetter(function -> function.durabilityRange), ((MapCodec)Codec.BOOL.fieldOf("add")).orElse(false).forGetter(function -> function.add))).apply((Applicative<SetDamageLootFunction, ?>)instance, SetDamageLootFunction::new));
    private final LootNumberProvider durabilityRange;
    private final boolean add;

    private SetDamageLootFunction(List<LootCondition> conditions, LootNumberProvider durabilityRange, boolean add) {
        super(conditions);
        this.durabilityRange = durabilityRange;
        this.add = add;
    }

    public LootFunctionType<SetDamageLootFunction> getType() {
        return LootFunctionTypes.SET_DAMAGE;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return this.durabilityRange.getRequiredParameters();
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        if (stack.isDamageable()) {
            int i = stack.getMaxDamage();
            float f = this.add ? 1.0f - (float)stack.getDamage() / (float)i : 0.0f;
            float g = 1.0f - MathHelper.clamp(this.durabilityRange.nextFloat(context) + f, 0.0f, 1.0f);
            stack.setDamage(MathHelper.floor(g * (float)i));
        } else {
            LOGGER.warn("Couldn't set damage of loot item {}", (Object)stack);
        }
        return stack;
    }

    public static ConditionalLootFunction.Builder<?> builder(LootNumberProvider durabilityRange) {
        return SetDamageLootFunction.builder((List<LootCondition> conditions) -> new SetDamageLootFunction((List<LootCondition>)conditions, durabilityRange, false));
    }

    public static ConditionalLootFunction.Builder<?> builder(LootNumberProvider durabilityRange, boolean add) {
        return SetDamageLootFunction.builder((List<LootCondition> conditions) -> new SetDamageLootFunction((List<LootCondition>)conditions, durabilityRange, add));
    }
}

