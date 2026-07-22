/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument.packrat;

import java.util.List;
import java.util.Optional;
import net.minecraft.command.argument.packrat.Cut;
import net.minecraft.command.argument.packrat.ParseResults;
import net.minecraft.command.argument.packrat.ParsingState;
import net.minecraft.command.argument.packrat.Symbol;
import org.apache.commons.lang3.mutable.MutableBoolean;

public interface Term<S> {
    public boolean matches(ParsingState<S> var1, ParseResults var2, Cut var3);

    public static <S> Term<S> symbol(Symbol<?> symbol) {
        return new SymbolTerm(symbol);
    }

    public static <S, T> Term<S> always(Symbol<T> symbol, T value) {
        return new AlwaysTerm(symbol, value);
    }

    @SafeVarargs
    public static <S> Term<S> sequence(Term<S> ... terms) {
        return new SequenceTerm<S>(List.of(terms));
    }

    @SafeVarargs
    public static <S> Term<S> anyOf(Term<S> ... terms) {
        return new AnyOfTerm<S>(List.of(terms));
    }

    public static <S> Term<S> optional(Term<S> term) {
        return new OptionalTerm<S>(term);
    }

    public static <S> Term<S> cutting() {
        return new Term<S>(){

            @Override
            public boolean matches(ParsingState<S> state, ParseResults results, Cut cut) {
                cut.cut();
                return true;
            }

            public String toString() {
                return "\u2191";
            }
        };
    }

    public static <S> Term<S> epsilon() {
        return new Term<S>(){

            @Override
            public boolean matches(ParsingState<S> state, ParseResults results, Cut cut) {
                return true;
            }

            public String toString() {
                return "\u03b5";
            }
        };
    }

    public record SymbolTerm<S, T>(Symbol<T> name) implements Term<S>
    {
        @Override
        public boolean matches(ParsingState<S> state, ParseResults results, Cut cut) {
            Optional<T> optional = state.parse(this.name);
            if (optional.isEmpty()) {
                return false;
            }
            results.put(this.name, optional.get());
            return true;
        }
    }

    public record AlwaysTerm<S, T>(Symbol<T> name, T value) implements Term<S>
    {
        @Override
        public boolean matches(ParsingState<S> state, ParseResults results, Cut cut) {
            results.put(this.name, this.value);
            return true;
        }
    }

    public record SequenceTerm<S>(List<Term<S>> elements) implements Term<S>
    {
        @Override
        public boolean matches(ParsingState<S> state, ParseResults results, Cut cut) {
            int i = state.getCursor();
            for (Term<S> term : this.elements) {
                if (term.matches(state, results, cut)) continue;
                state.setCursor(i);
                return false;
            }
            return true;
        }
    }

    public record AnyOfTerm<S>(List<Term<S>> elements) implements Term<S>
    {
        @Override
        public boolean matches(ParsingState<S> state, ParseResults results, Cut cut) {
            MutableBoolean mutableBoolean = new MutableBoolean();
            Cut cut2 = mutableBoolean::setTrue;
            int i = state.getCursor();
            for (Term<S> term : this.elements) {
                if (mutableBoolean.isTrue()) break;
                ParseResults parseResults = new ParseResults();
                if (term.matches(state, parseResults, cut2)) {
                    results.putAll(parseResults);
                    return true;
                }
                state.setCursor(i);
            }
            return false;
        }
    }

    public record OptionalTerm<S>(Term<S> term) implements Term<S>
    {
        @Override
        public boolean matches(ParsingState<S> state, ParseResults results, Cut cut) {
            int i = state.getCursor();
            if (!this.term.matches(state, results, cut)) {
                state.setCursor(i);
            }
            return true;
        }
    }
}

