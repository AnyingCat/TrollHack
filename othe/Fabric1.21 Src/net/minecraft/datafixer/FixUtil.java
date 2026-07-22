/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer;

import com.mojang.datafixers.Typed;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

public class FixUtil {
    public static Dynamic<?> fixBlockPos(Dynamic<?> dynamic) {
        Optional<Number> optional = dynamic.get("X").asNumber().result();
        Optional<Number> optional2 = dynamic.get("Y").asNumber().result();
        Optional<Number> optional3 = dynamic.get("Z").asNumber().result();
        if (optional.isEmpty() || optional2.isEmpty() || optional3.isEmpty()) {
            return dynamic;
        }
        return dynamic.createIntList(IntStream.of(optional.get().intValue(), optional2.get().intValue(), optional3.get().intValue()));
    }

    public static <T, R> Typed<R> withType(Type<R> type, Typed<T> typed) {
        return new Typed<R>(type, typed.getOps(), typed.getValue());
    }

    @SafeVarargs
    public static <T> Function<Typed<?>, Typed<?>> compose(Function<Typed<?>, Typed<?>> ... fixes) {
        return typed -> {
            for (Function function : fixes) {
                typed = (Typed)function.apply(typed);
            }
            return typed;
        };
    }
}

