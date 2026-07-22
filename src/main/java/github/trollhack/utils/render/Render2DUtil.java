package github.trollhack.utils.render;

import github.trollhack.utils.interfaces.Mc;
import github.trollhack.utils.render.shader.BlurProgram;
import github.trollhack.utils.render.shader.MainMenuShader;
import github.trollhack.utils.render.shader.RoundRectProgram;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Render2DUtil implements Mc {
    public static BlurProgram BLUR;
    public static RoundRectProgram ROUND_RECT;
    public static MainMenuShader MAIN_MENU;

    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void drawRoundedBlur(MatrixStack matrices, float x, float y, float width, float height, float radius, Color c1, float blurStrenth, float blurOpacity) {
        drawRoundedBlur(matrices, x, y, width, height, radius, radius, radius, radius, c1, blurStrenth, blurOpacity);
    }

    public static void drawRoundedBlur(MatrixStack matrices, float x, float y, float width, float height, float tl, float tr, float br, float bl, Color c1, float blurStrenth, float blurOpacity) {
        BufferBuilder bb = preShaderDraw(matrices, x - 10, y - 10, width + 20, height + 20);
        BLUR.setParameters(x, y, width, height, tl, tr, br, bl, c1, blurStrenth, blurOpacity);
        BLUR.use();
        BufferRenderer.drawWithGlobalProgram(bb.end());
        endRender();
    }

    public static void drawRoundedRect(MatrixStack matrices, float x, float y, float width, float height, float radius, Color color) {
        drawRoundedRect(matrices, x, y, width, height, radius, radius, radius, radius, color);
    }

    public static void drawRoundedRect(MatrixStack matrices, float x, float y, float width, float height, float tl, float tr, float br, float bl, Color color) {
        BufferBuilder bb = preShaderDraw(matrices, x, y, width, height);
        ROUND_RECT.setParameters(x, y, width, height, tl, tr, br, bl, color);
        ROUND_RECT.use();
        BufferRenderer.drawWithGlobalProgram(bb.end());
        endRender();
    }

    public static void drawMainMenuShader(MatrixStack matrices, float x, float y, float width, float height) {
        BufferBuilder bb = preShaderDraw(matrices, x, y, width, height);
        MAIN_MENU.setParameters(x, y, width, height);
        MAIN_MENU.use();
        BufferRenderer.drawWithGlobalProgram(bb.end());
        endRender();
    }

    public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, Color color) {
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        buffer.vertex(matrix, x, y, 0).color(r, g, b, a);
        buffer.vertex(matrix, x, y + height, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + width, y + height, 0).color(r, g, b, a);
        buffer.vertex(matrix, x + width, y, 0).color(r, g, b, a);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        endRender();
    }

    public static BufferBuilder preShaderDraw(MatrixStack matrices, float x, float y, float width, float height) {
        setupRender();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        buffer.vertex(matrix, x, y, 0);
        buffer.vertex(matrix, x, y + height, 0);
        buffer.vertex(matrix, x + width, y + height, 0);
        buffer.vertex(matrix, x + width, y, 0);
        return buffer;
    }

    public static void endRender() {
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void drawRectOutline(MatrixStack matrices, float x, float y, float width, float height, float lineWidth, Color color) {
        drawRect(matrices, x, y, lineWidth, height, color);
        drawRect(matrices, x + width - lineWidth, y, lineWidth, height, color);
        drawRect(matrices, x + lineWidth, y, width - lineWidth * 2, lineWidth, color);
        drawRect(matrices, x + lineWidth, y + height - lineWidth, width - lineWidth * 2, lineWidth, color);
    }

    public static void drawLine(MatrixStack matrices, float x1, float y1, float x2, float y2, float lineWidth, Color color) {
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.lineWidth(lineWidth);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        buffer.vertex(matrix, x1, y1, 0).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, 0).color(r, g, b, a);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        endRender();
    }

    public static void drawGradientRect(MatrixStack matrices, float x, float y, float width, float height,
                                         Color topLeft, Color topRight, Color bottomLeft, Color bottomRight) {
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        buffer.vertex(matrix, x, y, 0).color(topLeft.getRed() / 255f, topLeft.getGreen() / 255f, topLeft.getBlue() / 255f, topLeft.getAlpha() / 255f);
        buffer.vertex(matrix, x, y + height, 0).color(bottomLeft.getRed() / 255f, bottomLeft.getGreen() / 255f, bottomLeft.getBlue() / 255f, bottomLeft.getAlpha() / 255f);
        buffer.vertex(matrix, x + width, y + height, 0).color(bottomRight.getRed() / 255f, bottomRight.getGreen() / 255f, bottomRight.getBlue() / 255f, bottomRight.getAlpha() / 255f);
        buffer.vertex(matrix, x + width, y, 0).color(topRight.getRed() / 255f, topRight.getGreen() / 255f, topRight.getBlue() / 255f, topRight.getAlpha() / 255f);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        endRender();
    }

    public static void drawHueBar(MatrixStack matrices, float x, float y, float width, float height) {
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        float partH = height / 6.0f;
        Color[] hueColors = {
            new Color(255, 0, 0),
            new Color(255, 255, 0),
            new Color(0, 255, 0),
            new Color(0, 255, 255),
            new Color(0, 0, 255),
            new Color(255, 0, 255),
            new Color(255, 0, 0)
        };

        for (int i = 0; i < 6; i++) {
            Color c1 = hueColors[i];
            Color c2 = hueColors[i + 1];
            float y1 = y + partH * i;
            float y2 = y + partH * (i + 1);
            buffer.vertex(matrix, x, y1, 0).color(c1.getRed() / 255f, c1.getGreen() / 255f, c1.getBlue() / 255f, 1.0f);
            buffer.vertex(matrix, x, y2, 0).color(c2.getRed() / 255f, c2.getGreen() / 255f, c2.getBlue() / 255f, 1.0f);
            buffer.vertex(matrix, x + width, y2, 0).color(c2.getRed() / 255f, c2.getGreen() / 255f, c2.getBlue() / 255f, 1.0f);
            buffer.vertex(matrix, x + width, y1, 0).color(c1.getRed() / 255f, c1.getGreen() / 255f, c1.getBlue() / 255f, 1.0f);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        endRender();
    }

    public static void initShaders() {
        BLUR = new BlurProgram();
        ROUND_RECT = new RoundRectProgram();
        MAIN_MENU = new MainMenuShader();
    }
}
