/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.profiler.log;

import net.minecraft.util.profiler.log.DebugSampleLog;

public abstract class ArrayDebugSampleLog
implements DebugSampleLog {
    protected final long[] defaults;
    protected final long[] values;

    protected ArrayDebugSampleLog(int size, long[] defaults) {
        if (defaults.length != size) {
            throw new IllegalArgumentException("defaults have incorrect length of " + defaults.length);
        }
        this.values = new long[size];
        this.defaults = defaults;
    }

    @Override
    public void set(long[] values) {
        System.arraycopy(values, 0, this.values, 0, values.length);
        this.onPush();
        this.clearValues();
    }

    @Override
    public void push(long value) {
        this.values[0] = value;
        this.onPush();
        this.clearValues();
    }

    @Override
    public void push(long value, int column) {
        if (column < 1 || column >= this.values.length) {
            throw new IndexOutOfBoundsException(column + " out of bounds for dimensions " + this.values.length);
        }
        this.values[column] = value;
    }

    protected abstract void onPush();

    protected void clearValues() {
        System.arraycopy(this.defaults, 0, this.values, 0, this.defaults.length);
    }
}

