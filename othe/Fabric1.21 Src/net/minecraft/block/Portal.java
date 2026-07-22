/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import org.jetbrains.annotations.Nullable;

public interface Portal {
    default public int getPortalDelay(ServerWorld world, Entity entity) {
        return 0;
    }

    @Nullable
    public TeleportTarget createTeleportTarget(ServerWorld var1, Entity var2, BlockPos var3);

    default public Effect getPortalEffect() {
        return Effect.NONE;
    }

    public static enum Effect {
        CONFUSION,
        NONE;

    }
}

