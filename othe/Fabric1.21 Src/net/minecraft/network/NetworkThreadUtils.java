/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network;

import com.mojang.logging.LogUtils;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.thread.ThreadExecutor;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class NetworkThreadUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static <T extends PacketListener> void forceMainThread(Packet<T> packet, T listener, ServerWorld world) throws OffThreadException {
        NetworkThreadUtils.forceMainThread(packet, listener, world.getServer());
    }

    public static <T extends PacketListener> void forceMainThread(Packet<T> packet, T listener, ThreadExecutor<?> engine) throws OffThreadException {
        if (!engine.isOnThread()) {
            engine.executeSync(() -> {
                if (listener.accepts(packet)) {
                    try {
                        packet.apply(listener);
                    } catch (Exception exception) {
                        CrashException crashException;
                        if (exception instanceof CrashException && (crashException = (CrashException)exception).getCause() instanceof OutOfMemoryError) {
                            throw NetworkThreadUtils.createCrashException(exception, packet, listener);
                        }
                        listener.onPacketException(packet, exception);
                    }
                } else {
                    LOGGER.debug("Ignoring packet due to disconnection: {}", (Object)packet);
                }
            });
            throw OffThreadException.INSTANCE;
        }
    }

    public static <T extends PacketListener> CrashException createCrashException(Exception exception, Packet<T> packet, T listener) {
        if (exception instanceof CrashException) {
            CrashException crashException = (CrashException)exception;
            NetworkThreadUtils.fillCrashReport(crashException.getReport(), listener, packet);
            return crashException;
        }
        CrashReport crashReport = CrashReport.create(exception, "Main thread packet handler");
        NetworkThreadUtils.fillCrashReport(crashReport, listener, packet);
        return new CrashException(crashReport);
    }

    public static <T extends PacketListener> void fillCrashReport(CrashReport report, T listener, @Nullable Packet<T> packet) {
        if (packet != null) {
            CrashReportSection crashReportSection = report.addElement("Incoming Packet");
            crashReportSection.add("Type", () -> packet.getPacketId().toString());
            crashReportSection.add("Is Terminal", () -> Boolean.toString(packet.transitionsNetworkState()));
            crashReportSection.add("Is Skippable", () -> Boolean.toString(packet.isWritingErrorSkippable()));
        }
        listener.fillCrashReport(report);
    }
}

