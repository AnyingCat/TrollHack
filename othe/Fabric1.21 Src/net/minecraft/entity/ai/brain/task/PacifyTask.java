/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;

public class PacifyTask {
    public static Task<LivingEntity> create(MemoryModuleType<?> requiredMemory, int duration) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryOptional(MemoryModuleType.ATTACK_TARGET), context.queryMemoryAbsent(MemoryModuleType.PACIFIED), context.queryMemoryValue(requiredMemory)).apply(context, context.supply(() -> "[BecomePassive if " + String.valueOf(requiredMemory) + " present]", (attackTarget, pacified, requiredMemoryResult) -> (world, entity, time) -> {
            pacified.remember(true, duration);
            attackTarget.forget();
            return true;
        })));
    }
}

