/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;

public class PlayDeadTimerTask {
    public static Task<LivingEntity> create() {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(MemoryModuleType.PLAY_DEAD_TICKS), context.queryMemoryOptional(MemoryModuleType.HURT_BY_ENTITY)).apply(context, (playDeadTicks, hurtByEntity) -> (world, entity, time) -> {
            int i = (Integer)context.getValue(playDeadTicks);
            if (i <= 0) {
                playDeadTicks.forget();
                hurtByEntity.forget();
                entity.getBrain().resetPossibleActivities();
            } else {
                playDeadTicks.remember(i - 1);
            }
            return true;
        }));
    }
}

