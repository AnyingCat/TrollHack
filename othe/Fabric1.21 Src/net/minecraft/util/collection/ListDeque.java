/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.collection;

import java.io.Serializable;
import java.util.Deque;
import java.util.List;
import java.util.RandomAccess;
import java.util.SequencedCollection;
import org.jetbrains.annotations.Nullable;

public interface ListDeque<T>
extends Serializable,
Cloneable,
Deque<T>,
List<T>,
RandomAccess {
    @Override
    public ListDeque<T> reversed();

    @Override
    public T getFirst();

    @Override
    public T getLast();

    @Override
    public void addFirst(T var1);

    @Override
    public void addLast(T var1);

    @Override
    public T removeFirst();

    @Override
    public T removeLast();

    @Override
    default public boolean offer(T object) {
        return this.offerLast(object);
    }

    @Override
    default public T remove() {
        return this.removeFirst();
    }

    @Override
    @Nullable
    default public T poll() {
        return (T)this.pollFirst();
    }

    @Override
    default public T element() {
        return this.getFirst();
    }

    @Override
    @Nullable
    default public T peek() {
        return (T)this.peekFirst();
    }

    @Override
    default public void push(T object) {
        this.addFirst(object);
    }

    @Override
    default public T pop() {
        return this.removeFirst();
    }

    @Override
    default public /* synthetic */ List reversed() {
        return this.reversed();
    }

    @Override
    default public /* synthetic */ SequencedCollection reversed() {
        return this.reversed();
    }

    @Override
    default public /* synthetic */ Deque reversed() {
        return this.reversed();
    }
}

