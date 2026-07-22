/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.profiling.jfr.event;

import jdk.jfr.Category;
import jdk.jfr.Enabled;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.ChunkCompressionFormat;
import net.minecraft.world.storage.StorageKey;

@Category(value={"Minecraft", "Storage"})
@StackTrace(value=false)
@Enabled(value=false)
public abstract class ChunkRegionEvent
extends Event {
    @Name(value="regionPosX")
    @Label(value="Region X Position")
    public final int regionPosX;
    @Name(value="regionPosZ")
    @Label(value="Region Z Position")
    public final int regionPosZ;
    @Name(value="localPosX")
    @Label(value="Local X Position")
    public final int localChunkPosX;
    @Name(value="localPosZ")
    @Label(value="Local Z Position")
    public final int localChunkPosZ;
    @Name(value="chunkPosX")
    @Label(value="Chunk X Position")
    public final int chunkPosX;
    @Name(value="chunkPosZ")
    @Label(value="Chunk Z Position")
    public final int chunkPosZ;
    @Name(value="level")
    @Label(value="Level Id")
    public final String worldId;
    @Name(value="dimension")
    @Label(value="Dimension")
    public final String dimension;
    @Name(value="type")
    @Label(value="Type")
    public final String type;
    @Name(value="compression")
    @Label(value="Compression")
    public final String compression;
    @Name(value="bytes")
    @Label(value="Bytes")
    public final int bytes;

    public ChunkRegionEvent(StorageKey key, ChunkPos chunkPos, ChunkCompressionFormat format, int bytes) {
        this.regionPosX = chunkPos.getRegionX();
        this.regionPosZ = chunkPos.getRegionZ();
        this.localChunkPosX = chunkPos.getRegionRelativeX();
        this.localChunkPosZ = chunkPos.getRegionRelativeZ();
        this.chunkPosX = chunkPos.x;
        this.chunkPosZ = chunkPos.z;
        this.worldId = key.level();
        this.dimension = key.dimension().getValue().toString();
        this.type = key.type();
        this.compression = "standard:" + format.getId();
        this.bytes = bytes;
    }

    public static class Names {
        public static final String REGION_POS_X = "regionPosX";
        public static final String REGION_POS_Z = "regionPosZ";
        public static final String LOCAL_POS_X = "localPosX";
        public static final String LOCAL_POS_Z = "localPosZ";
        public static final String CHUNK_POS_X = "chunkPosX";
        public static final String CHUNK_POS_Z = "chunkPosZ";
        public static final String LEVEL = "level";
        public static final String DIMENSION = "dimension";
        public static final String TYPE = "type";
        public static final String COMPRESSION = "compression";
        public static final String BYTES = "bytes";

        private Names() {
        }
    }
}

