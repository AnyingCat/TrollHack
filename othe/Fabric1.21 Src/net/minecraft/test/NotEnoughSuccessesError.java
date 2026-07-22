/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.test;

import net.minecraft.test.GameTestState;

class NotEnoughSuccessesError
extends Throwable {
    public NotEnoughSuccessesError(int attempts, int successes, GameTestState test) {
        super("Not enough successes: " + successes + " out of " + attempts + " attempts. Required successes: " + test.getRequiredSuccesses() + ". max attempts: " + test.getMaxAttempts() + ".", test.getThrowable());
    }
}

