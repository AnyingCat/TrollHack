/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.entity;

import com.google.common.annotations.VisibleForTesting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

@Environment(value=EnvType.CLIENT)
public class ItemEntityRenderer
extends EntityRenderer<ItemEntity> {
    private static final float field_32924 = 0.15f;
    private static final float field_32929 = 0.0f;
    private static final float field_32930 = 0.0f;
    private static final float field_32931 = 0.09375f;
    private final ItemRenderer itemRenderer;
    private final Random random = Random.create();

    public ItemEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.15f;
        this.shadowOpacity = 0.75f;
    }

    @Override
    public Identifier getTexture(ItemEntity itemEntity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }

    @Override
    public void render(ItemEntity itemEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();
        ItemStack itemStack = itemEntity.getStack();
        this.random.setSeed(ItemEntityRenderer.getSeed(itemStack));
        BakedModel bakedModel = this.itemRenderer.getModel(itemStack, itemEntity.getWorld(), null, itemEntity.getId());
        boolean bl = bakedModel.hasDepth();
        float h = 0.25f;
        float j = MathHelper.sin(((float)itemEntity.getItemAge() + g) / 10.0f + itemEntity.uniqueOffset) * 0.1f + 0.1f;
        float k = bakedModel.getTransformation().getTransformation((ModelTransformationMode)ModelTransformationMode.GROUND).scale.y();
        matrixStack.translate(0.0f, j + 0.25f * k, 0.0f);
        float l = itemEntity.getRotation(g);
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(l));
        ItemEntityRenderer.renderStack(this.itemRenderer, matrixStack, vertexConsumerProvider, i, itemStack, bakedModel, bl, this.random);
        matrixStack.pop();
        super.render(itemEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    public static int getSeed(ItemStack stack) {
        return stack.isEmpty() ? 187 : Item.getRawId(stack.getItem()) + stack.getDamage();
    }

    @VisibleForTesting
    static int getRenderedAmount(int stackSize) {
        if (stackSize <= 1) {
            return 1;
        }
        if (stackSize <= 16) {
            return 2;
        }
        if (stackSize <= 32) {
            return 3;
        }
        if (stackSize <= 48) {
            return 4;
        }
        return 5;
    }

    public static void renderStack(ItemRenderer itemRenderer, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ItemStack stack, Random random, World world) {
        BakedModel bakedModel = itemRenderer.getModel(stack, world, null, 0);
        ItemEntityRenderer.renderStack(itemRenderer, matrices, vertexConsumers, light, stack, bakedModel, bakedModel.hasDepth(), random);
    }

    public static void renderStack(ItemRenderer itemRenderer, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ItemStack stack, BakedModel model, boolean depth, Random random) {
        float l;
        float k;
        int i = ItemEntityRenderer.getRenderedAmount(stack.getCount());
        float f = model.getTransformation().ground.scale.x();
        float g = model.getTransformation().ground.scale.y();
        float h = model.getTransformation().ground.scale.z();
        if (!depth) {
            float j = -0.0f * (float)(i - 1) * 0.5f * f;
            k = -0.0f * (float)(i - 1) * 0.5f * g;
            l = -0.09375f * (float)(i - 1) * 0.5f * h;
            matrices.translate(j, k, l);
        }
        for (int m = 0; m < i; ++m) {
            matrices.push();
            if (m > 0) {
                if (depth) {
                    k = (random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    l = (random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    float n = (random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    matrices.translate(k, l, n);
                } else {
                    k = (random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                    l = (random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                    matrices.translate(k, l, 0.0f);
                }
            }
            itemRenderer.renderItem(stack, ModelTransformationMode.GROUND, false, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV, model);
            matrices.pop();
            if (depth) continue;
            matrices.translate(0.0f * f, 0.0f * g, 0.09375f * h);
        }
    }
}

