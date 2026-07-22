/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server;

import com.google.common.collect.Maps;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import net.minecraft.network.packet.s2c.play.DebugSampleS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.log.DebugSampleType;

public class SampleSubscriptionTracker {
    public static final int STOP_TRACK_TICK = 200;
    public static final int STOP_TRACK_MS = 10000;
    private final PlayerManager playerManager;
    private final EnumMap<DebugSampleType, Map<ServerPlayerEntity, MeasureTimeTick>> subscriptionMap;
    private final Queue<PlayerSubscriptionData> pendingQueue = new LinkedList<PlayerSubscriptionData>();

    public SampleSubscriptionTracker(PlayerManager playerManager) {
        this.playerManager = playerManager;
        this.subscriptionMap = new EnumMap(DebugSampleType.class);
        for (DebugSampleType debugSampleType : DebugSampleType.values()) {
            this.subscriptionMap.put(debugSampleType, Maps.newHashMap());
        }
    }

    public boolean shouldPush(DebugSampleType type) {
        return !this.subscriptionMap.get((Object)type).isEmpty();
    }

    public void sendPacket(DebugSampleS2CPacket packet) {
        Set<ServerPlayerEntity> set = this.subscriptionMap.get((Object)packet.debugSampleType()).keySet();
        for (ServerPlayerEntity serverPlayerEntity : set) {
            serverPlayerEntity.networkHandler.sendPacket(packet);
        }
    }

    public void addPlayer(ServerPlayerEntity player, DebugSampleType type) {
        if (this.playerManager.isOperator(player.getGameProfile())) {
            this.pendingQueue.add(new PlayerSubscriptionData(player, type));
        }
    }

    public void tick(int tick) {
        long l = Util.getMeasuringTimeMs();
        this.onSubscription(l, tick);
        this.onUnsubscription(l, tick);
    }

    private void onSubscription(long time, int tick) {
        for (PlayerSubscriptionData playerSubscriptionData : this.pendingQueue) {
            this.subscriptionMap.get((Object)playerSubscriptionData.sampleType()).put(playerSubscriptionData.player(), new MeasureTimeTick(time, tick));
        }
    }

    private void onUnsubscription(long measuringTimeMs, int tick) {
        for (Map<ServerPlayerEntity, MeasureTimeTick> map : this.subscriptionMap.values()) {
            map.entrySet().removeIf(entry -> {
                boolean bl = !this.playerManager.isOperator(((ServerPlayerEntity)entry.getKey()).getGameProfile());
                MeasureTimeTick measureTimeTick = (MeasureTimeTick)entry.getValue();
                return bl || tick > measureTimeTick.tick() + 200 && measuringTimeMs > measureTimeTick.millis() + 10000L;
            });
        }
    }

    record PlayerSubscriptionData(ServerPlayerEntity player, DebugSampleType sampleType) {
    }

    record MeasureTimeTick(long millis, int tick) {
    }
}

