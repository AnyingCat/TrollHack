/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument.packrat;

import java.util.stream.Stream;
import net.minecraft.command.argument.packrat.ParsingState;

public interface Suggestable<S> {
    public Stream<String> possibleValues(ParsingState<S> var1);

    public static <S> Suggestable<S> empty() {
        return state -> Stream.empty();
    }
}

