//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class AutoEsu extends Module {
   private final Setting<Integer> offsetX = this.add(new Setting<>("OffsetX", -42, -500, 500));
   private final Setting<Integer> offsetY = this.add(new Setting<>("OffsetY", -27, -500, 500));
   private final Setting<Integer> width = this.add(new Setting<>("Width", 84, 0, 500));
   private final Setting<Integer> height = this.add(new Setting<>("Height", 40, 0, 500));
   private final Setting<Boolean> noFriend = this.add(new Setting<>("NoFriend", true));
   private final Setting<AutoEsu.Mode> mode = this.add(new Setting<>("Mode", AutoEsu.Mode.RuiNan));

   public AutoEsu() {
      super("AutoEsu", "IQ RuiNan", Category.RENDER);
   }

   @Override
   public void onRender3D(Render3DEvent event) {
      for(EntityPlayer target : mc.world.playerEntities) {
         if (!this.invalid(target)) {
            this.drawBurrowESP(target.posX, target.posY + 1.5, target.posZ);
         }
      }
   }

   private void drawBurrowESP(double x, double y, double z) {
      GlStateManager.pushMatrix();
      RenderHelper.enableStandardItemLighting();
      GlStateManager.enablePolygonOffset();
      GlStateManager.doPolygonOffset(1.0F, -1500000.0F);
      GlStateManager.disableLighting();
      GlStateManager.disableDepth();
      double scale = 0.0245;
      GlStateManager.translate(x - mc.getRenderManager().renderPosX, y - mc.getRenderManager().renderPosY, z - mc.getRenderManager().renderPosZ);
      GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
      float var10001 = mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F;
      GlStateManager.rotate(mc.getRenderManager().playerViewX, var10001, 0.0F, 0.0F);
      GlStateManager.scale(-scale, -scale, -scale);
      RenderUtil.glColor(new Color(255, 255, 255, 120));
      RenderUtil.drawCircle(1.5F, -5.0F, 16.0F, ColorUtil.injectAlpha(new Color(255, 255, 255, 120).getRGB(), 0));
      GlStateManager.enableAlpha();
      if (this.mode.getValue() == AutoEsu.Mode.ShengJie) {
         mc.getTextureManager().bindTexture(new ResourceLocation("textures/rebirth/mugshot/shengjie.png"));
      } else if (this.mode.getValue() == AutoEsu.Mode.RuiNan) {
         mc.getTextureManager().bindTexture(new ResourceLocation("textures/rebirth/mugshot/ruinan.png"));
      } else if (this.mode.getValue() == AutoEsu.Mode.ShanZhu) {
         mc.getTextureManager().bindTexture(new ResourceLocation("textures/rebirth/mugshot/shanzhu.png"));
      }

      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      RenderUtil.drawModalRect(
         this.offsetX.getValue(), this.offsetY.getValue(), 0.0F, 0.0F, 12, 12, this.width.getValue(), this.height.getValue(), 12.0F, 12.0F
      );
      GlStateManager.disableAlpha();
      GlStateManager.enableDepth();
      GlStateManager.disableBlend();
      GlStateManager.disablePolygonOffset();
      GlStateManager.doPolygonOffset(1.0F, 1500000.0F);
      GlStateManager.popMatrix();
   }

   private boolean invalid(Entity entity) {
      return entity == null
         || EntityUtil.isDead(entity)
         || entity.equals(mc.player)
         || entity instanceof EntityPlayer && Managers.FRIENDS.isFriend(entity.getName()) && this.noFriend.getValue()
         || mc.player.getDistanceSq(entity) < MathUtil.square(0.5);
   }

   public static enum Mode {
      RuiNan,
      ShengJie,
      ShanZhu;
   }
}
