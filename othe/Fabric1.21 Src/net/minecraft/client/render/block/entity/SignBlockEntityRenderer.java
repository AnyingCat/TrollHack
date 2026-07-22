/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.block.entity;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SignBlock;
import net.minecraft.block.WoodType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@Environment(value=EnvType.CLIENT)
public class SignBlockEntityRenderer
implements BlockEntityRenderer<SignBlockEntity> {
    private static final String STICK = "stick";
    private static final int GLOWING_BLACK_COLOR = -988212;
    private static final int RENDER_DISTANCE = MathHelper.square(16);
    private static final float SCALE = 0.6666667f;
    private static final Vec3d TEXT_OFFSET = new Vec3d(0.0, 0.3333333432674408, 0.046666666865348816);
    private final Map<WoodType, SignModel> typeToModel = WoodType.stream().collect(ImmutableMap.toImmutableMap(signType -> signType, signType -> new SignModel(ctx.getLayerModelPart(EntityModelLayers.createSign(signType)))));
    private final TextRenderer textRenderer;

    public SignBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.textRenderer = ctx.getTextRenderer();
    }

    @Override
    public void render(SignBlockEntity signBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
        BlockState blockState = signBlockEntity.getCachedState();
        AbstractSignBlock abstractSignBlock = (AbstractSignBlock)blockState.getBlock();
        WoodType woodType = AbstractSignBlock.getWoodType(abstractSignBlock);
        SignModel signModel = this.typeToModel.get(woodType);
        signModel.stick.visible = blockState.getBlock() instanceof SignBlock;
        this.render(signBlockEntity, matrixStack, vertexConsumerProvider, i, j, blockState, abstractSignBlock, woodType, signModel);
    }

    public float getSignScale() {
        return 0.6666667f;
    }

    public float getTextScale() {
        return 0.6666667f;
    }

    void render(SignBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BlockState state, AbstractSignBlock block, WoodType woodType, Model model) {
        matrices.push();
        this.setAngles(matrices, -block.getRotationDegrees(state), state);
        this.renderSign(matrices, vertexConsumers, light, overlay, woodType, model);
        this.renderText(entity.getPos(), entity.getFrontText(), matrices, vertexConsumers, light, entity.getTextLineHeight(), entity.getMaxTextWidth(), true);
        this.renderText(entity.getPos(), entity.getBackText(), matrices, vertexConsumers, light, entity.getTextLineHeight(), entity.getMaxTextWidth(), false);
        matrices.pop();
    }

    void setAngles(MatrixStack matrices, float rotationDegrees, BlockState state) {
        matrices.translate(0.5f, 0.75f * this.getSignScale(), 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationDegrees));
        if (!(state.getBlock() instanceof SignBlock)) {
            matrices.translate(0.0f, -0.3125f, -0.4375f);
        }
    }

    void renderSign(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, WoodType woodType, Model model) {
        matrices.push();
        float f = this.getSignScale();
        matrices.scale(f, -f, -f);
        SpriteIdentifier spriteIdentifier = this.getTextureId(woodType);
        VertexConsumer vertexConsumer = spriteIdentifier.getVertexConsumer(vertexConsumers, model::getLayer);
        this.renderSignModel(matrices, light, overlay, model, vertexConsumer);
        matrices.pop();
    }

    void renderSignModel(MatrixStack matrices, int light, int overlay, Model model, VertexConsumer vertexConsumers) {
        SignModel signModel = (SignModel)model;
        signModel.root.render(matrices, vertexConsumers, light, overlay);
    }

    SpriteIdentifier getTextureId(WoodType signType) {
        return TexturedRenderLayers.getSignTextureId(signType);
    }

    void renderText(BlockPos pos, SignText signText, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int lineHeight, int lineWidth, boolean front) {
        int l;
        boolean bl;
        int k;
        matrices.push();
        this.setTextAngles(matrices, front, this.getTextOffset());
        int i = SignBlockEntityRenderer.getColor(signText);
        int j = 4 * lineHeight / 2;
        OrderedText[] orderedTexts = signText.getOrderedMessages(MinecraftClient.getInstance().shouldFilterText(), text -> {
            List<OrderedText> list = this.textRenderer.wrapLines((StringVisitable)text, lineWidth);
            return list.isEmpty() ? OrderedText.EMPTY : list.get(0);
        });
        if (signText.isGlowing()) {
            k = signText.getColor().getSignColor();
            bl = SignBlockEntityRenderer.shouldRender(pos, k);
            l = 0xF000F0;
        } else {
            k = i;
            bl = false;
            l = light;
        }
        for (int m = 0; m < 4; ++m) {
            OrderedText orderedText = orderedTexts[m];
            float f = -this.textRenderer.getWidth(orderedText) / 2;
            if (bl) {
                this.textRenderer.drawWithOutline(orderedText, f, m * lineHeight - j, k, i, matrices.peek().getPositionMatrix(), vertexConsumers, l);
                continue;
            }
            this.textRenderer.draw(orderedText, f, (float)(m * lineHeight - j), k, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.POLYGON_OFFSET, 0, l);
        }
        matrices.pop();
    }

    private void setTextAngles(MatrixStack matrices, boolean front, Vec3d translation) {
        if (!front) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
        }
        float f = 0.015625f * this.getTextScale();
        matrices.translate(translation.x, translation.y, translation.z);
        matrices.scale(f, -f, f);
    }

    Vec3d getTextOffset() {
        return TEXT_OFFSET;
    }

    static boolean shouldRender(BlockPos pos, int signColor) {
        if (signColor == DyeColor.BLACK.getSignColor()) {
            return true;
        }
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        ClientPlayerEntity clientPlayerEntity = minecraftClient.player;
        if (clientPlayerEntity != null && minecraftClient.options.getPerspective().isFirstPerson() && clientPlayerEntity.isUsingSpyglass()) {
            return true;
        }
        Entity entity = minecraftClient.getCameraEntity();
        return entity != null && entity.squaredDistanceTo(Vec3d.ofCenter(pos)) < (double)RENDER_DISTANCE;
    }

    public static int getColor(SignText sign) {
        int i = sign.getColor().getSignColor();
        if (i == DyeColor.BLACK.getSignColor() && sign.isGlowing()) {
            return -988212;
        }
        double d = 0.4;
        int j = (int)((double)ColorHelper.Argb.getRed(i) * 0.4);
        int k = (int)((double)ColorHelper.Argb.getGreen(i) * 0.4);
        int l = (int)((double)ColorHelper.Argb.getBlue(i) * 0.4);
        return ColorHelper.Argb.getArgb(0, j, k, l);
    }

    public static SignModel createSignModel(EntityModelLoader entityModelLoader, WoodType type) {
        return new SignModel(entityModelLoader.getModelPart(EntityModelLayers.createSign(type)));
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("sign", ModelPartBuilder.create().uv(0, 0).cuboid(-12.0f, -14.0f, -1.0f, 24.0f, 12.0f, 2.0f), ModelTransform.NONE);
        modelPartData.addChild(STICK, ModelPartBuilder.create().uv(0, 14).cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 14.0f, 2.0f), ModelTransform.NONE);
        return TexturedModelData.of(modelData, 64, 32);
    }

    @Environment(value=EnvType.CLIENT)
    public static final class SignModel
    extends Model {
        public final ModelPart root;
        public final ModelPart stick;

        public SignModel(ModelPart root) {
            super(RenderLayer::getEntityCutoutNoCull);
            this.root = root;
            this.stick = root.getChild(SignBlockEntityRenderer.STICK);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
            this.root.render(matrices, vertices, light, overlay, color);
        }
    }
}

