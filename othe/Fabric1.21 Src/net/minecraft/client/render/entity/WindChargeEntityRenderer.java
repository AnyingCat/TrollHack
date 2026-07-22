/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.WindChargeEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.AbstractWindChargeEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class WindChargeEntityRenderer
extends EntityRenderer<AbstractWindChargeEntity> {
    private static final float field_52258 = MathHelper.square(3.5f);
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/projectiles/wind_charge.png");
    private final WindChargeEntityModel model;

    public WindChargeEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.model = new WindChargeEntityModel(context.getPart(EntityModelLayers.WIND_CHARGE));
    }

    @Override
    public void render(AbstractWindChargeEntity abstractWindChargeEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        if (abstractWindChargeEntity.age < 2 && this.dispatcher.camera.getFocusedEntity().squaredDistanceTo(abstractWindChargeEntity) < (double)field_52258) {
            return;
        }
        float h = (float)abstractWindChargeEntity.age + g;
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getBreezeWind(TEXTURE, this.getXOffset(h) % 1.0f, 0.0f));
        this.model.setAngles(abstractWindChargeEntity, 0.0f, 0.0f, h, 0.0f, 0.0f);
        this.model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV);
        super.render(abstractWindChargeEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    protected float getXOffset(float tickDelta) {
        return tickDelta * 0.03f;
    }

    @Override
    public Identifier getTexture(AbstractWindChargeEntity abstractWindChargeEntity) {
        return TEXTURE;
    }
}

