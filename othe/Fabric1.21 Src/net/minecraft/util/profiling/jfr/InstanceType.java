/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.profiling.jfr;

import net.minecraft.server.MinecraftServer;

public enum InstanceType {
    CLIENT("client"),
    SERVER("server");

    private final String name;

    private InstanceType(String name) {
        this.name = name;
    }

    public static InstanceType get(MinecraftServer server) {
        return server.isDedicated() ? SERVER : CLIENT;
    }

    public String getName() {
        return this.name;
    }
}

