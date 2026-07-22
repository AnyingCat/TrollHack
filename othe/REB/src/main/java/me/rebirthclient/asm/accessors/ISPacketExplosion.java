package me.rebirthclient.asm.accessors;

import net.minecraft.network.play.server.SPacketExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({SPacketExplosion.class})
public interface ISPacketExplosion {
   @Accessor("motionX")
   float getX();

   @Accessor("motionX")
   void setX(float var1);

   @Accessor("motionY")
   float getY();

   @Accessor("motionY")
   void setY(float var1);

   @Accessor("motionZ")
   float getZ();

   @Accessor("motionZ")
   void setZ(float var1);
}
