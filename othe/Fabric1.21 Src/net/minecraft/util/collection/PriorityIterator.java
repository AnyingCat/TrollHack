/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.collection;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Queues;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Deque;
import org.jetbrains.annotations.Nullable;

/**
 * A queue-like iterator that orders its values by the priority, or the insertion order
 * if the priorities equal.
 */
public final class PriorityIterator<T>
extends AbstractIterator<T> {
    private static final int LOWEST_PRIORITY = Integer.MIN_VALUE;
    @Nullable
    private Deque<T> maxPriorityQueue = null;
    private int maxPriority = Integer.MIN_VALUE;
    private final Int2ObjectMap<Deque<T>> queuesByPriority = new Int2ObjectOpenHashMap<Deque<T>>();

    /**
     * Adds {@code value} with the priority {@code priority}.
     */
    public void enqueue(T value, int priority) {
        if (priority == this.maxPriority && this.maxPriorityQueue != null) {
            this.maxPriorityQueue.addLast(value);
            return;
        }
        Deque deque = this.queuesByPriority.computeIfAbsent(priority, p -> Queues.newArrayDeque());
        deque.addLast(value);
        if (priority >= this.maxPriority) {
            this.maxPriorityQueue = deque;
            this.maxPriority = priority;
        }
    }

    @Override
    @Nullable
    protected T computeNext() {
        if (this.maxPriorityQueue == null) {
            return this.endOfData();
        }
        T object = this.maxPriorityQueue.removeFirst();
        if (object == null) {
            return this.endOfData();
        }
        if (this.maxPriorityQueue.isEmpty()) {
            this.refreshMaxPriority();
        }
        return object;
    }

    private void refreshMaxPriority() {
        int i = Integer.MIN_VALUE;
        Deque deque = null;
        for (Int2ObjectMap.Entry entry : Int2ObjectMaps.fastIterable(this.queuesByPriority)) {
            Deque deque2 = (Deque)entry.getValue();
            int j = entry.getIntKey();
            if (j <= i || deque2.isEmpty()) continue;
            i = j;
            deque = deque2;
            if (j != this.maxPriority - 1) continue;
            break;
        }
        this.maxPriority = i;
        this.maxPriorityQueue = deque;
    }
}

