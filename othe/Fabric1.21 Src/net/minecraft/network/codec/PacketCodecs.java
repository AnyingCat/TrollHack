/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.codec;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtEnd;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.encoding.StringEncoding;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.network.encoding.VarLongs;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.collection.IndexedIterable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A set of pre-defined packet codecs.
 * 
 * @see PacketCodec
 */
public interface PacketCodecs {
    public static final int field_49674 = 65536;
    /**
     * A codec for a boolean value.
     * 
     * @see io.netty.buffer.ByteBuf#readBoolean
     * @see io.netty.buffer.ByteBuf#writeBoolean
     */
    public static final PacketCodec<ByteBuf, Boolean> BOOL = new PacketCodec<ByteBuf, Boolean>(){

        @Override
        public Boolean decode(ByteBuf byteBuf) {
            return byteBuf.readBoolean();
        }

        @Override
        public void encode(ByteBuf byteBuf, Boolean boolean_) {
            byteBuf.writeBoolean(boolean_);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Boolean)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    /**
     * A codec for a byte value.
     * 
     * @see io.netty.buffer.ByteBuf#readByte
     * @see io.netty.buffer.ByteBuf#writeByte
     */
    public static final PacketCodec<ByteBuf, Byte> BYTE = new PacketCodec<ByteBuf, Byte>(){

        @Override
        public Byte decode(ByteBuf byteBuf) {
            return byteBuf.readByte();
        }

        @Override
        public void encode(ByteBuf byteBuf, Byte byte_) {
            byteBuf.writeByte(byte_.byteValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Byte)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    /**
     * A codec for a short value.
     * 
     * @see io.netty.buffer.ByteBuf#readShort
     * @see io.netty.buffer.ByteBuf#writeShort
     */
    public static final PacketCodec<ByteBuf, Short> SHORT = new PacketCodec<ByteBuf, Short>(){

        @Override
        public Short decode(ByteBuf byteBuf) {
            return byteBuf.readShort();
        }

        @Override
        public void encode(ByteBuf byteBuf, Short short_) {
            byteBuf.writeShort(short_.shortValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Short)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    /**
     * A codec for an unsigned short value.
     * 
     * @see io.netty.buffer.ByteBuf#readUnsignedShort
     * @see io.netty.buffer.ByteBuf#writeShort
     */
    public static final PacketCodec<ByteBuf, Integer> UNSIGNED_SHORT = new PacketCodec<ByteBuf, Integer>(){

        @Override
        public Integer decode(ByteBuf byteBuf) {
            return byteBuf.readUnsignedShort();
        }

        @Override
        public void encode(ByteBuf byteBuf, Integer integer) {
            byteBuf.writeShort(integer);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Integer)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    /**
     * A codec for an integer value.
     * 
     * @see io.netty.buffer.ByteBuf#readInt
     * @see io.netty.buffer.ByteBuf#writeInt
     */
    public static final PacketCodec<ByteBuf, Integer> INTEGER = new PacketCodec<ByteBuf, Integer>(){

        @Override
        public Integer decode(ByteBuf byteBuf) {
            return byteBuf.readInt();
        }

        @Override
        public void encode(ByteBuf byteBuf, Integer integer) {
            byteBuf.writeInt(integer);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Integer)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    /**
     * A codec for a variable-length integer (var int) value.
     * 
     * @see net.minecraft.network.PacketByteBuf#readVarInt
     * @see net.minecraft.network.PacketByteBuf#writeVarInt
     */
    public static final PacketCodec<ByteBuf, Integer> VAR_INT = new PacketCodec<ByteBuf, Integer>(){

        @Override
        public Integer decode(ByteBuf byteBuf) {
            return VarInts.read(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, Integer integer) {
            VarInts.write(byteBuf, integer);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Integer)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    /**
     * A codec for a variable-length long (var long) value.
     * 
     * @see net.minecraft.network.PacketByteBuf#readVarLong
     * @see net.minecraft.network.PacketByteBuf#writeVarLong
     */
    public static final PacketCodec<ByteBuf, Long> VAR_LONG = new PacketCodec<ByteBuf, Long>(){

        @Override
        public Long decode(ByteBuf byteBuf) {
            return VarLongs.read(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, Long long_) {
            VarLongs.write(byteBuf, long_);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Long)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    /**
     * A codec for a float value.
     * 
     * @see io.netty.buffer.ByteBuf#readFloat
     * @see io.netty.buffer.ByteBuf#writeFloat
     */
    public static final PacketCodec<ByteBuf, Float> FLOAT = new PacketCodec<ByteBuf, Float>(){

        @Override
        public Float decode(ByteBuf byteBuf) {
            return Float.valueOf(byteBuf.readFloat());
        }

        @Override
        public void encode(ByteBuf byteBuf, Float float_) {
            byteBuf.writeFloat(float_.floatValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Float)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    /**
     * A codec for a double value.
     * 
     * @see io.netty.buffer.ByteBuf#readDouble
     * @see io.netty.buffer.ByteBuf#writeDouble
     */
    public static final PacketCodec<ByteBuf, Double> DOUBLE = new PacketCodec<ByteBuf, Double>(){

        @Override
        public Double decode(ByteBuf byteBuf) {
            return byteBuf.readDouble();
        }

        @Override
        public void encode(ByteBuf byteBuf, Double double_) {
            byteBuf.writeDouble(double_);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Double)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    /**
     * A codec for a byte array.
     * 
     * @see net.minecraft.network.PacketByteBuf#readByteArray()
     * @see net.minecraft.network.PacketByteBuf#writeByteArray(byte[])
     */
    public static final PacketCodec<ByteBuf, byte[]> BYTE_ARRAY = new PacketCodec<ByteBuf, byte[]>(){

        public byte[] method_59799(ByteBuf byteBuf) {
            return PacketByteBuf.readByteArray(byteBuf);
        }

        public void method_59800(ByteBuf byteBuf, byte[] bs) {
            PacketByteBuf.writeByteArray(byteBuf, bs);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.method_59800((ByteBuf)object, (byte[])object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.method_59799((ByteBuf)object);
        }
    };
    /**
     * A codec for a string value with maximum length {@value Short#MAX_VALUE}.
     * 
     * @see #string
     * @see net.minecraft.network.PacketByteBuf#readString()
     * @see net.minecraft.network.PacketByteBuf#writeString(String)
     */
    public static final PacketCodec<ByteBuf, String> STRING = PacketCodecs.string(Short.MAX_VALUE);
    /**
     * A codec for an NBT element of up to {@code 0x200000L} bytes.
     * 
     * @see #nbt
     * @see net.minecraft.network.PacketByteBuf#readNbt(NbtSizeTracker)
     * @see net.minecraft.network.PacketByteBuf#writeNbt(NbtElement)
     */
    public static final PacketCodec<ByteBuf, NbtElement> NBT_ELEMENT = PacketCodecs.nbt(() -> NbtSizeTracker.of(0x200000L));
    /**
     * A codec for an NBT element of unlimited size.
     * 
     * @see #nbt
     * @see net.minecraft.network.PacketByteBuf#readNbt(NbtSizeTracker)
     * @see net.minecraft.network.PacketByteBuf#writeNbt(NbtElement)
     */
    public static final PacketCodec<ByteBuf, NbtElement> UNLIMITED_NBT_ELEMENT = PacketCodecs.nbt(NbtSizeTracker::ofUnlimitedBytes);
    /**
     * A codec for an NBT compound of up to {@code 0x200000L} bytes.
     * 
     * @see #nbt
     * @see net.minecraft.network.PacketByteBuf#readNbt(NbtSizeTracker)
     * @see net.minecraft.network.PacketByteBuf#writeNbt(NbtElement)
     */
    public static final PacketCodec<ByteBuf, NbtCompound> NBT_COMPOUND = PacketCodecs.nbtCompound(() -> NbtSizeTracker.of(0x200000L));
    /**
     * A codec for an NBT compound of unlimited size.
     * 
     * @see #nbt
     * @see net.minecraft.network.PacketByteBuf#readNbt(NbtSizeTracker)
     * @see net.minecraft.network.PacketByteBuf#writeNbt(NbtElement)
     */
    public static final PacketCodec<ByteBuf, NbtCompound> UNLIMITED_NBT_COMPOUND = PacketCodecs.nbtCompound(NbtSizeTracker::ofUnlimitedBytes);
    /**
     * A codec for an optional NBT compound of up to {@value
     * net.minecraft.network.PacketByteBuf#MAX_READ_NBT_SIZE} bytes.
     * 
     * @see #nbt
     * @see net.minecraft.network.PacketByteBuf#readNbt(PacketByteBuf)
     * @see net.minecraft.network.PacketByteBuf#writeNbt(io.netty.buffer.ByteBuf, NbtElement)
     */
    public static final PacketCodec<ByteBuf, Optional<NbtCompound>> OPTIONAL_NBT = new PacketCodec<ByteBuf, Optional<NbtCompound>>(){

        @Override
        public Optional<NbtCompound> decode(ByteBuf byteBuf) {
            return Optional.ofNullable(PacketByteBuf.readNbt(byteBuf));
        }

        @Override
        public void encode(ByteBuf byteBuf, Optional<NbtCompound> optional) {
            PacketByteBuf.writeNbt(byteBuf, optional.orElse(null));
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Optional)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    /**
     * A codec for a {@link org.joml.Vector3f}.
     * 
     * @see net.minecraft.network.PacketByteBuf#readVector3f()
     * @see net.minecraft.network.PacketByteBuf#writeVector3f(Vector3f)
     */
    public static final PacketCodec<ByteBuf, Vector3f> VECTOR3F = new PacketCodec<ByteBuf, Vector3f>(){

        @Override
        public Vector3f decode(ByteBuf byteBuf) {
            return PacketByteBuf.readVector3f(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, Vector3f vector3f) {
            PacketByteBuf.writeVector3f(byteBuf, vector3f);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Vector3f)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    /**
     * A codec for a {@link org.joml.Quaternionf}.
     * 
     * @see net.minecraft.network.PacketByteBuf#readQuaternionf()
     * @see net.minecraft.network.PacketByteBuf#writeQuaternionf(Quaternionf)
     */
    public static final PacketCodec<ByteBuf, Quaternionf> QUATERNIONF = new PacketCodec<ByteBuf, Quaternionf>(){

        @Override
        public Quaternionf decode(ByteBuf byteBuf) {
            return PacketByteBuf.readQuaternionf(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, Quaternionf quaternionf) {
            PacketByteBuf.writeQuaternionf(byteBuf, quaternionf);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Quaternionf)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, PropertyMap> PROPERTY_MAP = new PacketCodec<ByteBuf, PropertyMap>(){
        private static final int NAME_MAX_LENGTH = 64;
        private static final int VALUE_MAX_LENGTH = Short.MAX_VALUE;
        private static final int SIGNATURE_MAX_LENGTH = 1024;
        private static final int MAP_MAX_SIZE = 16;

        @Override
        public PropertyMap decode(ByteBuf byteBuf) {
            int i = PacketCodecs.readCollectionSize(byteBuf, 16);
            PropertyMap propertyMap = new PropertyMap();
            for (int j = 0; j < i; ++j) {
                String string = StringEncoding.decode(byteBuf, 64);
                String string2 = StringEncoding.decode(byteBuf, Short.MAX_VALUE);
                String string3 = PacketByteBuf.readNullable(byteBuf, buf2 -> StringEncoding.decode(buf2, 1024));
                Property property = new Property(string, string2, string3);
                propertyMap.put(property.name(), property);
            }
            return propertyMap;
        }

        @Override
        public void encode(ByteBuf byteBuf, PropertyMap propertyMap) {
            PacketCodecs.writeCollectionSize(byteBuf, propertyMap.size(), 16);
            for (Property property : propertyMap.values()) {
                StringEncoding.encode(byteBuf, property.name(), 64);
                StringEncoding.encode(byteBuf, property.value(), Short.MAX_VALUE);
                PacketByteBuf.writeNullable(byteBuf, property.signature(), (buf2, signature) -> StringEncoding.encode(buf2, signature, 1024));
            }
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (PropertyMap)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final PacketCodec<ByteBuf, GameProfile> GAME_PROFILE = new PacketCodec<ByteBuf, GameProfile>(){

        @Override
        public GameProfile decode(ByteBuf byteBuf) {
            UUID uUID = (UUID)Uuids.PACKET_CODEC.decode(byteBuf);
            String string = StringEncoding.decode(byteBuf, 16);
            GameProfile gameProfile = new GameProfile(uUID, string);
            gameProfile.getProperties().putAll((Multimap)PROPERTY_MAP.decode(byteBuf));
            return gameProfile;
        }

        @Override
        public void encode(ByteBuf byteBuf, GameProfile gameProfile) {
            Uuids.PACKET_CODEC.encode(byteBuf, gameProfile.getId());
            StringEncoding.encode(byteBuf, gameProfile.getName(), 16);
            PROPERTY_MAP.encode(byteBuf, gameProfile.getProperties());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (GameProfile)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };

    /**
     * {@return a codec for a byte array with maximum length {@code maxLength}}
     * 
     * @see #BYTE_ARRAY
     * @see net.minecraft.network.PacketByteBuf#readByteArray(ByteBuf, int)
     * @see net.minecraft.network.PacketByteBuf#writeByteArray(ByteBuf, byte[])
     */
    public static PacketCodec<ByteBuf, byte[]> byteArray(final int maxLength) {
        return new PacketCodec<ByteBuf, byte[]>(){

            @Override
            public byte[] decode(ByteBuf buf) {
                return PacketByteBuf.readByteArray(buf, maxLength);
            }

            @Override
            public void encode(ByteBuf byteBuf, byte[] bs) {
                if (bs.length > maxLength) {
                    throw new EncoderException("ByteArray with size " + bs.length + " is bigger than allowed " + maxLength);
                }
                PacketByteBuf.writeByteArray(byteBuf, bs);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, (byte[])object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    /**
     * {@return a codec for a string value with maximum length {@code maxLength}}
     * 
     * @see #STRING
     * @see net.minecraft.network.PacketByteBuf#readString(int)
     * @see net.minecraft.network.PacketByteBuf#writeString(String, int)
     */
    public static PacketCodec<ByteBuf, String> string(final int maxLength) {
        return new PacketCodec<ByteBuf, String>(){

            @Override
            public String decode(ByteBuf byteBuf) {
                return StringEncoding.decode(byteBuf, maxLength);
            }

            @Override
            public void encode(ByteBuf byteBuf, String string) {
                StringEncoding.encode(byteBuf, string, maxLength);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, (String)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    /**
     * {@return a codec for an NBT element}
     * 
     * @see #NBT_ELEMENT
     * @see net.minecraft.network.PacketByteBuf#readNbt(NbtSizeTracker)
     * @see net.minecraft.network.PacketByteBuf#writeNbt(NbtElement)
     */
    public static PacketCodec<ByteBuf, NbtElement> nbt(final Supplier<NbtSizeTracker> sizeTracker) {
        return new PacketCodec<ByteBuf, NbtElement>(){

            @Override
            public NbtElement decode(ByteBuf byteBuf) {
                NbtElement nbtElement = PacketByteBuf.readNbt(byteBuf, (NbtSizeTracker)sizeTracker.get());
                if (nbtElement == null) {
                    throw new DecoderException("Expected non-null compound tag");
                }
                return nbtElement;
            }

            @Override
            public void encode(ByteBuf byteBuf, NbtElement nbtElement) {
                if (nbtElement == NbtEnd.INSTANCE) {
                    throw new EncoderException("Expected non-null compound tag");
                }
                PacketByteBuf.writeNbt(byteBuf, nbtElement);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, (NbtElement)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    public static PacketCodec<ByteBuf, NbtCompound> nbtCompound(Supplier<NbtSizeTracker> sizeTracker) {
        return PacketCodecs.nbt(sizeTracker).xmap(nbt -> {
            if (nbt instanceof NbtCompound) {
                NbtCompound nbtCompound = (NbtCompound)nbt;
                return nbtCompound;
            }
            throw new DecoderException("Not a compound tag: " + String.valueOf(nbt));
        }, nbt -> nbt);
    }

    /**
     * {@return a codec from DataFixerUpper codec {@code codec}}
     * 
     * <p>Internally, the data is serialized as an NBT element of unlimited size.
     */
    public static <T> PacketCodec<ByteBuf, T> unlimitedCodec(Codec<T> codec) {
        return PacketCodecs.codec(codec, NbtSizeTracker::ofUnlimitedBytes);
    }

    /**
     * {@return a codec from DataFixerUpper codec {@code codec}}
     * 
     * <p>Internally, the data is serialized as an NBT element of up to {@code 200000L}
     * bytes.
     */
    public static <T> PacketCodec<ByteBuf, T> codec(Codec<T> codec) {
        return PacketCodecs.codec(codec, () -> NbtSizeTracker.of(0x200000L));
    }

    public static <T> PacketCodec<ByteBuf, T> codec(Codec<T> codec, Supplier<NbtSizeTracker> sizeTracker) {
        return PacketCodecs.nbt(sizeTracker).xmap(nbt -> codec.parse(NbtOps.INSTANCE, nbt).getOrThrow(error -> new DecoderException("Failed to decode: " + error + " " + String.valueOf(nbt))), value -> codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow(error -> new EncoderException("Failed to encode: " + error + " " + String.valueOf(value))));
    }

    public static <T> PacketCodec<RegistryByteBuf, T> unlimitedRegistryCodec(Codec<T> codec) {
        return PacketCodecs.registryCodec(codec, NbtSizeTracker::ofUnlimitedBytes);
    }

    public static <T> PacketCodec<RegistryByteBuf, T> registryCodec(Codec<T> codec) {
        return PacketCodecs.registryCodec(codec, () -> NbtSizeTracker.of(0x200000L));
    }

    public static <T> PacketCodec<RegistryByteBuf, T> registryCodec(final Codec<T> codec, Supplier<NbtSizeTracker> sizeTracker) {
        final PacketCodec<ByteBuf, NbtElement> packetCodec = PacketCodecs.nbt(sizeTracker);
        return new PacketCodec<RegistryByteBuf, T>(){

            @Override
            public T decode(RegistryByteBuf registryByteBuf) {
                NbtElement nbtElement = (NbtElement)packetCodec.decode(registryByteBuf);
                RegistryOps<NbtElement> registryOps = registryByteBuf.getRegistryManager().getOps(NbtOps.INSTANCE);
                return codec.parse(registryOps, nbtElement).getOrThrow(error -> new DecoderException("Failed to decode: " + error + " " + String.valueOf(nbtElement)));
            }

            @Override
            public void encode(RegistryByteBuf registryByteBuf, T object) {
                RegistryOps<NbtElement> registryOps = registryByteBuf.getRegistryManager().getOps(NbtOps.INSTANCE);
                NbtElement nbtElement = codec.encodeStart(registryOps, object).getOrThrow(error -> new EncoderException("Failed to encode: " + error + " " + String.valueOf(object)));
                packetCodec.encode(registryByteBuf, nbtElement);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryByteBuf)object, object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryByteBuf)object);
            }
        };
    }

    /**
     * {@return a codec wrapping another codec, the value of which is optional}
     * 
     * <p>This can be used with {@link PacketCodec#collect} like
     * {@code codec.collect(PacketCodecs::optional)}.
     * 
     * @see net.minecraft.network.PacketByteBuf#readOptional
     * @see net.minecraft.network.PacketByteBuf#writeOptional
     */
    public static <B extends ByteBuf, V> PacketCodec<B, Optional<V>> optional(final PacketCodec<B, V> codec) {
        return new PacketCodec<B, Optional<V>>(){

            @Override
            public Optional<V> decode(B byteBuf) {
                if (((ByteBuf)byteBuf).readBoolean()) {
                    return Optional.of(codec.decode(byteBuf));
                }
                return Optional.empty();
            }

            @Override
            public void encode(B byteBuf, Optional<V> optional) {
                if (optional.isPresent()) {
                    ((ByteBuf)byteBuf).writeBoolean(true);
                    codec.encode(byteBuf, optional.get());
                } else {
                    ((ByteBuf)byteBuf).writeBoolean(false);
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((Object)((ByteBuf)object), (Optional)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static int readCollectionSize(ByteBuf buf, int maxSize) {
        int i = VarInts.read(buf);
        if (i > maxSize) {
            throw new DecoderException(i + " elements exceeded max size of: " + maxSize);
        }
        return i;
    }

    public static void writeCollectionSize(ByteBuf buf, int size, int maxSize) {
        if (size > maxSize) {
            throw new EncoderException(size + " elements exceeded max size of: " + maxSize);
        }
        VarInts.write(buf, size);
    }

    /**
     * {@return a codec for a collection of values}
     * 
     * @see net.minecraft.network.PacketByteBuf#readCollection
     * @see net.minecraft.network.PacketByteBuf#writeCollection
     * 
     * @param elementCodec the codec of the collection's elements
     * @param factory a function that, given the collection's size, returns a new empty collection
     */
    public static <B extends ByteBuf, V, C extends Collection<V>> PacketCodec<B, C> collection(IntFunction<C> factory, PacketCodec<? super B, V> elementCodec) {
        return PacketCodecs.collection(factory, elementCodec, Integer.MAX_VALUE);
    }

    public static <B extends ByteBuf, V, C extends Collection<V>> PacketCodec<B, C> collection(final IntFunction<C> factory, final PacketCodec<? super B, V> elementCodec, final int maxSize) {
        return new PacketCodec<B, C>(){

            @Override
            public C decode(B byteBuf) {
                int i = PacketCodecs.readCollectionSize(byteBuf, maxSize);
                Collection collection = (Collection)factory.apply(Math.min(i, 65536));
                for (int j = 0; j < i; ++j) {
                    collection.add(elementCodec.decode(byteBuf));
                }
                return collection;
            }

            @Override
            public void encode(B byteBuf, C collection) {
                PacketCodecs.writeCollectionSize(byteBuf, collection.size(), maxSize);
                for (Object object : collection) {
                    elementCodec.encode(byteBuf, object);
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((B)((ByteBuf)object), (C)((Collection)object2));
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    /**
     * Used to make a codec for a collection of values using {@link PacketCodec#collect}.
     * 
     * <p>For example, to make a codec for a set of values, write {@code
     * codec.collect(PacketCodecs.toCollection(HashSet::new))}.
     * 
     * @see #toList
     * 
     * @param collectionFactory a function that, given the collection's size, returns a new empty collection
     */
    public static <B extends ByteBuf, V, C extends Collection<V>> PacketCodec.ResultFunction<B, V, C> toCollection(IntFunction<C> collectionFactory) {
        return codec -> PacketCodecs.collection(collectionFactory, codec);
    }

    /**
     * Used to make a codec for a list of values using {@link PacketCodec#collect}.
     * This creates an {@link java.util.ArrayList}, so the decoded result can be modified.
     * 
     * <p>For example, to make a codec for a list of values, write {@code
     * codec.collect(PacketCodecs.toList())}.
     * 
     * @see #toCollection
     */
    public static <B extends ByteBuf, V> PacketCodec.ResultFunction<B, V, List<V>> toList() {
        return codec -> PacketCodecs.collection(ArrayList::new, codec);
    }

    public static <B extends ByteBuf, V> PacketCodec.ResultFunction<B, V, List<V>> toList(int maxLength) {
        return codec -> PacketCodecs.collection(ArrayList::new, codec, maxLength);
    }

    /**
     * {@return a codec for a map}
     * 
     * @see net.minecraft.network.PacketByteBuf#readMap(IntFunction, PacketDecoder, PacketDecoder)
     * @see net.minecraft.network.PacketByteBuf#writeMap(java.util.Map, PacketEncoder, PacketEncoder)
     * 
     * @param factory a function that, given the map's size, returns a new empty map
     * @param keyCodec the codec for the map's keys
     * @param valueCodec the codec for the map's values
     */
    public static <B extends ByteBuf, K, V, M extends Map<K, V>> PacketCodec<B, M> map(IntFunction<? extends M> factory, PacketCodec<? super B, K> keyCodec, PacketCodec<? super B, V> valueCodec) {
        return PacketCodecs.map(factory, keyCodec, valueCodec, Integer.MAX_VALUE);
    }

    public static <B extends ByteBuf, K, V, M extends Map<K, V>> PacketCodec<B, M> map(final IntFunction<? extends M> factory, final PacketCodec<? super B, K> keyCodec, final PacketCodec<? super B, V> valueCodec, final int maxSize) {
        return new PacketCodec<B, M>(){

            @Override
            public void encode(B byteBuf, M map) {
                PacketCodecs.writeCollectionSize(byteBuf, map.size(), maxSize);
                map.forEach((k, v) -> {
                    keyCodec.encode(byteBuf, k);
                    valueCodec.encode(byteBuf, v);
                });
            }

            @Override
            public M decode(B byteBuf) {
                int i = PacketCodecs.readCollectionSize(byteBuf, maxSize);
                Map map = (Map)factory.apply(Math.min(i, 65536));
                for (int j = 0; j < i; ++j) {
                    Object object = keyCodec.decode(byteBuf);
                    Object object2 = valueCodec.decode(byteBuf);
                    map.put(object, object2);
                }
                return map;
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((B)((ByteBuf)object), (M)((Map)object2));
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static <B extends ByteBuf, L, R> PacketCodec<B, Either<L, R>> either(final PacketCodec<? super B, L> left, final PacketCodec<? super B, R> right) {
        return new PacketCodec<B, Either<L, R>>(){

            @Override
            public Either<L, R> decode(B byteBuf) {
                if (((ByteBuf)byteBuf).readBoolean()) {
                    return Either.left(left.decode(byteBuf));
                }
                return Either.right(right.decode(byteBuf));
            }

            @Override
            public void encode(B byteBuf, Either<L, R> either) {
                either.ifLeft(left -> {
                    byteBuf.writeBoolean(true);
                    left.encode(byteBuf, left);
                }).ifRight(right -> {
                    byteBuf.writeBoolean(false);
                    right.encode(byteBuf, right);
                });
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((Object)((ByteBuf)object), (Either)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    /**
     * {@return a codec for an indexed value}
     * 
     * <p>An example of an indexed value is an enum.
     * 
     * @see net.minecraft.util.function.ValueLists
     * @see net.minecraft.network.PacketByteBuf#encode(ToIntFunction, Object)
     * @see net.minecraft.network.PacketByteBuf#decode(IntFunction)
     * 
     * @param valueToIndex a function that gets a value's index
     * @param indexToValue a function that gets a value from its index
     */
    public static <T> PacketCodec<ByteBuf, T> indexed(final IntFunction<T> indexToValue, final ToIntFunction<T> valueToIndex) {
        return new PacketCodec<ByteBuf, T>(){

            @Override
            public T decode(ByteBuf byteBuf) {
                int i = VarInts.read(byteBuf);
                return indexToValue.apply(i);
            }

            @Override
            public void encode(ByteBuf byteBuf, T object) {
                int i = valueToIndex.applyAsInt(object);
                VarInts.write(byteBuf, i);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    /**
     * {@return a codec for an entry of {@code iterable}}
     * 
     * @see #indexed
     */
    public static <T> PacketCodec<ByteBuf, T> entryOf(IndexedIterable<T> iterable) {
        return PacketCodecs.indexed(iterable::getOrThrow, iterable::getRawIdOrThrow);
    }

    private static <T, R> PacketCodec<RegistryByteBuf, R> registry(final RegistryKey<? extends Registry<T>> registry, final Function<Registry<T>, IndexedIterable<R>> registryTransformer) {
        return new PacketCodec<RegistryByteBuf, R>(){

            private IndexedIterable<R> getIterable(RegistryByteBuf buf) {
                return (IndexedIterable)registryTransformer.apply(buf.getRegistryManager().get(registry));
            }

            @Override
            public R decode(RegistryByteBuf registryByteBuf) {
                int i = VarInts.read(registryByteBuf);
                return this.getIterable(registryByteBuf).getOrThrow(i);
            }

            @Override
            public void encode(RegistryByteBuf registryByteBuf, R object) {
                int i = this.getIterable(registryByteBuf).getRawIdOrThrow(object);
                VarInts.write(registryByteBuf, i);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryByteBuf)object, object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryByteBuf)object);
            }
        };
    }

    /**
     * {@return a codec for a {@link net.minecraft.registry.Registry}-registered value}
     * 
     * <p>This codec only works with {@link net.minecraft.network.RegistryByteBuf}, used
     * during the play phase. Consider using {@link #entryOf} for encoding a value of a
     * static registry during login or configuration phases.
     * 
     * @implNote The value is serialized as the corresponding raw ID (as {@link #VAR_INT
     * a var int}).
     * 
     * @see #entryOf
     */
    public static <T> PacketCodec<RegistryByteBuf, T> registryValue(RegistryKey<? extends Registry<T>> registry2) {
        return PacketCodecs.registry(registry2, registry -> registry);
    }

    /**
     * {@return a codec for a reference {@link net.minecraft.registry.entry.RegistryEntry}}
     * 
     * <p>This codec only works with {@link net.minecraft.network.RegistryByteBuf}, used
     * during the play phase. Consider using {@link #entryOf} for encoding a value of a
     * static registry during login or configuration phases.
     * 
     * @implNote The value is serialized as the corresponding raw ID (as {@link #VAR_INT
     * a var int}). This does not handle direct (unregistered) entries.
     * 
     * @see #registryValue
     * @see #registryEntry(RegistryKey, PacketCodec)
     */
    public static <T> PacketCodec<RegistryByteBuf, RegistryEntry<T>> registryEntry(RegistryKey<? extends Registry<T>> registry) {
        return PacketCodecs.registry(registry, Registry::getIndexedEntries);
    }

    /**
     * {@return a codec for a {@link net.minecraft.registry.entry.RegistryEntry}}
     * 
     * <p>This codec only works with {@link net.minecraft.network.RegistryByteBuf}, used
     * during the play phase. Consider using {@link #entryOf} for encoding a value of a
     * static registry during login or configuration phases.
     * 
     * @implNote If the entry is a reference entry, the value is serialized as the
     * corresponding raw ID (as {@link #VAR_INT a var int}). If it is a direct entry,
     * it is encoded using {@code directCodec}.
     * 
     * @see #registryValue
     * @see #registryEntry(RegistryKey)
     */
    public static <T> PacketCodec<RegistryByteBuf, RegistryEntry<T>> registryEntry(final RegistryKey<? extends Registry<T>> registry, final PacketCodec<? super RegistryByteBuf, T> directCodec) {
        return new PacketCodec<RegistryByteBuf, RegistryEntry<T>>(){
            private static final int DIRECT_ENTRY_MARKER = 0;

            private IndexedIterable<RegistryEntry<T>> getEntries(RegistryByteBuf buf) {
                return buf.getRegistryManager().get(registry).getIndexedEntries();
            }

            @Override
            public RegistryEntry<T> decode(RegistryByteBuf registryByteBuf) {
                int i = VarInts.read(registryByteBuf);
                if (i == 0) {
                    return RegistryEntry.of(directCodec.decode(registryByteBuf));
                }
                return this.getEntries(registryByteBuf).getOrThrow(i - 1);
            }

            @Override
            public void encode(RegistryByteBuf registryByteBuf, RegistryEntry<T> registryEntry) {
                switch (registryEntry.getType()) {
                    case REFERENCE: {
                        int i = this.getEntries(registryByteBuf).getRawIdOrThrow(registryEntry);
                        VarInts.write(registryByteBuf, i + 1);
                        break;
                    }
                    case DIRECT: {
                        VarInts.write(registryByteBuf, 0);
                        directCodec.encode(registryByteBuf, registryEntry.value());
                    }
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryByteBuf)object, (RegistryEntry)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryByteBuf)object);
            }
        };
    }

    public static <T> PacketCodec<RegistryByteBuf, RegistryEntryList<T>> registryEntryList(final RegistryKey<? extends Registry<T>> registryRef) {
        return new PacketCodec<RegistryByteBuf, RegistryEntryList<T>>(){
            private static final int DIRECT_MARKER = -1;
            private final PacketCodec<RegistryByteBuf, RegistryEntry<T>> entryPacketCodec;
            {
                this.entryPacketCodec = PacketCodecs.registryEntry(registryRef);
            }

            @Override
            public RegistryEntryList<T> decode(RegistryByteBuf registryByteBuf) {
                int i = VarInts.read(registryByteBuf) - 1;
                if (i == -1) {
                    Registry registry = registryByteBuf.getRegistryManager().get(registryRef);
                    return registry.getEntryList(TagKey.of(registryRef, (Identifier)Identifier.PACKET_CODEC.decode(registryByteBuf))).orElseThrow();
                }
                ArrayList<RegistryEntry> list = new ArrayList<RegistryEntry>(Math.min(i, 65536));
                for (int j = 0; j < i; ++j) {
                    list.add((RegistryEntry)this.entryPacketCodec.decode(registryByteBuf));
                }
                return RegistryEntryList.of(list);
            }

            @Override
            public void encode(RegistryByteBuf registryByteBuf, RegistryEntryList<T> registryEntryList) {
                Optional optional = registryEntryList.getTagKey();
                if (optional.isPresent()) {
                    VarInts.write(registryByteBuf, 0);
                    Identifier.PACKET_CODEC.encode(registryByteBuf, optional.get().id());
                } else {
                    VarInts.write(registryByteBuf, registryEntryList.size() + 1);
                    for (RegistryEntry registryEntry : registryEntryList) {
                        this.entryPacketCodec.encode(registryByteBuf, registryEntry);
                    }
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryByteBuf)object, (RegistryEntryList)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryByteBuf)object);
            }
        };
    }
}

