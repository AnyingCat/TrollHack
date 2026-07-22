//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PredictESP extends Module {
   private final Setting<Integer> predictTicks = this.add(new Setting<>("PredictTicks", 4, 0, 10));
   private final Setting<Boolean> collision = this.add(new Setting<>("Collision", false));
   public final Setting<Color> color = this.add(new Setting<>("Color", new Color(255, 255, 255, 150)));

   public PredictESP() {
      super("PredictESP", "show predict", Category.RENDER);
   }

   @Override
   public void onRender3D(Render3DEvent event) {
      for(EntityPlayer player : mc.world.playerEntities) {
         RenderUtil.drawBBFill(
            player.getEntityBoundingBox().offset(this.getMotionVec(player, this.predictTicks.getValue())),
            this.color.getValue(),
            this.color.getValue().getAlpha()
         );
      }
   }

   private Vec3d getMotionVec(Entity entity, int ticks) {
      double dX = entity.posX - entity.prevPosX;
      double dZ = entity.posZ - entity.prevPosZ;
      double entityMotionPosX = 0.0;
      double entityMotionPosZ = 0.0;
      if (this.collision.getValue()) {
         for(int i = 1;
            i <= ticks
               && mc.world
                     .getBlockState(new BlockPos(entity.posX + dX * (double)i, entity.posY, entity.posZ + dZ * (double)i))
                     .getBlock()
                  == Blocks.AIR
               && mc.world
                     .getBlockState(
                        new BlockPos(
                           entity.posX + dX * (double)i, entity.posY + (double)entity.getEyeHeight(), entity.posZ + dZ * (double)i
                        )
                     )
                     .getBlock()
                  == Blocks.AIR;
            ++i
         ) {
            entityMotionPosX = dX * (double)i;
            entityMotionPosZ = dZ * (double)i;
         }
      } else {
         entityMotionPosX = dX * (double)ticks;
         entityMotionPosZ = dZ * (double)ticks;
      }

      return new Vec3d(entityMotionPosX, 0.0, entityMotionPosZ);
   }
}
