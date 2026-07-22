//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.render;

import com.mojang.authlib.GameProfile;
import java.awt.Color;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class PopChams extends Module {
   public static Setting<Boolean> self;
   public static Setting<Boolean> elevator;
   public static Setting<Integer> fadestart;
   public static Setting<Float> fadetime;
   public static Setting<Boolean> onlyOneEsp;
   public static Setting<PopChams.ElevatorMode> elevatorMode;
   public static PopChams INSTANCE;
   private final Setting<Color> outlineColor = this.add(new Setting<>("Outline Color", new Color(255, 255, 255, 100)));
   private final Setting<Color> fillColor = this.add(new Setting<>("Fill Color", new Color(255, 255, 255, 100)));
   EntityOtherPlayerMP player;
   ModelPlayer playerModel;
   Long startTime;
   double alphaFill;
   double alphaLine;

   public PopChams() {
      super("PopChams", "Pop rendering", Category.RENDER);
      INSTANCE = this;
      self = this.add(new Setting<>("Self", true));
      elevator = this.add(new Setting<>("Travel", true).setParent());
      elevatorMode = this.add(new Setting<>("Elevator", PopChams.ElevatorMode.UP, v -> elevator.isOpen()));
      fadestart = this.add(new Setting<>("Fade Start", 0, 0, 255));
      fadetime = this.add(new Setting<>("Fade Time", 0.5F, 0.0F, 2.0F));
      onlyOneEsp = this.add(new Setting<>("Only Render One", true));
   }

   public static void renderEntity(EntityLivingBase entity, ModelBase modelBase, float limbSwing, float limbSwingAmount, int scale) {
      float partialTicks = mc.getRenderPartialTicks();
      double x = entity.posX - mc.getRenderManager().viewerPosX;
      double y = entity.posY - mc.getRenderManager().viewerPosY;
      double z = entity.posZ - mc.getRenderManager().viewerPosZ;
      GlStateManager.pushMatrix();
      if (entity.isSneaking()) {
         y -= 0.125;
      }

      renderLivingAt(x, y, z);
      float f8 = handleRotationFloat();
      prepareRotations(entity);
      float f9 = prepareScale(entity, (float)scale);
      GlStateManager.enableAlpha();
      modelBase.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
      modelBase.setRotationAngles(limbSwing, limbSwingAmount, f8, entity.rotationYaw, entity.rotationPitch, f9, entity);
      modelBase.render(entity, limbSwing, limbSwingAmount, f8, entity.rotationYaw, entity.rotationPitch, f9);
      GlStateManager.popMatrix();
   }

   public static void renderLivingAt(double x, double y, double z) {
      GlStateManager.translate(x, y, z);
   }

   public static float prepareScale(EntityLivingBase entity, float scale) {
      GlStateManager.enableRescaleNormal();
      GlStateManager.scale(-1.0F, -1.0F, 1.0F);
      double widthX = entity.getRenderBoundingBox().maxX - entity.getRenderBoundingBox().minX;
      double widthZ = entity.getRenderBoundingBox().maxZ - entity.getRenderBoundingBox().minZ;
      GlStateManager.scale((double)scale + widthX, (double)(scale * entity.height), (double)scale + widthZ);
      GlStateManager.translate(0.0F, -1.501F, 0.0F);
      return 0.0625F;
   }

   public static void prepareRotations(EntityLivingBase entityLivingBase) {
      GlStateManager.rotate(180.0F - entityLivingBase.rotationYaw, 0.0F, 1.0F, 0.0F);
   }

   public static Color newAlpha(Color color, int alpha) {
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
   }

   public static void glColor(Color color) {
      GL11.glColor4f((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)color.getAlpha() / 255.0F);
   }

   public static float handleRotationFloat() {
      return 0.0F;
   }

   public static void prepareGL() {
      GL11.glBlendFunc(770, 771);
      GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
      GlStateManager.glLineWidth(1.5F);
      GlStateManager.disableTexture2D();
      GlStateManager.depthMask(false);
      GlStateManager.enableBlend();
      GlStateManager.disableDepth();
      GlStateManager.disableLighting();
      GlStateManager.disableCull();
      GlStateManager.enableAlpha();
      GlStateManager.color(1.0F, 1.0F, 1.0F);
   }

   public static void releaseGL() {
      GlStateManager.enableCull();
      GlStateManager.depthMask(true);
      GlStateManager.enableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.enableDepth();
   }

   @SubscribeEvent
   public void onReceivePacket(PacketEvent.Receive event) {
      if (!event.isCanceled() && !fullNullCheck()) {
         if (EarthPopChams.INSTANCE.isOn()) {
            this.disable();
         } else {
            SPacketEntityStatus packet;
            if (event.getPacket() instanceof SPacketEntityStatus && (packet = event.getPacket()).getOpCode() == 35) {
               packet.getEntity(mc.world);
               if (self.getValue() || packet.getEntity(mc.world).getEntityId() != mc.player.getEntityId()) {
                  GameProfile profile = new GameProfile(mc.player.getUniqueID(), "");
                  this.player = new EntityOtherPlayerMP(mc.world, profile);
                  this.player.copyLocationAndAnglesFrom(packet.getEntity(mc.world));
                  this.playerModel = new ModelPlayer(0.0F, false);
                  this.startTime = System.currentTimeMillis();
                  this.playerModel.bipedHead.showModel = false;
                  this.playerModel.bipedBody.showModel = false;
                  this.playerModel.bipedLeftArmwear.showModel = false;
                  this.playerModel.bipedLeftLegwear.showModel = false;
                  this.playerModel.bipedRightArmwear.showModel = false;
                  this.playerModel.bipedRightLegwear.showModel = false;
                  this.alphaFill = (double)this.fillColor.getValue().getAlpha();
                  this.alphaLine = (double)this.outlineColor.getValue().getAlpha();
                  if (!onlyOneEsp.getValue()) {
                     new PopChams.TotemPopChams(this.player, this.playerModel, this.startTime, this.alphaFill);
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onRenderWorld(RenderWorldLastEvent event) {
      if (!fullNullCheck()) {
         if (EarthPopChams.INSTANCE.isOn()) {
            this.disable();
         } else {
            if (onlyOneEsp.getValue()) {
               if (this.player == null || mc.world == null || mc.player == null) {
                  return;
               }

               if (elevator.getValue()) {
                  if (elevatorMode.getValue() == PopChams.ElevatorMode.UP) {
                     this.player.posY += (double)(0.05F * event.getPartialTicks());
                  } else if (elevatorMode.getValue() == PopChams.ElevatorMode.DOWN) {
                     this.player.posY -= (double)(0.05F * event.getPartialTicks());
                  }
               }

               GL11.glLineWidth(1.0F);
               Color lineColorS = this.outlineColor.getValue();
               Color fillColorS = this.fillColor.getValue();
               int lineA = lineColorS.getAlpha();
               int fillA = fillColorS.getAlpha();
               long time = System.currentTimeMillis() - this.startTime - fadestart.getValue().longValue();
               if (System.currentTimeMillis() - this.startTime > fadestart.getValue().longValue()) {
                  double normal = this.normalize((double)time, fadetime.getValue().doubleValue());
                  normal = MathHelper.clamp(normal, 0.0, 1.0);
                  normal = -normal + 1.0;
                  lineA *= (int)normal;
                  fillA *= (int)normal;
               }

               Color lineColor = newAlpha(lineColorS, lineA);
               Color fillColor = newAlpha(fillColorS, fillA);
               if (this.player != null && this.playerModel != null) {
                  prepareGL();
                  GL11.glPushAttrib(1048575);
                  GL11.glEnable(2881);
                  GL11.glEnable(2848);
                  if (this.alphaFill > 1.0) {
                     this.alphaFill -= (double)fadetime.getValue().floatValue();
                  }

                  Color fillFinal = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), (int)this.alphaFill);
                  if (this.alphaLine > 1.0) {
                     this.alphaLine -= (double)fadetime.getValue().floatValue();
                  }

                  Color outlineFinal = new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), (int)this.alphaLine);
                  glColor(fillFinal);
                  GL11.glPolygonMode(1032, 6914);
                  renderEntity(this.player, this.playerModel, this.player.limbSwing, this.player.limbSwingAmount, 1);
                  glColor(outlineFinal);
                  GL11.glPolygonMode(1032, 6913);
                  renderEntity(this.player, this.playerModel, this.player.limbSwing, this.player.limbSwingAmount, 1);
                  GL11.glPolygonMode(1032, 6914);
                  GL11.glPopAttrib();
                  releaseGL();
               }
            }
         }
      }
   }

   double normalize(double value, double max) {
      return (value - 0.0) / (max - 0.0);
   }

   public static enum ElevatorMode {
      UP,
      DOWN;
   }

   public static class TotemPopChams {
      final EntityOtherPlayerMP player;
      final ModelPlayer playerModel;
      final Long startTime;
      double alphaFill;
      double alphaLine;

      public TotemPopChams(EntityOtherPlayerMP player, ModelPlayer playerModel, Long startTime, double alphaFill) {
         MinecraftForge.EVENT_BUS.register(this);
         this.player = player;
         this.playerModel = playerModel;
         this.startTime = startTime;
         this.alphaFill = alphaFill;
         this.alphaLine = alphaFill;
      }

      public static void renderEntity(EntityLivingBase entity, ModelBase modelBase, float limbSwing, float limbSwingAmount, float scale) {
         float partialTicks = Wrapper.mc.getRenderPartialTicks();
         double x = entity.posX - Wrapper.mc.getRenderManager().viewerPosX;
         double y = entity.posY - Wrapper.mc.getRenderManager().viewerPosY;
         double z = entity.posZ - Wrapper.mc.getRenderManager().viewerPosZ;
         GlStateManager.pushMatrix();
         if (entity.isSneaking()) {
            y -= 0.125;
         }

         renderLivingAt(x, y, z);
         float f8 = handleRotationFloat();
         prepareRotations(entity);
         float f9 = prepareScale(entity, scale);
         GlStateManager.enableAlpha();
         modelBase.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
         modelBase.setRotationAngles(limbSwing, limbSwingAmount, f8, entity.rotationYawHead, entity.rotationPitch, f9, entity);
         modelBase.render(entity, limbSwing, limbSwingAmount, f8, entity.rotationYawHead, entity.rotationPitch, f9);
         GlStateManager.popMatrix();
      }

      public static void renderLivingAt(double x, double y, double z) {
         GlStateManager.translate((float)x, (float)y, (float)z);
      }

      public static float prepareScale(EntityLivingBase entity, float scale) {
         GlStateManager.enableRescaleNormal();
         GlStateManager.scale(-1.0F, -1.0F, 1.0F);
         double widthX = entity.getRenderBoundingBox().maxX - entity.getRenderBoundingBox().minX;
         double widthZ = entity.getRenderBoundingBox().maxZ - entity.getRenderBoundingBox().minZ;
         GlStateManager.scale((double)scale + widthX, (double)(scale * entity.height), (double)scale + widthZ);
         GlStateManager.translate(0.0F, -1.501F, 0.0F);
         return 0.0625F;
      }

      public static void prepareRotations(EntityLivingBase entityLivingBase) {
         GlStateManager.rotate(180.0F - entityLivingBase.rotationYaw, 0.0F, 1.0F, 0.0F);
      }

      public static Color newAlpha(Color color, int alpha) {
         return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
      }

      public static void glColor(Color color) {
         GL11.glColor4f((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)color.getAlpha() / 255.0F);
      }

      public static float handleRotationFloat() {
         return 0.0F;
      }

      @SubscribeEvent
      public void onRenderWorld(RenderWorldLastEvent event) {
         if (this.player != null && Wrapper.mc.world != null && Wrapper.mc.player != null) {
            GL11.glLineWidth(1.0F);
            Color lineColorS = PopChams.INSTANCE.outlineColor.getValue();
            Color fillColorS = PopChams.INSTANCE.fillColor.getValue();
            int lineA = lineColorS.getAlpha();
            int fillA = fillColorS.getAlpha();
            long time = System.currentTimeMillis() - this.startTime - PopChams.fadestart.getValue().longValue();
            if (System.currentTimeMillis() - this.startTime > PopChams.fadestart.getValue().longValue()) {
               double normal = this.normalize((double)time, PopChams.fadetime.getValue().doubleValue());
               normal = MathHelper.clamp(normal, 0.0, 1.0);
               normal = -normal + 1.0;
               lineA *= (int)normal;
               fillA *= (int)normal;
            }

            Color lineColor = newAlpha(lineColorS, lineA);
            Color fillColor = newAlpha(fillColorS, fillA);
            if (this.playerModel != null) {
               PopChams.prepareGL();
               GL11.glPushAttrib(1048575);
               GL11.glEnable(2881);
               GL11.glEnable(2848);
               if (this.alphaFill > 1.0) {
                  this.alphaFill -= (double)PopChams.fadetime.getValue().floatValue();
               }

               Color fillFinal = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), (int)this.alphaFill);
               if (this.alphaLine > 1.0) {
                  this.alphaLine -= (double)PopChams.fadetime.getValue().floatValue();
               }

               Color outlineFinal = new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), (int)this.alphaLine);
               glColor(fillFinal);
               GL11.glPolygonMode(1032, 6914);
               renderEntity(this.player, this.playerModel, this.player.limbSwing, this.player.limbSwingAmount, 1.0F);
               glColor(outlineFinal);
               GL11.glPolygonMode(1032, 6913);
               renderEntity(this.player, this.playerModel, this.player.limbSwing, this.player.limbSwingAmount, 1.0F);
               GL11.glPolygonMode(1032, 6914);
               GL11.glPopAttrib();
               PopChams.releaseGL();
            }
         }
      }

      double normalize(double value, double max) {
         return (value - 0.0) / (max - 0.0);
      }
   }
}
