//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.Arrays;
import java.util.List;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SelfWeb extends Module {
   public final List<Block> blackList = Arrays.asList(
      Blocks.ENDER_CHEST,
      Blocks.CHEST,
      Blocks.TRAPPED_CHEST,
      Blocks.CRAFTING_TABLE,
      Blocks.ANVIL,
      Blocks.BREWING_STAND,
      Blocks.HOPPER,
      Blocks.DROPPER,
      Blocks.DISPENSER
   );
   private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
   private final Setting<Boolean> smart = this.add(new Setting<>("Smart", false).setParent());
   private final Setting<Integer> enemyRange = this.add(new Setting<>("EnemyRange", 4, 0, 8, v -> this.smart.isOpen()));
   private int newSlot = -1;
   private boolean sneak;

   public SelfWeb() {
      super("SelfWeb", "Places webs at your feet", Category.COMBAT);
   }

   @Override
   public void onEnable() {
      if (mc.player != null) {
         this.newSlot = this.getHotbarItem();
         if (this.newSlot == -1) {
            this.sendMessage("[" + this.getName() + "] " + ChatFormatting.RED + "No Webs in hotbar. disabling...");
            this.disable();
         }
      }
   }

   @Override
   public void onDisable() {
      if (mc.player != null && this.sneak) {
         mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
         this.sneak = false;
      }
   }

   @Override
   public void onUpdate() {
      if (!fullNullCheck()) {
         if (this.smart.getValue()) {
            EntityPlayer target = this.getClosestTarget();
            if (target == null) {
               return;
            }

            if (Managers.FRIENDS.isFriend(target.getName())) {
               return;
            }

            if (mc.player.getDistance(target) < (float)this.enemyRange.getValue().intValue() && this.isSafe()) {
               int last_slot = mc.player.inventory.currentItem;
               InventoryUtil.doSwap(this.newSlot);
               this.placeBlock(this.getFloorPos());
               InventoryUtil.doSwap(last_slot);
            }
         } else {
            int last_slot = mc.player.inventory.currentItem;
            InventoryUtil.doSwap(this.newSlot);
            this.placeBlock(this.getFloorPos());
            InventoryUtil.doSwap(last_slot);
            this.disable();
         }
      }
   }

   private EntityPlayer getClosestTarget() {
      if (mc.world.playerEntities.isEmpty()) {
         return null;
      } else {
         EntityPlayer closestTarget = null;

         for(EntityPlayer target : mc.world.playerEntities) {
            if (target != mc.player
               && EntityUtil.isLiving(target)
               && !(target.getHealth() <= 0.0F)
               && (closestTarget == null || !(mc.player.getDistance(target) > mc.player.getDistance(closestTarget)))) {
               closestTarget = target;
            }
         }

         return closestTarget;
      }
   }

   private int getHotbarItem() {
      for(int i = 0; i < 9; ++i) {
         ItemStack stack = mc.player.inventory.getStackInSlot(i);
         if (stack.getItem() == Item.getItemById(30)) {
            return i;
         }
      }

      return -1;
   }

   private boolean isSafe() {
      BlockPos player_block = this.getFloorPos();
      return mc.world.getBlockState(player_block.east()).getBlock() != Blocks.AIR
         && mc.world.getBlockState(player_block.west()).getBlock() != Blocks.AIR
         && mc.world.getBlockState(player_block.north()).getBlock() != Blocks.AIR
         && mc.world.getBlockState(player_block.south()).getBlock() != Blocks.AIR
         && mc.world.getBlockState(player_block).getBlock() == Blocks.AIR;
   }

   private void placeBlock(BlockPos pos) {
      if (mc.world.getBlockState(pos).getMaterial().isReplaceable()) {
         if (this.checkForNeighbours(pos)) {
            for(EnumFacing side : EnumFacing.values()) {
               BlockPos neighbor = pos.offset(side);
               EnumFacing side2 = side.getOpposite();
               if (this.canBeClicked(neighbor)) {
                  if (this.blackList.contains(mc.world.getBlockState(neighbor).getBlock())) {
                     mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
                     this.sneak = true;
                  }

                  Vec3d hitVec = new Vec3d(neighbor).add(0.5, 0.5, 0.5).add(new Vec3d(side2.getDirectionVec()).scale(0.5));
                  if (this.rotate.getValue()) {
                     Managers.ROTATIONS.lookAtVec3dPacket(hitVec);
                  }

                  mc.playerController.processRightClickBlock(mc.player, mc.world, neighbor, side2, hitVec, EnumHand.MAIN_HAND);
                  mc.player.swingArm(EnumHand.MAIN_HAND);
                  return;
               }
            }
         }
      }
   }

   private boolean checkForNeighbours(BlockPos blockPos) {
      if (!this.hasNeighbour(blockPos)) {
         for(EnumFacing side : EnumFacing.values()) {
            BlockPos neighbour = blockPos.offset(side);
            if (this.hasNeighbour(neighbour)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private boolean hasNeighbour(BlockPos blockPos) {
      for(EnumFacing side : EnumFacing.values()) {
         BlockPos neighbour = blockPos.offset(side);
         if (!mc.world.getBlockState(neighbour).getMaterial().isReplaceable()) {
            return true;
         }
      }

      return false;
   }

   private boolean canBeClicked(BlockPos pos) {
      return BlockUtil.getBlock(pos).canCollideCheck(BlockUtil.getState(pos), false);
   }

   private BlockPos getFloorPos() {
      return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
   }
}
