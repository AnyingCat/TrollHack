/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate.item;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.collection.CollectionPredicate;
import net.minecraft.predicate.item.ComponentSubPredicate;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.Identifier;

public record AttributeModifiersPredicate(Optional<CollectionPredicate<AttributeModifiersComponent.Entry, AttributeModifierPredicate>> modifiers) implements ComponentSubPredicate<AttributeModifiersComponent>
{
    public static final Codec<AttributeModifiersPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(CollectionPredicate.createCodec(AttributeModifierPredicate.CODEC).optionalFieldOf("modifiers").forGetter(AttributeModifiersPredicate::modifiers)).apply((Applicative<AttributeModifiersPredicate, ?>)instance, AttributeModifiersPredicate::new));

    @Override
    public ComponentType<AttributeModifiersComponent> getComponentType() {
        return DataComponentTypes.ATTRIBUTE_MODIFIERS;
    }

    @Override
    public boolean test(ItemStack itemStack, AttributeModifiersComponent attributeModifiersComponent) {
        return !this.modifiers.isPresent() || this.modifiers.get().test(attributeModifiersComponent.modifiers());
    }

    public record AttributeModifierPredicate(Optional<RegistryEntryList<EntityAttribute>> attribute, Optional<Identifier> id, NumberRange.DoubleRange amount, Optional<EntityAttributeModifier.Operation> operation, Optional<AttributeModifierSlot> slot) implements Predicate<AttributeModifiersComponent.Entry>
    {
        public static final Codec<AttributeModifierPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(RegistryCodecs.entryList(RegistryKeys.ATTRIBUTE).optionalFieldOf("attribute").forGetter(AttributeModifierPredicate::attribute), Identifier.CODEC.optionalFieldOf("id").forGetter(AttributeModifierPredicate::id), NumberRange.DoubleRange.CODEC.optionalFieldOf("amount", NumberRange.DoubleRange.ANY).forGetter(AttributeModifierPredicate::amount), EntityAttributeModifier.Operation.CODEC.optionalFieldOf("operation").forGetter(AttributeModifierPredicate::operation), AttributeModifierSlot.CODEC.optionalFieldOf("slot").forGetter(AttributeModifierPredicate::slot)).apply((Applicative<AttributeModifierPredicate, ?>)instance, AttributeModifierPredicate::new));

        @Override
        public boolean test(AttributeModifiersComponent.Entry entry) {
            if (this.attribute.isPresent() && !this.attribute.get().contains(entry.attribute())) {
                return false;
            }
            if (this.id.isPresent() && !this.id.get().equals(entry.modifier().id())) {
                return false;
            }
            if (!this.amount.test(entry.modifier().value())) {
                return false;
            }
            if (this.operation.isPresent() && this.operation.get() != entry.modifier().operation()) {
                return false;
            }
            return !this.slot.isPresent() || this.slot.get() == entry.slot();
        }

        @Override
        public /* synthetic */ boolean test(Object attributeModifier) {
            return this.test((AttributeModifiersComponent.Entry)attributeModifier);
        }
    }
}

