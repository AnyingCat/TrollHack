//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.misc;

import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.network.play.client.CPacketConfirmTransaction;

public class InventorySync extends Module {
   private final Setting<Integer> delay = this.add(new Setting<>("Delay", 1000, 0, 5000));
   private final Timer timer = new Timer();

   public InventorySync() {
      super("InventorySync", "sync inventory", Category.MISC);
   }

   @Override
   public void onTick() {
      if (this.timer.passedMs((long)this.delay.getValue().intValue())) {
         this.timer.reset();
         mc.player
            .connection
            .sendPacket(
               new CPacketConfirmTransaction(
                  mc.player.inventoryContainer.windowId, mc.player.openContainer.getNextTransactionID(mc.player.inventory), true
               )
            );
      }
   }
}
