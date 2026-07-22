/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.profiling.jfr.sample;

import jdk.jfr.consumer.RecordedEvent;

public record CpuLoadSample(double jvm, double userJvm, double system) {
    public static CpuLoadSample fromEvent(RecordedEvent event) {
        return new CpuLoadSample(event.getFloat("jvmSystem"), event.getFloat("jvmUser"), event.getFloat("machineTotal"));
    }
}

