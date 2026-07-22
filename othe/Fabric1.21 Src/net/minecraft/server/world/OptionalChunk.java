/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.world;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public interface OptionalChunk<T> {
    public static <T> OptionalChunk<T> of(T chunk) {
        return new ActualChunk<T>(chunk);
    }

    public static <T> OptionalChunk<T> of(String error) {
        return OptionalChunk.of(() -> error);
    }

    public static <T> OptionalChunk<T> of(Supplier<String> error) {
        return new LoadFailure(error);
    }

    public boolean isPresent();

    @Nullable
    public T orElse(@Nullable T var1);

    @Nullable
    public static <R> R orElse(OptionalChunk<? extends R> optionalChunk, @Nullable R other) {
        R object = optionalChunk.orElse(null);
        return object != null ? object : (R)other;
    }

    @Nullable
    public String getError();

    public OptionalChunk<T> ifPresent(Consumer<T> var1);

    public <R> OptionalChunk<R> map(Function<T, R> var1);

    public <E extends Throwable> T orElseThrow(Supplier<E> var1) throws E;

    public record ActualChunk<T>(T value) implements OptionalChunk<T>
    {
        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public T orElse(@Nullable T other) {
            return this.value;
        }

        @Override
        @Nullable
        public String getError() {
            return null;
        }

        @Override
        public OptionalChunk<T> ifPresent(Consumer<T> callback) {
            callback.accept(this.value);
            return this;
        }

        @Override
        public <R> OptionalChunk<R> map(Function<T, R> mapper) {
            return new ActualChunk<R>(mapper.apply(this.value));
        }

        @Override
        public <E extends Throwable> T orElseThrow(Supplier<E> exceptionSupplier) throws E {
            return this.value;
        }
    }

    public record LoadFailure<T>(Supplier<String> error) implements OptionalChunk<T>
    {
        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        @Nullable
        public T orElse(@Nullable T other) {
            return other;
        }

        @Override
        public String getError() {
            return this.error.get();
        }

        @Override
        public OptionalChunk<T> ifPresent(Consumer<T> callback) {
            return this;
        }

        @Override
        public <R> OptionalChunk<R> map(Function<T, R> mapper) {
            return new LoadFailure<T>(this.error);
        }

        @Override
        public <E extends Throwable> T orElseThrow(Supplier<E> exceptionSupplier) throws E {
            throw (Throwable)exceptionSupplier.get();
        }
    }
}

