//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoWalk extends Module {
   public AutoWalk() {
      super("AutoWalk", "Auto forward move", Category.MOVEMENT);
   }

   @SubscribeEvent
   public void onUpdateInput(InputUpdateEvent event) {
      event.getMovementInput().moveForward = 1.0F;
   }
}
