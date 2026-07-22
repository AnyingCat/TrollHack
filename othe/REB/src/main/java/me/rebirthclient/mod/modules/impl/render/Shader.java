//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import me.rebirthclient.api.events.impl.Render2DEvent;
import me.rebirthclient.api.events.impl.RenderItemInFirstPersonEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.InterpolationUtil;
import me.rebirthclient.api.util.render.shader.framebuffer.impl.ItemShader;
import me.rebirthclient.asm.accessors.IEntityRenderer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.Display;

public class Shader extends Module {
   public static Shader INSTANCE;
   private final Setting<Boolean> players = this.add(new Setting<>("Players", false));
   private final Setting<Boolean> crystals = this.add(new Setting<>("Crystals", false));
   private final Setting<Boolean> xp = this.add(new Setting<>("Exp", false));
   private final Setting<Boolean> items = this.add(new Setting<>("Items", false));
   private final Setting<Boolean> self = this.add(new Setting<>("Self", true));
   private final Setting<Color> color = this.add(new Setting<>("Color", new Color(-8553003, true)));
   private final Setting<Boolean> glow = this.add(new Setting<>("Glow", true).setParent());
   private final Setting<Float> radius = this.add(new Setting<>("Radius", 4.0F, 0.1F, 6.0F, v -> this.glow.isOpen()));
   private final Setting<Float> smoothness = this.add(new Setting<>("Smoothness", 1.0F, 0.1F, 1.0F, v -> this.glow.isOpen()));
   private final Setting<Integer> alpha = this.add(new Setting<>("Alpha", 50, 1, 50, v -> this.glow.isOpen()));
   private final Setting<Boolean> model = this.add(new Setting<>("Model", true));
   private final Setting<Integer> range = this.add(new Setting<>("Range", 75, 5, 250));
   private final Setting<Boolean> fovOnly = this.add(new Setting<>("FOVOnly", false));
   private boolean forceRender;
   public static boolean crystalRender;

   public Shader() {
      super("Shader", "Is in beta test stage", Category.RENDER);
      INSTANCE = this;
   }

   @SubscribeEvent
   public void renderItemInFirstPerson(RenderItemInFirstPersonEvent event) {
      if (!fullNullCheck() && this.isOn() && event.getStage() == 0 && !this.forceRender && this.self.getValue()) {
         event.setCanceled(true);
      }
   }

   @Override
   public void onRender2D(Render2DEvent event) {
      if ((Display.isActive() || Display.isVisible()) && !(mc.currentScreen instanceof GuiDownloadTerrain)) {
         GlStateManager.pushMatrix();
         GlStateManager.pushAttrib();
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
         GlStateManager.enableDepth();
         GlStateManager.depthMask(true);
         GlStateManager.enableAlpha();
         ItemShader shader = ItemShader.INSTANCE;
         shader.mix = (float)this.color.getValue().getAlpha() / 255.0F;
         shader.alpha = (float)(205 + this.alpha.getValue()) / 255.0F;
         shader.model = this.model.getValue();
         shader.startDraw(mc.getRenderPartialTicks());
         this.forceRender = true;
         if (this.self.getValue()) {
            ((IEntityRenderer)mc.entityRenderer).invokeRenderHand(mc.getRenderPartialTicks(), 2);
         }

         this.forceRender = false;
         shader.stopDraw(this.color.getValue(), this.glow.getValue() ? this.radius.getValue() : 0.0F, this.smoothness.getValue());
         GlStateManager.disableBlend();
         GlStateManager.disableAlpha();
         GlStateManager.disableDepth();
         GlStateManager.popAttrib();
         GlStateManager.popMatrix();
      }
   }

   @SubscribeEvent
   public void onRenderWorldLastEvent(RenderWorldLastEvent event) {
      if (!fullNullCheck() && this.isOn()) {
         if ((Display.isActive() || Display.isVisible()) && !(mc.currentScreen instanceof GuiDownloadTerrain)) {
            GlStateManager.pushMatrix();
            GlStateManager.pushAttrib();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableAlpha();
            ItemShader shader = ItemShader.INSTANCE;
            shader.mix = (float)this.color.getValue().getAlpha() / 255.0F;
            shader.alpha = (float)(205 + this.alpha.getValue()) / 255.0F;
            shader.model = false;
            shader.startDraw(mc.getRenderPartialTicks());
            crystalRender = true;
            mc.world
               .loadedEntityList
               .stream()
               .filter(
                  entity -> entity != null
                        && (entity != mc.player || entity != mc.getRenderViewEntity())
                        && mc.getRenderManager().getEntityRenderObject(entity) != null
                        && (
                           entity instanceof EntityPlayer && this.players.getValue() && !((EntityPlayer)entity).isSpectator()
                              || entity instanceof EntityEnderCrystal && this.crystals.getValue()
                              || entity instanceof EntityExpBottle && this.xp.getValue()
                              || entity instanceof EntityItem && this.items.getValue()
                        )
               )
               .forEach(
                  entity -> {
                     if (!(entity.getDistance(mc.player) > (float)this.range.getValue().intValue())
                        && (!this.fovOnly.getValue() || Managers.ROTATIONS.isInFov(entity.getPosition()))) {
                        if (!entity.getName().equals(mc.player.getName()) || mc.gameSettings.thirdPersonView != 0) {
                           Vec3d vector = InterpolationUtil.getInterpolatedRenderPos(entity, event.getPartialTicks());
                           if (entity instanceof EntityPlayer) {
                              ((EntityPlayer)entity).hurtTime = 0;
                           }
      
                           Render<Entity> render = mc.getRenderManager().getEntityRenderObject(entity);
                           if (render != null) {
                              try {
                                 render.doRender(
                                    entity, vector.x, vector.y, vector.z, entity.rotationYaw, event.getPartialTicks()
                                 );
                              } catch (Exception var6) {
                              }
                           }
                        }
                     }
                  }
               );
            crystalRender = false;
            shader.stopDraw(this.color.getValue(), this.glow.getValue() ? this.radius.getValue() : 0.0F, this.smoothness.getValue());
            GlStateManager.disableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.disableDepth();
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
         }
      }
   }

   @Override
   public void onLogin() {
      if (this.isOn()) {
         this.disable();
         this.enable();
      }
   }
}
