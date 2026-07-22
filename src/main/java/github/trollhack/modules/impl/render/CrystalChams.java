package github.trollhack.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.settings.impl.ColorSetting;
import github.trollhack.settings.impl.FloatSetting;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.EnderDragonEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class CrystalChams extends Module {
    public static final CrystalChams INSTANCE = new CrystalChams();

    private static final float SINE_45_DEGREES = (float) Math.sin(0.7853981633974483);

    public final FloatSetting scale = floatSetting("Scale", 1.0f, 0.1f, 4.0f, 0.1f);
    public final FloatSetting spinSpeed = floatSetting("Spin Speed", 1.0f, 0.0f, 4.0f, 0.1f);
    public final FloatSetting floatSpeed = floatSetting("Float Speed", 1.0f, 0.0f, 4.0f, 0.1f);
    public final BooleanSetting filled = booleanSetting("Filled", true);
    public final BooleanSetting filledDepth = booleanSetting("Filled Depth", true, filled::getValue);
    public final ColorSetting filledColor = colorSetting("Filled Color", new Color(133, 255, 200, 63), filled::getValue);
    public final BooleanSetting outline = booleanSetting("Outline", true);
    public final BooleanSetting outlineDepth = booleanSetting("Outline Depth", false, outline::getValue);
    public final ColorSetting outlineColor = colorSetting("Outline Color", new Color(133, 255, 200, 200), outline::getValue);
    public final FloatSetting width = floatSetting("Width", 2.0f, 0.25f, 4.0f, 0.25f, outline::getValue);
    public final FloatSetting range = floatSetting("Range", 16.0f, 0.0f, 16.0f, 0.5f);

    public CrystalChams() {
        super("Crystal Chams", Category.RENDER);
    }

    public boolean shouldRender(EndCrystalEntity entity) {
        if (!isEnabled() || nullCheck() || entity == null) return false;
        float rangeSq = range.getValue() * range.getValue();
        return mc.player.squaredDistanceTo(entity.getPos()) <= rangeSq;
    }

    public static void renderCrystal(
            EndCrystalEntity entity, float tickDelta, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light,
            ModelPart core, ModelPart frame, ModelPart bottom) {
        if (!INSTANCE.shouldRender(entity)) return;

        float scaleVal = INSTANCE.scale.getValue();
        float spinSpeedVal = INSTANCE.spinSpeed.getValue();
        float floatSpeedVal = INSTANCE.floatSpeed.getValue();

        float age = entity.endCrystalAge + tickDelta;
        float spin = age * 3.0f * spinSpeedVal;
        float floatRaw = MathHelper.sin(age * 0.2f * floatSpeedVal) / 2.0f + 0.5f;
        float h = (floatRaw * floatRaw + floatRaw) * 0.4f - 1.4f;

        int overlay = OverlayTexture.DEFAULT_UV;

        matrices.push();
        matrices.scale(2.0f * scaleVal, 2.0f * scaleVal, 2.0f * scaleVal);
        matrices.translate(0.0f, -0.5f, 0.0f);

        if (INSTANCE.filled.getValue()) {
            renderPass(matrices, core, frame, bottom, entity, spin, h, light, overlay,
                INSTANCE.filledColor.getValue(), INSTANCE.filledDepth.getValue(), false, 0.0f);
        }

        if (INSTANCE.outline.getValue()) {
            renderPass(matrices, core, frame, bottom, entity, spin, h, light, overlay,
                INSTANCE.outlineColor.getValue(), INSTANCE.outlineDepth.getValue(), true, INSTANCE.width.getValue());
        }

        matrices.pop();

        BlockPos beamTarget = entity.getBeamTarget();
        if (beamTarget != null) {
            float m = beamTarget.getX() + 0.5f;
            float n = beamTarget.getY() + 0.5f;
            float o = beamTarget.getZ() + 0.5f;
            float p = m - (float) entity.getX();
            float q = n - (float) entity.getY();
            float r = o - (float) entity.getZ();
            matrices.translate(p, q, r);
            EnderDragonEntityRenderer.renderCrystalBeam(-p, -q + h, -r, tickDelta, entity.endCrystalAge, matrices, vertexConsumers, light);
        }
    }

    private static void renderPass(
            MatrixStack matrices, ModelPart core, ModelPart frame, ModelPart bottom,
            EndCrystalEntity entity, float spin, float h, int light, int overlay,
            Color color, boolean depth, boolean outlinePass, float lineWidth) {
        BufferBuilder buffer = Tessellator.getInstance().begin(
            VertexFormat.DrawMode.QUADS,
            VertexFormats.POSITION_COLOR
        );

        matrices.push();
        if (entity.shouldShowBottom()) {
            bottom.render(matrices, buffer, light, overlay);
        }
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spin));
        matrices.translate(0.0f, 1.5f + h / 2.0f, 0.0f);
        matrices.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
        frame.render(matrices, buffer, light, overlay);
        matrices.scale(0.875f, 0.875f, 0.875f);
        matrices.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spin));
        frame.render(matrices, buffer, light, overlay);
        matrices.scale(0.875f, 0.875f, 0.875f);
        matrices.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spin));
        core.render(matrices, buffer, light, overlay);
        matrices.pop();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(
            color.getRed() / 255f,
            color.getGreen() / 255f,
            color.getBlue() / 255f,
            color.getAlpha() / 255f
        );
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        if (depth) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
        } else {
            RenderSystem.disableDepthTest();
        }

        if (outlinePass) {
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            RenderSystem.lineWidth(lineWidth);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        if (outlinePass) {
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        }
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
    }
}
