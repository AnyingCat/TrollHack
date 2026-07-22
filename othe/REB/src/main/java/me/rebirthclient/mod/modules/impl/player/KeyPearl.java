//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.combat.PacketExp;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import org.lwjgl.input.Mouse;

public class KeyPearl extends Module {
   private final Setting<KeyPearl.Mode> mode = this.add(new Setting<>("Mode", KeyPearl.Mode.Middle));
   private final Setting<Boolean> noPlayerTrace = this.add(new Setting<>("NoPlayerTrace", true));
   private final Setting<Boolean> inventory = this.add(new Setting<>("Inventory", true).setParent());
   private final Setting<Boolean> sync = this.add(new Setting<>("Sync", true, v -> this.inventory.isOpen()));
   private final Setting<Boolean> testSync = this.add(new Setting<>("TestSync", true, v -> this.inventory.isOpen()));
   private boolean clicked;

   public KeyPearl() {
      super("KeyPearl", "Throws a pearl", Category.PLAYER);
   }

   @Override
   public String getInfo() {
      return this.mode.getValue().name();
   }

   @Override
   public void onEnable() {
      if (!fullNullCheck() && this.mode.getValue() == KeyPearl.Mode.Key) {
         this.throwPearl();
         this.disable();
      }
   }

   @Override
   public void onTick() {
      if (this.mode.getValue() == KeyPearl.Mode.Middle) {
         if (Mouse.isButtonDown(2) && mc.currentScreen == null) {
            this.clicked = true;
         } else if (this.clicked) {
            this.throwPearl();
            this.clicked = false;
         }
      }
   }

   private void throwPearl() {
      if (this.noPlayerTrace.getValue()) {
         RayTraceResult result = mc.objectMouseOver;
         if (mc.objectMouseOver != null && result.typeOfHit == Type.ENTITY && result.entityHit instanceof EntityPlayer) {
            return;
         }
      }

      int pearlSlot = InventoryUtil.findHotbarClass(ItemEnderPearl.class);
      boolean offhand = mc.player.getHeldItemOffhand().getItem() == Items.ENDER_PEARL;
      boolean mainhand = mc.player.getHeldItemOffhand().getItem() == Items.ENDER_PEARL;
      if (pearlSlot == -1 && !offhand && !mainhand) {
         if (this.inventory.getValue()) {
            pearlSlot = InventoryUtil.findClassInventorySlot(ItemEnderPearl.class, false);
            if (pearlSlot != -1) {
               mc.playerController.windowClick(0, pearlSlot, mc.player.inventory.currentItem, ClickType.SWAP, mc.player);
               mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
               mc.playerController.windowClick(0, pearlSlot, mc.player.inventory.currentItem, ClickType.SWAP, mc.player);
               if (this.sync.getValue()) {
                  PacketExp.INSTANCE.throwExp();
               }

               if (this.testSync.getValue()) {
                  mc.playerController.windowClick(0, pearlSlot, 0, ClickType.PICKUP, mc.player);
                  mc.playerController.windowClick(0, pearlSlot, 0, ClickType.PICKUP, mc.player);
                  mc.playerController.windowClick(0, 36 + mc.player.inventory.currentItem, 0, ClickType.PICKUP, mc.player);
                  mc.playerController.windowClick(0, 36 + mc.player.inventory.currentItem, 0, ClickType.PICKUP, mc.player);
               }
            }
         }
      } else {
         int oldslot = mc.player.inventory.currentItem;
         if (!offhand && !mainhand) {
            InventoryUtil.switchToHotbarSlot(pearlSlot, false);
         }

         mc.playerController.processRightClick(mc.player, mc.world, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
         if (!offhand && !mainhand) {
            InventoryUtil.switchToHotbarSlot(oldslot, false);
         }
      }
   }

   private static enum Mode {
      Key,
      Middle;
   }
}
