/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.StringReader;
import java.lang.reflect.Field;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Util;
import net.minecraft.util.function.CharPredicate;

public class JsonReaderUtils {
    private static final Field POS = Util.make(() -> {
        try {
            Field field = JsonReader.class.getDeclaredField("pos");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException noSuchFieldException) {
            throw new IllegalStateException("Couldn't get field 'pos' for JsonReader", noSuchFieldException);
        }
    });
    private static final Field LINE_START = Util.make(() -> {
        try {
            Field field = JsonReader.class.getDeclaredField("lineStart");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException noSuchFieldException) {
            throw new IllegalStateException("Couldn't get field 'lineStart' for JsonReader", noSuchFieldException);
        }
    });

    private static int getPos(JsonReader jsonReader) {
        try {
            return POS.getInt(jsonReader) - LINE_START.getInt(jsonReader);
        } catch (IllegalAccessException illegalAccessException) {
            throw new IllegalStateException("Couldn't read position of JsonReader", illegalAccessException);
        }
    }

    public static <T> T parse(RegistryWrapper.WrapperLookup registryLookup, com.mojang.brigadier.StringReader stringReader, Codec<T> codec) {
        JsonReader jsonReader = new JsonReader(new StringReader(stringReader.getRemaining()));
        jsonReader.setLenient(false);
        try {
            JsonElement jsonElement = Streams.parse(jsonReader);
            Object a = codec.parse(registryLookup.getOps(JsonOps.INSTANCE), jsonElement).getOrThrow(JsonParseException::new);
            return (T)a;
        } catch (StackOverflowError stackOverflowError) {
            throw new JsonParseException(stackOverflowError);
        } finally {
            stringReader.setCursor(stringReader.getCursor() + JsonReaderUtils.getPos(jsonReader));
        }
    }

    public static String readWhileMatching(com.mojang.brigadier.StringReader stringReader, CharPredicate predicate) {
        int i = stringReader.getCursor();
        while (stringReader.canRead() && predicate.test(stringReader.peek())) {
            stringReader.skip();
        }
        return stringReader.getString().substring(i, stringReader.getCursor());
    }
}

