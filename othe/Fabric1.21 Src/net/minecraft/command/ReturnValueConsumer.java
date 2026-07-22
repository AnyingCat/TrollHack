/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command;

@FunctionalInterface
public interface ReturnValueConsumer {
    public static final ReturnValueConsumer EMPTY = new ReturnValueConsumer(){

        @Override
        public void onResult(boolean bl, int i) {
        }

        public String toString() {
            return "<empty>";
        }
    };

    public void onResult(boolean var1, int var2);

    default public void onSuccess(int successful) {
        this.onResult(true, successful);
    }

    default public void onFailure() {
        this.onResult(false, 0);
    }

    public static ReturnValueConsumer chain(ReturnValueConsumer a, ReturnValueConsumer b) {
        if (a == EMPTY) {
            return b;
        }
        if (b == EMPTY) {
            return a;
        }
        return (successful, returnValue) -> {
            a.onResult(successful, returnValue);
            b.onResult(successful, returnValue);
        };
    }
}

