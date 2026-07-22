/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument.packrat;

import java.util.Optional;
import net.minecraft.command.argument.packrat.Cut;
import net.minecraft.command.argument.packrat.ParseResults;
import net.minecraft.command.argument.packrat.ParsingState;
import net.minecraft.command.argument.packrat.Term;

public interface ParsingRule<S, T> {
    public Optional<T> parse(ParsingState<S> var1);

    public static <S, T> ParsingRule<S, T> of(Term<S> term, RuleAction<S, T> action) {
        return new SimpleRule<S, T>(action, term);
    }

    public static <S, T> ParsingRule<S, T> of(Term<S> term, StatelessAction<T> action) {
        return new SimpleRule((state, results) -> Optional.of(action.run(results)), term);
    }

    public record SimpleRule<S, T>(RuleAction<S, T> action, Term<S> child) implements ParsingRule<S, T>
    {
        @Override
        public Optional<T> parse(ParsingState<S> state) {
            ParseResults parseResults = new ParseResults();
            if (this.child.matches(state, parseResults, Cut.NOOP)) {
                return this.action.run(state, parseResults);
            }
            return Optional.empty();
        }
    }

    @FunctionalInterface
    public static interface RuleAction<S, T> {
        public Optional<T> run(ParsingState<S> var1, ParseResults var2);
    }

    @FunctionalInterface
    public static interface StatelessAction<T> {
        public T run(ParseResults var1);
    }
}

