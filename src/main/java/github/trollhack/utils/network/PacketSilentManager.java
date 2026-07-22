package github.trollhack.utils.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PacketSilentManager {
    private static final Set<Packet<?>> silentPackets = ConcurrentHashMap.newKeySet();

    public static void addSilent(Packet<?> packet) {
        silentPackets.add(packet);
    }

    public static boolean removeSilent(Packet<?> packet) {
        return silentPackets.remove(packet);
    }

    public static boolean isSilent(Packet<?> packet) {
        return silentPackets.contains(packet);
    }

    public static void sendSilent(Packet<?> packet) {
        silentPackets.add(packet);
        if (MinecraftClient.getInstance().getNetworkHandler() != null) {
            MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
        }
    }
}
