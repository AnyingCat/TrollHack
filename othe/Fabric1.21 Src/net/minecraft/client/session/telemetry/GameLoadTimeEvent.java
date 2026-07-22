/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.session.telemetry;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.session.telemetry.TelemetryEventProperty;
import net.minecraft.client.session.telemetry.TelemetryEventType;
import net.minecraft.client.session.telemetry.TelemetrySender;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class GameLoadTimeEvent {
    public static final GameLoadTimeEvent INSTANCE = new GameLoadTimeEvent(Ticker.systemTicker());
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Ticker ticker;
    private final Map<TelemetryEventProperty<Measurement>, Stopwatch> stopwatches = new HashMap<TelemetryEventProperty<Measurement>, Stopwatch>();
    private OptionalLong bootstrapTime = OptionalLong.empty();

    protected GameLoadTimeEvent(Ticker ticker) {
        this.ticker = ticker;
    }

    public synchronized void startTimer(TelemetryEventProperty<Measurement> property2) {
        this.addTimer(property2, (TelemetryEventProperty<Measurement> property) -> Stopwatch.createStarted(this.ticker));
    }

    public synchronized void addTimer(TelemetryEventProperty<Measurement> property2, Stopwatch stopwatch) {
        this.addTimer(property2, (TelemetryEventProperty<Measurement> property) -> stopwatch);
    }

    private synchronized void addTimer(TelemetryEventProperty<Measurement> property, Function<TelemetryEventProperty<Measurement>, Stopwatch> stopwatchProvider) {
        this.stopwatches.computeIfAbsent(property, stopwatchProvider);
    }

    public synchronized void stopTimer(TelemetryEventProperty<Measurement> property) {
        Stopwatch stopwatch = this.stopwatches.get(property);
        if (stopwatch == null) {
            LOGGER.warn("Attempted to end step for {} before starting it", (Object)property.id());
            return;
        }
        if (stopwatch.isRunning()) {
            stopwatch.stop();
        }
    }

    public void send(TelemetrySender sender) {
        sender.send(TelemetryEventType.GAME_LOAD_TIMES, properties -> {
            GameLoadTimeEvent gameLoadTimeEvent = this;
            synchronized (gameLoadTimeEvent) {
                this.stopwatches.forEach((property, stopwatch) -> {
                    if (!stopwatch.isRunning()) {
                        long l = stopwatch.elapsed(TimeUnit.MILLISECONDS);
                        properties.put(property, new Measurement((int)l));
                    } else {
                        LOGGER.warn("Measurement {} was discarded since it was still ongoing when the event {} was sent.", (Object)property.id(), (Object)TelemetryEventType.GAME_LOAD_TIMES.getId());
                    }
                });
                this.bootstrapTime.ifPresent(bootstrapTime -> properties.put(TelemetryEventProperty.LOAD_TIME_BOOTSTRAP_MS, new Measurement((int)bootstrapTime)));
                this.stopwatches.clear();
            }
        });
    }

    public synchronized void setBootstrapTime(long bootstrapTime) {
        this.bootstrapTime = OptionalLong.of(bootstrapTime);
    }

    @Environment(value=EnvType.CLIENT)
    public record Measurement(int millis) {
        public static final Codec<Measurement> CODEC = Codec.INT.xmap(Measurement::new, measurement -> measurement.millis);
    }
}

