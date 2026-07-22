/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk;

import net.minecraft.server.world.ChunkTaskPrioritySystem;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.thread.MessageListener;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public record ChunkGenerationContext(ServerWorld world, ChunkGenerator generator, StructureTemplateManager structureManager, ServerLightingProvider lightingProvider, MessageListener<ChunkTaskPrioritySystem.Task<Runnable>> mainThreadMailBox) {
}

