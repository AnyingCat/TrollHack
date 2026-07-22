/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.world;

import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.world.chunk.ChunkGenerationStep;
import net.minecraft.world.chunk.ChunkGenerationSteps;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class ChunkLevels {
    private static final int FULL = 33;
    private static final int BLOCK_TICKING = 32;
    private static final int ENTITY_TICKING = 31;
    private static final ChunkGenerationStep FULL_GENERATION_STEP = ChunkGenerationSteps.GENERATION.get(ChunkStatus.FULL);
    public static final int FULL_GENERATION_REQUIRED_LEVEL = FULL_GENERATION_STEP.accumulatedDependencies().getMaxLevel();
    public static final int INACCESSIBLE = 33 + FULL_GENERATION_REQUIRED_LEVEL;

    @Nullable
    public static ChunkStatus getStatus(int level) {
        return ChunkLevels.getStatusForAdditionalLevel(level - 33, null);
    }

    @Nullable
    @Contract(value="_,!null->!null;_,_->_")
    public static ChunkStatus getStatusForAdditionalLevel(int additionalLevel, @Nullable ChunkStatus emptyStatus) {
        if (additionalLevel > FULL_GENERATION_REQUIRED_LEVEL) {
            return emptyStatus;
        }
        if (additionalLevel <= 0) {
            return ChunkStatus.FULL;
        }
        return FULL_GENERATION_STEP.accumulatedDependencies().get(additionalLevel);
    }

    public static ChunkStatus getStatusForAdditionalLevel(int level) {
        return ChunkLevels.getStatusForAdditionalLevel(level, ChunkStatus.EMPTY);
    }

    public static int getLevelFromStatus(ChunkStatus status) {
        return 33 + FULL_GENERATION_STEP.getAdditionalLevel(status);
    }

    public static ChunkLevelType getType(int level) {
        if (level <= 31) {
            return ChunkLevelType.ENTITY_TICKING;
        }
        if (level <= 32) {
            return ChunkLevelType.BLOCK_TICKING;
        }
        if (level <= 33) {
            return ChunkLevelType.FULL;
        }
        return ChunkLevelType.INACCESSIBLE;
    }

    public static int getLevelFromType(ChunkLevelType type) {
        return switch (type) {
            default -> throw new MatchException(null, null);
            case ChunkLevelType.INACCESSIBLE -> INACCESSIBLE;
            case ChunkLevelType.FULL -> 33;
            case ChunkLevelType.BLOCK_TICKING -> 32;
            case ChunkLevelType.ENTITY_TICKING -> 31;
        };
    }

    public static boolean shouldTickEntities(int level) {
        return level <= 31;
    }

    public static boolean shouldTickBlocks(int level) {
        return level <= 32;
    }

    public static boolean isAccessible(int level) {
        return level <= INACCESSIBLE;
    }
}

