/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.codec;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.mojang.datafixers.util.Function6;
import io.netty.buffer.ByteBuf;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.network.codec.PacketDecoder;
import net.minecraft.network.codec.PacketEncoder;
import net.minecraft.network.codec.ValueFirstEncoder;

/**
 * A codec that is used for serializing a packet.
 * 
 * <p>Packet codecs serialize to, and deserialize from, {@link net.minecraft.network.PacketByteBuf},
 * which is a stream of data. To integrate the classic {@link net.minecraft.network.PacketByteBuf}-based
 * code, use {@link #of(ValueFirstEncoder, PacketDecoder)}
 * like this:
 * 
 * <pre>{@code
 * public static final PacketCodec<PacketByteBuf, MyPacket> CODEC = PacketCodec.of(MyPacket::write, MyPacket::new);
 * 
 * private MyPacket(PacketByteBuf buf) {
 * \u0009this.text = buf.readString();
 * }
 * 
 * private void write(PacketByteBuf buf) {
 * \u0009buf.writeString(this.text);
 * }
 * }</pre>
 * 
 * <p>While this serves similar functions as codecs in the DataFixerUpper library,
 * the two are wholly separate and DataFixerUpper methods cannot be used with this.
 * However, a packet codec may reference a regular codec by using {@link
 * PacketCodecs#codec}, which serializes the data to NBT.
 * 
 * <p>See {@link PacketCodecs} for codecs to serialize various objects.
 * 
 * @param <B> the type of the buffer; {@link net.minecraft.network.RegistryByteBuf}
 * for play-phase packets, {@link net.minecraft.network.PacketByteBuf} for other
 * phases (like configuration)
 * @param <V> the type of the value to be encoded/decoded
 */
public interface PacketCodec<B, V>
extends PacketDecoder<B, V>,
PacketEncoder<B, V> {
    /**
     * {@return a packet codec from the {@code encoder} and {@code decoder}}
     * 
     * @apiNote This is useful for integrating with code that uses static methods for
     * packet writing, where the buffer is the first argument, like
     * {@code static void write(PacketByteBuf buf, Data data)}.
     * For code that uses instance methods like {@code void write(PacketByteBuf buf)},
     * use {@link #of(ValueFirstEncoder, PacketDecoder)}.
     */
    public static <B, V> PacketCodec<B, V> ofStatic(final PacketEncoder<B, V> encoder, final PacketDecoder<B, V> decoder) {
        return new PacketCodec<B, V>(){

            @Override
            public V decode(B object) {
                return decoder.decode(object);
            }

            @Override
            public void encode(B object, V object2) {
                encoder.encode(object, object2);
            }
        };
    }

    /**
     * {@return a packet codec from the {@code encoder} and {@code decoder}}
     * 
     * @apiNote This is useful for integrating with code that uses instance methods for
     * packet writing, like {@code void write(PacketByteBuf buf)}.
     * For code that uses static methods like {@code static void write(PacketByteBuf buf, Data data)},
     * where the buffer is the first argument, use {@link #ofStatic(PacketEncoder, PacketDecoder)}.
     */
    public static <B, V> PacketCodec<B, V> of(final ValueFirstEncoder<B, V> encoder, final PacketDecoder<B, V> decoder) {
        return new PacketCodec<B, V>(){

            @Override
            public V decode(B object) {
                return decoder.decode(object);
            }

            @Override
            public void encode(B object, V object2) {
                encoder.encode(object2, object);
            }
        };
    }

    /**
     * {@return a codec that always returns {@code value}}
     * 
     * <p>This does not encode anything. Instead, it throws {@link
     * IllegalStateException} when the value does not
     * equal {@code value}. This comparison is made with {@code equals()}, not
     * reference equality ({@code ==}).
     */
    public static <B, V> PacketCodec<B, V> unit(final V value) {
        return new PacketCodec<B, V>(){

            @Override
            public V decode(B object) {
                return value;
            }

            @Override
            public void encode(B object, V object2) {
                if (!object2.equals(value)) {
                    throw new IllegalStateException("Can't encode '" + String.valueOf(object2) + "', expected '" + String.valueOf(value) + "'");
                }
            }
        };
    }

    /**
     * {@return the result mapped with {@code function}}
     * 
     * <p>For example, passing {@code PacketCodecs::optional} makes the value
     * optional. Additionally, this method can be used like Stream {@link
     * java.util.stream.Collectors} - hence its name. For example, to make a codec
     * for a list of something, write {@code parentCodec.collect(PacketCodecs.toList())}.
     * 
     * @see PacketCodecs#optional
     * @see PacketCodecs#toCollection
     * @see PacketCodecs#toList
     */
    default public <O> PacketCodec<B, O> collect(ResultFunction<B, V, O> function) {
        return function.apply(this);
    }

    /**
     * {@return a codec that maps its encode input and decode output with {@code from}
     * and {@code to}, respectively}
     * 
     * <p>This can be used to transform a codec for a simple value (like a string)
     * into a corresponding, more complex value (like an identifier). An example:
     * 
     * <pre>{@code
     * public static final PacketCodec<ByteBuf, Identifier> PACKET_CODEC = PacketCodecs.STRING.xmap(Identifier::new, Identifier::toString);
     * }</pre>
     */
    default public <O> PacketCodec<B, O> xmap(final Function<? super V, ? extends O> to, final Function<? super O, ? extends V> from) {
        return new PacketCodec<B, O>(){

            @Override
            public O decode(B object) {
                return to.apply(PacketCodec.this.decode(object));
            }

            @Override
            public void encode(B object, O object2) {
                PacketCodec.this.encode(object, from.apply(object2));
            }
        };
    }

    default public <O extends ByteBuf> PacketCodec<O, V> mapBuf(final Function<O, ? extends B> function) {
        return new PacketCodec<O, V>(){

            @Override
            public V decode(O byteBuf) {
                Object object = function.apply(byteBuf);
                return PacketCodec.this.decode(object);
            }

            @Override
            public void encode(O byteBuf, V object) {
                Object object2 = function.apply(byteBuf);
                PacketCodec.this.encode(object2, object);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((O)((ByteBuf)object), (V)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((O)((ByteBuf)object));
            }
        };
    }

    /**
     * {@return a codec that dispatches one of the sub-codecs based on the type}
     * 
     * <p>For example, subtypes of {@link net.minecraft.stat.Stat} requires different values
     * to be serialized, yet it makes sense to use the same codec for all stats.
     * This method should be called on the codec for the "type" - like {@link
     * net.minecraft.stat.StatType}. An example:
     * 
     * <pre>{@code
     * public static final PacketCodec<RegistryByteBuf, Thing<?>> PACKET_CODEC = PacketCodecs.registryValue(RegistryKeys.THING_TYPE).dispatch(Thing::getType, ThingType::getPacketCodec);
     * }</pre>
     * 
     * @param codec a function that, given a "type", returns the codec for encoding/decoding the value
     * @param type a function that, given a value, returns its "type"
     */
    default public <U> PacketCodec<B, U> dispatch(final Function<? super U, ? extends V> type, final Function<? super V, ? extends PacketCodec<? super B, ? extends U>> codec) {
        return new PacketCodec<B, U>(){

            @Override
            public U decode(B object) {
                Object object2 = PacketCodec.this.decode(object);
                PacketCodec packetCodec = (PacketCodec)codec.apply(object2);
                return packetCodec.decode(object);
            }

            @Override
            public void encode(B object, U object2) {
                Object object3 = type.apply(object2);
                PacketCodec packetCodec = (PacketCodec)codec.apply(object3);
                PacketCodec.this.encode(object, object3);
                packetCodec.encode(object, object2);
            }
        };
    }

    /**
     * {@return a codec for encoding one value}
     */
    public static <B, C, T1> PacketCodec<B, C> tuple(final PacketCodec<? super B, T1> codec, final Function<C, T1> from, final Function<T1, C> to) {
        return new PacketCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = codec.decode(object);
                return to.apply(object2);
            }

            @Override
            public void encode(B object, C object2) {
                codec.encode(object, from.apply(object2));
            }
        };
    }

    /**
     * {@return a codec for encoding two values}
     */
    public static <B, C, T1, T2> PacketCodec<B, C> tuple(final PacketCodec<? super B, T1> codec1, final Function<C, T1> from1, final PacketCodec<? super B, T2> codec2, final Function<C, T2> from2, final BiFunction<T1, T2, C> to) {
        return new PacketCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = codec1.decode(object);
                Object object3 = codec2.decode(object);
                return to.apply(object2, object3);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, from1.apply(object2));
                codec2.encode(object, from2.apply(object2));
            }
        };
    }

    /**
     * {@return a codec for encoding three values}
     */
    public static <B, C, T1, T2, T3> PacketCodec<B, C> tuple(final PacketCodec<? super B, T1> codec1, final Function<C, T1> from1, final PacketCodec<? super B, T2> codec2, final Function<C, T2> from2, final PacketCodec<? super B, T3> codec3, final Function<C, T3> from3, final Function3<T1, T2, T3, C> to) {
        return new PacketCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = codec1.decode(object);
                Object object3 = codec2.decode(object);
                Object object4 = codec3.decode(object);
                return to.apply(object2, object3, object4);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, from1.apply(object2));
                codec2.encode(object, from2.apply(object2));
                codec3.encode(object, from3.apply(object2));
            }
        };
    }

    /**
     * {@return a codec for encoding four values}
     */
    public static <B, C, T1, T2, T3, T4> PacketCodec<B, C> tuple(final PacketCodec<? super B, T1> codec1, final Function<C, T1> from1, final PacketCodec<? super B, T2> codec2, final Function<C, T2> from2, final PacketCodec<? super B, T3> codec3, final Function<C, T3> from3, final PacketCodec<? super B, T4> codec4, final Function<C, T4> from4, final Function4<T1, T2, T3, T4, C> to) {
        return new PacketCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = codec1.decode(object);
                Object object3 = codec2.decode(object);
                Object object4 = codec3.decode(object);
                Object object5 = codec4.decode(object);
                return to.apply(object2, object3, object4, object5);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, from1.apply(object2));
                codec2.encode(object, from2.apply(object2));
                codec3.encode(object, from3.apply(object2));
                codec4.encode(object, from4.apply(object2));
            }
        };
    }

    /**
     * {@return a codec for encoding five values}
     */
    public static <B, C, T1, T2, T3, T4, T5> PacketCodec<B, C> tuple(final PacketCodec<? super B, T1> codec1, final Function<C, T1> from1, final PacketCodec<? super B, T2> codec2, final Function<C, T2> from2, final PacketCodec<? super B, T3> codec3, final Function<C, T3> from3, final PacketCodec<? super B, T4> codec4, final Function<C, T4> from4, final PacketCodec<? super B, T5> codec5, final Function<C, T5> from5, final Function5<T1, T2, T3, T4, T5, C> to) {
        return new PacketCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = codec1.decode(object);
                Object object3 = codec2.decode(object);
                Object object4 = codec3.decode(object);
                Object object5 = codec4.decode(object);
                Object object6 = codec5.decode(object);
                return to.apply(object2, object3, object4, object5, object6);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, from1.apply(object2));
                codec2.encode(object, from2.apply(object2));
                codec3.encode(object, from3.apply(object2));
                codec4.encode(object, from4.apply(object2));
                codec5.encode(object, from5.apply(object2));
            }
        };
    }

    /**
     * {@return a codec for encoding six values}
     */
    public static <B, C, T1, T2, T3, T4, T5, T6> PacketCodec<B, C> tuple(final PacketCodec<? super B, T1> codec1, final Function<C, T1> from1, final PacketCodec<? super B, T2> codec2, final Function<C, T2> from2, final PacketCodec<? super B, T3> codec3, final Function<C, T3> from3, final PacketCodec<? super B, T4> codec4, final Function<C, T4> from4, final PacketCodec<? super B, T5> codec5, final Function<C, T5> from5, final PacketCodec<? super B, T6> codec6, final Function<C, T6> from6, final Function6<T1, T2, T3, T4, T5, T6, C> to) {
        return new PacketCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = codec1.decode(object);
                Object object3 = codec2.decode(object);
                Object object4 = codec3.decode(object);
                Object object5 = codec4.decode(object);
                Object object6 = codec5.decode(object);
                Object object7 = codec6.decode(object);
                return to.apply(object2, object3, object4, object5, object6, object7);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, from1.apply(object2));
                codec2.encode(object, from2.apply(object2));
                codec3.encode(object, from3.apply(object2));
                codec4.encode(object, from4.apply(object2));
                codec5.encode(object, from5.apply(object2));
                codec6.encode(object, from6.apply(object2));
            }
        };
    }

    public static <B, T> PacketCodec<B, T> recursive(final UnaryOperator<PacketCodec<B, T>> codecGetter) {
        return new PacketCodec<B, T>(){
            private final Supplier<PacketCodec<B, T>> codecSupplier = Suppliers.memoize(() -> (PacketCodec)codecGetter.apply(this));

            @Override
            public T decode(B object) {
                return this.codecSupplier.get().decode(object);
            }

            @Override
            public void encode(B object, T object2) {
                this.codecSupplier.get().encode(object, object2);
            }
        };
    }

    /**
     * {@return the same codec, casted to work with buffers of type {@code S}}
     * 
     * @apiNote For example, {@link net.minecraft.util.math.BlockPos#PACKET_CODEC}
     * is defined as {@code PacketCodec<ByteBuf, BlockPos>}. To use this codec
     * where {@link net.minecraft.network.PacketByteBuf} is expected, you can call
     * this method for easy casting, like: {@code PACKET_CODEC.cast()}.
     * Doing this is generally safe and will not result in exceptions.
     */
    default public <S extends B> PacketCodec<S, V> cast() {
        return this;
    }

    @FunctionalInterface
    public static interface ResultFunction<B, S, T> {
        public PacketCodec<B, T> apply(PacketCodec<B, S> var1);
    }
}

