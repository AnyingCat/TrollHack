//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class BreadCrumbs extends Module {
   protected final Map trails = new HashMap();
   private final Setting<Float> lineWidth = this.add(new Setting<>("Width", 0.8F, 0.1F, 3.0F));
   private final Setting<Integer> timeExisted = this.add(new Setting<>("Delay", 1000, 100, 3000));
   private final Setting<Boolean> xp = this.add(new Setting<>("Exp", true));
   private final Setting<Boolean> arrow = this.add(new Setting<>("Arrows", true));
   private final Setting<Boolean> self = this.add(new Setting<>("Self", true));
   private final Setting<Color> color = this.add(new Setting<>("Color", new Color(125, 125, 213)).hideAlpha());
   private final Setting<Color> secondColor = this.add(new Setting<>("SecondColor", new Color(12550399)).injectBoolean(false).hideAlpha());

   public BreadCrumbs() {
      super("BreadCrumbs", "Draws trails behind projectiles and you (bread crumbs)", Category.RENDER);
   }

   public static Vec3d updateToCamera(Vec3d vec) {
      return new Vec3d(
         vec.x - mc.getRenderManager().viewerPosX,
         vec.y - mc.getRenderManager().viewerPosY,
         vec.z - mc.getRenderManager().viewerPosZ
      );
   }

   public static void addBuilderVertex(BufferBuilder bufferBuilder, double x, double y, double z, Color color) {
      bufferBuilder.pos(x, y, z)
         .color((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)color.getAlpha() / 255.0F)
         .endVertex();
   }

   @Override
   public void onTick() {
      for(Entity entity : mc.world.loadedEntityList) {
         if (this.isValid(entity)) {
            if (this.trails.containsKey(entity.getUniqueID())) {
               if (entity.isDead) {
                  if (((BreadCrumbs.ItemTrail)this.trails.get(entity.getUniqueID())).timer.isPaused()) {
                     ((BreadCrumbs.ItemTrail)this.trails.get(entity.getUniqueID())).timer.resetDelay();
                  }

                  ((BreadCrumbs.ItemTrail)this.trails.get(entity.getUniqueID())).timer.setPaused(false);
               } else {
                  ((BreadCrumbs.ItemTrail)this.trails.get(entity.getUniqueID())).positions.add(new BreadCrumbs.Position(entity.getPositionVector()));
               }
            } else {
               this.trails.put(entity.getUniqueID(), new BreadCrumbs.ItemTrail(entity));
            }
         }
      }

      if (this.self.getValue()) {
         if (this.trails.containsKey(mc.player.getUniqueID())) {
            BreadCrumbs.ItemTrail playerTrail = (BreadCrumbs.ItemTrail)this.trails.get(mc.player.getUniqueID());
            playerTrail.timer.resetDelay();
            List toRemove = new ArrayList();

            for(Object o : playerTrail.positions) {
               BreadCrumbs.Position position = (BreadCrumbs.Position)o;
               if (System.currentTimeMillis() - position.time > (long)this.timeExisted.getValue().intValue()) {
                  toRemove.add(position);
               }
            }

            playerTrail.positions.removeAll(toRemove);
            playerTrail.positions.add(new BreadCrumbs.Position(mc.player.getPositionVector()));
         } else {
            this.trails.put(mc.player.getUniqueID(), new BreadCrumbs.ItemTrail(mc.player));
         }
      } else {
         this.trails.remove(mc.player.getUniqueID());
      }
   }

   @SubscribeEvent
   public void onRenderWorld(RenderWorldLastEvent event) {
      if (!nullCheck()) {
         for(Object o : this.trails.entrySet()) {
            Entry entry = (Entry)o;
            if (((BreadCrumbs.ItemTrail)entry.getValue()).entity.isDead
               || mc.world.getEntityByID(((BreadCrumbs.ItemTrail)entry.getValue()).entity.getEntityId()) == null) {
               if (((BreadCrumbs.ItemTrail)entry.getValue()).timer.isPaused()) {
                  ((BreadCrumbs.ItemTrail)entry.getValue()).timer.resetDelay();
               }

               ((BreadCrumbs.ItemTrail)entry.getValue()).timer.setPaused(false);
            }

            if (!((BreadCrumbs.ItemTrail)entry.getValue()).timer.isPassed()) {
               this.drawTrail((BreadCrumbs.ItemTrail)entry.getValue());
            }
         }
      }
   }

   public void drawTrail(BreadCrumbs.ItemTrail trail) {
      double fadeAmount = MathUtil.normalize(
         (double)(System.currentTimeMillis() - trail.timer.getStartTime()), 0.0, (double)this.timeExisted.getValue().intValue()
      );
      int alpha = (int)(fadeAmount * 255.0);
      alpha = MathHelper.clamp(alpha, 0, 255);
      alpha = 255 - alpha;
      alpha = trail.timer.isPaused() ? 255 : alpha;
      Color fadeColor = new Color(this.secondColor.getValue().getRed(), this.secondColor.getValue().getGreen(), this.secondColor.getValue().getBlue(), alpha);
      GlStateManager.pushMatrix();
      GlStateManager.disableDepth();
      GlStateManager.disableLighting();
      GlStateManager.depthMask(false);
      GlStateManager.disableAlpha();
      GlStateManager.disableCull();
      GlStateManager.enableBlend();
      GL11.glDisable(3553);
      GL11.glEnable(2848);
      GL11.glBlendFunc(770, 771);
      GL11.glLineWidth(this.lineWidth.getValue().floatValue());
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder builder = tessellator.getBuffer();
      builder.begin(3, DefaultVertexFormats.POSITION_COLOR);
      this.buildBuffer(
         builder, trail, new Color(this.color.getValue().getRGB()), this.secondColor.booleanValue ? fadeColor : new Color(this.color.getValue().getRGB())
      );
      tessellator.draw();
      GlStateManager.depthMask(true);
      GlStateManager.enableLighting();
      GlStateManager.enableDepth();
      GlStateManager.enableAlpha();
      GlStateManager.popMatrix();
      GL11.glEnable(3553);
      GL11.glPolygonMode(1032, 6914);
   }

   public void buildBuffer(BufferBuilder builder, BreadCrumbs.ItemTrail trail, Color start, Color end) {
      for(Object o : trail.positions) {
         BreadCrumbs.Position p = (BreadCrumbs.Position)o;
         Vec3d pos = updateToCamera(p.pos);
         double value = MathUtil.normalize((double)trail.positions.indexOf(p), 0.0, (double)trail.positions.size());
         addBuilderVertex(builder, pos.x, pos.y, pos.z, ColorUtil.interpolate((float)value, start, end));
      }
   }

   boolean isValid(Entity e) {
      return e instanceof EntityEnderPearl
         || e instanceof EntityExpBottle && this.xp.getValue()
         || e instanceof EntityArrow && this.arrow.getValue() && e.ticksExisted <= this.timeExisted.getValue();
   }

   public class ItemTrail {
      public final Entity entity;
      public final List positions;
      public final BreadCrumbs.Timer timer;

      public ItemTrail(Entity entity) {
         this.entity = entity;
         this.positions = new ArrayList();
         this.timer = new BreadCrumbs.Timer();
         this.timer.setDelay((long)BreadCrumbs.this.timeExisted.getValue().intValue());
         this.timer.setPaused(true);
      }
   }

   public static class Position {
      public final Vec3d pos;
      public final long time;

      public Position(Vec3d pos) {
         this.pos = pos;
         this.time = System.currentTimeMillis();
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            BreadCrumbs.Position position = (BreadCrumbs.Position)o;
            return this.time == position.time && Objects.equals(this.pos, position.pos);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.pos, this.time);
      }
   }

   public static class Timer {
      long startTime = System.currentTimeMillis();
      long delay;
      boolean paused;

      public boolean isPassed() {
         return !this.paused && System.currentTimeMillis() - this.startTime >= this.delay;
      }

      public long getTime() {
         return System.currentTimeMillis() - this.startTime;
      }

      public void resetDelay() {
         this.startTime = System.currentTimeMillis();
      }

      public void setDelay(long delay) {
         this.delay = delay;
      }

      public boolean isPaused() {
         return this.paused;
      }

      public void setPaused(boolean paused) {
         this.paused = paused;
      }

      public long getStartTime() {
         return this.startTime;
      }
   }
}
