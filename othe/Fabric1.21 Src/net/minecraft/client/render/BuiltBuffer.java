/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render;

import com.mojang.blaze3d.systems.VertexSorter;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.util.BufferAllocator;
import org.apache.commons.lang3.mutable.MutableLong;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

@Environment(value=EnvType.CLIENT)
public class BuiltBuffer
implements AutoCloseable {
    private final BufferAllocator.CloseableBuffer buffer;
    @Nullable
    private BufferAllocator.CloseableBuffer sortedBuffer;
    private final DrawParameters drawParameters;

    public BuiltBuffer(BufferAllocator.CloseableBuffer buffer, DrawParameters drawParameters) {
        this.buffer = buffer;
        this.drawParameters = drawParameters;
    }

    private static Vector3f[] collectCentroids(ByteBuffer buf, int vertexCount, VertexFormat format) {
        int i = format.getOffset(VertexFormatElement.POSITION);
        if (i == -1) {
            throw new IllegalArgumentException("Cannot identify quad centers with no position element");
        }
        FloatBuffer floatBuffer = buf.asFloatBuffer();
        int j = format.getVertexSizeByte() / 4;
        int k = j * 4;
        int l = vertexCount / 4;
        Vector3f[] vector3fs = new Vector3f[l];
        for (int m = 0; m < l; ++m) {
            int n = m * k + i;
            int o = n + j * 2;
            float f = floatBuffer.get(n + 0);
            float g = floatBuffer.get(n + 1);
            float h = floatBuffer.get(n + 2);
            float p = floatBuffer.get(o + 0);
            float q = floatBuffer.get(o + 1);
            float r = floatBuffer.get(o + 2);
            vector3fs[m] = new Vector3f((f + p) / 2.0f, (g + q) / 2.0f, (h + r) / 2.0f);
        }
        return vector3fs;
    }

    public ByteBuffer getBuffer() {
        return this.buffer.getBuffer();
    }

    @Nullable
    public ByteBuffer getSortedBuffer() {
        return this.sortedBuffer != null ? this.sortedBuffer.getBuffer() : null;
    }

    public DrawParameters getDrawParameters() {
        return this.drawParameters;
    }

    @Nullable
    public SortState sortQuads(BufferAllocator allocator, VertexSorter sorter) {
        if (this.drawParameters.mode() != VertexFormat.DrawMode.QUADS) {
            return null;
        }
        Vector3f[] vector3fs = BuiltBuffer.collectCentroids(this.buffer.getBuffer(), this.drawParameters.vertexCount(), this.drawParameters.format());
        SortState sortState = new SortState(vector3fs, this.drawParameters.indexType());
        this.sortedBuffer = sortState.sortAndStore(allocator, sorter);
        return sortState;
    }

    @Override
    public void close() {
        this.buffer.close();
        if (this.sortedBuffer != null) {
            this.sortedBuffer.close();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record DrawParameters(VertexFormat format, int vertexCount, int indexCount, VertexFormat.DrawMode mode, VertexFormat.IndexType indexType) {
    }

    @Environment(value=EnvType.CLIENT)
    public record SortState(Vector3f[] centroids, VertexFormat.IndexType indexType) {
        @Nullable
        public BufferAllocator.CloseableBuffer sortAndStore(BufferAllocator allocator, VertexSorter sorter) {
            int[] is = sorter.sort(this.centroids);
            long l = allocator.allocate(is.length * 6 * this.indexType.size);
            IntConsumer intConsumer = this.getStorer(l, this.indexType);
            for (int i : is) {
                intConsumer.accept(i * 4 + 0);
                intConsumer.accept(i * 4 + 1);
                intConsumer.accept(i * 4 + 2);
                intConsumer.accept(i * 4 + 2);
                intConsumer.accept(i * 4 + 3);
                intConsumer.accept(i * 4 + 0);
            }
            return allocator.getAllocated();
        }

        private IntConsumer getStorer(long pointer, VertexFormat.IndexType indexTyp) {
            MutableLong mutableLong = new MutableLong(pointer);
            return switch (indexTyp) {
                default -> throw new MatchException(null, null);
                case VertexFormat.IndexType.SHORT -> i -> MemoryUtil.memPutShort(mutableLong.getAndAdd(2L), (short)i);
                case VertexFormat.IndexType.INT -> i -> MemoryUtil.memPutInt(mutableLong.getAndAdd(4L), i);
            };
        }
    }
}

