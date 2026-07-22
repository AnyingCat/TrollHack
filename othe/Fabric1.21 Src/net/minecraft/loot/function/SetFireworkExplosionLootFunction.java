/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;

public class SetFireworkExplosionLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<SetFireworkExplosionLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetFireworkExplosionLootFunction.addConditionsField(instance).and(instance.group(FireworkExplosionComponent.Type.CODEC.optionalFieldOf("shape").forGetter(function -> function.shape), FireworkExplosionComponent.COLORS_CODEC.optionalFieldOf("colors").forGetter(function -> function.colors), FireworkExplosionComponent.COLORS_CODEC.optionalFieldOf("fade_colors").forGetter(function -> function.fadeColors), Codec.BOOL.optionalFieldOf("trail").forGetter(function -> function.trail), Codec.BOOL.optionalFieldOf("twinkle").forGetter(function -> function.twinkle))).apply((Applicative<SetFireworkExplosionLootFunction, ?>)instance, SetFireworkExplosionLootFunction::new));
    public static final FireworkExplosionComponent DEFAULT_EXPLOSION = new FireworkExplosionComponent(FireworkExplosionComponent.Type.SMALL_BALL, IntList.of(), IntList.of(), false, false);
    final Optional<FireworkExplosionComponent.Type> shape;
    final Optional<IntList> colors;
    final Optional<IntList> fadeColors;
    final Optional<Boolean> trail;
    final Optional<Boolean> twinkle;

    public SetFireworkExplosionLootFunction(List<LootCondition> conditions, Optional<FireworkExplosionComponent.Type> shape, Optional<IntList> colors, Optional<IntList> fadeColors, Optional<Boolean> trail, Optional<Boolean> twinkle) {
        super(conditions);
        this.shape = shape;
        this.colors = colors;
        this.fadeColors = fadeColors;
        this.trail = trail;
        this.twinkle = twinkle;
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        stack.apply(DataComponentTypes.FIREWORK_EXPLOSION, DEFAULT_EXPLOSION, this::apply);
        return stack;
    }

    private FireworkExplosionComponent apply(FireworkExplosionComponent current) {
        return new FireworkExplosionComponent(this.shape.orElseGet(current::shape), this.colors.orElseGet(current::colors), this.fadeColors.orElseGet(current::fadeColors), this.trail.orElseGet(current::hasTrail), this.twinkle.orElseGet(current::hasTwinkle));
    }

    public LootFunctionType<SetFireworkExplosionLootFunction> getType() {
        return LootFunctionTypes.SET_FIREWORK_EXPLOSION;
    }
}

