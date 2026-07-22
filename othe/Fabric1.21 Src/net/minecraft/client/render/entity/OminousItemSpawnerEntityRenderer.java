/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.OminousItemSpawnerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

@Environment(value=EnvType.CLIENT)
public class OminousItemSpawnerEntityRenderer
extends EntityRenderer<OminousItemSpawnerEntity> {
    private static final float field_50231 = 40.0f;
    private static final int field_50232 = 50;
    private final ItemRenderer itemRenderer;

    protected OminousItemSpawnerEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public Identifier getTexture(OminousItemSpawnerEntity ominousItemSpawnerEntity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }

    @Override
    public void render(OminousItemSpawnerEntity ominousItemSpawnerEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        ItemStack itemStack = ominousItemSpawnerEntity.getItem();
        if (itemStack.isEmpty()) {
            return;
        }
        matrixStack.push();
        if (ominousItemSpawnerEntity.age <= 50) {
            float h = Math.min((float)ominousItemSpawnerEntity.age + g, 50.0f) / 50.0f;
            matrixStack.scale(h, h, h);
        }
        World world = ominousItemSpawnerEntity.getWorld();
        float j = MathHelper.wrapDegrees(world.getTime() - 1L) * 40.0f;
        float k = MathHelper.wrapDegrees(world.getTime()) * 40.0f;
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerpAngleDegrees(g, j, k)));
        ItemEntityRenderer.renderStack(this.itemRenderer, matrixStack, vertexConsumerProvider, 0xF000F0, itemStack, world.random, world);
        matrixStack.pop();
    }
}

