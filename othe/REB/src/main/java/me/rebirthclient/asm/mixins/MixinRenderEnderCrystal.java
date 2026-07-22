//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.asm.mixins;

import java.awt.Color;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.impl.render.CrystalChams;
import me.rebirthclient.mod.modules.impl.render.Shader;
import me.rebirthclient.mod.modules.impl.render.Shaders;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderEnderCrystal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({RenderEnderCrystal.class})
public class MixinRenderEnderCrystal {
   @Redirect(
      method = {"doRender(Lnet/minecraft/entity/item/EntityEnderCrystal;DDDFF)V"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V"
)
   )
   public void renderModelBaseHook(
      ModelBase model, Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale
   ) {
      CrystalChams mod = CrystalChams.INSTANCE;
      float newLimbSwingAmount = mod.changeSpeed.getValue() ? limbSwingAmount * mod.spinSpeed.getValue() : limbSwingAmount;
      float newAgeInTicks = mod.changeSpeed.getValue() ? (mod.floatFactor.getValue() == 0.0F ? 0.15F : ageInTicks * mod.floatFactor.getValue()) : ageInTicks;
      if (mod.isOn()) {
         GlStateManager.scale(mod.scale.getValue(), mod.scale.getValue(), mod.scale.getValue());
         if (mod.model.getValue() == CrystalChams.Model.VANILLA) {
            model.render(entity, limbSwing, newLimbSwingAmount, newAgeInTicks, netHeadYaw, headPitch, scale);
         } else if (mod.model.getValue() == CrystalChams.Model.XQZ) {
            GL11.glEnable(32823);
            GlStateManager.enablePolygonOffset();
            GL11.glPolygonOffset(1.0F, -1000000.0F);
            if (mod.modelColor.booleanValue) {
               Color color = new Color(
                  mod.modelColor.getValue().getRed(),
                  mod.modelColor.getValue().getGreen(),
                  mod.modelColor.getValue().getBlue(),
                  mod.modelColor.getValue().getAlpha()
               );
               RenderUtil.glColor(color);
            }

            model.render(entity, limbSwing, newLimbSwingAmount, newAgeInTicks, netHeadYaw, headPitch, scale);
            GL11.glDisable(32823);
            GlStateManager.disablePolygonOffset();
            GL11.glPolygonOffset(1.0F, 1000000.0F);
         } else if (Shader.INSTANCE.isOn() && Shader.crystalRender) {
            model.render(entity, limbSwing, newLimbSwingAmount, newAgeInTicks, netHeadYaw, headPitch, scale);
         } else if (Shaders.INSTANCE.isOn() && Shaders.INSTANCE.crystal.getValue() != Shaders.Crystal1.None && !Shaders.INSTANCE.notShader) {
            model.render(entity, limbSwing, newLimbSwingAmount, newAgeInTicks, netHeadYaw, headPitch, scale);
         }

         if (mod.wireframe.getValue()) {
            Color color = mod.lineColor.booleanValue
               ? new Color(
                  mod.lineColor.getValue().getRed(),
                  mod.lineColor.getValue().getGreen(),
                  mod.lineColor.getValue().getBlue(),
                  mod.lineColor.getValue().getAlpha()
               )
               : new Color(mod.color.getValue().getRed(), mod.color.getValue().getGreen(), mod.color.getValue().getBlue(), mod.color.getValue().getAlpha());
            GL11.glPushMatrix();
            GL11.glPushAttrib(1048575);
            GL11.glPolygonMode(1032, 6913);
            GL11.glDisable(3553);
            GL11.glDisable(2896);
            GL11.glDisable(2929);
            GL11.glEnable(2848);
            GL11.glEnable(3042);
            GlStateManager.blendFunc(770, 771);
            RenderUtil.glColor(color);
            GlStateManager.glLineWidth(mod.lineWidth.getValue());
            model.render(entity, limbSwing, newLimbSwingAmount, newAgeInTicks, netHeadYaw, headPitch, scale);
            GL11.glPopAttrib();
            GL11.glPopMatrix();
         }

         if (mod.fill.getValue()) {
            Color color = new Color(
               mod.color.getValue().getRed(), mod.color.getValue().getGreen(), mod.color.getValue().getBlue(), mod.color.getValue().getAlpha()
            );
            GL11.glPushAttrib(1048575);
            GL11.glDisable(3008);
            GL11.glDisable(3553);
            GL11.glDisable(2896);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glLineWidth(1.5F);
            GL11.glEnable(2960);
            if (mod.xqz.getValue()) {
               GL11.glDisable(2929);
               GL11.glDepthMask(false);
            }

            GL11.glEnable(10754);
            RenderUtil.glColor(color);
            model.render(entity, limbSwing, newLimbSwingAmount, newAgeInTicks, netHeadYaw, headPitch, scale);
            if (mod.xqz.getValue()) {
               GL11.glEnable(2929);
               GL11.glDepthMask(true);
            }

            GL11.glEnable(3042);
            GL11.glEnable(2896);
            GL11.glEnable(3553);
            GL11.glEnable(3008);
            GL11.glPopAttrib();
         }

         if (mod.glint.getValue() && entity instanceof EntityEnderCrystal) {
            Color color = new Color(
               mod.color.getValue().getRed(), mod.color.getValue().getGreen(), mod.color.getValue().getBlue(), mod.color.getValue().getAlpha()
            );
            GL11.glPushMatrix();
            GL11.glPushAttrib(1048575);
            GL11.glPolygonMode(1032, 6914);
            GL11.glDisable(2896);
            GL11.glDepthRange(0.0, 0.1);
            GL11.glEnable(3042);
            RenderUtil.glColor(color);
            GlStateManager.blendFunc(SourceFactor.SRC_COLOR, DestFactor.ONE);
            float f = (float)entity.ticksExisted + Wrapper.mc.getRenderPartialTicks();
            Wrapper.mc.getRenderManager().renderEngine.bindTexture(new ResourceLocation("textures/misc/enchanted_item_glint.png"));

            for(int i = 0; i < 2; ++i) {
               GlStateManager.matrixMode(5890);
               GlStateManager.loadIdentity();
               GL11.glScalef(1.0F, 1.0F, 1.0F);
               GlStateManager.rotate(30.0F - (float)i * 60.0F, 0.0F, 0.0F, 1.0F);
               GlStateManager.translate(0.0F, f * (0.001F + (float)i * 0.003F) * 20.0F, 0.0F);
               GlStateManager.matrixMode(5888);
               model.render(entity, limbSwing, newLimbSwingAmount, newAgeInTicks, netHeadYaw, headPitch, scale);
            }

            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            GlStateManager.matrixMode(5888);
            GL11.glDisable(3042);
            GL11.glDepthRange(0.0, 1.0);
            GL11.glEnable(2896);
            GL11.glPopAttrib();
            GL11.glPopMatrix();
         }

         GlStateManager.scale(1.0F / mod.scale.getValue(), 1.0F / mod.scale.getValue(), 1.0F / mod.scale.getValue());
      } else {
         model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
      }
   }
}
