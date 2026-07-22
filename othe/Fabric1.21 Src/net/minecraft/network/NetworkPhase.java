/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network;

public enum NetworkPhase {
    HANDSHAKING("handshake"),
    PLAY("play"),
    STATUS("status"),
    LOGIN("login"),
    CONFIGURATION("configuration");

    private final String id;

    private NetworkPhase(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}

