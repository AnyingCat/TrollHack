/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormat;
import org.jetbrains.annotations.Nullable;

/**
 * Containing methods for immediately drawing a buffer built with {@link
 * BufferBuilder}.
 */
@Environment(value=EnvType.CLIENT)
public class BufferRenderer {
    @Nullable
    private static VertexBuffer currentVertexBuffer;

    public static void reset() {
        if (currentVertexBuffer != null) {
            BufferRenderer.resetCurrentVertexBuffer();
            VertexBuffer.unbind();
        }
    }

    public static void resetCurrentVertexBuffer() {
        currentVertexBuffer = null;
    }

    /**
     * Draws {@code buffer} using the shader program specified with {@link
     * com.mojang.blaze3d.systems.RenderSystem#setShader
     * RenderSystem#setShader}
     */
    public static void drawWithGlobalProgram(BuiltBuffer buffer) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> BufferRenderer.drawWithGlobalProgramInternal(buffer));
        } else {
            BufferRenderer.drawWithGlobalProgramInternal(buffer);
        }
    }

    private static void drawWithGlobalProgramInternal(BuiltBuffer buffer) {
        VertexBuffer vertexBuffer = BufferRenderer.upload(buffer);
        vertexBuffer.draw(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
    }

    /**
     * Draws {@code buffer}.
     * 
     * <p>Unlike {@link #drawWithGlobalProgram}, the shader program cannot be
     * specified with {@link com.mojang.blaze3d.systems.RenderSystem#setShader
     * RenderSystem#setShader}. The caller of this method must manually bind a
     * shader program before calling this method.
     */
    public static void draw(BuiltBuffer buffer) {
        VertexBuffer vertexBuffer = BufferRenderer.upload(buffer);
        vertexBuffer.draw();
    }

    private static VertexBuffer upload(BuiltBuffer buffer) {
        RenderSystem.assertOnRenderThread();
        VertexBuffer vertexBuffer = BufferRenderer.bind(buffer.getDrawParameters().format());
        vertexBuffer.upload(buffer);
        return vertexBuffer;
    }

    private static VertexBuffer bind(VertexFormat vertexFormat) {
        VertexBuffer vertexBuffer = vertexFormat.getBuffer();
        BufferRenderer.bind(vertexBuffer);
        return vertexBuffer;
    }

    private static void bind(VertexBuffer vertexBuffer) {
        if (vertexBuffer != currentVertexBuffer) {
            vertexBuffer.bind();
            currentVertexBuffer = vertexBuffer;
        }
    }
}

