/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.util.UndashedUuid;
import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Util;

public final class Uuids {
    public static final Codec<UUID> INT_STREAM_CODEC = Codec.INT_STREAM.comapFlatMap(uuidStream -> Util.decodeFixedLengthArray(uuidStream, 4).map(Uuids::toUuid), uuid -> Arrays.stream(Uuids.toIntArray(uuid)));
    public static final Codec<Set<UUID>> SET_CODEC = Codec.list(INT_STREAM_CODEC).xmap(Sets::newHashSet, Lists::newArrayList);
    public static final Codec<Set<UUID>> LINKED_SET_CODEC = Codec.list(INT_STREAM_CODEC).xmap(Sets::newLinkedHashSet, Lists::newArrayList);
    public static final Codec<UUID> STRING_CODEC = Codec.STRING.comapFlatMap(string -> {
        try {
            return DataResult.success(UUID.fromString(string), Lifecycle.stable());
        } catch (IllegalArgumentException illegalArgumentException) {
            return DataResult.error(() -> "Invalid UUID " + string + ": " + illegalArgumentException.getMessage());
        }
    }, UUID::toString);
    public static final Codec<UUID> CODEC = Codec.withAlternative(Codec.STRING.comapFlatMap(string -> {
        try {
            return DataResult.success(UndashedUuid.fromStringLenient(string), Lifecycle.stable());
        } catch (IllegalArgumentException illegalArgumentException) {
            return DataResult.error(() -> "Invalid UUID " + string + ": " + illegalArgumentException.getMessage());
        }
    }, UndashedUuid::toString), INT_STREAM_CODEC);
    public static final Codec<UUID> STRICT_CODEC = Codec.withAlternative(INT_STREAM_CODEC, STRING_CODEC);
    public static final PacketCodec<ByteBuf, UUID> PACKET_CODEC = new PacketCodec<ByteBuf, UUID>(){

        @Override
        public UUID decode(ByteBuf byteBuf) {
            return PacketByteBuf.readUuid(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, UUID uUID) {
            PacketByteBuf.writeUuid(byteBuf, uUID);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (UUID)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final int BYTE_ARRAY_SIZE = 16;
    private static final String OFFLINE_PLAYER_UUID_PREFIX = "OfflinePlayer:";

    private Uuids() {
    }

    public static UUID toUuid(int[] array) {
        return new UUID((long)array[0] << 32 | (long)array[1] & 0xFFFFFFFFL, (long)array[2] << 32 | (long)array[3] & 0xFFFFFFFFL);
    }

    public static int[] toIntArray(UUID uuid) {
        long l = uuid.getMostSignificantBits();
        long m = uuid.getLeastSignificantBits();
        return Uuids.toIntArray(l, m);
    }

    private static int[] toIntArray(long uuidMost, long uuidLeast) {
        return new int[]{(int)(uuidMost >> 32), (int)uuidMost, (int)(uuidLeast >> 32), (int)uuidLeast};
    }

    public static byte[] toByteArray(UUID uuid) {
        byte[] bs = new byte[16];
        ByteBuffer.wrap(bs).order(ByteOrder.BIG_ENDIAN).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits());
        return bs;
    }

    public static UUID toUuid(Dynamic<?> dynamic) {
        int[] is = dynamic.asIntStream().toArray();
        if (is.length != 4) {
            throw new IllegalArgumentException("Could not read UUID. Expected int-array of length 4, got " + is.length + ".");
        }
        return Uuids.toUuid(is);
    }

    public static UUID getOfflinePlayerUuid(String nickname) {
        return UUID.nameUUIDFromBytes((OFFLINE_PLAYER_UUID_PREFIX + nickname).getBytes(StandardCharsets.UTF_8));
    }

    public static GameProfile getOfflinePlayerProfile(String nickname) {
        UUID uUID = Uuids.getOfflinePlayerUuid(nickname);
        return new GameProfile(uUID, nickname);
    }
}

