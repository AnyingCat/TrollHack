/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a singular field within a larger {@link
 * net.minecraft.client.render.VertexFormat vertex format}.
 * 
 * <p>This element comprises a component type, the number of components,
 * and a type that describes how the components should be interpreted.
 */
@Environment(value=EnvType.CLIENT)
public record VertexFormatElement(int id, int uvIndex, ComponentType type, Usage usage, int count) {
    public static final int field_52106 = 32;
    private static final VertexFormatElement[] ELEMENTS = new VertexFormatElement[32];
    private static final List<VertexFormatElement> ELEMENTS_LIST = new ArrayList<VertexFormatElement>(32);
    public static final VertexFormatElement POSITION = VertexFormatElement.register(0, 0, ComponentType.FLOAT, Usage.POSITION, 3);
    public static final VertexFormatElement COLOR = VertexFormatElement.register(1, 0, ComponentType.UBYTE, Usage.COLOR, 4);
    public static final VertexFormatElement UV_0;
    public static final VertexFormatElement UV;
    public static final VertexFormatElement UV_1;
    public static final VertexFormatElement UV_2;
    public static final VertexFormatElement NORMAL;

    public VertexFormatElement(int uvIndex, int i, ComponentType componentType, Usage usage, int j) {
        if (uvIndex < 0 || uvIndex >= ELEMENTS.length) {
            throw new IllegalArgumentException("Element ID must be in range [0; " + ELEMENTS.length + ")");
        }
        if (!this.isValidType(i, usage)) {
            throw new IllegalStateException("Multiple vertex elements of the same type other than UVs are not supported");
        }
        this.id = uvIndex;
        this.uvIndex = i;
        this.type = componentType;
        this.usage = usage;
        this.count = j;
    }

    public static VertexFormatElement register(int id, int uvIndex, ComponentType type, Usage usage, int count) {
        VertexFormatElement vertexFormatElement = new VertexFormatElement(id, uvIndex, type, usage, count);
        if (ELEMENTS[id] != null) {
            throw new IllegalArgumentException("Duplicate element registration for: " + id);
        }
        VertexFormatElement.ELEMENTS[id] = vertexFormatElement;
        ELEMENTS_LIST.add(vertexFormatElement);
        return vertexFormatElement;
    }

    private boolean isValidType(int uvIndex, Usage type) {
        return uvIndex == 0 || type == Usage.UV;
    }

    @Override
    public String toString() {
        return this.count + "," + String.valueOf((Object)this.usage) + "," + String.valueOf((Object)this.type) + " (" + this.id + ")";
    }

    public int getBit() {
        return 1 << this.id;
    }

    public int getSizeInBytes() {
        return this.type.getByteLength() * this.count;
    }

    /**
     * Specifies for OpenGL how the vertex data corresponding to this element
     * should be interpreted.
     * 
     * @param elementIndex the index of the element in a vertex format
     * @param offset the distance between the start of the buffer and the first instance of
     * the element in the buffer
     * @param stride the distance between consecutive instances of the element in the buffer
     */
    public void setupState(int elementIndex, long offset, int stride) {
        this.usage.setupTask.setupBufferState(this.count, this.type.getGlType(), stride, offset, elementIndex);
    }

    @Nullable
    public static VertexFormatElement get(int id) {
        return ELEMENTS[id];
    }

    public static Stream<VertexFormatElement> streamFromMask(int mask) {
        return ELEMENTS_LIST.stream().filter(element -> element != null && (mask & element.getBit()) != 0);
    }

    static {
        UV = UV_0 = VertexFormatElement.register(2, 0, ComponentType.FLOAT, Usage.UV, 2);
        UV_1 = VertexFormatElement.register(3, 1, ComponentType.SHORT, Usage.UV, 2);
        UV_2 = VertexFormatElement.register(4, 2, ComponentType.SHORT, Usage.UV, 2);
        NORMAL = VertexFormatElement.register(5, 0, ComponentType.BYTE, Usage.NORMAL, 3);
    }

    @Environment(value=EnvType.CLIENT)
    public static enum ComponentType {
        FLOAT(4, "Float", GlConst.GL_FLOAT),
        UBYTE(1, "Unsigned Byte", GlConst.GL_UNSIGNED_BYTE),
        BYTE(1, "Byte", GlConst.GL_BYTE),
        USHORT(2, "Unsigned Short", GlConst.GL_UNSIGNED_SHORT),
        SHORT(2, "Short", GlConst.GL_SHORT),
        UINT(4, "Unsigned Int", GlConst.GL_UNSIGNED_INT),
        INT(4, "Int", GlConst.GL_INT);

        private final int byteLength;
        private final String name;
        private final int glType;

        private ComponentType(int byteLength, String name, int glType) {
            this.byteLength = byteLength;
            this.name = name;
            this.glType = glType;
        }

        public int getByteLength() {
            return this.byteLength;
        }

        public int getGlType() {
            return this.glType;
        }

        public String toString() {
            return this.name;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Usage {
        POSITION("Position", (componentCount, componentType, stride, offset, uvIndex) -> GlStateManager._vertexAttribPointer(uvIndex, componentCount, componentType, false, stride, offset)),
        NORMAL("Normal", (componentCount, componentType, stride, offset, uvIndex) -> GlStateManager._vertexAttribPointer(uvIndex, componentCount, componentType, true, stride, offset)),
        COLOR("Vertex Color", (componentCount, componentType, stride, offset, uvIndex) -> GlStateManager._vertexAttribPointer(uvIndex, componentCount, componentType, true, stride, offset)),
        UV("UV", (componentCount, componentType, stride, offset, uvIndex) -> {
            if (componentType == 5126) {
                GlStateManager._vertexAttribPointer(uvIndex, componentCount, componentType, false, stride, offset);
            } else {
                GlStateManager._vertexAttribIPointer(uvIndex, componentCount, componentType, stride, offset);
            }
        }),
        GENERIC("Generic", (componentCount, componentType, stride, offset, uvIndex) -> GlStateManager._vertexAttribPointer(uvIndex, componentCount, componentType, false, stride, offset));

        private final String name;
        final SetupTask setupTask;

        private Usage(String name, SetupTask setupTask) {
            this.name = name;
            this.setupTask = setupTask;
        }

        public String toString() {
            return this.name;
        }

        @FunctionalInterface
        @Environment(value=EnvType.CLIENT)
        static interface SetupTask {
            public void setupBufferState(int var1, int var2, int var3, long var4, int var6);
        }
    }
}

