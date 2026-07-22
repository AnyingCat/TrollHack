/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Util;

public record SuspiciousStewEffectsComponent(List<StewEffect> effects) {
    public static final SuspiciousStewEffectsComponent DEFAULT = new SuspiciousStewEffectsComponent(List.of());
    public static final Codec<SuspiciousStewEffectsComponent> CODEC = StewEffect.CODEC.listOf().xmap(SuspiciousStewEffectsComponent::new, SuspiciousStewEffectsComponent::effects);
    public static final PacketCodec<RegistryByteBuf, SuspiciousStewEffectsComponent> PACKET_CODEC = StewEffect.PACKET_CODEC.collect(PacketCodecs.toList()).xmap(SuspiciousStewEffectsComponent::new, SuspiciousStewEffectsComponent::effects);

    public SuspiciousStewEffectsComponent with(StewEffect stewEffect) {
        return new SuspiciousStewEffectsComponent(Util.withAppended(this.effects, stewEffect));
    }

    public record StewEffect(RegistryEntry<StatusEffect> effect, int duration) {
        public static final Codec<StewEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)StatusEffect.ENTRY_CODEC.fieldOf("id")).forGetter(StewEffect::effect), Codec.INT.lenientOptionalFieldOf("duration", 160).forGetter(StewEffect::duration)).apply((Applicative<StewEffect, ?>)instance, StewEffect::new));
        public static final PacketCodec<RegistryByteBuf, StewEffect> PACKET_CODEC = PacketCodec.tuple(StatusEffect.ENTRY_PACKET_CODEC, StewEffect::effect, PacketCodecs.VAR_INT, StewEffect::duration, StewEffect::new);

        public StatusEffectInstance createStatusEffectInstance() {
            return new StatusEffectInstance(this.effect, this.duration);
        }
    }
}

