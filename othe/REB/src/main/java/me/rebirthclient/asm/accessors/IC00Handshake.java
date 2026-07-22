package me.rebirthclient.asm.accessors;

import net.minecraft.network.handshake.client.C00Handshake;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({C00Handshake.class})
public interface IC00Handshake {
   @Accessor("ip")
   String getIp();

   @Accessor("ip")
   void setIp(String var1);
}
