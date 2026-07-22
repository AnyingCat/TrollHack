/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry;

import io.netty.buffer.ByteBuf;
import net.minecraft.SharedConstants;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record VersionedIdentifier(String namespace, String id, String version) {
    public static final PacketCodec<ByteBuf, VersionedIdentifier> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.STRING, VersionedIdentifier::namespace, PacketCodecs.STRING, VersionedIdentifier::id, PacketCodecs.STRING, VersionedIdentifier::version, VersionedIdentifier::new);
    public static final String DEFAULT_NAMESPACE = "minecraft";

    public static VersionedIdentifier createVanilla(String path) {
        return new VersionedIdentifier(DEFAULT_NAMESPACE, path, SharedConstants.getGameVersion().getId());
    }

    public boolean isVanilla() {
        return this.namespace.equals(DEFAULT_NAMESPACE);
    }

    @Override
    public String toString() {
        return this.namespace + ":" + this.id + ":" + this.version;
    }
}

