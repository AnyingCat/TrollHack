/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.session.telemetry;

import java.time.Duration;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.client.session.telemetry.PerformanceMetricsEvent;
import net.minecraft.client.session.telemetry.PropertyMap;
import net.minecraft.client.session.telemetry.TelemetryEventProperty;
import net.minecraft.client.session.telemetry.TelemetryEventType;
import net.minecraft.client.session.telemetry.TelemetrySender;
import net.minecraft.client.session.telemetry.WorldLoadTimesEvent;
import net.minecraft.client.session.telemetry.WorldLoadedEvent;
import net.minecraft.client.session.telemetry.WorldUnloadedEvent;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class WorldSession {
    private final UUID sessionId = UUID.randomUUID();
    private final TelemetrySender sender;
    private final WorldLoadedEvent worldLoadedEvent;
    private final WorldUnloadedEvent worldUnloadedEvent = new WorldUnloadedEvent();
    private final PerformanceMetricsEvent performanceMetricsEvent;
    private final WorldLoadTimesEvent worldLoadTimesEvent;

    public WorldSession(TelemetrySender sender, boolean newWorld, @Nullable Duration worldLoadTime, @Nullable String minigameName) {
        this.worldLoadedEvent = new WorldLoadedEvent(minigameName);
        this.performanceMetricsEvent = new PerformanceMetricsEvent();
        this.worldLoadTimesEvent = new WorldLoadTimesEvent(newWorld, worldLoadTime);
        this.sender = sender.decorate(builder -> {
            this.worldLoadedEvent.putServerType((PropertyMap.Builder)builder);
            builder.put(TelemetryEventProperty.WORLD_SESSION_ID, this.sessionId);
        });
    }

    public void tick() {
        this.performanceMetricsEvent.tick(this.sender);
    }

    public void setGameMode(GameMode gameMode, boolean hardcore) {
        this.worldLoadedEvent.setGameMode(gameMode, hardcore);
        this.worldUnloadedEvent.start();
        this.onLoad();
    }

    public void setBrand(String brand) {
        this.worldLoadedEvent.setBrand(brand);
        this.onLoad();
    }

    public void setTick(long tick) {
        this.worldUnloadedEvent.setTick(tick);
    }

    public void onLoad() {
        if (this.worldLoadedEvent.send(this.sender)) {
            this.worldLoadTimesEvent.send(this.sender);
            this.performanceMetricsEvent.start();
        }
    }

    public void onUnload() {
        this.worldLoadedEvent.send(this.sender);
        this.performanceMetricsEvent.disableSampling();
        this.worldUnloadedEvent.send(this.sender);
    }

    public void onAdvancementMade(World world, AdvancementEntry advancement) {
        Identifier identifier = advancement.id();
        if (advancement.value().sendsTelemetryEvent() && "minecraft".equals(identifier.getNamespace())) {
            long l = world.getTime();
            this.sender.send(TelemetryEventType.ADVANCEMENT_MADE, properties -> {
                properties.put(TelemetryEventProperty.ADVANCEMENT_ID, identifier.toString());
                properties.put(TelemetryEventProperty.ADVANCEMENT_GAME_TIME, l);
            });
        }
    }
}

