/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public record AttributeModifiersComponent(List<Entry> modifiers, boolean showInTooltip) {
    public static final AttributeModifiersComponent DEFAULT = new AttributeModifiersComponent(List.of(), true);
    private static final Codec<AttributeModifiersComponent> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Entry.CODEC.listOf().fieldOf("modifiers")).forGetter(AttributeModifiersComponent::modifiers), Codec.BOOL.optionalFieldOf("show_in_tooltip", true).forGetter(AttributeModifiersComponent::showInTooltip)).apply((Applicative<AttributeModifiersComponent, ?>)instance, AttributeModifiersComponent::new));
    public static final Codec<AttributeModifiersComponent> CODEC = Codec.withAlternative(BASE_CODEC, Entry.CODEC.listOf(), attributeModifiers -> new AttributeModifiersComponent((List<Entry>)attributeModifiers, true));
    public static final PacketCodec<RegistryByteBuf, AttributeModifiersComponent> PACKET_CODEC = PacketCodec.tuple(Entry.PACKET_CODEC.collect(PacketCodecs.toList()), AttributeModifiersComponent::modifiers, PacketCodecs.BOOL, AttributeModifiersComponent::showInTooltip, AttributeModifiersComponent::new);
    public static final DecimalFormat DECIMAL_FORMAT = Util.make(new DecimalFormat("#.##"), format -> format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT)));

    public AttributeModifiersComponent withShowInTooltip(boolean showInTooltip) {
        return new AttributeModifiersComponent(this.modifiers, showInTooltip);
    }

    public static Builder builder() {
        return new Builder();
    }

    public AttributeModifiersComponent with(RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier, AttributeModifierSlot slot) {
        ImmutableList.Builder builder = ImmutableList.builderWithExpectedSize(this.modifiers.size() + 1);
        for (Entry entry : this.modifiers) {
            if (entry.matches(attribute, modifier.id())) continue;
            builder.add(entry);
        }
        builder.add(new Entry(attribute, modifier, slot));
        return new AttributeModifiersComponent((List<Entry>)((Object)builder.build()), this.showInTooltip);
    }

    public void applyModifiers(AttributeModifierSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeConsumer) {
        for (Entry entry : this.modifiers) {
            if (!entry.slot.equals(slot)) continue;
            attributeConsumer.accept(entry.attribute, entry.modifier);
        }
    }

    public void applyModifiers(EquipmentSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeConsumer) {
        for (Entry entry : this.modifiers) {
            if (!entry.slot.matches(slot)) continue;
            attributeConsumer.accept(entry.attribute, entry.modifier);
        }
    }

    public double applyOperations(double base, EquipmentSlot slot) {
        double d = base;
        for (Entry entry : this.modifiers) {
            if (!entry.slot.matches(slot)) continue;
            double e = entry.modifier.value();
            d += (switch (entry.modifier.operation()) {
                default -> throw new MatchException(null, null);
                case EntityAttributeModifier.Operation.ADD_VALUE -> e;
                case EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE -> e * base;
                case EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL -> e * d;
            });
        }
        return d;
    }

    public static class Builder {
        private final ImmutableList.Builder<Entry> entries = ImmutableList.builder();

        Builder() {
        }

        public Builder add(RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier, AttributeModifierSlot slot) {
            this.entries.add((Object)new Entry(attribute, modifier, slot));
            return this;
        }

        public AttributeModifiersComponent build() {
            return new AttributeModifiersComponent((List<Entry>)((Object)this.entries.build()), true);
        }
    }

    public record Entry(RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier, AttributeModifierSlot slot) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)EntityAttribute.CODEC.fieldOf("type")).forGetter(Entry::attribute), EntityAttributeModifier.MAP_CODEC.forGetter(Entry::modifier), AttributeModifierSlot.CODEC.optionalFieldOf("slot", AttributeModifierSlot.ANY).forGetter(Entry::slot)).apply((Applicative<Entry, ?>)instance, Entry::new));
        public static final PacketCodec<RegistryByteBuf, Entry> PACKET_CODEC = PacketCodec.tuple(EntityAttribute.PACKET_CODEC, Entry::attribute, EntityAttributeModifier.PACKET_CODEC, Entry::modifier, AttributeModifierSlot.PACKET_CODEC, Entry::slot, Entry::new);

        public boolean matches(RegistryEntry<EntityAttribute> attribute, Identifier modifierId) {
            return attribute.equals(this.attribute) && this.modifier.idMatches(modifierId);
        }
    }
}

