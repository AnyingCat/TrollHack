//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.item.ItemLingeringPotion;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemSplashPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;

public class Trajectories extends Module {
   public Trajectories() {
      super("Trajectories", "Draws trajectories", Category.RENDER);
   }

   @Override
   public void onRender3D(Render3DEvent event) {
      if (mc.player != null && mc.world != null && mc.gameSettings.thirdPersonView != 2) {
         if (mc.player.getHeldItemMainhand() != ItemStack.EMPTY && mc.player.getHeldItemMainhand().getItem() instanceof ItemBow
            || mc.player.getHeldItemMainhand() != ItemStack.EMPTY && this.isThrowable(mc.player.getHeldItemMainhand().getItem())
            || mc.player.getHeldItemOffhand() != ItemStack.EMPTY && this.isThrowable(mc.player.getHeldItemOffhand().getItem())
            || Mouse.isButtonDown(2)) {
            double renderPosX = mc.getRenderManager().renderPosX;
            double renderPosY = mc.getRenderManager().renderPosY;
            double renderPosZ = mc.getRenderManager().renderPosZ;
            Item item = null;
            if (mc.player.getHeldItemMainhand() == ItemStack.EMPTY
               || !(mc.player.getHeldItemMainhand().getItem() instanceof ItemBow)
                  && !this.isThrowable(mc.player.getHeldItemMainhand().getItem())) {
               if (mc.player.getHeldItemOffhand() != ItemStack.EMPTY && this.isThrowable(mc.player.getHeldItemOffhand().getItem())) {
                  item = mc.player.getHeldItemOffhand().getItem();
               }
            } else {
               item = mc.player.getHeldItemMainhand().getItem();
            }

            if (item == null && Mouse.isButtonDown(2)) {
               item = Items.ENDER_PEARL;
            } else if (item == null) {
               return;
            }

            GL11.glPushAttrib(1048575);
            GL11.glPushMatrix();
            GL11.glDisable(3008);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glDisable(3553);
            GL11.glDisable(2929);
            GL11.glDepthMask(false);
            GL11.glEnable(2884);
            GL11.glEnable(2848);
            GL11.glHint(3154, 4353);
            GL11.glDisable(2896);
            double posX = renderPosX - (double)(MathHelper.cos(mc.player.rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
            double posY = renderPosY + (double)mc.player.getEyeHeight() - 0.1000000014901161;
            double posZ = renderPosZ - (double)(MathHelper.sin(mc.player.rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
            float maxDist = this.getDistance(item);
            double motionX = (double)(
               -MathHelper.sin(mc.player.rotationYaw / 180.0F * (float) Math.PI)
                  * MathHelper.cos(mc.player.rotationPitch / 180.0F * (float) Math.PI)
                  * maxDist
            );
            double motionY = (double)(-MathHelper.sin((mc.player.rotationPitch - (float)this.getPitch(item)) / 180.0F * 3.141593F) * maxDist);
            double motionZ = (double)(
               MathHelper.cos(mc.player.rotationYaw / 180.0F * (float) Math.PI)
                  * MathHelper.cos(mc.player.rotationPitch / 180.0F * (float) Math.PI)
                  * maxDist
            );
            int var6 = 72000 - mc.player.getItemInUseCount();
            float power = (float)var6 / 20.0F;
            power = (power * power + power * 2.0F) / 3.0F;
            if (power > 1.0F) {
               power = 1.0F;
            }

            float distance = MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
            motionX /= (double)distance;
            motionY /= (double)distance;
            motionZ /= (double)distance;
            float pow = (item instanceof ItemBow ? power * 2.0F : 1.0F) * this.getVelocity(item);
            motionX *= (double)pow;
            motionY *= (double)pow;
            motionZ *= (double)pow;
            if (!mc.player.onGround) {
               motionY += mc.player.motionY;
            }

            RenderUtil.glColor(Managers.COLORS.getCurrent());
            GL11.glEnable(2848);
            float size = (float)(item instanceof ItemBow ? 0.3 : 0.25);
            boolean hasLanded = false;
            Entity landingOnEntity = null;
            RayTraceResult landingPosition = null;
            GL11.glBegin(3);

            while(!hasLanded && posY > 0.0) {
               Vec3d present = new Vec3d(posX, posY, posZ);
               Vec3d future = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);
               RayTraceResult possibleLandingStrip = mc.world.rayTraceBlocks(present, future, false, true, false);
               if (possibleLandingStrip != null && possibleLandingStrip.typeOfHit != Type.MISS) {
                  landingPosition = possibleLandingStrip;
                  hasLanded = true;
               }

               AxisAlignedBB arrowBox = new AxisAlignedBB(
                  posX - (double)size, posY - (double)size, posZ - (double)size, posX + (double)size, posY + (double)size, posZ + (double)size
               );

               for(Entity entity : this.getEntitiesWithinAABB(arrowBox.offset(motionX, motionY, motionZ).expand(1.0, 1.0, 1.0))) {
                  if (entity.canBeCollidedWith() && entity != mc.player) {
                     float var7 = 0.3F;
                     AxisAlignedBB var8 = entity.getEntityBoundingBox().expand((double)var7, (double)var7, (double)var7);
                     RayTraceResult possibleEntityLanding = var8.calculateIntercept(present, future);
                     if (possibleEntityLanding != null) {
                        hasLanded = true;
                        landingOnEntity = entity;
                        landingPosition = possibleEntityLanding;
                     }
                  }
               }

               posX += motionX;
               posY += motionY;
               posZ += motionZ;
               float motionAdjustment = 0.99F;
               motionX *= 0.99F;
               motionY *= 0.99F;
               motionZ *= 0.99F;
               motionY -= (double)this.getGravity(item);
               this.drawTracer(posX - renderPosX, posY - renderPosY, posZ - renderPosZ);
            }

            GL11.glEnd();
            if (landingPosition != null && landingPosition.typeOfHit == Type.BLOCK) {
               GlStateManager.translate(posX - renderPosX, posY - renderPosY, posZ - renderPosZ);
               int side = landingPosition.sideHit.getIndex();
               if (side == 2) {
                  GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
               } else if (side == 3) {
                  GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
               } else if (side == 4) {
                  GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
               } else if (side == 5) {
                  GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
               }

               Cylinder c = new Cylinder();
               GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
               c.setDrawStyle(100011);
               c.draw(0.5F, 0.2F, 0.0F, 4, 1);
               c.setDrawStyle(100012);
               RenderUtil.glColor(ColorUtil.injectAlpha(Managers.COLORS.getCurrent(), 100));
               c.draw(0.5F, 0.2F, 0.0F, 4, 1);
            }

            GL11.glEnable(2896);
            GL11.glDisable(2848);
            GL11.glEnable(3553);
            GL11.glEnable(2929);
            GL11.glDisable(3042);
            GL11.glEnable(3008);
            GL11.glDepthMask(true);
            GL11.glCullFace(1029);
            GL11.glPopMatrix();
            GL11.glPopAttrib();
            if (landingOnEntity != null) {
               RenderUtil.drawEntityBoxESP(landingOnEntity, Managers.COLORS.getCurrent(), false, new Color(-1), 1.0F, false, true, 100);
            }
         }
      }
   }

   public void drawTracer(double x, double y, double z) {
      GL11.glVertex3d(x, y, z);
   }

   private boolean isThrowable(Item item) {
      return item instanceof ItemEnderPearl
         || item instanceof ItemExpBottle
         || item instanceof ItemSnowball
         || item instanceof ItemEgg
         || item instanceof ItemSplashPotion
         || item instanceof ItemLingeringPotion;
   }

   private float getDistance(Item item) {
      return item instanceof ItemBow ? 1.0F : 0.4F;
   }

   private float getVelocity(Item item) {
      if (item instanceof ItemSplashPotion || item instanceof ItemLingeringPotion) {
         return 0.5F;
      } else {
         return item instanceof ItemExpBottle ? 0.59F : 1.5F;
      }
   }

   private int getPitch(Item item) {
      return !(item instanceof ItemSplashPotion) && !(item instanceof ItemLingeringPotion) && !(item instanceof ItemExpBottle) ? 0 : 20;
   }

   private float getGravity(Item item) {
      return !(item instanceof ItemBow) && !(item instanceof ItemSplashPotion) && !(item instanceof ItemLingeringPotion) && !(item instanceof ItemExpBottle)
         ? 0.03F
         : 0.05F;
   }

   private List<Entity> getEntitiesWithinAABB(AxisAlignedBB bb) {
      ArrayList<Entity> list = new ArrayList();
      int chunkMinX = MathHelper.floor((bb.minX - 2.0) / 16.0);
      int chunkMaxX = MathHelper.floor((bb.maxX + 2.0) / 16.0);
      int chunkMinZ = MathHelper.floor((bb.minZ - 2.0) / 16.0);
      int chunkMaxZ = MathHelper.floor((bb.maxZ + 2.0) / 16.0);

      for(int x = chunkMinX; x <= chunkMaxX; ++x) {
         for(int z = chunkMinZ; z <= chunkMaxZ; ++z) {
            if (mc.world.getChunkProvider().getLoadedChunk(x, z) != null) {
               mc.world.getChunk(x, z).getEntitiesWithinAABBForEntity(mc.player, bb, list, EntitySelectors.NOT_SPECTATING);
            }
         }
      }

      return list;
   }
}
