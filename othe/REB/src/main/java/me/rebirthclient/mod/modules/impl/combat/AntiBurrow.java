//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class AntiBurrow extends Module {
   private final Setting<AntiBurrow.Block> blockSetting = this.add(new Setting<>("Block", AntiBurrow.Block.RedStone));
   public final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
   public final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
   public final Setting<Integer> delay = this.add(new Setting<>("Delay", 50, 0, 500));
   private final Setting<Integer> multiPlace = this.add(new Setting<>("MultiPlace", 1, 1, 4));
   private final Timer timer = new Timer();
   private final Setting<Boolean> onlyGround = this.add(new Setting<>("SelfGround", true));
   private final Setting<Float> range = this.add(new Setting<>("Range", 5.0F, 1.0F, 6.0F));
   public EntityPlayer target;
   private int progress = 0;

   public AntiBurrow() {
      super("AntiBurrow", "put something under foot", Category.COMBAT);
   }

   @Override
   public String getInfo() {
      return this.target != null ? this.target.getName() : null;
   }

   @Override
   public void onTick() {
      if (this.timer.passedMs((long)this.delay.getValue().intValue())) {
         this.target = CombatUtil.getTarget((double)this.range.getValue().floatValue());
         if (this.target != null) {
            if (!this.onlyGround.getValue() || mc.player.onGround) {
               this.progress = 0;
               this.placeBlock(new BlockPos(this.target.posX + 0.2, this.target.posY + 0.5, this.target.posZ + 0.2));
               this.placeBlock(new BlockPos(this.target.posX - 0.2, this.target.posY + 0.5, this.target.posZ + 0.2));
               this.placeBlock(new BlockPos(this.target.posX - 0.2, this.target.posY + 0.5, this.target.posZ - 0.2));
               this.placeBlock(new BlockPos(this.target.posX + 0.2, this.target.posY + 0.5, this.target.posZ - 0.2));
            }
         }
      }
   }

   private boolean checkEntity(BlockPos pos) {
      for(Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
         if (entity instanceof EntityPlayer && entity != mc.player) {
            return true;
         }
      }

      return false;
   }

   private void placeBlock(BlockPos pos) {
      if (this.progress < this.multiPlace.getValue()) {
         if (this.checkEntity(pos)) {
            if (this.canPlace(pos)) {
               int old = mc.player.inventory.currentItem;
               if (this.blockSetting.getValue() != AntiBurrow.Block.Button
                  || InventoryUtil.findHotbarBlock(Blocks.STONE_BUTTON) == -1 && InventoryUtil.findHotbarBlock(Blocks.WOODEN_BUTTON) == -1) {
                  if (this.blockSetting.getValue() != AntiBurrow.Block.RedStone || InventoryUtil.findItemInHotbar(Items.REDSTONE) == -1) {
                     return;
                  }

                  InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.REDSTONE));
                  BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
               } else {
                  if (InventoryUtil.findHotbarBlock(Blocks.STONE_BUTTON) != -1) {
                     InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(Blocks.STONE_BUTTON));
                  } else {
                     InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(Blocks.WOODEN_BUTTON));
                  }

                  BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
               }

               ++this.progress;
               InventoryUtil.doSwap(old);
               this.timer.reset();
            }
         }
      }
   }

   public boolean canPlace(BlockPos pos) {
      if (BlockUtil.canBlockReplace(pos.down())) {
         return false;
      } else if (!BlockUtil.canReplace(pos)) {
         return false;
      } else if (!CombatSetting.INSTANCE.strictPlace.getValue()) {
         return true;
      } else {
         for(EnumFacing side : BlockUtil.getPlacableFacings(pos, true, CombatSetting.INSTANCE.checkRaytrace.getValue())) {
            if (BlockUtil.canClick(pos.offset(side))) {
               return true;
            }
         }

         return false;
      }
   }

   private static enum Block {
      Button,
      RedStone;
   }
}
