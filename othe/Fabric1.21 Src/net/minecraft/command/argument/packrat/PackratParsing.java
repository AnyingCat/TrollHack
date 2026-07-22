/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument.packrat;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.command.argument.packrat.AnyIdParsingRule;
import net.minecraft.command.argument.packrat.ArgumentParser;
import net.minecraft.command.argument.packrat.IdentifiableParsingRule;
import net.minecraft.command.argument.packrat.Literals;
import net.minecraft.command.argument.packrat.NbtParsingRule;
import net.minecraft.command.argument.packrat.ParsingRules;
import net.minecraft.command.argument.packrat.Symbol;
import net.minecraft.command.argument.packrat.Term;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;

public class PackratParsing {
    public static <T, C, P> ArgumentParser<List<T>> createParser(Callbacks<T, C, P> callbacks) {
        Symbol symbol = Symbol.of("top");
        Symbol symbol2 = Symbol.of("type");
        Symbol symbol3 = Symbol.of("any_type");
        Symbol symbol4 = Symbol.of("element_type");
        Symbol symbol5 = Symbol.of("tag_type");
        Symbol symbol6 = Symbol.of("conditions");
        Symbol symbol7 = Symbol.of("alternatives");
        Symbol symbol8 = Symbol.of("term");
        Symbol symbol9 = Symbol.of("negation");
        Symbol symbol10 = Symbol.of("test");
        Symbol symbol11 = Symbol.of("component_type");
        Symbol symbol12 = Symbol.of("predicate_type");
        Symbol<Identifier> symbol13 = Symbol.of("id");
        Symbol symbol14 = Symbol.of("tag");
        ParsingRules<StringReader> parsingRules = new ParsingRules<StringReader>();
        parsingRules.set(symbol, Term.anyOf(Term.sequence(Term.symbol(symbol2), Literals.character('['), Term.cutting(), Term.optional(Term.symbol(symbol6)), Literals.character(']')), Term.symbol(symbol2)), results -> {
            ImmutableList.Builder builder = ImmutableList.builder();
            ((Optional)results.getOrThrow(symbol2)).ifPresent(builder::add);
            List list = (List)results.get(symbol6);
            if (list != null) {
                builder.addAll((Iterable)list);
            }
            return builder.build();
        });
        parsingRules.set(symbol2, Term.anyOf(Term.symbol(symbol4), Term.sequence(Literals.character('#'), Term.cutting(), Term.symbol(symbol5)), Term.symbol(symbol3)), results -> Optional.ofNullable(results.getAny(symbol4, symbol5)));
        parsingRules.set(symbol3, Literals.character('*'), results -> Unit.INSTANCE);
        parsingRules.set(symbol4, new ItemParsingRule<T, C, P>(symbol13, callbacks));
        parsingRules.set(symbol5, new TagParsingRule<T, C, P>(symbol13, callbacks));
        parsingRules.set(symbol6, Term.sequence(Term.symbol(symbol7), Term.optional(Term.sequence(Literals.character(','), Term.symbol(symbol6)))), results -> {
            Object object = callbacks.anyOf((List)results.getOrThrow(symbol7));
            return Optional.ofNullable((List)results.get(symbol6)).map(predicates -> Util.withPrepended(object, predicates)).orElse(List.of(object));
        });
        parsingRules.set(symbol7, Term.sequence(Term.symbol(symbol8), Term.optional(Term.sequence(Literals.character('|'), Term.symbol(symbol7)))), results -> {
            Object object = results.getOrThrow(symbol8);
            return Optional.ofNullable((List)results.get(symbol7)).map(predicates -> Util.withPrepended(object, predicates)).orElse(List.of(object));
        });
        parsingRules.set(symbol8, Term.anyOf(Term.symbol(symbol10), Term.sequence(Literals.character('!'), Term.symbol(symbol9))), results -> results.getAnyOrThrow(symbol10, symbol9));
        parsingRules.set(symbol9, Term.symbol(symbol10), results -> callbacks.negate(results.getOrThrow(symbol10)));
        parsingRules.set(symbol10, Term.anyOf(Term.sequence(Term.symbol(symbol11), Literals.character('='), Term.cutting(), Term.symbol(symbol14)), Term.sequence(Term.symbol(symbol12), Literals.character('~'), Term.cutting(), Term.symbol(symbol14)), Term.symbol(symbol11)), (state, results) -> {
            Object object = results.get(symbol12);
            try {
                if (object != null) {
                    NbtElement nbtElement = (NbtElement)results.getOrThrow(symbol14);
                    return Optional.of(callbacks.subPredicatePredicate((ImmutableStringReader)state.getReader(), object, nbtElement));
                }
                Object object2 = results.getOrThrow(symbol11);
                NbtElement nbtElement2 = (NbtElement)results.get(symbol14);
                return Optional.of(nbtElement2 != null ? callbacks.componentMatchPredicate((ImmutableStringReader)state.getReader(), object2, nbtElement2) : callbacks.componentPresencePredicate((ImmutableStringReader)state.getReader(), object2));
            } catch (CommandSyntaxException commandSyntaxException) {
                state.getErrors().add(state.getCursor(), commandSyntaxException);
                return Optional.empty();
            }
        });
        parsingRules.set(symbol11, new ComponentParsingRule<T, C, P>(symbol13, callbacks));
        parsingRules.set(symbol12, new SubPredicateParsingRule<T, C, P>(symbol13, callbacks));
        parsingRules.set(symbol14, NbtParsingRule.INSTANCE);
        parsingRules.set(symbol13, AnyIdParsingRule.INSTANCE);
        return new ArgumentParser<List<T>>(parsingRules, symbol);
    }

    static class ItemParsingRule<T, C, P>
    extends IdentifiableParsingRule<Callbacks<T, C, P>, T> {
        ItemParsingRule(Symbol<Identifier> symbol, Callbacks<T, C, P> callbacks) {
            super(symbol, callbacks);
        }

        @Override
        protected T parse(ImmutableStringReader reader, Identifier id) throws Exception {
            return ((Callbacks)this.callbacks).itemMatchPredicate(reader, id);
        }

        @Override
        public Stream<Identifier> possibleIds() {
            return ((Callbacks)this.callbacks).streamItemIds();
        }
    }

    public static interface Callbacks<T, C, P> {
        public T itemMatchPredicate(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

        public Stream<Identifier> streamItemIds();

        public T tagMatchPredicate(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

        public Stream<Identifier> streamTags();

        public C componentCheck(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

        public Stream<Identifier> streamComponentIds();

        public T componentMatchPredicate(ImmutableStringReader var1, C var2, NbtElement var3) throws CommandSyntaxException;

        public T componentPresencePredicate(ImmutableStringReader var1, C var2);

        public P subPredicateCheck(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

        public Stream<Identifier> streamSubPredicateIds();

        public T subPredicatePredicate(ImmutableStringReader var1, P var2, NbtElement var3) throws CommandSyntaxException;

        public T negate(T var1);

        public T anyOf(List<T> var1);
    }

    static class TagParsingRule<T, C, P>
    extends IdentifiableParsingRule<Callbacks<T, C, P>, T> {
        TagParsingRule(Symbol<Identifier> symbol, Callbacks<T, C, P> callbacks) {
            super(symbol, callbacks);
        }

        @Override
        protected T parse(ImmutableStringReader reader, Identifier id) throws Exception {
            return ((Callbacks)this.callbacks).tagMatchPredicate(reader, id);
        }

        @Override
        public Stream<Identifier> possibleIds() {
            return ((Callbacks)this.callbacks).streamTags();
        }
    }

    static class ComponentParsingRule<T, C, P>
    extends IdentifiableParsingRule<Callbacks<T, C, P>, C> {
        ComponentParsingRule(Symbol<Identifier> symbol, Callbacks<T, C, P> callbacks) {
            super(symbol, callbacks);
        }

        @Override
        protected C parse(ImmutableStringReader reader, Identifier id) throws Exception {
            return ((Callbacks)this.callbacks).componentCheck(reader, id);
        }

        @Override
        public Stream<Identifier> possibleIds() {
            return ((Callbacks)this.callbacks).streamComponentIds();
        }
    }

    static class SubPredicateParsingRule<T, C, P>
    extends IdentifiableParsingRule<Callbacks<T, C, P>, P> {
        SubPredicateParsingRule(Symbol<Identifier> symbol, Callbacks<T, C, P> callbacks) {
            super(symbol, callbacks);
        }

        @Override
        protected P parse(ImmutableStringReader reader, Identifier id) throws Exception {
            return ((Callbacks)this.callbacks).subPredicateCheck(reader, id);
        }

        @Override
        public Stream<Identifier> possibleIds() {
            return ((Callbacks)this.callbacks).streamSubPredicateIds();
        }
    }
}

