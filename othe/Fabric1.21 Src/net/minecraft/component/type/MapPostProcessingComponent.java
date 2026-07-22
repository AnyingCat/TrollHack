/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.function.ValueLists;

public enum MapPostProcessingComponent {
    LOCK(0),
    SCALE(1);

    public static final IntFunction<MapPostProcessingComponent> ID_TO_VALUE;
    public static final PacketCodec<ByteBuf, MapPostProcessingComponent> PACKET_CODEC;
    private final int id;

    private MapPostProcessingComponent(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    static {
        ID_TO_VALUE = ValueLists.createIdToValueFunction(MapPostProcessingComponent::getId, MapPostProcessingComponent.values(), ValueLists.OutOfBoundsHandling.ZERO);
        PACKET_CODEC = PacketCodecs.indexed(ID_TO_VALUE, MapPostProcessingComponent::getId);
    }
}

