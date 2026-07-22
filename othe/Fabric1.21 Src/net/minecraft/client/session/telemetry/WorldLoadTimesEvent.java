/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.session.telemetry;

import java.time.Duration;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.session.telemetry.TelemetryEventProperty;
import net.minecraft.client.session.telemetry.TelemetryEventType;
import net.minecraft.client.session.telemetry.TelemetrySender;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class WorldLoadTimesEvent {
    private final boolean newWorld;
    @Nullable
    private final Duration worldLoadTime;

    public WorldLoadTimesEvent(boolean newWorld, @Nullable Duration worldLoadTime) {
        this.worldLoadTime = worldLoadTime;
        this.newWorld = newWorld;
    }

    public void send(TelemetrySender sender) {
        if (this.worldLoadTime != null) {
            sender.send(TelemetryEventType.WORLD_LOAD_TIMES, builder -> {
                builder.put(TelemetryEventProperty.WORLD_LOAD_TIME_MS, (int)this.worldLoadTime.toMillis());
                builder.put(TelemetryEventProperty.NEW_WORLD, this.newWorld);
            });
        }
    }
}

