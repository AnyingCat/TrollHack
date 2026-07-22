/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

@Environment(value=EnvType.CLIENT)
public class WorldLoadingState {
    private final ClientPlayerEntity player;
    private final ClientWorld world;
    private final WorldRenderer renderer;
    private Step currentStep = Step.WAITING_FOR_SERVER;

    public WorldLoadingState(ClientPlayerEntity player, ClientWorld world, WorldRenderer renderer) {
        this.player = player;
        this.world = world;
        this.renderer = renderer;
    }

    public void tick() {
        switch (this.currentStep.ordinal()) {
            case 0: 
            case 2: {
                break;
            }
            case 1: {
                BlockPos blockPos = this.player.getBlockPos();
                boolean bl = this.world.isOutOfHeightLimit(blockPos.getY());
                if (!bl && !this.renderer.isRenderingReady(blockPos) && !this.player.isSpectator() && this.player.isAlive()) break;
                this.currentStep = Step.LEVEL_READY;
            }
        }
    }

    public boolean isReady() {
        return this.currentStep == Step.LEVEL_READY;
    }

    public void handleChunksComingPacket() {
        if (this.currentStep == Step.WAITING_FOR_SERVER) {
            this.currentStep = Step.WAITING_FOR_PLAYER_CHUNK;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static enum Step {
        WAITING_FOR_SERVER,
        WAITING_FOR_PLAYER_CHUNK,
        LEVEL_READY;

    }
}

