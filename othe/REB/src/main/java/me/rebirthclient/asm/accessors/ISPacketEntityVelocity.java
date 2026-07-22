package me.rebirthclient.asm.accessors;

import net.minecraft.network.play.server.SPacketEntityVelocity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({SPacketEntityVelocity.class})
public interface ISPacketEntityVelocity {
   @Accessor("entityID")
   int getEntityID();

   @Accessor("motionX")
   int getX();

   @Accessor("motionX")
   void setX(int var1);

   @Accessor("motionY")
   int getY();

   @Accessor("motionY")
   void setY(int var1);

   @Accessor("motionZ")
   int getZ();

   @Accessor("motionZ")
   void setZ(int var1);
}
