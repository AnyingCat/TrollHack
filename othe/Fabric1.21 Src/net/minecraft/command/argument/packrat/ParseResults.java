/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument.packrat;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import java.util.Objects;
import net.minecraft.command.argument.packrat.Symbol;
import org.jetbrains.annotations.Nullable;

public final class ParseResults {
    private final Object2ObjectMap<Symbol<?>, Object> results = new Object2ObjectArrayMap();

    public <T> void put(Symbol<T> symbol, @Nullable T value) {
        this.results.put(symbol, value);
    }

    @Nullable
    public <T> T get(Symbol<T> symbol) {
        return (T)this.results.get(symbol);
    }

    public <T> T getOrThrow(Symbol<T> symbol) {
        return Objects.requireNonNull(this.get(symbol));
    }

    public <T> T getOrDefault(Symbol<T> symbol, T fallback) {
        return Objects.requireNonNullElse(this.get(symbol), fallback);
    }

    @Nullable
    @SafeVarargs
    public final <T> T getAny(Symbol<T> ... symbols) {
        for (Symbol<T> symbol : symbols) {
            T object = this.get(symbol);
            if (object == null) continue;
            return object;
        }
        return null;
    }

    @SafeVarargs
    public final <T> T getAnyOrThrow(Symbol<T> ... symbols) {
        return Objects.requireNonNull(this.getAny(symbols));
    }

    public String toString() {
        return this.results.toString();
    }

    public void putAll(ParseResults results) {
        this.results.putAll(results.results);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ParseResults) {
            ParseResults parseResults = (ParseResults)o;
            return this.results.equals(parseResults.results);
        }
        return false;
    }

    public int hashCode() {
        return this.results.hashCode();
    }
}

