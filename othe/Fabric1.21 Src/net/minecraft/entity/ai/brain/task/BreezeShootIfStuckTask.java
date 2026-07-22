/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import java.util.Map;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Unit;

public class BreezeShootIfStuckTask
extends MultiTickTask<BreezeEntity> {
    public BreezeShootIfStuckTask() {
        super(Map.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.BREEZE_JUMP_INHALING, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.BREEZE_JUMP_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.BREEZE_SHOOT, MemoryModuleState.VALUE_ABSENT));
    }

    @Override
    protected boolean shouldRun(ServerWorld serverWorld, BreezeEntity breezeEntity) {
        return breezeEntity.hasVehicle() || breezeEntity.isTouchingWater() || breezeEntity.getStatusEffect(StatusEffects.LEVITATION) != null;
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld serverWorld, BreezeEntity breezeEntity, long l) {
        return false;
    }

    @Override
    protected void run(ServerWorld serverWorld, BreezeEntity breezeEntity, long l) {
        breezeEntity.getBrain().remember(MemoryModuleType.BREEZE_SHOOT, Unit.INSTANCE, 60L);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (BreezeEntity)entity, time);
    }
}

