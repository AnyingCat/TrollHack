/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument.packrat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.stream.Stream;
import net.minecraft.command.argument.packrat.Cut;
import net.minecraft.command.argument.packrat.ParseResults;
import net.minecraft.command.argument.packrat.ParsingState;
import net.minecraft.command.argument.packrat.Term;

public interface Literals {
    public static Term<StringReader> string(String string) {
        return new StringLiteral(string);
    }

    public static Term<StringReader> character(char c) {
        return new CharLiteral(c);
    }

    public record StringLiteral(String value) implements Term<StringReader>
    {
        @Override
        public boolean matches(ParsingState<StringReader> state, ParseResults results, Cut cut) {
            state.getReader().skipWhitespace();
            int i = state.getCursor();
            String string = state.getReader().readUnquotedString();
            if (!string.equals(this.value)) {
                state.getErrors().add(i, suggestState -> Stream.of(this.value), CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create(this.value));
                return false;
            }
            return true;
        }
    }

    public record CharLiteral(char value) implements Term<StringReader>
    {
        @Override
        public boolean matches(ParsingState<StringReader> state, ParseResults results, Cut cut) {
            state.getReader().skipWhitespace();
            int i = state.getCursor();
            if (!state.getReader().canRead() || state.getReader().read() != this.value) {
                state.getErrors().add(i, suggestState -> Stream.of(String.valueOf(this.value)), CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create(Character.valueOf(this.value)));
                return false;
            }
            return true;
        }
    }
}

