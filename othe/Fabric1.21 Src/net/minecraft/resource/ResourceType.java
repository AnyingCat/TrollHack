/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource;

public enum ResourceType {
    CLIENT_RESOURCES("assets"),
    SERVER_DATA("data");

    private final String directory;

    private ResourceType(String directory) {
        this.directory = directory;
    }

    public String getDirectory() {
        return this.directory;
    }
}

