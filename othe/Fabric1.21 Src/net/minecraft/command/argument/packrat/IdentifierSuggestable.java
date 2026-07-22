/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument.packrat;

import com.mojang.brigadier.StringReader;
import java.util.stream.Stream;
import net.minecraft.command.argument.packrat.ParsingState;
import net.minecraft.command.argument.packrat.Suggestable;
import net.minecraft.util.Identifier;

public interface IdentifierSuggestable
extends Suggestable<StringReader> {
    public Stream<Identifier> possibleIds();

    @Override
    default public Stream<String> possibleValues(ParsingState<StringReader> parsingState) {
        return this.possibleIds().map(Identifier::toString);
    }
}

