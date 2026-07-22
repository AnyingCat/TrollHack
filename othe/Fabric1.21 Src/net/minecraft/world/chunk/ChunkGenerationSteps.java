/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.world.chunk.ChunkGenerating;
import net.minecraft.world.chunk.ChunkGenerationStep;
import net.minecraft.world.chunk.ChunkStatus;

public record ChunkGenerationSteps(ImmutableList<ChunkGenerationStep> steps) {
    public static final ChunkGenerationSteps GENERATION = new Builder().then(ChunkStatus.EMPTY, builder -> builder).then(ChunkStatus.STRUCTURE_STARTS, builder -> builder.task(ChunkGenerating::generateStructures)).then(ChunkStatus.STRUCTURE_REFERENCES, builder -> builder.dependsOn(ChunkStatus.STRUCTURE_STARTS, 8).task(ChunkGenerating::generateStructureReferences)).then(ChunkStatus.BIOMES, builder -> builder.dependsOn(ChunkStatus.STRUCTURE_STARTS, 8).task(ChunkGenerating::populateBiomes)).then(ChunkStatus.NOISE, builder -> builder.dependsOn(ChunkStatus.STRUCTURE_STARTS, 8).dependsOn(ChunkStatus.BIOMES, 1).blockStateWriteRadius(0).task(ChunkGenerating::populateNoise)).then(ChunkStatus.SURFACE, builder -> builder.dependsOn(ChunkStatus.STRUCTURE_STARTS, 8).dependsOn(ChunkStatus.BIOMES, 1).blockStateWriteRadius(0).task(ChunkGenerating::buildSurface)).then(ChunkStatus.CARVERS, builder -> builder.dependsOn(ChunkStatus.STRUCTURE_STARTS, 8).blockStateWriteRadius(0).task(ChunkGenerating::carve)).then(ChunkStatus.FEATURES, builder -> builder.dependsOn(ChunkStatus.STRUCTURE_STARTS, 8).dependsOn(ChunkStatus.CARVERS, 1).blockStateWriteRadius(1).task(ChunkGenerating::generateFeatures)).then(ChunkStatus.INITIALIZE_LIGHT, builder -> builder.task(ChunkGenerating::initializeLight)).then(ChunkStatus.LIGHT, builder -> builder.dependsOn(ChunkStatus.INITIALIZE_LIGHT, 1).task(ChunkGenerating::light)).then(ChunkStatus.SPAWN, builder -> builder.dependsOn(ChunkStatus.BIOMES, 1).task(ChunkGenerating::generateEntities)).then(ChunkStatus.FULL, builder -> builder.task(ChunkGenerating::convertToFullChunk)).build();
    public static final ChunkGenerationSteps LOADING = new Builder().then(ChunkStatus.EMPTY, builder -> builder).then(ChunkStatus.STRUCTURE_STARTS, builder -> builder.task(ChunkGenerating::loadStructures)).then(ChunkStatus.STRUCTURE_REFERENCES, builder -> builder).then(ChunkStatus.BIOMES, builder -> builder).then(ChunkStatus.NOISE, builder -> builder).then(ChunkStatus.SURFACE, builder -> builder).then(ChunkStatus.CARVERS, builder -> builder).then(ChunkStatus.FEATURES, builder -> builder).then(ChunkStatus.INITIALIZE_LIGHT, builder -> builder.task(ChunkGenerating::initializeLight)).then(ChunkStatus.LIGHT, builder -> builder.dependsOn(ChunkStatus.INITIALIZE_LIGHT, 1).task(ChunkGenerating::light)).then(ChunkStatus.SPAWN, builder -> builder).then(ChunkStatus.FULL, builder -> builder.task(ChunkGenerating::convertToFullChunk)).build();

    public ChunkGenerationStep get(ChunkStatus status) {
        return (ChunkGenerationStep)this.steps.get(status.getIndex());
    }

    public static class Builder {
        private final List<ChunkGenerationStep> steps = new ArrayList<ChunkGenerationStep>();

        public ChunkGenerationSteps build() {
            return new ChunkGenerationSteps(ImmutableList.copyOf(this.steps));
        }

        public Builder then(ChunkStatus status, UnaryOperator<ChunkGenerationStep.Builder> stepFactory) {
            ChunkGenerationStep.Builder builder = this.steps.isEmpty() ? new ChunkGenerationStep.Builder(status) : new ChunkGenerationStep.Builder(status, this.steps.getLast());
            this.steps.add(((ChunkGenerationStep.Builder)stepFactory.apply(builder)).build());
            return this;
        }
    }
}

