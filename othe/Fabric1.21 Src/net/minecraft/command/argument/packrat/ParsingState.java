/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument.packrat;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.command.argument.packrat.ParseErrorList;
import net.minecraft.command.argument.packrat.ParsingRule;
import net.minecraft.command.argument.packrat.ParsingRules;
import net.minecraft.command.argument.packrat.Symbol;
import org.jetbrains.annotations.Nullable;

public abstract class ParsingState<S> {
    private final Map<PackratKey<?>, PackratCache<?>> packrats = new HashMap();
    private final ParsingRules<S> rules;
    private final ParseErrorList<S> errors;

    protected ParsingState(ParsingRules<S> rules, ParseErrorList<S> errors) {
        this.rules = rules;
        this.errors = errors;
    }

    public ParseErrorList<S> getErrors() {
        return this.errors;
    }

    public <T> Optional<T> startParsing(Symbol<T> startSymbol) {
        Optional<T> optional = this.parse(startSymbol);
        if (optional.isPresent()) {
            this.errors.setCursor(this.getCursor());
        }
        return optional;
    }

    public <T> Optional<T> parse(Symbol<T> symbol) {
        PackratKey<T> packratKey = new PackratKey<T>(symbol, this.getCursor());
        PackratCache<T> packratCache = this.getCache(packratKey);
        if (packratCache != null) {
            this.setCursor(packratCache.mark());
            return packratCache.value;
        }
        ParsingRule<S, T> parsingRule = this.rules.get(symbol);
        if (parsingRule == null) {
            throw new IllegalStateException("No symbol " + String.valueOf(symbol));
        }
        Optional<T> optional = parsingRule.parse(this);
        this.putCache(packratKey, optional);
        return optional;
    }

    @Nullable
    private <T> PackratCache<T> getCache(PackratKey<T> key) {
        return this.packrats.get(key);
    }

    private <T> void putCache(PackratKey<T> key, Optional<T> value) {
        this.packrats.put(key, new PackratCache<T>(value, this.getCursor()));
    }

    public abstract S getReader();

    public abstract int getCursor();

    public abstract void setCursor(int var1);

    record PackratKey<T>(Symbol<T> name, int mark) {
    }

    record PackratCache<T>(Optional<T> value, int mark) {
    }
}

