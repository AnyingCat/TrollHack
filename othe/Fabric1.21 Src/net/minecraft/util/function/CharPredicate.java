/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.function;

import java.util.Objects;

@FunctionalInterface
public interface CharPredicate {
    public boolean test(char var1);

    default public CharPredicate and(CharPredicate predicate) {
        Objects.requireNonNull(predicate);
        return c -> this.test(c) && predicate.test(c);
    }

    default public CharPredicate negate() {
        return c -> !this.test(c);
    }

    default public CharPredicate or(CharPredicate predicate) {
        Objects.requireNonNull(predicate);
        return c -> this.test(c) || predicate.test(c);
    }
}

