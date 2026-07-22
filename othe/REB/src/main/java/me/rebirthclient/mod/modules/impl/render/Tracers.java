//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.render;

import com.google.common.collect.Maps;
import java.awt.Color;
import java.util.Map;
import me.rebirthclient.api.events.impl.Render2DEvent;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public class Tracers extends Module {
   private final Setting<Tracers.Mode> mode = this.add(new Setting<>("Mode", Tracers.Mode.ARROW));
   private final Setting<Integer> range = this.add(new Setting<>("Range", 100, 10, 200));
   private final Setting<Color> color = this.add(new Setting<>("Color", new Color(11935519)));
   private final Setting<Float> lineWidth = this.add(new Setting<>("LineWidth", 1.0F, 0.5F, 2.0F, v -> this.mode.getValue() == Tracers.Mode.TRACER));
   private final Setting<Integer> radius = this.add(new Setting<>("Radius", 80, 10, 200, v -> this.mode.getValue() == Tracers.Mode.ARROW));
   private final Setting<Float> size = this.add(new Setting<>("Size", 7.5F, 5.0F, 25.0F, v -> this.mode.getValue() == Tracers.Mode.ARROW));
   private final Setting<Boolean> outline = this.add(new Setting<>("Outline", true, v -> this.mode.getValue() == Tracers.Mode.ARROW));
   private final Tracers.EntityListener entityListener = new Tracers.EntityListener();
   private final Frustum frustum = new Frustum();

   public Tracers() {
      super("Tracers", "Points to the players on your screen", Category.RENDER);
   }

   @Override
   public String getInfo() {
      return String.valueOf(this.mode.getValue());
   }

   @Override
   public void onRender2D(Render2DEvent event) {
      if (this.mode.getValue() == Tracers.Mode.ARROW) {
         this.entityListener.render();
         mc.world
            .loadedEntityList
            .forEach(
               o -> {
                  if (o instanceof EntityPlayer && this.isValid((EntityPlayer)o)) {
                     EntityPlayer entity = (EntityPlayer)o;
                     Vec3d pos = (Vec3d)this.entityListener.getEntityLowerBounds().get(entity);
                     if (pos != null && !this.isOnScreen(pos) && !this.isInViewFrustum(entity)) {
                        int alpha = (int)MathHelper.clamp(
                           255.0F - 255.0F / (float)this.range.getValue().intValue() * mc.player.getDistance(entity), 100.0F, 255.0F
                        );
                        Color friendColor = new Color(0, 191, 255);
                        Color color = Managers.FRIENDS.isFriend(entity.getName())
                           ? ColorUtil.injectAlpha(friendColor, alpha)
                           : ColorUtil.injectAlpha(this.color.getValue(), alpha);
                        int x = Display.getWidth() / 2 / (mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale);
                        int y = Display.getHeight() / 2 / (mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale);
                        float yaw = this.getRotations(entity) - mc.player.rotationYaw;
                        GL11.glTranslatef((float)x, (float)y, 0.0F);
                        GL11.glRotatef(yaw, 0.0F, 0.0F, 1.0F);
                        GL11.glTranslatef((float)(-x), (float)(-y), 0.0F);
                        RenderUtil.drawArrowPointer(
                           (float)x, (float)(y - this.radius.getValue()), this.size.getValue(), 2.0F, 1.0F, this.outline.getValue(), 1.1F, color.getRGB()
                        );
                        GL11.glTranslatef((float)x, (float)y, 0.0F);
                        GL11.glRotatef(-yaw, 0.0F, 0.0F, 1.0F);
                        GL11.glTranslatef((float)(-x), (float)(-y), 0.0F);
                     }
                  }
               }
            );
      }
   }

   @Override
   public void onRender3D(Render3DEvent event) {
      if (mc.getRenderViewEntity() != null) {
         if (this.mode.getValue() == Tracers.Mode.TRACER) {
            GL11.glBlendFunc(770, 771);
            GL11.glEnable(3042);
            GL11.glEnable(2848);
            GL11.glHint(3154, 4354);
            GL11.glLineWidth(this.lineWidth.getValue());
            GL11.glDisable(3553);
            GL11.glDisable(2929);
            GL11.glDepthMask(false);
            GL11.glBegin(1);

            for(EntityPlayer entity : mc.world.playerEntities) {
               if (entity != mc.player) {
                  this.drawTraces(entity);
               }
            }

            GL11.glEnd();
            GL11.glEnable(3553);
            GL11.glDisable(2848);
            GL11.glEnable(2929);
            GL11.glDepthMask(true);
            GL11.glDisable(3042);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         }
      }
   }

   private boolean isOnScreen(Vec3d pos) {
      if (!(pos.x > -1.0)) {
         return false;
      } else if (!(pos.y < 1.0)) {
         return false;
      } else if (!(pos.z < 1.0)) {
         return false;
      } else {
         int n = mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale;
         if (!(pos.x / (double)n >= 0.0)) {
            return false;
         } else {
            int n2 = mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale;
            if (!(pos.x / (double)n2 <= (double)Display.getWidth())) {
               return false;
            } else {
               int n3 = mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale;
               if (!(pos.y / (double)n3 >= 0.0)) {
                  return false;
               } else {
                  int n4 = mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale;
                  return pos.y / (double)n4 <= (double)Display.getHeight();
               }
            }
         }
      }
   }

   private boolean isInViewFrustum(Entity entity) {
      Entity current = mc.getRenderViewEntity();
      this.frustum.setPosition(current.posX, current.posY, current.posZ);
      return this.frustum.isBoundingBoxInFrustum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck;
   }

   private boolean isValid(EntityPlayer entity) {
      return entity != mc.player && entity.isEntityAlive();
   }

   private float getRotations(EntityLivingBase ent) {
      double x = ent.posX - mc.player.posX;
      double z = ent.posZ - mc.player.posZ;
      return (float)(-(Math.atan2(x, z) * (180.0 / Math.PI)));
   }

   private void drawTraces(Entity entity) {
      if (mc.getRenderViewEntity() != null) {
         double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)mc.getRenderPartialTicks() - mc.getRenderManager().viewerPosX;
         double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)mc.getRenderPartialTicks() - mc.getRenderManager().viewerPosY;
         double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)mc.getRenderPartialTicks() - mc.getRenderManager().viewerPosZ;
         Vec3d eyes = new Vec3d(0.0, 0.0, 1.0)
            .rotatePitch(-((float)Math.toRadians((double)mc.getRenderViewEntity().rotationPitch)))
            .rotateYaw(-((float)Math.toRadians((double)mc.getRenderViewEntity().rotationYaw)));
         boolean isFriend = Managers.FRIENDS.isFriend(entity.getName());
         GL11.glColor4f(
            (isFriend ? 0.0F : (float)this.color.getValue().getRed()) / 255.0F,
            (isFriend ? 191.0F : (float)this.color.getValue().getGreen()) / 255.0F,
            (isFriend ? 255.0F : (float)this.color.getValue().getBlue()) / 255.0F,
            MathHelper.clamp(255.0F - 255.0F / (float)this.range.getValue().intValue() * mc.player.getDistance(entity), 100.0F, 255.0F)
         );
         GL11.glVertex3d(eyes.x, eyes.y + (double)mc.getRenderViewEntity().getEyeHeight(), eyes.z);
         GL11.glVertex3d(x, y, z);
      }
   }

   private static class EntityListener {
      private final Map<Entity, Vec3d> entityUpperBounds = Maps.newHashMap();
      private final Map<Entity, Vec3d> entityLowerBounds = Maps.newHashMap();

      private EntityListener() {
      }

      private void render() {
         if (!this.entityUpperBounds.isEmpty()) {
            this.entityUpperBounds.clear();
         }

         if (!this.entityLowerBounds.isEmpty()) {
            this.entityLowerBounds.clear();
         }

         for(Entity e : Wrapper.mc.world.loadedEntityList) {
            Vec3d bound = this.getEntityRenderPosition(e);
            bound.add(new Vec3d(0.0, (double)e.height + 0.2, 0.0));
            Vec3d upperBounds = RenderUtil.get2DPos(bound.x, bound.y, bound.z);
            Vec3d lowerBounds = RenderUtil.get2DPos(bound.x, bound.y - 2.0, bound.z);
            if (upperBounds != null && lowerBounds != null) {
               this.entityUpperBounds.put(e, upperBounds);
               this.entityLowerBounds.put(e, lowerBounds);
            }
         }
      }

      private Vec3d getEntityRenderPosition(Entity entity) {
         double partial = (double)Wrapper.mc.timer.renderPartialTicks;
         double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partial - Wrapper.mc.getRenderManager().viewerPosX;
         double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partial - Wrapper.mc.getRenderManager().viewerPosY;
         double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partial - Wrapper.mc.getRenderManager().viewerPosZ;
         return new Vec3d(x, y, z);
      }

      public Map<Entity, Vec3d> getEntityLowerBounds() {
         return this.entityLowerBounds;
      }
   }

   private static enum Mode {
      TRACER,
      ARROW;
   }
}
