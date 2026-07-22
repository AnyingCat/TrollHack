package me.catrix.api.utils.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.catrix.api.utils.Wrapper;
import me.catrix.mod.modules.impl.client.Colors;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import java.awt.*;

public class TargetESPUtil implements Wrapper {
    private static float prevCircleStep;
    private static float circleStep;
    private static float espValue = 1f, prevEspValue;
    private static float espSpeed = 1f;
    private static boolean flipSpeed;

    public static final Identifier firefly = new Identifier("textures/targetesp/firefly.png");
    public static final Identifier capture = new Identifier("textures/targetesp/capture.png");

    public static void drawJello(MatrixStack matrix, Entity target, Color color) {
        double cs = prevCircleStep + (circleStep - prevCircleStep) * mc.getTickDelta();
        double prevSinAnim = absSinAnimation(cs - 0.45f);
        double sinAnim = absSinAnimation(cs);
        double x = target.prevX + (target.getX() - target.prevX) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = target.prevY + (target.getY() - target.prevY) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY() + prevSinAnim * target.getHeight();
        double z = target.prevZ + (target.getZ() - target.prevZ) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getZ();
        double nextY = target.prevY + (target.getY() - target.prevY) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY() + sinAnim * target.getHeight();

        matrix.push();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        float cos;
        float sin;
        for (int i = 0; i <= 30; i++) {
            cos = (float) (x + Math.cos(i * 6.28 / 30) * ((target.getBoundingBox().maxX - target.getBoundingBox().minX) + (target.getBoundingBox().maxZ - target.getBoundingBox().minZ)) * 0.5f);
            sin = (float) (z + Math.sin(i * 6.28 / 30) * ((target.getBoundingBox().maxX - target.getBoundingBox().minX) + (target.getBoundingBox().maxZ - target.getBoundingBox().minZ)) * 0.5f);
            bufferBuilder.vertex(matrix.peek().getPositionMatrix(), cos, (float) nextY, sin).color(color.getRGB()).next();
            bufferBuilder.vertex(matrix.peek().getPositionMatrix(), cos, (float) y, sin).color(ColorUtil.injectAlpha(color, 0).getRGB()).next();
        }

        tessellator.draw();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        matrix.pop();
    }

    public static void renderGhosts(int espLength, int factor, float shaking, float amplitude, Color color, Entity target) {
        Camera camera = mc.gameRenderer.getCamera();

        double tPosX = ColorUtil.interpolate(target.prevX, target.getX(), mc.getTickDelta()) - camera.getPos().x;
        double tPosY = ColorUtil.interpolate(target.prevY, target.getY(), mc.getTickDelta()) - camera.getPos().y;
        double tPosZ = ColorUtil.interpolate(target.prevZ, target.getZ(), mc.getTickDelta()) - camera.getPos().z;
        float iAge = (float) ColorUtil.interpolate(target.age - 1, target.age, mc.getTickDelta());

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.setShaderTexture(0, firefly);
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        boolean canSee = mc.player.canSee(target);

        if (canSee) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
        } else RenderSystem.disableDepthTest();

        for (int j = 0; j < 3; j++) {
            for (int i = 0; i <= espLength; i++) {
                double radians = Math.toRadians((((float) i / 1.5f + iAge) * factor + (j * 120)) % (factor * 360));
                double sinQuad = Math.sin(Math.toRadians(iAge * 2.5f + i * (j + 1)) * amplitude) / shaking;
                float offset = ((float) i / espLength);
                MatrixStack matrices = new MatrixStack();
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
                matrices.translate(tPosX + Math.cos(radians) * target.getWidth(), (tPosY + 1 + sinQuad), tPosZ + Math.sin(radians) * target.getWidth());
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                Matrix4f matrix = matrices.peek().getPositionMatrix();
                float scale = Math.max(0.24f * (offset), 0.2f);
                buffer.vertex(matrix, -scale, scale, 0).texture(0f, 1f).color(color.getRGB()).next();
                buffer.vertex(matrix, scale, scale, 0).texture(1f, 1f).color(color.getRGB()).next();
                buffer.vertex(matrix, scale, -scale, 0).texture(1f, 0).color(color.getRGB()).next();
                buffer.vertex(matrix, -scale, -scale, 0).texture(0, 0).color(color.getRGB()).next();
            }
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        if (canSee) {
            RenderSystem.depthMask(true);
            RenderSystem.disableDepthTest();
        } else RenderSystem.enableDepthTest();

        RenderSystem.disableBlend();
    }

    public static void renderCaptureMark(Entity target) {
        Camera camera = mc.gameRenderer.getCamera();

        double tPosX = ColorUtil.interpolate(target.prevX, target.getX(), mc.getTickDelta()) - camera.getPos().x;
        double tPosY = ColorUtil.interpolate(target.prevY, target.getY(), mc.getTickDelta()) - camera.getPos().y;
        double tPosZ = ColorUtil.interpolate(target.prevZ, target.getZ(), mc.getTickDelta()) - camera.getPos().z;

        MatrixStack matrices = new MatrixStack();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
        matrices.translate(tPosX, (tPosY + target.getEyeHeight(target.getPose()) / 2f), tPosZ);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(ColorUtil.interpolateFloat(prevEspValue, espValue, mc.getTickDelta())));
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.setShaderTexture(0, capture);
        matrices.translate(-0.75, -0.75, -0.01);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(matrix, 0, 1.5f, 0).texture(0f, 1f).color(Colors.INSTANCE.getColor(20)).next();
        buffer.vertex(matrix, 1.5f, 1.5f, 0).texture(1f, 1f).color(Colors.INSTANCE.getColor(20)).next();
        buffer.vertex(matrix, 1.5f, 0, 0).texture(1f, 0).color(Colors.INSTANCE.getColor(20)).next();
        buffer.vertex(matrix, 0, 0, 0).texture(0, 0).color(Colors.INSTANCE.getColor(20)).next();
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void updateCaptureMark() {
        prevEspValue = espValue;
        espValue += espSpeed;
        if (espSpeed > 25) flipSpeed = true;
        if (espSpeed < -25) flipSpeed = false;
        espSpeed = flipSpeed ? espSpeed - 0.5f : espSpeed + 0.5f;
    }

    public static void updateJello() {
        prevCircleStep = circleStep;
        circleStep += 0.15f;
    }

    private static double absSinAnimation(double input) {
        return Math.abs(1 + Math.sin(input)) / 2;
    }
}
