/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.profiler;

public enum SampleType {
    PATH_FINDING("pathfinding"),
    EVENT_LOOPS("event-loops"),
    MAIL_BOXES("mailboxes"),
    TICK_LOOP("ticking"),
    JVM("jvm"),
    CHUNK_RENDERING("chunk rendering"),
    CHUNK_RENDERING_DISPATCHING("chunk rendering dispatching"),
    CPU("cpu"),
    GPU("gpu");

    private final String name;

    private SampleType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}

