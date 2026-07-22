/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.random.Random;

public class SetAttributesLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<SetAttributesLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetAttributesLootFunction.addConditionsField(instance).and(instance.group(((MapCodec)Attribute.CODEC.listOf().fieldOf("modifiers")).forGetter(function -> function.attributes), Codec.BOOL.optionalFieldOf("replace", true).forGetter(lootFunction -> lootFunction.replace))).apply((Applicative<SetAttributesLootFunction, ?>)instance, SetAttributesLootFunction::new));
    private final List<Attribute> attributes;
    private final boolean replace;

    SetAttributesLootFunction(List<LootCondition> conditions, List<Attribute> attributes, boolean replace) {
        super(conditions);
        this.attributes = List.copyOf(attributes);
        this.replace = replace;
    }

    public LootFunctionType<SetAttributesLootFunction> getType() {
        return LootFunctionTypes.SET_ATTRIBUTES;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return this.attributes.stream().flatMap(attribute -> attribute.amount.getRequiredParameters().stream()).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        if (this.replace) {
            stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, this.applyTo(context, AttributeModifiersComponent.DEFAULT));
        } else {
            stack.apply(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT, component -> {
                if (component.modifiers().isEmpty()) {
                    return this.applyTo(context, stack.getItem().getAttributeModifiers());
                }
                return this.applyTo(context, (AttributeModifiersComponent)component);
            });
        }
        return stack;
    }

    private AttributeModifiersComponent applyTo(LootContext context, AttributeModifiersComponent attributeModifiersComponent) {
        Random random = context.getRandom();
        for (Attribute attribute : this.attributes) {
            AttributeModifierSlot attributeModifierSlot = Util.getRandom(attribute.slots, random);
            attributeModifiersComponent = attributeModifiersComponent.with(attribute.attribute, new EntityAttributeModifier(attribute.id, attribute.amount.nextFloat(context), attribute.operation), attributeModifierSlot);
        }
        return attributeModifiersComponent;
    }

    public static AttributeBuilder attributeBuilder(Identifier id, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier.Operation operation, LootNumberProvider amountRange) {
        return new AttributeBuilder(id, attribute, operation, amountRange);
    }

    public static Builder builder() {
        return new Builder();
    }

    record Attribute(Identifier id, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier.Operation operation, LootNumberProvider amount, List<AttributeModifierSlot> slots) {
        private static final Codec<List<AttributeModifierSlot>> EQUIPMENT_SLOT_LIST_CODEC = Codecs.nonEmptyList(Codec.either(AttributeModifierSlot.CODEC, AttributeModifierSlot.CODEC.listOf()).xmap(either -> either.map(List::of, Function.identity()), slots -> slots.size() == 1 ? Either.left((AttributeModifierSlot)slots.getFirst()) : Either.right(slots)));
        public static final Codec<Attribute> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("id")).forGetter(Attribute::id), ((MapCodec)EntityAttribute.CODEC.fieldOf("attribute")).forGetter(Attribute::attribute), ((MapCodec)EntityAttributeModifier.Operation.CODEC.fieldOf("operation")).forGetter(Attribute::operation), ((MapCodec)LootNumberProviderTypes.CODEC.fieldOf("amount")).forGetter(Attribute::amount), ((MapCodec)EQUIPMENT_SLOT_LIST_CODEC.fieldOf("slot")).forGetter(Attribute::slots)).apply((Applicative<Attribute, ?>)instance, Attribute::new));
    }

    public static class AttributeBuilder {
        private final Identifier id;
        private final RegistryEntry<EntityAttribute> attribute;
        private final EntityAttributeModifier.Operation operation;
        private final LootNumberProvider amount;
        private final Set<AttributeModifierSlot> slots = EnumSet.noneOf(AttributeModifierSlot.class);

        public AttributeBuilder(Identifier id, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier.Operation operation, LootNumberProvider amount) {
            this.id = id;
            this.attribute = attribute;
            this.operation = operation;
            this.amount = amount;
        }

        public AttributeBuilder slot(AttributeModifierSlot slot) {
            this.slots.add(slot);
            return this;
        }

        public Attribute build() {
            return new Attribute(this.id, this.attribute, this.operation, this.amount, List.copyOf(this.slots));
        }
    }

    public static class Builder
    extends ConditionalLootFunction.Builder<Builder> {
        private final boolean replace;
        private final List<Attribute> attributes = Lists.newArrayList();

        public Builder(boolean replace) {
            this.replace = replace;
        }

        public Builder() {
            this(false);
        }

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        public Builder attribute(AttributeBuilder attribute) {
            this.attributes.add(attribute.build());
            return this;
        }

        @Override
        public LootFunction build() {
            return new SetAttributesLootFunction(this.getConditions(), this.attributes, this.replace);
        }

        @Override
        protected /* synthetic */ ConditionalLootFunction.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }
}

