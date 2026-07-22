/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.SuspiciousStewEffectsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Util;

public class SetStewEffectLootFunction
extends ConditionalLootFunction {
    private static final Codec<List<StewEffect>> STEW_EFFECT_LIST_CODEC = StewEffect.CODEC.listOf().validate((A stewEffects) -> {
        ObjectOpenHashSet set = new ObjectOpenHashSet();
        for (StewEffect stewEffect : stewEffects) {
            if (set.add(stewEffect.effect())) continue;
            return DataResult.error(() -> "Encountered duplicate mob effect: '" + String.valueOf(stewEffect.effect()) + "'");
        }
        return DataResult.success(stewEffects);
    });
    public static final MapCodec<SetStewEffectLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetStewEffectLootFunction.addConditionsField(instance).and(STEW_EFFECT_LIST_CODEC.optionalFieldOf("effects", List.of()).forGetter(function -> function.stewEffects)).apply((Applicative<SetStewEffectLootFunction, ?>)instance, SetStewEffectLootFunction::new));
    private final List<StewEffect> stewEffects;

    SetStewEffectLootFunction(List<LootCondition> conditions, List<StewEffect> stewEffects) {
        super(conditions);
        this.stewEffects = stewEffects;
    }

    public LootFunctionType<SetStewEffectLootFunction> getType() {
        return LootFunctionTypes.SET_STEW_EFFECT;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return this.stewEffects.stream().flatMap(stewEffect -> stewEffect.duration().getRequiredParameters().stream()).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        if (!stack.isOf(Items.SUSPICIOUS_STEW) || this.stewEffects.isEmpty()) {
            return stack;
        }
        StewEffect stewEffect = Util.getRandom(this.stewEffects, context.getRandom());
        RegistryEntry<StatusEffect> registryEntry = stewEffect.effect();
        int i = stewEffect.duration().nextInt(context);
        if (!registryEntry.value().isInstant()) {
            i *= 20;
        }
        SuspiciousStewEffectsComponent.StewEffect stewEffect2 = new SuspiciousStewEffectsComponent.StewEffect(registryEntry, i);
        stack.apply(DataComponentTypes.SUSPICIOUS_STEW_EFFECTS, SuspiciousStewEffectsComponent.DEFAULT, stewEffect2, SuspiciousStewEffectsComponent::with);
        return stack;
    }

    public static Builder builder() {
        return new Builder();
    }

    record StewEffect(RegistryEntry<StatusEffect> effect, LootNumberProvider duration) {
        public static final Codec<StewEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)StatusEffect.ENTRY_CODEC.fieldOf("type")).forGetter(StewEffect::effect), ((MapCodec)LootNumberProviderTypes.CODEC.fieldOf("duration")).forGetter(StewEffect::duration)).apply((Applicative<StewEffect, ?>)instance, StewEffect::new));
    }

    public static class Builder
    extends ConditionalLootFunction.Builder<Builder> {
        private final ImmutableList.Builder<StewEffect> map = ImmutableList.builder();

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        public Builder withEffect(RegistryEntry<StatusEffect> effect, LootNumberProvider durationRange) {
            this.map.add((Object)new StewEffect(effect, durationRange));
            return this;
        }

        @Override
        public LootFunction build() {
            return new SetStewEffectLootFunction(this.getConditions(), (List<StewEffect>)((Object)this.map.build()));
        }

        @Override
        protected /* synthetic */ ConditionalLootFunction.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }
}

