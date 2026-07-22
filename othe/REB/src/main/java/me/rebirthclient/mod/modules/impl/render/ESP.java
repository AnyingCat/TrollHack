//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.InterpolationUtil;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class ESP extends Module {
   private final Setting<ESP.Page> page = this.add(new Setting<>("Settings", ESP.Page.GLOBAL));
   private final Setting<ESP.Items> items = this.add(new Setting<>("Items", ESP.Items.BOX, v -> this.page.getValue() == ESP.Page.GLOBAL));
   private final Setting<Boolean> xpOrbs = this.add(new Setting<>("ExpOrbs", false, v -> this.page.getValue() == ESP.Page.GLOBAL));
   private final Setting<Boolean> xp = this.add(new Setting<>("ExpBottles", false, v -> this.page.getValue() == ESP.Page.GLOBAL));
   private final Setting<Boolean> pearls = this.add(new Setting<>("Pearls", true, v -> this.page.getValue() == ESP.Page.GLOBAL));
   private final Setting<ESP.Players> players = this.add(new Setting<>("Players", ESP.Players.BOX, v -> this.page.getValue() == ESP.Page.GLOBAL));
   private final Setting<ESP.Burrow> burrow = this.add(new Setting<>("Burrow", ESP.Burrow.PRETTY, v -> this.page.getValue() == ESP.Page.GLOBAL));
   private final Setting<Color> textColor = this.add(
      new Setting<>("TextColor", new Color(-1), v -> this.page.getValue() == ESP.Page.COLORS).injectBoolean(false)
   );
   private final Setting<Color> color = this.add(new Setting<>("Color", new Color(125, 125, 213, 150), v -> this.page.getValue() == ESP.Page.COLORS));
   private final Setting<Color> lineColor = this.add(
      new Setting<>("LineColor", new Color(-1493172225, true), v -> this.page.getValue() == ESP.Page.COLORS).injectBoolean(false)
   );

   public ESP() {
      super("ESP", "Highlights entities through walls in several modes", Category.RENDER);
   }

   @Override
   public void onRender3D(Render3DEvent event) {
      if (!fullNullCheck()) {
         for(Entity entity : mc.world.loadedEntityList) {
            if (entity != mc.player
                  && entity instanceof EntityPlayer
                  && this.players.getValue() == ESP.Players.BOX
                  && !((EntityPlayer)entity).isSpectator()
               || entity instanceof EntityExpBottle && this.xp.getValue()
               || entity instanceof EntityXPOrb && this.xpOrbs.getValue()
               || entity instanceof EntityEnderPearl && this.pearls.getValue()
               || entity instanceof EntityItem && this.items.getValue() == ESP.Items.BOX) {
               RenderUtil.drawEntityBoxESP(
                  entity, this.color.getValue(), this.lineColor.booleanValue, this.lineColor.getValue(), 1.0F, true, true, this.color.getValue().getAlpha()
               );
            }
         }

         for(Entity entity : mc.world.loadedEntityList) {
            if (entity instanceof EntityItem && this.items.getValue() == ESP.Items.TEXT) {
               ItemStack stack = ((EntityItem)entity).getItem();
               String text = stack.getDisplayName() + (stack.isStackable() && stack.getCount() >= 2 ? " x" + stack.getCount() : "");
               Vec3d vec = InterpolationUtil.getInterpolatedPos(entity, mc.getRenderPartialTicks(), true);
               this.drawNameTag(text, vec);
            }
         }

         for(EntityPlayer player : mc.world.playerEntities) {
            BlockPos feetPos = new BlockPos(Math.floor(player.posX), Math.floor(player.posY + 0.2), Math.floor(player.posZ));
            if (!player.isSpectator()
               && !player.isRiding()
               && player != mc.player
               && BlockUtil.getBlock(feetPos) != Blocks.AIR
               && !BlockUtil.canReplace(feetPos)
               && !BlockUtil.isStair(BlockUtil.getBlock(feetPos))
               && !BlockUtil.isSlab(BlockUtil.getBlock(feetPos))
               && !BlockUtil.isFence(BlockUtil.getBlock(feetPos))
               && mc.player.getDistanceSq(feetPos) <= 200.0) {
               if (this.burrow.getValue() == ESP.Burrow.PRETTY) {
                  this.drawBurrowESP(feetPos);
               } else if (this.burrow.getValue() == ESP.Burrow.TEXT) {
                  this.drawNameTag(BlockUtil.getBlock(feetPos) == Blocks.WEB ? "Web" : "Burrow", feetPos);
               }
            }
         }
      }
   }

   private void drawBurrowESP(BlockPos pos) {
      GlStateManager.pushMatrix();
      RenderHelper.enableStandardItemLighting();
      GlStateManager.enablePolygonOffset();
      GlStateManager.doPolygonOffset(1.0F, -1500000.0F);
      GlStateManager.disableLighting();
      GlStateManager.disableDepth();
      double x = (double)pos.getX() + 0.5;
      double y = (double)pos.getY() + 0.4;
      double z = (double)pos.getZ() + 0.5;
      int distance = (int)mc.player.getDistance(x, y, z);
      double scale = (double)(0.0018F + 0.002F * (float)distance);
      if ((double)distance <= 8.0) {
         scale = 0.0245;
      }

      GlStateManager.translate(x - mc.getRenderManager().renderPosX, y - mc.getRenderManager().renderPosY, z - mc.getRenderManager().renderPosZ);
      GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
      float var10001 = mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F;
      GlStateManager.rotate(mc.getRenderManager().playerViewX, var10001, 0.0F, 0.0F);
      GlStateManager.scale(-scale, -scale, -scale);
      RenderUtil.glColor(this.color.getValue());
      RenderUtil.drawCircle(1.5F, -5.0F, 16.0F, ColorUtil.injectAlpha(this.color.getValue().getRGB(), 100));
      GlStateManager.enableAlpha();
      Block block = BlockUtil.getBlock(pos);
      if (block == Blocks.ENDER_CHEST) {
         mc.getTextureManager().bindTexture(new ResourceLocation("textures/rebirth/constant/ingame/echest.png"));
      } else if (block == Blocks.WEB) {
         mc.getTextureManager().bindTexture(new ResourceLocation("textures/rebirth/constant/ingame/web.png"));
      } else {
         mc.getTextureManager().bindTexture(new ResourceLocation("textures/rebirth/constant/ingame/obby.png"));
      }

      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      RenderUtil.drawModalRect(-10, -17, 0.0F, 0.0F, 12, 12, 24, 24, 12.0F, 12.0F);
      GlStateManager.disableAlpha();
      GlStateManager.enableDepth();
      GlStateManager.disableBlend();
      GlStateManager.disablePolygonOffset();
      GlStateManager.doPolygonOffset(1.0F, 1500000.0F);
      GlStateManager.popMatrix();
   }

   private void drawNameTag(String text, BlockPos pos) {
      GlStateManager.pushMatrix();
      RenderHelper.enableStandardItemLighting();
      GlStateManager.enablePolygonOffset();
      GlStateManager.doPolygonOffset(1.0F, -1500000.0F);
      GlStateManager.disableLighting();
      GlStateManager.disableDepth();
      GL11.glEnable(3553);
      double x = (double)pos.getX() + 0.5;
      double y = (double)pos.getY() + 0.7;
      double z = (double)pos.getZ() + 0.5;
      float scale = 0.030833336F;
      GlStateManager.translate(x - mc.getRenderManager().renderPosX, y - mc.getRenderManager().renderPosY, z - mc.getRenderManager().renderPosZ);
      GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(-mc.player.rotationYaw, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(mc.player.rotationPitch, mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F, 0.0F, 0.0F);
      GlStateManager.scale(-scale, -scale, scale);
      int distance = (int)mc.player.getDistance(x, y, z);
      float scaleD = (float)distance / 2.0F / 3.0F;
      if (scaleD < 1.0F) {
         scaleD = 1.0F;
      }

      GlStateManager.scale(scaleD, scaleD, scaleD);
      GlStateManager.translate(-((double)Managers.TEXT.getStringWidth(text) / 2.0), 0.0, 0.0);
      Managers.TEXT.drawStringWithShadow(text, 0.0F, 6.0F, this.textColor.booleanValue ? this.textColor.getValue().getRGB() : -1);
      GlStateManager.enableDepth();
      GlStateManager.disableBlend();
      GlStateManager.disablePolygonOffset();
      GlStateManager.doPolygonOffset(1.0F, 1500000.0F);
      GlStateManager.popMatrix();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void drawNameTag(String text, Vec3d vec) {
      GlStateManager.pushMatrix();
      RenderHelper.enableStandardItemLighting();
      GlStateManager.enablePolygonOffset();
      GlStateManager.doPolygonOffset(1.0F, -1500000.0F);
      GlStateManager.disableLighting();
      GlStateManager.disableDepth();
      GL11.glEnable(3553);
      double x = vec.x;
      double y = vec.y;
      double z = vec.z;
      Entity camera = mc.getRenderViewEntity();

      assert camera != null;

      double distance = camera.getDistance(
         x + mc.getRenderManager().viewerPosX, y + mc.getRenderManager().viewerPosY, z + mc.getRenderManager().viewerPosZ
      );
      double scale = 0.0018 + 0.003F * distance;
      int textWidth = Managers.TEXT.getStringWidth(text) / 2;
      if (distance <= 8.0) {
         scale = 0.0245;
      }

      GlStateManager.translate(x, y + 0.4F, z);
      GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
      float var10001 = mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F;
      GlStateManager.rotate(mc.getRenderManager().playerViewX, var10001, 0.0F, 0.0F);
      GlStateManager.scale(-scale, -scale, scale);
      Managers.TEXT
         .drawStringWithShadow(
            text,
            (float)(-textWidth) - 0.1F,
            (float)(-(mc.fontRenderer.FONT_HEIGHT - 1)),
            this.textColor.booleanValue ? this.textColor.getValue().getRGB() : -1
         );
      GlStateManager.enableDepth();
      GlStateManager.disableBlend();
      GlStateManager.disablePolygonOffset();
      GlStateManager.doPolygonOffset(1.0F, 1500000.0F);
      GlStateManager.popMatrix();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public static enum Burrow {
      PRETTY,
      TEXT,
      OFF;
   }

   public static enum Items {
      BOX,
      TEXT,
      OFF;
   }

   public static enum Page {
      COLORS,
      GLOBAL;
   }

   public static enum Players {
      BOX,
      OFF;
   }
}
