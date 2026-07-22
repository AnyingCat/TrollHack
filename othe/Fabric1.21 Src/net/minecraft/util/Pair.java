/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

public class Pair<A, B> {
    private A left;
    private B right;

    public Pair(A left, B right) {
        this.left = left;
        this.right = right;
    }

    public A getLeft() {
        return this.left;
    }

    public void setLeft(A left) {
        this.left = left;
    }

    public B getRight() {
        return this.right;
    }

    public void setRight(B right) {
        this.right = right;
    }
}

