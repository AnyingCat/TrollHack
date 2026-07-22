/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.data;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

public interface TrackedDataHandler<T> {
    public PacketCodec<? super RegistryByteBuf, T> codec();

    default public TrackedData<T> create(int id) {
        return new TrackedData(id, this);
    }

    public T copy(T var1);

    public static <T> TrackedDataHandler<T> create(PacketCodec<? super RegistryByteBuf, T> codec) {
        return () -> codec;
    }

    public static interface ImmutableHandler<T>
    extends TrackedDataHandler<T> {
        @Override
        default public T copy(T object) {
            return object;
        }
    }
}

