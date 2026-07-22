/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;

public interface ErrorReporter {
    public ErrorReporter makeChild(String var1);

    public void report(String var1);

    public static class Impl
    implements ErrorReporter {
        private final Multimap<String, String> errors;
        private final Supplier<String> pathSupplier;
        @Nullable
        private String path;

        public Impl() {
            this(HashMultimap.create(), () -> "");
        }

        private Impl(Multimap<String, String> errors, Supplier<String> pathSupplier) {
            this.errors = errors;
            this.pathSupplier = pathSupplier;
        }

        private String getPath() {
            if (this.path == null) {
                this.path = this.pathSupplier.get();
            }
            return this.path;
        }

        @Override
        public ErrorReporter makeChild(String name) {
            return new Impl(this.errors, () -> this.getPath() + name);
        }

        @Override
        public void report(String message) {
            this.errors.put(this.getPath(), message);
        }

        public Multimap<String, String> getErrors() {
            return ImmutableMultimap.copyOf(this.errors);
        }

        public Optional<String> getErrorsAsString() {
            Multimap<String, String> multimap = this.getErrors();
            if (!multimap.isEmpty()) {
                String string = multimap.asMap().entrySet().stream().map(entry -> " at " + (String)entry.getKey() + ": " + String.join((CharSequence)"; ", (Iterable)entry.getValue())).collect(Collectors.joining("\n"));
                return Optional.of(string);
            }
            return Optional.empty();
        }
    }
}

