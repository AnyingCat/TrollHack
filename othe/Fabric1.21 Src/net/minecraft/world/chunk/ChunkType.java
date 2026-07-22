/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk;

/**
 * Specifies the type of a chunk
 */
public enum ChunkType {
    /**
     * A chunk which is incomplete and not loaded to the world yet.
     */
    PROTOCHUNK,
    /**
     * A chunk which is complete and bound to a world.
     */
    LEVELCHUNK;

}

