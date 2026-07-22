/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.profiler;

import net.minecraft.util.profiler.Profiler;

public interface Recorder {
    public void stop();

    public void forceStop();

    public void startTick();

    public boolean isActive();

    public Profiler getProfiler();

    public void endTick();
}

