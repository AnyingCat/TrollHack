//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.network.play.client.CPacketConfirmTransaction;

public class AutoTotem extends Module {
   private final Setting<Float> health = this.add(new Setting<>("Health", 16.0F, 0.0F, 36.0F));
   private final Setting<Boolean> mainHand = this.add(new Setting<>("MainHand", false));
   private final Setting<Boolean> crystal = this.add(new Setting<>("Crystal", true, v -> !this.mainHand.getValue()));

   public AutoTotem() {
      super("AutoTotem", "AutoTotem", Category.COMBAT);
   }

   @Override
   public void onTick() {
      if (mc.player.getHealth() + mc.player.getAbsorptionAmount() > this.health.getValue()) {
         if (!mc.player.getHeldItemOffhand().getItem().equals(Items.END_CRYSTAL) && this.crystal.getValue() && !this.mainHand.getValue()) {
            int crystalSlot = InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL, true, true);
            if (crystalSlot != -1) {
               boolean offhandAir = mc.player.getHeldItemOffhand().getItem().equals(Items.AIR);
               mc.playerController.windowClick(0, crystalSlot, 0, ClickType.PICKUP, mc.player);
               mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
               if (!offhandAir) {
                  mc.playerController.windowClick(0, crystalSlot, 0, ClickType.PICKUP, mc.player);
               }
            }
         }
      } else if (!mc.player.getHeldItemOffhand().getItem().equals(Items.TOTEM_OF_UNDYING)
         && !mc.player.getHeldItemMainhand().getItem().equals(Items.TOTEM_OF_UNDYING)) {
         int totemSlot = InventoryUtil.findItemInventorySlot(Items.TOTEM_OF_UNDYING, true, true);
         if (totemSlot != -1) {
            if (!this.mainHand.getValue()) {
               boolean offhandAir = mc.player.getHeldItemOffhand().getItem().equals(Items.AIR);
               mc.playerController.windowClick(0, totemSlot, 0, ClickType.PICKUP, mc.player);
               mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
               if (!offhandAir) {
                  mc.playerController.windowClick(0, totemSlot, 0, ClickType.PICKUP, mc.player);
               }
            } else {
               InventoryUtil.switchToHotbarSlot(0, false);
               if (mc.player.inventory.getStackInSlot(0).getItem().equals(Items.TOTEM_OF_UNDYING)) {
                  return;
               }

               boolean mainAir = mc.player.getHeldItemMainhand().getItem().equals(Items.AIR);
               mc.playerController.windowClick(0, totemSlot, 0, ClickType.PICKUP, mc.player);
               mc.playerController.windowClick(0, 36, 0, ClickType.PICKUP, mc.player);
               if (!mainAir) {
                  mc.playerController.windowClick(0, totemSlot, 0, ClickType.PICKUP, mc.player);
               }
            }

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
}
