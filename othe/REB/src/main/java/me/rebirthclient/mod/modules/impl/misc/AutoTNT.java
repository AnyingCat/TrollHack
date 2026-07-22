//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.misc;

import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AutoTNT extends Module {
   private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
   private final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
   private final Setting<Float> range = this.add(new Setting<>("Range", 4.0F, 0.0F, 6.0F));
   private EntityPlayer target;

   public AutoTNT() {
      super("AutoTNT", "IQless", Category.MISC);
   }

   @Override
   public void onUpdate() {
      if (InventoryUtil.findHotbarBlock(Blocks.TNT) != -1) {
         if (InventoryUtil.findItemInHotbar(Items.FLINT_AND_STEEL) != -1 || InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK) != -1) {
            this.target = this.getTarget((double)this.range.getValue().floatValue());
            if (this.target != null) {
               int old = mc.player.inventory.currentItem;
               BlockPos pos = EntityUtil.getEntityPos(this.target).up(2);
               if (BlockUtil.canPlace(pos)) {
                  InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(Blocks.TNT));
                  BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
                  InventoryUtil.doSwap(old);
               }

               if (mc.world.getBlockState(pos).getBlock() == Blocks.TNT) {
                  if (InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK) != -1) {
                     for(EnumFacing i : EnumFacing.VALUES) {
                        if (mc.world.getBlockState(pos.offset(i)).getBlock() == Blocks.REDSTONE_BLOCK) {
                           return;
                        }
                     }

                     for(EnumFacing i : EnumFacing.VALUES) {
                        if (i != EnumFacing.DOWN && BlockUtil.canPlace(pos.offset(i))) {
                           InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK));
                           BlockUtil.placeBlock(pos.offset(i), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
                           InventoryUtil.doSwap(old);
                           return;
                        }
                     }
                  } else if (InventoryUtil.findItemInHotbar(Items.FLINT_AND_STEEL) != -1) {
                     InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.FLINT_AND_STEEL));
                     Vec3d vec = new Vec3d(pos)
                        .add(0.5, 0.5, 0.5)
                        .add(new Vec3d(BlockUtil.getRayTraceFacing(pos).getDirectionVec()).scale(0.5));
                     if (this.rotate.getValue()) {
                        EntityUtil.faceVector(vec);
                     }

                     float f = (float)(vec.x - (double)pos.getX());
                     float f1 = (float)(vec.y - (double)pos.getY());
                     float f2 = (float)(vec.z - (double)pos.getZ());
                     mc.player
                        .connection
                        .sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, BlockUtil.getRayTraceFacing(pos), EnumHand.MAIN_HAND, f, f1, f2));
                     InventoryUtil.doSwap(old);
                  }
               }
            }
         }
      }
   }

   @Override
   public String getInfo() {
      return this.target != null ? this.target.getName() : null;
   }

   private EntityPlayer getTarget(double range) {
      EntityPlayer target = null;
      double distance = range;

      for(EntityPlayer player : mc.world.playerEntities) {
         if (!EntityUtil.invalid(player, range)
            && (
               BlockUtil.canPlace(EntityUtil.getEntityPos(player).up(2))
                  || mc.world.getBlockState(EntityUtil.getEntityPos(player).up(2)).getBlock() == Blocks.TNT
            )) {
            if (target == null) {
               target = player;
               distance = mc.player.getDistanceSq(player);
            } else if (!(mc.player.getDistanceSq(player) >= distance)) {
               target = player;
               distance = mc.player.getDistanceSq(player);
            }
         }
      }

      return target;
   }
}
