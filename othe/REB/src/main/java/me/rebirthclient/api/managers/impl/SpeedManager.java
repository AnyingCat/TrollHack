//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.managers.impl;

import java.util.HashMap;
import me.rebirthclient.mod.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

public class SpeedManager extends Mod {
   public static final double LAST_JUMP_INFO_DURATION_DEFAULT = 3.0;
   public static final Minecraft mc = Minecraft.getMinecraft();
   public static boolean didJumpThisTick;
   public static boolean isJumping;
   public final HashMap<EntityPlayer, Double> playerSpeeds = new HashMap<>();
   private final int distancer = 20;
   public double firstJumpSpeed;
   public double lastJumpSpeed;
   public double percentJumpSpeedChanged;
   public double jumpSpeedChanged;
   public boolean didJumpLastTick;
   public long jumpInfoStartTime;
   public boolean wasFirstJump = true;
   public double speedometerCurrentSpeed;

   public static void setDidJumpThisTick(boolean val) {
      didJumpThisTick = val;
   }

   public static void setIsJumping(boolean val) {
      isJumping = val;
   }

   public float lastJumpInfoTimeRemaining() {
      return (float)(Minecraft.getSystemTime() - this.jumpInfoStartTime) / 1000.0F;
   }

   public void updateValues() {
      double distTraveledLastTickX = mc.player.posX - mc.player.prevPosX;
      double distTraveledLastTickZ = mc.player.posZ - mc.player.prevPosZ;
      this.speedometerCurrentSpeed = distTraveledLastTickX * distTraveledLastTickX + distTraveledLastTickZ * distTraveledLastTickZ;
      if (didJumpThisTick && (!mc.player.onGround || isJumping)) {
         if (didJumpThisTick && !this.didJumpLastTick) {
            this.wasFirstJump = this.lastJumpSpeed == 0.0;
            this.percentJumpSpeedChanged = this.speedometerCurrentSpeed != 0.0 ? this.speedometerCurrentSpeed / this.lastJumpSpeed - 1.0 : -1.0;
            this.jumpSpeedChanged = this.speedometerCurrentSpeed - this.lastJumpSpeed;
            this.jumpInfoStartTime = Minecraft.getSystemTime();
            this.lastJumpSpeed = this.speedometerCurrentSpeed;
            this.firstJumpSpeed = this.wasFirstJump ? this.lastJumpSpeed : 0.0;
         }

         this.didJumpLastTick = didJumpThisTick;
      } else {
         this.didJumpLastTick = false;
         this.lastJumpSpeed = 0.0;
      }

      this.updatePlayers();
   }

   public void updatePlayers() {
      for(EntityPlayer player : mc.world.playerEntities) {
         if (mc.player.getDistanceSq(player) < 400.0) {
            double distTraveledLastTickX = player.posX - player.prevPosX;
            double distTraveledLastTickZ = player.posZ - player.prevPosZ;
            double playerSpeed = distTraveledLastTickX * distTraveledLastTickX + distTraveledLastTickZ * distTraveledLastTickZ;
            this.playerSpeeds.put(player, playerSpeed);
         }
      }
   }

   public double getPlayerSpeed(EntityPlayer player) {
      return this.playerSpeeds.get(player) == null ? 0.0 : this.turnIntoKpH(this.playerSpeeds.get(player));
   }

   public double turnIntoKpH(double input) {
      return (double)MathHelper.sqrt(input) * 71.2729367892;
   }

   public double getSpeedKpH() {
      double speedometerkphdouble = this.turnIntoKpH(this.speedometerCurrentSpeed);
      return (double)Math.round(10.0 * speedometerkphdouble) / 10.0;
   }

   public double getSpeedMpS() {
      double speedometerMpsdouble = this.turnIntoKpH(this.speedometerCurrentSpeed) / 3.6;
      return (double)Math.round(10.0 * speedometerMpsdouble) / 10.0;
   }
}
