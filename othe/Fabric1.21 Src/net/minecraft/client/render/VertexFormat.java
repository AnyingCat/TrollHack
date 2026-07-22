/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.VertexFormatElement;
import org.jetbrains.annotations.Nullable;

/**
 * Defines what {@link VertexFormatElement elements} a vertex has.
 */
@Environment(value=EnvType.CLIENT)
public class VertexFormat {
    public static final int field_52099 = -1;
    private final List<VertexFormatElement> elements;
    private final List<String> names;
    private final int vertexSizeByte;
    private final int requiredMask;
    private final int[] offsetsByElementId = new int[32];
    @Nullable
    private VertexBuffer buffer;

    VertexFormat(List<VertexFormatElement> elements, List<String> names, IntList offsets, int vertexSizeByte) {
        this.elements = elements;
        this.names = names;
        this.vertexSizeByte = vertexSizeByte;
        this.requiredMask = elements.stream().mapToInt(VertexFormatElement::getBit).reduce(0, (a, b) -> a | b);
        for (int i = 0; i < this.offsetsByElementId.length; ++i) {
            VertexFormatElement vertexFormatElement = VertexFormatElement.get(i);
            int j = vertexFormatElement != null ? elements.indexOf(vertexFormatElement) : -1;
            this.offsetsByElementId[i] = j != -1 ? offsets.getInt(j) : -1;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Vertex format (").append(this.vertexSizeByte).append(" bytes):\n");
        for (int i = 0; i < this.elements.size(); ++i) {
            VertexFormatElement vertexFormatElement = this.elements.get(i);
            stringBuilder.append(i).append(". ").append(this.names.get(i)).append(": ").append(vertexFormatElement).append(" @ ").append(this.getOffset(vertexFormatElement)).append('\n');
        }
        return stringBuilder.toString();
    }

    public int getVertexSizeByte() {
        return this.vertexSizeByte;
    }

    public List<VertexFormatElement> getElements() {
        return this.elements;
    }

    public List<String> getAttributeNames() {
        return this.names;
    }

    public int[] getOffsetsByElementId() {
        return this.offsetsByElementId;
    }

    public int getOffset(VertexFormatElement element) {
        return this.offsetsByElementId[element.id()];
    }

    public boolean has(VertexFormatElement element) {
        return (this.requiredMask & element.getBit()) != 0;
    }

    public int getRequiredMask() {
        return this.requiredMask;
    }

    public String getName(VertexFormatElement element) {
        int i = this.elements.indexOf(element);
        if (i == -1) {
            throw new IllegalArgumentException(String.valueOf(element) + " is not contained in format");
        }
        return this.names.get(i);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VertexFormat)) return false;
        VertexFormat vertexFormat = (VertexFormat)o;
        if (this.requiredMask != vertexFormat.requiredMask) return false;
        if (this.vertexSizeByte != vertexFormat.vertexSizeByte) return false;
        if (!this.names.equals(vertexFormat.names)) return false;
        if (!Arrays.equals(this.offsetsByElementId, vertexFormat.offsetsByElementId)) return false;
        return true;
    }

    public int hashCode() {
        return this.requiredMask * 31 + Arrays.hashCode(this.offsetsByElementId);
    }

    /**
     * Specifies for OpenGL how the vertex data should be interpreted.
     */
    public void setupState() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::setupStateInternal);
            return;
        }
        this.setupStateInternal();
    }

    private void setupStateInternal() {
        int i = this.getVertexSizeByte();
        for (int j = 0; j < this.elements.size(); ++j) {
            GlStateManager._enableVertexAttribArray(j);
            VertexFormatElement vertexFormatElement = this.elements.get(j);
            vertexFormatElement.setupState(j, this.getOffset(vertexFormatElement), i);
        }
    }

    public void clearState() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::clearStateInternal);
            return;
        }
        this.clearStateInternal();
    }

    private void clearStateInternal() {
        for (int i = 0; i < this.elements.size(); ++i) {
            GlStateManager._disableVertexAttribArray(i);
        }
    }

    /**
     * {@return a vertex buffer shared with the users of this vertex format}
     * 
     * <p>The data uploaded to the returned vertex buffer cannot be reused as
     * it can be overwritten by other users of this method.
     */
    public VertexBuffer getBuffer() {
        VertexBuffer vertexBuffer = this.buffer;
        if (vertexBuffer == null) {
            this.buffer = vertexBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
        }
        return vertexBuffer;
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final ImmutableMap.Builder<String, VertexFormatElement> elements = ImmutableMap.builder();
        private final IntList offsets = new IntArrayList();
        private int currentOffset;

        Builder() {
        }

        public Builder add(String name, VertexFormatElement element) {
            this.elements.put(name, element);
            this.offsets.add(this.currentOffset);
            this.currentOffset += element.getSizeInBytes();
            return this;
        }

        public Builder skip(int offset) {
            this.currentOffset += offset;
            return this;
        }

        public VertexFormat build() {
            ImmutableMap<String, VertexFormatElement> immutableMap = this.elements.buildOrThrow();
            ImmutableList<VertexFormatElement> immutableList = ((ImmutableCollection)immutableMap.values()).asList();
            ImmutableList<String> immutableList2 = ((ImmutableCollection)((Object)immutableMap.keySet())).asList();
            return new VertexFormat(immutableList, immutableList2, this.offsets, this.currentOffset);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum DrawMode {
        LINES(4, 2, 2, false),
        LINE_STRIP(5, 2, 1, true),
        DEBUG_LINES(1, 2, 2, false),
        DEBUG_LINE_STRIP(3, 2, 1, true),
        TRIANGLES(4, 3, 3, false),
        TRIANGLE_STRIP(5, 3, 1, true),
        TRIANGLE_FAN(6, 3, 1, true),
        QUADS(4, 4, 4, false);

        public final int glMode;
        public final int firstVertexCount;
        public final int additionalVertexCount;
        public final boolean shareVertices;

        private DrawMode(int glMode, int firstVertexCount, int additionalVertexCount, boolean shareVertices) {
            this.glMode = glMode;
            this.firstVertexCount = firstVertexCount;
            this.additionalVertexCount = additionalVertexCount;
            this.shareVertices = shareVertices;
        }

        public int getIndexCount(int vertexCount) {
            return switch (this.ordinal()) {
                case 1, 2, 3, 4, 5, 6 -> vertexCount;
                case 0, 7 -> vertexCount / 4 * 6;
                default -> 0;
            };
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum IndexType {
        SHORT(GlConst.GL_UNSIGNED_SHORT, 2),
        INT(GlConst.GL_UNSIGNED_INT, 4);

        public final int glType;
        public final int size;

        private IndexType(int glType, int size) {
            this.glType = glType;
            this.size = size;
        }

        public static IndexType smallestFor(int indexCount) {
            if ((indexCount & 0xFFFF0000) != 0) {
                return INT;
            }
            return SHORT;
        }
    }
}

