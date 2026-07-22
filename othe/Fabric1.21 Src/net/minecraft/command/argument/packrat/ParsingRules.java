/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument.packrat;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.command.argument.packrat.ParsingRule;
import net.minecraft.command.argument.packrat.Symbol;
import net.minecraft.command.argument.packrat.Term;
import org.jetbrains.annotations.Nullable;

public class ParsingRules<S> {
    private final Map<Symbol<?>, ParsingRule<S, ?>> rules = new HashMap();

    public <T> void set(Symbol<T> symbol, ParsingRule<S, T> rule) {
        ParsingRule<S, T> parsingRule = this.rules.putIfAbsent(symbol, rule);
        if (parsingRule != null) {
            throw new IllegalArgumentException("Trying to override rule: " + String.valueOf(symbol));
        }
    }

    public <T> void set(Symbol<T> symbol, Term<S> term, ParsingRule.RuleAction<S, T> action) {
        this.set(symbol, ParsingRule.of(term, action));
    }

    public <T> void set(Symbol<T> symbol, Term<S> term, ParsingRule.StatelessAction<T> action) {
        this.set(symbol, ParsingRule.of(term, action));
    }

    @Nullable
    public <T> ParsingRule<S, T> get(Symbol<T> symbol) {
        return this.rules.get(symbol);
    }
}

