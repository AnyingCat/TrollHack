//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.movement;

import java.util.HashMap;
import java.util.Map;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FastFall extends Module {
   private final Setting<FastFall.Mode> mode = this.add(new Setting<>("Mode", FastFall.Mode.FAST));
   private final Setting<Boolean> noLag = this.add(new Setting<>("NoLag", true, v -> this.mode.getValue() == FastFall.Mode.FAST));
   private final Setting<Integer> height = this.add(new Setting<>("Height", 10, 1, 20));
   private final Timer lagTimer = new Timer();
   private boolean useTimer;

   public FastFall() {
      super("FastFall", "Miyagi son simulator", Category.MOVEMENT);
   }

   @Override
   public void onDisable() {
      Managers.TIMER.reset();
      this.useTimer = false;
   }

   @Override
   public String getInfo() {
      return Managers.TEXT.normalizeCases(this.mode.getValue());
   }

   @Override
   public void onTick() {
      if ((this.height.getValue() <= 0 || this.traceDown() <= this.height.getValue())
         && !mc.player.isEntityInsideOpaqueBlock()
         && !mc.player.isInWater()
         && !mc.player.isInLava()
         && !mc.player.isOnLadder()
         && this.lagTimer.passedMs(1000L)
         && !fullNullCheck()) {
         if (!mc.player.isInWeb) {
            if (mc.player.onGround && this.mode.getValue() == FastFall.Mode.FAST) {
               mc.player.motionY -= (double)(this.noLag.getValue() ? 0.62F : 1.0F);
            }

            if (this.traceDown() != 0 && this.traceDown() <= this.height.getValue() && this.trace() && mc.player.onGround) {
               mc.player.motionX *= 0.05F;
               mc.player.motionZ *= 0.05F;
            }

            if (this.mode.getValue() == FastFall.Mode.STRICT) {
               if (!mc.player.onGround) {
                  if (mc.player.motionY < 0.0 && this.useTimer) {
                     Managers.TIMER.set(2.5F);
                     return;
                  }

                  this.useTimer = false;
               } else {
                  mc.player.motionY = -0.08;
                  this.useTimer = true;
               }
            }

            Managers.TIMER.reset();
         }
      } else {
         if (!NewStep.timer) {
            Managers.TIMER.reset();
         }
      }
   }

   @SubscribeEvent
   public void onPacket(PacketEvent event) {
      if (!fullNullCheck() && event.getPacket() instanceof SPacketPlayerPosLook) {
         this.lagTimer.reset();
      }
   }

   private int traceDown() {
      int retval = 0;
      int y = (int)Math.round(mc.player.posY) - 1;

      for(int tracey = y; tracey >= 0; --tracey) {
         RayTraceResult trace = mc.world
            .rayTraceBlocks(mc.player.getPositionVector(), new Vec3d(mc.player.posX, (double)tracey, mc.player.posZ), false);
         if (trace != null && trace.typeOfHit == Type.BLOCK) {
            return retval;
         }

         ++retval;
      }

      return retval;
   }

   private boolean trace() {
      AxisAlignedBB bbox = mc.player.getEntityBoundingBox();
      Vec3d basepos = bbox.getCenter();
      double minX = bbox.minX;
      double minZ = bbox.minZ;
      double maxX = bbox.maxX;
      double maxZ = bbox.maxZ;
      Map<Vec3d, Vec3d> positions = new HashMap();
      positions.put(basepos, new Vec3d(basepos.x, basepos.y - 1.0, basepos.z));
      positions.put(new Vec3d(minX, basepos.y, minZ), new Vec3d(minX, basepos.y - 1.0, minZ));
      positions.put(new Vec3d(maxX, basepos.y, minZ), new Vec3d(maxX, basepos.y - 1.0, minZ));
      positions.put(new Vec3d(minX, basepos.y, maxZ), new Vec3d(minX, basepos.y - 1.0, maxZ));
      positions.put(new Vec3d(maxX, basepos.y, maxZ), new Vec3d(maxX, basepos.y - 1.0, maxZ));

      for(Vec3d key : positions.keySet()) {
         RayTraceResult result = mc.world.rayTraceBlocks(key, (Vec3d)positions.get(key), true);
         if (result != null && result.typeOfHit == Type.BLOCK) {
            return false;
         }
      }

      IBlockState state = mc.world
         .getBlockState(new BlockPos(mc.player.posX, mc.player.posY - 1.0, mc.player.posZ));
      return state.getBlock() == Blocks.AIR;
   }

   private static enum Mode {
      FAST,
      STRICT;
   }
}
