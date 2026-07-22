//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.api.managers.impl.SneakManager;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.MovementInput;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoSlowDown extends Module {
   private final Setting<NoSlowDown.Mode> mode = this.add(new Setting<>("Mode", NoSlowDown.Mode.Vanilla));
   private final Setting<Boolean> sneak = this.add(new Setting<>("Sneak", false));

   public NoSlowDown() {
      super("NoSlowDown", "No item use slow down", Category.MOVEMENT);
   }

   @SubscribeEvent
   public void Slow(InputUpdateEvent event) {
      if (!SneakManager.isSneaking && mc.player.isHandActive() && !mc.player.isRiding()) {
         if (this.mode.getValue() == NoSlowDown.Mode.Strict) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
         }

         mc.player.movementInput.moveForward = (float)((double)mc.player.movementInput.moveForward / 0.2);
         mc.player.movementInput.moveStrafe = (float)((double)mc.player.movementInput.moveStrafe / 0.2);
      } else if (SneakManager.isSneaking && this.sneak.getValue() && !mc.player.isRiding()) {
         MovementInput var10000 = event.getMovementInput();
         var10000.moveStrafe *= 5.0F;
         var10000 = event.getMovementInput();
         var10000.moveForward *= 5.0F;
      }
   }

   @Override
   public String getInfo() {
      return this.mode.getValue().name();
   }

   public static enum Mode {
      Vanilla,
      Strict;
   }
}
