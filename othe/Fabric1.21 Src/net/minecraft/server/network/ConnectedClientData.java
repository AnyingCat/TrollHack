/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;

public record ConnectedClientData(GameProfile gameProfile, int latency, SyncedClientOptions syncedOptions, boolean transferred) {
    public static ConnectedClientData createDefault(GameProfile profile, boolean bl) {
        return new ConnectedClientData(profile, 0, SyncedClientOptions.createDefault(), bl);
    }
}

