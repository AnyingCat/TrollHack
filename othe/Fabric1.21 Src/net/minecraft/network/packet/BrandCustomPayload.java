/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record BrandCustomPayload(String brand) implements CustomPayload
{
    public static final PacketCodec<PacketByteBuf, BrandCustomPayload> CODEC = CustomPayload.codecOf(BrandCustomPayload::write, BrandCustomPayload::new);
    public static final CustomPayload.Id<BrandCustomPayload> ID = CustomPayload.id("brand");

    private BrandCustomPayload(PacketByteBuf buf) {
        this(buf.readString());
    }

    private void write(PacketByteBuf buf) {
        buf.writeString(this.brand);
    }

    public CustomPayload.Id<BrandCustomPayload> getId() {
        return ID;
    }
}

