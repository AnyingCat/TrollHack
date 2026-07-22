/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.thread;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.slf4j.Logger;

/**
 * A functional interface that can enqueue completable futures.
 */
@FunctionalInterface
public interface FutureQueue {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static FutureQueue immediate(final Executor executor) {
        return new FutureQueue(){

            @Override
            public <T> void append(CompletableFuture<T> completableFuture, Consumer<T> consumer) {
                ((CompletableFuture)completableFuture.thenAcceptAsync((Consumer)consumer, executor)).exceptionally(throwable -> {
                    LOGGER.error("Task failed", (Throwable)throwable);
                    return null;
                });
            }
        };
    }

    default public void append(Runnable callback) {
        this.append(CompletableFuture.completedFuture(null), current -> callback.run());
    }

    public <T> void append(CompletableFuture<T> var1, Consumer<T> var2);
}

