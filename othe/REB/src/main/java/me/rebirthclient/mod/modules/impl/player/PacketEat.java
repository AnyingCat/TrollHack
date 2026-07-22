//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PacketEat extends Module {
   Item item;

   public PacketEat() {
      super("PacketEat", "cancel packet", Category.PLAYER);
   }

   @Override
   public void onTick() {
      if (mc.player.isHandActive()) {
         this.item = mc.player.getActiveItemStack().getItem();
      }
   }

   @SubscribeEvent
   public void onPacketSend(PacketEvent.Send event) {
      if (event.getPacket() instanceof CPacketPlayerDigging
         && ((CPacketPlayerDigging)event.getPacket()).getAction() == Action.RELEASE_USE_ITEM
         && this.item instanceof ItemFood) {
         event.setCanceled(true);
      }
   }
}
