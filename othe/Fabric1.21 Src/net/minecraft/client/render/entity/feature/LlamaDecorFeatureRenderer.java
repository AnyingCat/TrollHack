/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.LlamaEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class LlamaDecorFeatureRenderer
extends FeatureRenderer<LlamaEntity, LlamaEntityModel<LlamaEntity>> {
    private static final Identifier[] LLAMA_DECOR = new Identifier[]{Identifier.ofVanilla("textures/entity/llama/decor/white.png"), Identifier.ofVanilla("textures/entity/llama/decor/orange.png"), Identifier.ofVanilla("textures/entity/llama/decor/magenta.png"), Identifier.ofVanilla("textures/entity/llama/decor/light_blue.png"), Identifier.ofVanilla("textures/entity/llama/decor/yellow.png"), Identifier.ofVanilla("textures/entity/llama/decor/lime.png"), Identifier.ofVanilla("textures/entity/llama/decor/pink.png"), Identifier.ofVanilla("textures/entity/llama/decor/gray.png"), Identifier.ofVanilla("textures/entity/llama/decor/light_gray.png"), Identifier.ofVanilla("textures/entity/llama/decor/cyan.png"), Identifier.ofVanilla("textures/entity/llama/decor/purple.png"), Identifier.ofVanilla("textures/entity/llama/decor/blue.png"), Identifier.ofVanilla("textures/entity/llama/decor/brown.png"), Identifier.ofVanilla("textures/entity/llama/decor/green.png"), Identifier.ofVanilla("textures/entity/llama/decor/red.png"), Identifier.ofVanilla("textures/entity/llama/decor/black.png")};
    private static final Identifier TRADER_LLAMA_DECOR = Identifier.ofVanilla("textures/entity/llama/decor/trader_llama.png");
    private final LlamaEntityModel<LlamaEntity> model;

    public LlamaDecorFeatureRenderer(FeatureRendererContext<LlamaEntity, LlamaEntityModel<LlamaEntity>> context, EntityModelLoader loader) {
        super(context);
        this.model = new LlamaEntityModel(loader.getModelPart(EntityModelLayers.LLAMA_DECOR));
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, LlamaEntity llamaEntity, float f, float g, float h, float j, float k, float l) {
        Identifier identifier;
        DyeColor dyeColor = llamaEntity.getCarpetColor();
        if (dyeColor != null) {
            identifier = LLAMA_DECOR[dyeColor.getId()];
        } else if (llamaEntity.isTrader()) {
            identifier = TRADER_LLAMA_DECOR;
        } else {
            return;
        }
        ((LlamaEntityModel)this.getContextModel()).copyStateTo(this.model);
        this.model.setAngles(llamaEntity, f, g, j, k, l);
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutoutNoCull(identifier));
        this.model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV);
    }
}

