/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network;

public enum NetworkSide {
    SERVERBOUND("serverbound"),
    CLIENTBOUND("clientbound");

    private final String name;

    private NetworkSide(String name) {
        this.name = name;
    }

    public NetworkSide getOpposite() {
        return this == CLIENTBOUND ? SERVERBOUND : CLIENTBOUND;
    }

    public String getName() {
        return this.name;
    }
}

