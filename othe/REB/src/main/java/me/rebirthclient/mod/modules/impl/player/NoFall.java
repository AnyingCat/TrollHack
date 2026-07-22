//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoFall extends Module {
   public static NoFall INSTANCE = new NoFall();
   private final Setting<Integer> distance = this.add(new Setting<>("Distance", 3, 0, 50));

   public NoFall() {
      super("NoFall", "Prevents fall damage", Category.PLAYER);
      INSTANCE = this;
   }

   @SubscribeEvent
   public void onPacketSend(PacketEvent.Send event) {
      if (!fullNullCheck()) {
         if (event.getPacket() instanceof CPacketPlayer) {
            for(ItemStack is : mc.player.getArmorInventoryList()) {
               if (is.getItem() instanceof ItemElytra) {
                  return;
               }
            }

            if (mc.player.isElytraFlying()) {
               return;
            }

            if (mc.player.fallDistance >= (float)this.distance.getValue().intValue()) {
               CPacketPlayer packet = event.getPacket();
               packet.onGround = true;
            }
         }
      }
   }

   @Override
   public String getInfo() {
      return "Packet";
   }
}
