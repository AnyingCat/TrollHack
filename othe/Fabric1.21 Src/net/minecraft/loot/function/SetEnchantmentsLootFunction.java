/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
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
import net.minecraft.util.math.MathHelper;

public class SetEnchantmentsLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<SetEnchantmentsLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetEnchantmentsLootFunction.addConditionsField(instance).and(instance.group(Codec.unboundedMap(Enchantment.ENTRY_CODEC, LootNumberProviderTypes.CODEC).optionalFieldOf("enchantments", Map.of()).forGetter(function -> function.enchantments), ((MapCodec)Codec.BOOL.fieldOf("add")).orElse(false).forGetter(function -> function.add))).apply((Applicative<SetEnchantmentsLootFunction, ?>)instance, SetEnchantmentsLootFunction::new));
    private final Map<RegistryEntry<Enchantment>, LootNumberProvider> enchantments;
    private final boolean add;

    SetEnchantmentsLootFunction(List<LootCondition> conditions, Map<RegistryEntry<Enchantment>, LootNumberProvider> enchantments, boolean add) {
        super(conditions);
        this.enchantments = Map.copyOf(enchantments);
        this.add = add;
    }

    public LootFunctionType<SetEnchantmentsLootFunction> getType() {
        return LootFunctionTypes.SET_ENCHANTMENTS;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return this.enchantments.values().stream().flatMap(numberProvider -> numberProvider.getRequiredParameters().stream()).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        if (stack.isOf(Items.BOOK)) {
            stack = stack.withItem(Items.ENCHANTED_BOOK);
            stack.set(DataComponentTypes.STORED_ENCHANTMENTS, stack.remove(DataComponentTypes.ENCHANTMENTS));
        }
        EnchantmentHelper.apply(stack, builder -> {
            if (this.add) {
                this.enchantments.forEach((enchantment, level) -> builder.set((RegistryEntry<Enchantment>)enchantment, MathHelper.clamp(builder.getLevel((RegistryEntry<Enchantment>)enchantment) + level.nextInt(context), 0, 255)));
            } else {
                this.enchantments.forEach((enchantment, level) -> builder.set((RegistryEntry<Enchantment>)enchantment, MathHelper.clamp(level.nextInt(context), 0, 255)));
            }
        });
        return stack;
    }

    public static class Builder
    extends ConditionalLootFunction.Builder<Builder> {
        private final ImmutableMap.Builder<RegistryEntry<Enchantment>, LootNumberProvider> enchantments = ImmutableMap.builder();
        private final boolean add;

        public Builder() {
            this(false);
        }

        public Builder(boolean add) {
            this.add = add;
        }

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        public Builder enchantment(RegistryEntry<Enchantment> enchantment, LootNumberProvider level) {
            this.enchantments.put(enchantment, level);
            return this;
        }

        @Override
        public LootFunction build() {
            return new SetEnchantmentsLootFunction(this.getConditions(), this.enchantments.build(), this.add);
        }

        @Override
        protected /* synthetic */ ConditionalLootFunction.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }
}

