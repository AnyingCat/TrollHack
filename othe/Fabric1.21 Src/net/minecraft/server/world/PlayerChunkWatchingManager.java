/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.world;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Set;
import net.minecraft.server.network.ServerPlayerEntity;

public final class PlayerChunkWatchingManager {
    private final Object2BooleanMap<ServerPlayerEntity> watchingPlayers = new Object2BooleanOpenHashMap<ServerPlayerEntity>();

    public Set<ServerPlayerEntity> getPlayersWatchingChunk() {
        return this.watchingPlayers.keySet();
    }

    public void add(ServerPlayerEntity player, boolean inactive) {
        this.watchingPlayers.put(player, inactive);
    }

    public void remove(ServerPlayerEntity player) {
        this.watchingPlayers.removeBoolean(player);
    }

    public void disableWatch(ServerPlayerEntity player) {
        this.watchingPlayers.replace(player, true);
    }

    public void enableWatch(ServerPlayerEntity player) {
        this.watchingPlayers.replace(player, false);
    }

    public boolean isWatchInactive(ServerPlayerEntity player) {
        return this.watchingPlayers.getOrDefault((Object)player, true);
    }

    public boolean isWatchDisabled(ServerPlayerEntity player) {
        return this.watchingPlayers.getBoolean(player);
    }
}

