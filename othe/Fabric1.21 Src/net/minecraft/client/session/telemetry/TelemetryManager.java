/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.session.telemetry;

import com.google.common.base.Suppliers;
import com.mojang.authlib.minecraft.TelemetrySession;
import com.mojang.authlib.minecraft.UserApiService;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.telemetry.PropertyMap;
import net.minecraft.client.session.telemetry.SentTelemetryEvent;
import net.minecraft.client.session.telemetry.TelemetryEventProperty;
import net.minecraft.client.session.telemetry.TelemetryLogManager;
import net.minecraft.client.session.telemetry.TelemetryLogger;
import net.minecraft.client.session.telemetry.TelemetrySender;
import net.minecraft.client.session.telemetry.WorldSession;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TelemetryManager
implements AutoCloseable {
    private static final AtomicInteger NEXT_WORKER_ID = new AtomicInteger(1);
    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("Telemetry-Sender-#" + NEXT_WORKER_ID.getAndIncrement());
        return thread;
    });
    private final MinecraftClient client;
    private final UserApiService userApiService;
    private final PropertyMap propertyMap;
    private final Path logDirectory;
    private final CompletableFuture<Optional<TelemetryLogManager>> logManager;
    private final Supplier<TelemetrySender> lazySenderSupplier = Suppliers.memoize(this::computeSender);

    public TelemetryManager(MinecraftClient client, UserApiService userApiService, Session session) {
        this.client = client;
        this.userApiService = userApiService;
        PropertyMap.Builder builder = PropertyMap.builder();
        session.getXuid().ifPresent(xuid -> builder.put(TelemetryEventProperty.USER_ID, xuid));
        session.getClientId().ifPresent(clientId -> builder.put(TelemetryEventProperty.CLIENT_ID, clientId));
        builder.put(TelemetryEventProperty.MINECRAFT_SESSION_ID, UUID.randomUUID());
        builder.put(TelemetryEventProperty.GAME_VERSION, SharedConstants.getGameVersion().getId());
        builder.put(TelemetryEventProperty.OPERATING_SYSTEM, Util.getOperatingSystem().getName());
        builder.put(TelemetryEventProperty.PLATFORM, System.getProperty("os.name"));
        builder.put(TelemetryEventProperty.CLIENT_MODDED, MinecraftClient.getModStatus().isModded());
        builder.putIfNonNull(TelemetryEventProperty.LAUNCHER_NAME, MinecraftClient.getLauncherBrand());
        this.propertyMap = builder.build();
        this.logDirectory = client.runDirectory.toPath().resolve("logs/telemetry");
        this.logManager = TelemetryLogManager.create(this.logDirectory);
    }

    public WorldSession createWorldSession(boolean newWorld, @Nullable Duration worldLoadTime, @Nullable String minigameName) {
        return new WorldSession(this.computeSender(), newWorld, worldLoadTime, minigameName);
    }

    public TelemetrySender getSender() {
        return this.lazySenderSupplier.get();
    }

    private TelemetrySender computeSender() {
        if (!this.client.isTelemetryEnabledByApi()) {
            return TelemetrySender.NOOP;
        }
        TelemetrySession telemetrySession = this.userApiService.newTelemetrySession(EXECUTOR);
        if (!telemetrySession.isEnabled()) {
            return TelemetrySender.NOOP;
        }
        CompletionStage completableFuture = this.logManager.thenCompose(manager -> manager.map(TelemetryLogManager::getLogger).orElseGet(() -> CompletableFuture.completedFuture(Optional.empty())));
        return (eventType, adder) -> {
            if (eventType.isOptional() && !MinecraftClient.getInstance().isOptionalTelemetryEnabled()) {
                return;
            }
            PropertyMap.Builder builder = PropertyMap.builder();
            builder.putAll(this.propertyMap);
            builder.put(TelemetryEventProperty.EVENT_TIMESTAMP_UTC, Instant.now());
            builder.put(TelemetryEventProperty.OPT_IN, eventType.isOptional());
            adder.accept(builder);
            SentTelemetryEvent sentTelemetryEvent = new SentTelemetryEvent(eventType, builder.build());
            ((CompletableFuture)completableFuture).thenAccept(logger -> {
                if (logger.isEmpty()) {
                    return;
                }
                ((TelemetryLogger)logger.get()).log(sentTelemetryEvent);
                sentTelemetryEvent.createEvent(telemetrySession).send();
            });
        };
    }

    public Path getLogManager() {
        return this.logDirectory;
    }

    @Override
    public void close() {
        this.logManager.thenAccept(manager -> manager.ifPresent(TelemetryLogManager::close));
    }
}

