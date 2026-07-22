package me.rebirthclient.asm.accessors;

import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({EntityPlayerSP.class})
public interface IEntityPlayerSP {
   @Accessor("handActive")
   void mm_setHandActive(boolean var1);

   @Accessor("serverSneakState")
   boolean getServerSneakState();

   @Accessor("serverSneakState")
   void setServerSneakState(boolean var1);

   @Accessor("serverSprintState")
   boolean getServerSprintState();

   @Accessor("serverSprintState")
   void setServerSprintState(boolean var1);

   @Accessor("prevOnGround")
   boolean getPrevOnGround();

   @Accessor("prevOnGround")
   void setPrevOnGround(boolean var1);

   @Accessor("autoJumpEnabled")
   boolean getAutoJumpEnabled();

   @Accessor("autoJumpEnabled")
   void setAutoJumpEnabled(boolean var1);

   @Accessor("lastReportedPosX")
   double getLastReportedPosX();

   @Accessor("lastReportedPosX")
   void setLastReportedPosX(double var1);

   @Accessor("lastReportedPosY")
   double getLastReportedPosY();

   @Accessor("lastReportedPosY")
   void setLastReportedPosY(double var1);

   @Accessor("lastReportedPosZ")
   double getLastReportedPosZ();

   @Accessor("lastReportedPosZ")
   void setLastReportedPosZ(double var1);

   @Accessor("lastReportedYaw")
   float getLastReportedYaw();

   @Accessor("lastReportedYaw")
   void setLastReportedYaw(float var1);

   @Accessor("lastReportedPitch")
   float getLastReportedPitch();

   @Accessor("lastReportedPitch")
   void setLastReportedPitch(float var1);

   @Accessor("positionUpdateTicks")
   int getPositionUpdateTicks();

   @Accessor("positionUpdateTicks")
   void setPositionUpdateTicks(int var1);

   @Invoker("onUpdateWalkingPlayer")
   void invokeOnUpdateWalkingPlayer();
}
