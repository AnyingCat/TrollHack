/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.network;

import java.util.function.Consumer;
import net.minecraft.network.packet.Packet;

public interface ServerPlayerConfigurationTask {
    public void sendPacket(Consumer<Packet<?>> var1);

    public Key getKey();

    public record Key(String id) {
        @Override
        public String toString() {
            return this.id;
        }
    }
}

