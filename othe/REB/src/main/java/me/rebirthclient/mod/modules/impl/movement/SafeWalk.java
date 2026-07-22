//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.api.events.impl.MoveEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SafeWalk extends Module {
   public SafeWalk() {
      super("SafeWalk", "stop at the edge", Category.MOVEMENT);
   }

   @SubscribeEvent
   public void onMove(MoveEvent event) {
      double x = event.getX();
      double y = event.getY();
      double z = event.getZ();
      if (mc.player.onGround) {
         double increment = 0.05;

         while(x != 0.0 && this.isOffsetBBEmpty(x, -1.0, 0.0)) {
            if (x < increment && x >= -increment) {
               x = 0.0;
            } else if (x > 0.0) {
               x -= increment;
            } else {
               x += increment;
            }
         }

         while(z != 0.0 && this.isOffsetBBEmpty(0.0, -1.0, z)) {
            if (z < increment && z >= -increment) {
               z = 0.0;
            } else if (z > 0.0) {
               z -= increment;
            } else {
               z += increment;
            }
         }

         while(x != 0.0 && z != 0.0 && this.isOffsetBBEmpty(x, -1.0, z)) {
            x = x < increment && x >= -increment ? 0.0 : (x > 0.0 ? x - increment : x + increment);
            if (z < increment && z >= -increment) {
               z = 0.0;
            } else if (z > 0.0) {
               z -= increment;
            } else {
               z += increment;
            }
         }
      }

      event.setX(x);
      event.setY(y);
      event.setZ(z);
   }

   public boolean isOffsetBBEmpty(double offsetX, double offsetY, double offsetZ) {
      EntityPlayerSP playerSP = mc.player;
      return mc.world.getCollisionBoxes(playerSP, playerSP.getEntityBoundingBox().offset(offsetX, offsetY, offsetZ)).isEmpty();
   }
}
