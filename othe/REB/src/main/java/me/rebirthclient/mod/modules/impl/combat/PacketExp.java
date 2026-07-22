//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.events.impl.MotionEvent;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Bind;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

public class PacketExp extends Module {
   public static PacketExp INSTANCE;
   private final Setting<Integer> delay = this.add(new Setting<>("Delay", 1, 0, 5));
   public final Setting<Boolean> down = this.add(new Setting<>("Down", true));
   public final Setting<Boolean> allowGui = this.add(new Setting<>("allowGui", false));
   public final Setting<Boolean> checkDura = this.add(new Setting<>("CheckDura", true));
   private final Setting<PacketExp.Mode> mode = this.add(new Setting<>("Mode", PacketExp.Mode.Key));
   public final Setting<Bind> throwBind = this.add(new Setting<>("ThrowBind", new Bind(-1), v -> this.mode.getValue() == PacketExp.Mode.Key));
   private final Timer delayTimer = new Timer();

   public PacketExp() {
      super("PacketExp", "Robot module", Category.COMBAT);
      INSTANCE = this;
   }

   @Override
   public String getInfo() {
      return this.mode.getValue().name();
   }

   @Override
   public void onTick() {
      if (this.isThrow() && this.delayTimer.passedMs((long)(this.delay.getValue() * 20))) {
         this.throwExp();
      }
   }

   public void throwExp() {
      int oldSlot = mc.player.inventory.currentItem;
      int newSlot = InventoryUtil.findHotbarClass(ItemExpBottle.class);
      if (newSlot != -1) {
         mc.player.connection.sendPacket(new CPacketHeldItemChange(newSlot));
         mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
         mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
         this.delayTimer.reset();
      }
   }

   @SubscribeEvent(
      priority = EventPriority.NORMAL
   )
   public void RotateEvent(MotionEvent event) {
      if (!fullNullCheck()) {
         if (this.down.getValue()) {
            if (this.isThrow()) {
               event.setPitch(90.0F);
            }
         }
      }
   }

   public boolean isThrow() {
      if (this.isOff()) {
         return false;
      } else if (!this.allowGui.getValue() && mc.currentScreen != null) {
         return false;
      } else if (InventoryUtil.findHotbarClass(ItemExpBottle.class) == -1) {
         return false;
      } else {
         if (this.checkDura.getValue()) {
            ItemStack helm = mc.player.inventoryContainer.getSlot(5).getStack();
            ItemStack chest = mc.player.inventoryContainer.getSlot(6).getStack();
            ItemStack legging = mc.player.inventoryContainer.getSlot(7).getStack();
            ItemStack feet = mc.player.inventoryContainer.getSlot(8).getStack();
            if ((helm.isEmpty || EntityUtil.getDamagePercent(helm) >= 100)
               && (chest.isEmpty || EntityUtil.getDamagePercent(chest) >= 100)
               && (legging.isEmpty || EntityUtil.getDamagePercent(legging) >= 100)
               && (feet.isEmpty || EntityUtil.getDamagePercent(feet) >= 100)) {
               return false;
            }
         }

         if (this.mode.getValue() == PacketExp.Mode.Middle && Mouse.isButtonDown(2)) {
            return true;
         } else {
            return this.mode.getValue() == PacketExp.Mode.Key && this.throwBind.getValue().isDown();
         }
      }
   }

   protected static enum Mode {
      Key,
      Middle;
   }
}
