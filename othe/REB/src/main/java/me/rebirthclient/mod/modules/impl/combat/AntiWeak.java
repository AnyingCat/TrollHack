//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketUseEntity.Action;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AntiWeak extends Module {
   private final Setting<Integer> delay = this.add(new Setting<>("Delay", 100, 0, 500));
   private final Setting<AntiWeak.SwapMode> swapMode = this.add(new Setting<>("SwapMode", AntiWeak.SwapMode.Bypass));
   private final Setting<Boolean> onlyCrystal = this.add(new Setting<>("OnlyCrystal", true));
   private final Timer delayTimer = new Timer();
   private CPacketUseEntity packet = null;

   public AntiWeak() {
      super("AntiWeak", "anti weak", Category.COMBAT);
   }

   @Override
   public String getInfo() {
      return this.swapMode.getValue().name();
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public void onPacketSend(PacketEvent.Send event) {
      if (!fullNullCheck()) {
         if (!event.isCanceled()) {
            if (mc.player.isPotionActive(MobEffects.WEAKNESS)) {
               if (!(mc.player.getHeldItemMainhand().item instanceof ItemSword)) {
                  if (this.delayTimer.passedMs((long)this.delay.getValue().intValue())) {
                     if (event.getPacket() instanceof CPacketUseEntity && ((CPacketUseEntity)event.getPacket()).getAction() == Action.ATTACK) {
                        Entity attackedEntity = ((CPacketUseEntity)event.getPacket()).getEntityFromWorld(mc.world);
                        if (attackedEntity == null || !(attackedEntity instanceof EntityEnderCrystal) && this.onlyCrystal.getValue()) {
                           return;
                        }

                        this.packet = event.getPacket();
                        this.doAnti();
                        this.delayTimer.reset();
                        event.setCanceled(true);
                     }
                  }
               }
            }
         }
      }
   }

   private void doAnti() {
      if (this.packet != null) {
         int strong;
         if (this.swapMode.getValue() != AntiWeak.SwapMode.Bypass) {
            strong = InventoryUtil.findHotbarBlock(ItemSword.class);
         } else {
            strong = InventoryUtil.findClassInventorySlot(ItemSword.class, true);
         }

         if (strong != -1) {
            int old = mc.player.inventory.currentItem;
            if (this.swapMode.getValue() != AntiWeak.SwapMode.Bypass) {
               InventoryUtil.doSwap(strong);
            } else {
               mc.playerController.windowClick(0, strong, old, ClickType.SWAP, mc.player);
            }

            mc.player.connection.sendPacket(this.packet);
            if (this.swapMode.getValue() != AntiWeak.SwapMode.Bypass) {
               if (this.swapMode.getValue() != AntiWeak.SwapMode.Normal) {
                  InventoryUtil.doSwap(old);
               }
            } else {
               mc.playerController.windowClick(0, strong, old, ClickType.SWAP, mc.player);
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

   public static enum SwapMode {
      Normal,
      Silent,
      Bypass;
   }
}
