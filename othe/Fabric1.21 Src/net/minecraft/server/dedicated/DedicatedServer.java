/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated;

import net.minecraft.network.QueryableServer;
import net.minecraft.server.dedicated.ServerPropertiesHandler;

public interface DedicatedServer
extends QueryableServer {
    public ServerPropertiesHandler getProperties();

    public String getHostname();

    public int getPort();

    public String getMotd();

    public String[] getPlayerNames();

    public String getLevelName();

    public String getPlugins();

    public String executeRconCommand(String var1);
}

