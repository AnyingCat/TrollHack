/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.chunk;

import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;

public class ChunkGenerators {
    public static MapCodec<? extends ChunkGenerator> registerAndGetDefault(Registry<MapCodec<? extends ChunkGenerator>> registry) {
        Registry.register(registry, "noise", NoiseChunkGenerator.CODEC);
        Registry.register(registry, "flat", FlatChunkGenerator.CODEC);
        return Registry.register(registry, "debug", DebugChunkGenerator.CODEC);
    }
}

