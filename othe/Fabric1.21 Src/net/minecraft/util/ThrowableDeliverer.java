/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import org.jetbrains.annotations.Nullable;

public class ThrowableDeliverer<T extends Throwable> {
    @Nullable
    private T throwable;

    public void add(T throwable) {
        if (this.throwable == null) {
            this.throwable = throwable;
        } else {
            ((Throwable)this.throwable).addSuppressed((Throwable)throwable);
        }
    }

    public void deliver() throws T {
        if (this.throwable != null) {
            throw this.throwable;
        }
    }
}

