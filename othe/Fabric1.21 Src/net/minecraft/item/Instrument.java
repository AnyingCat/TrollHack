/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.dynamic.Codecs;

public record Instrument(RegistryEntry<SoundEvent> soundEvent, int useDuration, float range) {
    public static final Codec<Instrument> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)SoundEvent.ENTRY_CODEC.fieldOf("sound_event")).forGetter(Instrument::soundEvent), ((MapCodec)Codecs.POSITIVE_INT.fieldOf("use_duration")).forGetter(Instrument::useDuration), ((MapCodec)Codecs.POSITIVE_FLOAT.fieldOf("range")).forGetter(Instrument::range)).apply((Applicative<Instrument, ?>)instance, Instrument::new));
    public static final PacketCodec<RegistryByteBuf, Instrument> PACKET_CODEC = PacketCodec.tuple(SoundEvent.ENTRY_PACKET_CODEC, Instrument::soundEvent, PacketCodecs.VAR_INT, Instrument::useDuration, PacketCodecs.FLOAT, Instrument::range, Instrument::new);
    public static final Codec<RegistryEntry<Instrument>> ENTRY_CODEC = RegistryElementCodec.of(RegistryKeys.INSTRUMENT, CODEC);
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<Instrument>> ENTRY_PACKET_CODEC = PacketCodecs.registryEntry(RegistryKeys.INSTRUMENT, PACKET_CODEC);
}

