/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.advancement;

import java.util.List;
import net.minecraft.advancement.Advancement;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public record AdvancementEntry(Identifier id, Advancement value) {
    public static final PacketCodec<RegistryByteBuf, AdvancementEntry> PACKET_CODEC = PacketCodec.tuple(Identifier.PACKET_CODEC, AdvancementEntry::id, Advancement.PACKET_CODEC, AdvancementEntry::value, AdvancementEntry::new);
    public static final PacketCodec<RegistryByteBuf, List<AdvancementEntry>> LIST_PACKET_CODEC = PACKET_CODEC.collect(PacketCodecs.toList());

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AdvancementEntry)) return false;
        AdvancementEntry advancementEntry = (AdvancementEntry)o;
        if (!this.id.equals(advancementEntry.id)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return this.id.toString();
    }
}

