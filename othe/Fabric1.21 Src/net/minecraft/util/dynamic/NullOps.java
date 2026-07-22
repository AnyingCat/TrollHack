/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.dynamic;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.minecraft.util.Unit;

public class NullOps
implements DynamicOps<Unit> {
    public static final NullOps INSTANCE = new NullOps();

    private NullOps() {
    }

    @Override
    public <U> U convertTo(DynamicOps<U> dynamicOps, Unit unit) {
        return dynamicOps.empty();
    }

    @Override
    public Unit empty() {
        return Unit.INSTANCE;
    }

    @Override
    public Unit emptyMap() {
        return Unit.INSTANCE;
    }

    @Override
    public Unit emptyList() {
        return Unit.INSTANCE;
    }

    @Override
    public Unit createNumeric(Number number) {
        return Unit.INSTANCE;
    }

    @Override
    public Unit createByte(byte b) {
        return Unit.INSTANCE;
    }

    @Override
    public Unit createShort(short s) {
        return Unit.INSTANCE;
    }

    @Override
    public Unit createInt(int i) {
        return Unit.INSTANCE;
    }

    @Override
    public Unit createLong(long l) {
        return Unit.INSTANCE;
    }

    @Override
    public Unit createFloat(float f) {
        return Unit.INSTANCE;
    }

    @Override
    public Unit createDouble(double d) {
        return Unit.INSTANCE;
    }

    @Override
    public Unit createBoolean(boolean bl) {
        return Unit.INSTANCE;
    }

    @Override
    public Unit createString(String string) {
        return Unit.INSTANCE;
    }

    @Override
    public DataResult<Number> getNumberValue(Unit unit) {
        return DataResult.error(() -> "Not a number");
    }

    @Override
    public DataResult<Boolean> getBooleanValue(Unit unit) {
        return DataResult.error(() -> "Not a boolean");
    }

    @Override
    public DataResult<String> getStringValue(Unit unit) {
        return DataResult.error(() -> "Not a string");
    }

    @Override
    public DataResult<Unit> mergeToList(Unit unit, Unit unit2) {
        return DataResult.success(Unit.INSTANCE);
    }

    @Override
    public DataResult<Unit> mergeToList(Unit unit, List<Unit> list) {
        return DataResult.success(Unit.INSTANCE);
    }

    @Override
    public DataResult<Unit> mergeToMap(Unit unit, Unit unit2, Unit unit3) {
        return DataResult.success(Unit.INSTANCE);
    }

    @Override
    public DataResult<Unit> mergeToMap(Unit unit, Map<Unit, Unit> map) {
        return DataResult.success(Unit.INSTANCE);
    }

    @Override
    public DataResult<Unit> mergeToMap(Unit unit, MapLike<Unit> mapLike) {
        return DataResult.success(Unit.INSTANCE);
    }

    @Override
    public DataResult<Stream<Pair<Unit, Unit>>> getMapValues(Unit unit) {
        return DataResult.error(() -> "Not a map");
    }

    @Override
    public DataResult<Consumer<BiConsumer<Unit, Unit>>> getMapEntries(Unit unit) {
        return DataResult.error(() -> "Not a map");
    }

    @Override
    public DataResult<MapLike<Unit>> getMap(Unit unit) {
        return DataResult.error(() -> "Not a map");
    }

    @Override
    public DataResult<Stream<Unit>> getStream(Unit unit) {
        return DataResult.error(() -> "Not a list");
    }

    @Override
    public DataResult<Consumer<Consumer<Unit>>> getList(Unit unit) {
        return DataResult.error(() -> "Not a list");
    }

    @Override
    public DataResult<ByteBuffer> getByteBuffer(Unit unit) {
        return DataResult.error(() -> "Not a byte list");
    }

    @Override
    public DataResult<IntStream> getIntStream(Unit unit) {
        return DataResult.error(() -> "Not an int list");
    }

    @Override
    public DataResult<LongStream> getLongStream(Unit unit) {
        return DataResult.error(() -> "Not a long list");
    }

    @Override
    public Unit createMap(Stream<Pair<Unit, Unit>> stream) {
        return Unit.INSTANCE;
    }

    @Override
    public Unit createMap(Map<Unit, Unit> map) {
        return Unit.INSTANCE;
    }

    @Override
    public Unit createList(Stream<Unit> stream) {
        return Unit.INSTANCE;
    }

    @Override
    public Unit createByteList(ByteBuffer byteBuffer) {
        return Unit.INSTANCE;
    }

    @Override
    public Unit createIntList(IntStream intStream) {
        return Unit.INSTANCE;
    }

    @Override
    public Unit createLongList(LongStream longStream) {
        return Unit.INSTANCE;
    }

    @Override
    public Unit remove(Unit unit, String string) {
        return unit;
    }

    @Override
    public RecordBuilder<Unit> mapBuilder() {
        return new NullMapBuilder(this);
    }

    public String toString() {
        return "Null";
    }

    @Override
    public /* synthetic */ Object remove(Object object, String string) {
        return this.remove((Unit)((Object)object), string);
    }

    @Override
    public /* synthetic */ Object createLongList(LongStream longStream) {
        return this.createLongList(longStream);
    }

    @Override
    public /* synthetic */ DataResult getLongStream(Object object) {
        return this.getLongStream((Unit)((Object)object));
    }

    @Override
    public /* synthetic */ Object createIntList(IntStream intStream) {
        return this.createIntList(intStream);
    }

    @Override
    public /* synthetic */ DataResult getIntStream(Object object) {
        return this.getIntStream((Unit)((Object)object));
    }

    @Override
    public /* synthetic */ Object createByteList(ByteBuffer byteBuffer) {
        return this.createByteList(byteBuffer);
    }

    @Override
    public /* synthetic */ DataResult getByteBuffer(Object object) {
        return this.getByteBuffer((Unit)((Object)object));
    }

    @Override
    public /* synthetic */ Object createList(Stream stream) {
        return this.createList((Stream<Unit>)stream);
    }

    @Override
    public /* synthetic */ DataResult getList(Object object) {
        return this.getList((Unit)((Object)object));
    }

    @Override
    public /* synthetic */ DataResult getStream(Object object) {
        return this.getStream((Unit)((Object)object));
    }

    @Override
    public /* synthetic */ Object createMap(Map map) {
        return this.createMap((Map<Unit, Unit>)map);
    }

    @Override
    public /* synthetic */ DataResult getMap(Object object) {
        return this.getMap((Unit)((Object)object));
    }

    @Override
    public /* synthetic */ Object createMap(Stream stream) {
        return this.createMap((Stream<Pair<Unit, Unit>>)stream);
    }

    @Override
    public /* synthetic */ DataResult getMapEntries(Object object) {
        return this.getMapEntries((Unit)((Object)object));
    }

    @Override
    public /* synthetic */ DataResult getMapValues(Object object) {
        return this.getMapValues((Unit)((Object)object));
    }

    @Override
    public /* synthetic */ DataResult mergeToMap(Object object, MapLike mapLike) {
        return this.mergeToMap((Unit)((Object)object), (MapLike<Unit>)mapLike);
    }

    @Override
    public /* synthetic */ DataResult mergeToMap(Object object, Map map) {
        return this.mergeToMap((Unit)((Object)object), (Map<Unit, Unit>)map);
    }

    @Override
    public /* synthetic */ DataResult mergeToMap(Object object, Object object2, Object object3) {
        return this.mergeToMap((Unit)((Object)object), (Unit)((Object)object2), (Unit)((Object)object3));
    }

    @Override
    public /* synthetic */ DataResult mergeToList(Object object, List list) {
        return this.mergeToList((Unit)((Object)object), (List<Unit>)list);
    }

    @Override
    public /* synthetic */ DataResult mergeToList(Object object, Object object2) {
        return this.mergeToList((Unit)((Object)object), (Unit)((Object)object2));
    }

    @Override
    public /* synthetic */ Object createString(String string) {
        return this.createString(string);
    }

    @Override
    public /* synthetic */ DataResult getStringValue(Object object) {
        return this.getStringValue((Unit)((Object)object));
    }

    @Override
    public /* synthetic */ Object createBoolean(boolean bl) {
        return this.createBoolean(bl);
    }

    @Override
    public /* synthetic */ DataResult getBooleanValue(Object object) {
        return this.getBooleanValue((Unit)((Object)object));
    }

    @Override
    public /* synthetic */ Object createDouble(double d) {
        return this.createDouble(d);
    }

    @Override
    public /* synthetic */ Object createFloat(float f) {
        return this.createFloat(f);
    }

    @Override
    public /* synthetic */ Object createLong(long l) {
        return this.createLong(l);
    }

    @Override
    public /* synthetic */ Object createInt(int i) {
        return this.createInt(i);
    }

    @Override
    public /* synthetic */ Object createShort(short s) {
        return this.createShort(s);
    }

    @Override
    public /* synthetic */ Object createByte(byte b) {
        return this.createByte(b);
    }

    @Override
    public /* synthetic */ Object createNumeric(Number number) {
        return this.createNumeric(number);
    }

    @Override
    public /* synthetic */ DataResult getNumberValue(Object object) {
        return this.getNumberValue((Unit)((Object)object));
    }

    @Override
    public /* synthetic */ Object convertTo(DynamicOps ops, Object unit) {
        return this.convertTo(ops, (Unit)((Object)unit));
    }

    @Override
    public /* synthetic */ Object emptyList() {
        return this.emptyList();
    }

    @Override
    public /* synthetic */ Object emptyMap() {
        return this.emptyMap();
    }

    @Override
    public /* synthetic */ Object empty() {
        return this.empty();
    }

    static final class NullMapBuilder
    extends RecordBuilder.AbstractUniversalBuilder<Unit, Unit> {
        public NullMapBuilder(DynamicOps<Unit> dynamicOps) {
            super(dynamicOps);
        }

        @Override
        protected Unit initBuilder() {
            return Unit.INSTANCE;
        }

        @Override
        protected Unit append(Unit unit, Unit unit2, Unit unit3) {
            return unit3;
        }

        @Override
        protected DataResult<Unit> build(Unit unit, Unit unit2) {
            return DataResult.success(unit2);
        }

        @Override
        protected /* synthetic */ Object append(Object object, Object object2, Object object3) {
            return this.append((Unit)((Object)object), (Unit)((Object)object2), (Unit)((Object)object3));
        }

        @Override
        protected /* synthetic */ DataResult build(Object object, Object object2) {
            return this.build((Unit)((Object)object), (Unit)((Object)object2));
        }

        @Override
        protected /* synthetic */ Object initBuilder() {
            return this.initBuilder();
        }
    }
}

