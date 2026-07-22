/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.util.collection.ListOperation;
import net.minecraft.util.dynamic.Codecs;

public class SetFireworksLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<SetFireworksLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetFireworksLootFunction.addConditionsField(instance).and(instance.group(ListOperation.Values.createCodec(FireworkExplosionComponent.CODEC, 256).optionalFieldOf("explosions").forGetter(function -> function.explosions), Codecs.UNSIGNED_BYTE.optionalFieldOf("flight_duration").forGetter(function -> function.flightDuration))).apply((Applicative<SetFireworksLootFunction, ?>)instance, SetFireworksLootFunction::new));
    public static final FireworksComponent DEFAULT_FIREWORKS = new FireworksComponent(0, List.of());
    private final Optional<ListOperation.Values<FireworkExplosionComponent>> explosions;
    private final Optional<Integer> flightDuration;

    protected SetFireworksLootFunction(List<LootCondition> conditions, Optional<ListOperation.Values<FireworkExplosionComponent>> explosions, Optional<Integer> flightDuration) {
        super(conditions);
        this.explosions = explosions;
        this.flightDuration = flightDuration;
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        stack.apply(DataComponentTypes.FIREWORKS, DEFAULT_FIREWORKS, this::apply);
        return stack;
    }

    private FireworksComponent apply(FireworksComponent fireworksComponent) {
        return new FireworksComponent(this.flightDuration.orElseGet(fireworksComponent::flightDuration), this.explosions.map(values -> values.apply(fireworksComponent.explosions())).orElse(fireworksComponent.explosions()));
    }

    public LootFunctionType<SetFireworksLootFunction> getType() {
        return LootFunctionTypes.SET_FIREWORKS;
    }
}

