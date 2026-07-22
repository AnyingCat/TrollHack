package github.trollhack.utils.render;

import github.trollhack.utils.interfaces.Mc;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.Color;

public class Render3DUtil implements Mc {

    public static float getTickDelta() {
        return mc.getRenderTickCounter().getTickDelta(true);
    }

    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void endRender() {
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void drawFilledBox(MatrixStack matrices, Box box, Color color, float alpha) {
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.disableCull();

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;

        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, alpha);

        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, alpha);

        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, alpha);

        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, alpha);

        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, alpha);

        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, alpha);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        endRender();
        RenderSystem.enableCull();
    }

    public static void drawBoxOutline(MatrixStack matrices, Box box, Color color, float alpha, float lineWidth) {
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.lineWidth(lineWidth);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;

        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, alpha);

        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, alpha);

        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, alpha);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, alpha);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        endRender();
    }

    public static void drawTracer(MatrixStack matrices, Vec3d from, Vec3d to, Color color, float alpha, float lineWidth) {
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.lineWidth(lineWidth);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;

        buffer.vertex(matrix, (float) from.x, (float) from.y, (float) from.z).color(r, g, b, alpha);
        buffer.vertex(matrix, (float) to.x, (float) to.y, (float) to.z).color(r, g, b, alpha);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        endRender();
    }

    public static void drawFlatRect(MatrixStack matrices, double x1, double y, double z1, double x2, double z2, Color color, float alpha, float lineWidth) {
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.lineWidth(lineWidth);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float yy = (float) y;
        float xx1 = (float) x1;
        float xx2 = (float) x2;
        float zz1 = (float) z1;
        float zz2 = (float) z2;

        buffer.vertex(matrix, xx1, yy, zz1).color(r, g, b, alpha);
        buffer.vertex(matrix, xx2, yy, zz1).color(r, g, b, alpha);
        buffer.vertex(matrix, xx2, yy, zz1).color(r, g, b, alpha);
        buffer.vertex(matrix, xx2, yy, zz2).color(r, g, b, alpha);
        buffer.vertex(matrix, xx2, yy, zz2).color(r, g, b, alpha);
        buffer.vertex(matrix, xx1, yy, zz2).color(r, g, b, alpha);
        buffer.vertex(matrix, xx1, yy, zz2).color(r, g, b, alpha);
        buffer.vertex(matrix, xx1, yy, zz1).color(r, g, b, alpha);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        endRender();
    }
}
