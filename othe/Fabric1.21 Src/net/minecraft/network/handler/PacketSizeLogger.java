/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.handler;

import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.util.profiler.MultiValueDebugSampleLogImpl;

public class PacketSizeLogger {
    private final AtomicInteger packetSizeInBytes = new AtomicInteger();
    private final MultiValueDebugSampleLogImpl log;

    public PacketSizeLogger(MultiValueDebugSampleLogImpl log) {
        this.log = log;
    }

    public void increment(int bytes) {
        this.packetSizeInBytes.getAndAdd(bytes);
    }

    public void push() {
        this.log.push(this.packetSizeInBytes.getAndSet(0));
    }
}

