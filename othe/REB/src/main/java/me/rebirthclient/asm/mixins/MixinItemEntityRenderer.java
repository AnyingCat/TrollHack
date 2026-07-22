//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.asm.mixins;

import java.util.Random;
import me.rebirthclient.mod.modules.impl.render.ItemPhysics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({RenderEntityItem.class})
public abstract class MixinItemEntityRenderer extends Render<EntityItem> {
   private static final float RAD_TO_DEG = (float) (180.0 / Math.PI);
   @Shadow
   @Final
   private Random random;
   @Shadow
   private RenderItem itemRenderer;

   protected MixinItemEntityRenderer(RenderManager p_i1487_1_) {
      super(p_i1487_1_);
   }

   @Shadow
   public abstract int getModelCount(ItemStack var1);

   @Inject(
      at = {@At("HEAD")},
      method = {"doRender"},
      cancellable = true
   )
   public void render(EntityItem itemEntity, double x, double y, double z, float yaw, float partialTicks, CallbackInfo callbackInfo) {
      ItemStack itemStack = itemEntity.getItem();
      if (ItemPhysics.INSTANCE.isOn() && itemStack.getItem() != null) {
         int renderCount = this.getModelCount(itemStack);
         Item item = itemStack.getItem();
         int seed = Item.getIdFromItem(item) + itemStack.getItemDamage();
         this.random.setSeed((long)seed);
         float rotation = (((float)itemEntity.getAge() + partialTicks) / 20.0F + itemEntity.height)
            / 20.0F
            * ItemPhysics.INSTANCE.rotateSpeed.getValue();
         this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
         this.getRenderManager().renderEngine.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
         GlStateManager.enableRescaleNormal();
         GlStateManager.alphaFunc(516, 0.1F);
         GlStateManager.enableBlend();
         RenderHelper.enableStandardItemLighting();
         GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
         GlStateManager.pushMatrix();
         GlStateManager.translate((float)x, (float)y, (float)z);
         if (itemEntity.getItem().item instanceof ItemShulkerBox) {
            GlStateManager.scale(
               ItemPhysics.INSTANCE.Scaling.getValue() + 1.1F + ItemPhysics.INSTANCE.shulkerBox.getValue(),
               ItemPhysics.INSTANCE.Scaling.getValue() + 1.1F + ItemPhysics.INSTANCE.shulkerBox.getValue(),
               ItemPhysics.INSTANCE.Scaling.getValue() + 1.1F + ItemPhysics.INSTANCE.shulkerBox.getValue()
            );
         } else if (itemEntity.getItem().item instanceof ItemBlock) {
            GlStateManager.scale(
               ItemPhysics.INSTANCE.Scaling.getValue() + 1.1F, ItemPhysics.INSTANCE.Scaling.getValue() + 1.1F, ItemPhysics.INSTANCE.Scaling.getValue() + 1.1F
            );
         } else {
            GlStateManager.scale(
               ItemPhysics.INSTANCE.Scaling.getValue() + 0.3F, ItemPhysics.INSTANCE.Scaling.getValue() + 0.3F, ItemPhysics.INSTANCE.Scaling.getValue() + 0.3F
            );
         }

         GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(itemEntity.rotationYaw * (float) (180.0 / Math.PI), 0.0F, 0.0F, 1.0F);
         Minecraft mc = Minecraft.getMinecraft();
         IBakedModel iBakedModel = mc.getRenderItem().getItemModelMesher().getItemModel(itemStack);
         this.rotateX(itemEntity, rotation);
         if (iBakedModel.isGui3d()) {
            GlStateManager.translate(0.0F, -0.2F, -0.08F);
         } else if (itemEntity.getEntityWorld().getBlockState(itemEntity.getPosition()).getBlock() != Blocks.SNOW
            && itemEntity.getEntityWorld().getBlockState(itemEntity.getPosition().down()).getBlock() != Blocks.SOUL_SAND) {
            GlStateManager.translate(0.0F, 0.0F, -0.04F);
         } else {
            GlStateManager.translate(0.0F, 0.0F, -0.14F);
         }

         float height = 0.2F;
         if (iBakedModel.isGui3d()) {
            GlStateManager.translate(0.0F, height, 0.0F);
         }

         GlStateManager.rotate(itemEntity.rotationPitch * (float) (180.0 / Math.PI), 0.0F, 1.0F, 0.0F);
         if (iBakedModel.isGui3d()) {
            GlStateManager.translate(0.0F, -height, 0.0F);
         }

         if (!iBakedModel.isGui3d()) {
            float xO = -0.0F * (float)(renderCount - 1) * 0.5F;
            float yO = -0.0F * (float)(renderCount - 1) * 0.5F;
            float zO = -0.09375F * (float)(renderCount - 1) * 0.5F;
            GlStateManager.translate(xO, yO, zO);
         }

         for(int k = 0; k < renderCount; ++k) {
            GlStateManager.pushMatrix();
            if (k > 0 && iBakedModel.isGui3d()) {
               float f11 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               float f13 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               float f10 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               GlStateManager.translate(f11, f13, f10);
            }

            iBakedModel.getItemCameraTransforms().applyTransform(TransformType.GROUND);
            this.itemRenderer.renderItem(itemStack, iBakedModel);
            GlStateManager.popMatrix();
            if (!iBakedModel.isGui3d()) {
               GlStateManager.translate(0.0F, 0.0F, 0.09375F);
            }
         }

         GlStateManager.popMatrix();
         GlStateManager.disableRescaleNormal();
         GlStateManager.disableBlend();
         this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
         this.getRenderManager().renderEngine.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
         callbackInfo.cancel();
      }
   }

   private void rotateX(EntityItem itemEntity, float rotation) {
      if (!itemEntity.onGround) {
         itemEntity.rotationPitch += rotation * 2.0F;
      }
   }
}
