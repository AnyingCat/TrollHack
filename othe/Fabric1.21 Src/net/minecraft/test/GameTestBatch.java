/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.test;

import java.util.Collection;
import java.util.function.Consumer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTestState;

public record GameTestBatch(String id, Collection<GameTestState> states, Consumer<ServerWorld> beforeBatchFunction, Consumer<ServerWorld> afterBatchFunction) {
    public static final String DEFAULT_BATCH = "defaultBatch";

    public GameTestBatch {
        if (testFunctions.isEmpty()) {
            throw new IllegalArgumentException("A GameTestBatch must include at least one GameTestInfo!");
        }
    }
}

