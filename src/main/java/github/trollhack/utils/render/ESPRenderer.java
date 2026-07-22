package github.trollhack.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import github.trollhack.utils.interfaces.Mc;
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
import java.util.ArrayList;
import java.util.List;

public class ESPRenderer implements Mc {

    public static class Info {
        public Box box;
        public Color color;

        public Info(Box box, Color color) {
            this.box = box;
            this.color = color;
        }
    }

    private final List<Info> toRender = new ArrayList<>();

    public int aFilled = 0;
    public int aOutline = 0;
    public float thickness = 2f;
    public boolean through = true;

    public void add(Box box, Color color) {
        toRender.add(new Info(box, color));
    }

    public int size() { return toRender.size(); }

    public void clear() {
        toRender.clear();
    }

    public void render(MatrixStack matrices, boolean clear) {
        boolean filled = aFilled != 0;
        boolean outline = aOutline != 0;
        if (toRender.isEmpty() || (!filled && !outline)) return;

        matrices.push();
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        if (through) RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.lineWidth(thickness);

        if (filled) {
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            for (Info info : toRender) {
                float r = info.color.getRed() / 255f;
                float g = info.color.getGreen() / 255f;
                float b = info.color.getBlue() / 255f;
                float alpha = (aFilled / 255f) * (info.color.getAlpha() / 255f);
                float minX = (float) info.box.minX;
                float minY = (float) info.box.minY;
                float minZ = (float) info.box.minZ;
                float maxX = (float) info.box.maxX;
                float maxY = (float) info.box.maxY;
                float maxZ = (float) info.box.maxZ;

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
            }
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }

        if (outline) {
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            for (Info info : toRender) {
                float r = info.color.getRed() / 255f;
                float g = info.color.getGreen() / 255f;
                float b = info.color.getBlue() / 255f;
                float alpha = (aOutline / 255f) * (info.color.getAlpha() / 255f);
                float minX = (float) info.box.minX;
                float minY = (float) info.box.minY;
                float minZ = (float) info.box.minZ;
                float maxX = (float) info.box.maxX;
                float maxY = (float) info.box.maxY;
                float maxZ = (float) info.box.maxZ;

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
            }
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }

        RenderSystem.enableCull();
        if (through) RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();

        matrices.pop();

        if (clear) clear();
    }
}
