/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.profiler.log;

import net.minecraft.network.packet.s2c.play.DebugSampleS2CPacket;
import net.minecraft.server.SampleSubscriptionTracker;
import net.minecraft.util.profiler.log.ArrayDebugSampleLog;
import net.minecraft.util.profiler.log.DebugSampleType;

public class SubscribableDebugSampleLog
extends ArrayDebugSampleLog {
    private final SampleSubscriptionTracker tracker;
    private final DebugSampleType type;

    public SubscribableDebugSampleLog(int size, SampleSubscriptionTracker tracker, DebugSampleType type) {
        this(size, tracker, type, new long[size]);
    }

    public SubscribableDebugSampleLog(int size, SampleSubscriptionTracker tracker, DebugSampleType type, long[] defaults) {
        super(size, defaults);
        this.tracker = tracker;
        this.type = type;
    }

    @Override
    protected void onPush() {
        this.tracker.sendPacket(new DebugSampleS2CPacket((long[])this.values.clone(), this.type));
    }
}

