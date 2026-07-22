//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.DamageUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.MovementUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;

public class AutoWire extends Module {
   private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
   private final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
   private final Setting<Boolean> onlyGround = this.add(new Setting<>("OnlyGround", true));
   private final Setting<Boolean> face = this.add(new Setting<>("Face", true));
   private final Setting<Boolean> checkDamage = this.add(new Setting<>("CheckDamage", true).setParent());
   private final Setting<Float> crystalRange = this.add(new Setting<>("CrystalRange", 6.0F, 0.0F, 16.0F, v -> this.checkDamage.isOpen()));
   private final Setting<Double> minDamage = this.add(new Setting<>("MinDamage", 5.0, 0.0, 20.0, v -> this.checkDamage.isOpen()));
   private final Setting<Double> maxSelfSpeed = this.add(new Setting<>("MaxSelfSpeed", 12.0, 1.0, 30.0));
   boolean active = false;

   public AutoWire() {
      super("AutoWire", "", Category.COMBAT);
   }

   @Override
   public void onTick() {
      if (InventoryUtil.findItemInHotbar(Items.STRING) == -1) {
         this.active = false;
      } else if (!this.onlyGround.getValue() || mc.player.onGround && !MovementUtil.isJumping()) {
         if (Managers.SPEED.getPlayerSpeed(mc.player) > this.maxSelfSpeed.getValue()) {
            this.active = false;
         } else {
            if (this.checkDamage.getValue()) {
               boolean shouldReturn = true;

               for(Entity crystal : mc.world.loadedEntityList) {
                  if (crystal instanceof EntityEnderCrystal && !(mc.player.getDistance(crystal) > this.crystalRange.getValue())) {
                     float selfDamage = DamageUtil.calculateDamage(crystal, mc.player);
                     if ((double)selfDamage > this.minDamage.getValue()) {
                        shouldReturn = false;
                        break;
                     }
                  }
               }

               if (shouldReturn) {
                  this.active = false;
                  return;
               }
            }

            this.active = true;
            if (BlockUtil.canBlockFacing(EntityUtil.getPlayerPos()) && mc.world.isAirBlock(EntityUtil.getPlayerPos())) {
               int old = mc.player.inventory.currentItem;
               if (InventoryUtil.findItemInHotbar(Items.STRING) == -1) {
                  return;
               }

               InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.STRING));
               BlockUtil.placeBlock(EntityUtil.getPlayerPos(), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
               InventoryUtil.doSwap(old);
            }

            if (this.face.getValue()
               && BlockUtil.canBlockFacing(EntityUtil.getPlayerPos().up())
               && mc.world.isAirBlock(EntityUtil.getPlayerPos().up())) {
               int old = mc.player.inventory.currentItem;
               if (InventoryUtil.findItemInHotbar(Items.STRING) == -1) {
                  return;
               }

               InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.STRING));
               BlockUtil.placeBlock(EntityUtil.getPlayerPos().up(), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
               InventoryUtil.doSwap(old);
            }
         }
      } else {
         this.active = false;
      }
   }

   @Override
   public String getInfo() {
      return this.active ? "Active" : null;
   }
}
