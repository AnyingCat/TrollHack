/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network;

public interface QueryableServer {
    public String getServerMotd();

    public String getVersion();

    public int getCurrentPlayerCount();

    public int getMaxPlayerCount();
}

