//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.managers.impl;

import me.rebirthclient.api.events.impl.PacketEvent;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SneakManager {
   public static boolean isSneaking = false;

   public void init() {
      MinecraftForge.EVENT_BUS.register(this);
   }

   @SubscribeEvent
   public void onPacketSend(PacketEvent.Send event) {
      if (event.getPacket() instanceof CPacketEntityAction) {
         if (((CPacketEntityAction)event.getPacket()).getAction() == Action.START_SNEAKING) {
            isSneaking = true;
         }

         if (((CPacketEntityAction)event.getPacket()).getAction() == Action.STOP_SNEAKING) {
            isSneaking = false;
         }
      }
   }
}
