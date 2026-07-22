/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.block.vault.VaultClientData;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

@Environment(value=EnvType.CLIENT)
public class VaultBlockEntityRenderer
implements BlockEntityRenderer<VaultBlockEntity> {
    private final ItemRenderer itemRenderer;
    private final Random random = Random.create();

    public VaultBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(VaultBlockEntity vaultBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
        if (!VaultBlockEntity.Client.hasDisplayItem(vaultBlockEntity.getSharedData())) {
            return;
        }
        World world = vaultBlockEntity.getWorld();
        if (world == null) {
            return;
        }
        ItemStack itemStack = vaultBlockEntity.getSharedData().getDisplayItem();
        if (itemStack.isEmpty()) {
            return;
        }
        this.random.setSeed(ItemEntityRenderer.getSeed(itemStack));
        VaultClientData vaultClientData = vaultBlockEntity.getClientData();
        VaultBlockEntityRenderer.renderDisplayItem(f, world, matrixStack, vertexConsumerProvider, i, itemStack, this.itemRenderer, vaultClientData.getPreviousDisplayRotation(), vaultClientData.getDisplayRotation(), this.random);
    }

    public static void renderDisplayItem(float tickDelta, World world, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ItemStack stack, ItemRenderer itemRenderer, float prevRotation, float rotation, Random random) {
        matrices.push();
        matrices.translate(0.5f, 0.4f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerpAngleDegrees(tickDelta, prevRotation, rotation)));
        ItemEntityRenderer.renderStack(itemRenderer, matrices, vertexConsumers, light, stack, random, world);
        matrices.pop();
    }
}

