//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.asm.mixins;

import me.rebirthclient.mod.modules.impl.render.Chams;
import me.rebirthclient.mod.modules.impl.render.Models;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LayerArmorBase.class})
public class MixinLayerArmorBase {
   @Shadow
   private void renderArmorLayer(
      EntityLivingBase entityLivingBaseIn,
      float limbSwing,
      float limbSwingAmount,
      float partialTicks,
      float ageInTicks,
      float netHeadYaw,
      float headPitch,
      float scale,
      EntityEquipmentSlot slotIn
   ) {
   }

   @Inject(
      method = {"doRenderLayer"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/renderer/entity/layers/LayerArmorBase;renderArmorLayer(Lnet/minecraft/entity/EntityLivingBase;FFFFFFFLnet/minecraft/inventory/EntityEquipmentSlot;)V",
   shift = At.Shift.BEFORE
)},
      cancellable = true
   )
   public void doRenderLayerHook(
      EntityLivingBase entity,
      float limbSwing,
      float limbSwingAmount,
      float partialTicks,
      float ageInTicks,
      float netHeadYaw,
      float headPitch,
      float scale,
      CallbackInfo info
   ) {
      Chams mod = Chams.INSTANCE;
      if (mod.isOn() && entity instanceof EntityPlayer) {
         info.cancel();
         float newLimbSwing = mod.isOn() && mod.noInterp.getValue() ? 0.0F : limbSwing;
         float newLimbSwingAmount = mod.isOn() && mod.noInterp.getValue() ? 0.0F : limbSwingAmount;
         if (!mod.self.getValue() && entity instanceof EntityPlayerSP) {
            this.renderLayers(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
            return;
         }

         this.renderLayers(entity, newLimbSwing, newLimbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
      } else {
         this.renderLayers(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
      }
   }

   @Inject(
      method = {"doRenderLayer"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void doRenderLayer(
      EntityLivingBase entitylivingbaseIn,
      float limbSwing,
      float limbSwingAmount,
      float partialTicks,
      float ageInTicks,
      float netHeadYaw,
      float headPitch,
      float scale,
      CallbackInfo ci
   ) {
      if (Models.INSTANCE.isOn() && Models.INSTANCE.onlySelf.getValue() && entitylivingbaseIn == Minecraft.getMinecraft().player) {
         ci.cancel();
      } else if (Models.INSTANCE.isOn() && !Models.INSTANCE.onlySelf.getValue()) {
         ci.cancel();
      }
   }

   private void renderLayers(
      EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale
   ) {
      this.renderArmorLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.CHEST);
      this.renderArmorLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.LEGS);
      this.renderArmorLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.FEET);
      this.renderArmorLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.HEAD);
   }
}
