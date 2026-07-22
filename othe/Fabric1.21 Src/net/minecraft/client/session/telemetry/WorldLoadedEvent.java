/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.session.telemetry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.session.telemetry.PropertyMap;
import net.minecraft.client.session.telemetry.TelemetryEventProperty;
import net.minecraft.client.session.telemetry.TelemetryEventType;
import net.minecraft.client.session.telemetry.TelemetrySender;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class WorldLoadedEvent {
    private boolean sent;
    @Nullable
    private TelemetryEventProperty.GameMode gameMode;
    @Nullable
    private String brand;
    @Nullable
    private final String minigameName;

    public WorldLoadedEvent(@Nullable String minigameName) {
        this.minigameName = minigameName;
    }

    public void putServerType(PropertyMap.Builder builder) {
        if (this.brand != null) {
            builder.put(TelemetryEventProperty.SERVER_MODDED, !this.brand.equals("vanilla"));
        }
        builder.put(TelemetryEventProperty.SERVER_TYPE, this.getServerType());
    }

    private TelemetryEventProperty.ServerType getServerType() {
        ServerInfo serverInfo = MinecraftClient.getInstance().getCurrentServerEntry();
        if (serverInfo != null && serverInfo.isRealm()) {
            return TelemetryEventProperty.ServerType.REALM;
        }
        if (MinecraftClient.getInstance().isIntegratedServerRunning()) {
            return TelemetryEventProperty.ServerType.LOCAL;
        }
        return TelemetryEventProperty.ServerType.OTHER;
    }

    public boolean send(TelemetrySender sender) {
        if (this.sent || this.gameMode == null || this.brand == null) {
            return false;
        }
        this.sent = true;
        sender.send(TelemetryEventType.WORLD_LOADED, adder -> {
            adder.put(TelemetryEventProperty.GAME_MODE, this.gameMode);
            if (this.minigameName != null) {
                adder.put(TelemetryEventProperty.REALMS_MAP_CONTENT, this.minigameName);
            }
        });
        return true;
    }

    public void setGameMode(GameMode gameMode, boolean hardcore) {
        this.gameMode = switch (gameMode) {
            default -> throw new MatchException(null, null);
            case GameMode.SURVIVAL -> {
                if (hardcore) {
                    yield TelemetryEventProperty.GameMode.HARDCORE;
                }
                yield TelemetryEventProperty.GameMode.SURVIVAL;
            }
            case GameMode.CREATIVE -> TelemetryEventProperty.GameMode.CREATIVE;
            case GameMode.ADVENTURE -> TelemetryEventProperty.GameMode.ADVENTURE;
            case GameMode.SPECTATOR -> TelemetryEventProperty.GameMode.SPECTATOR;
        };
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
}

