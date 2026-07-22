//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import me.rebirthclient.api.managers.impl.SneakManager;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.asm.accessors.IGuiShulkerBox;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.render.PlaceRender;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiShulkerBox;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AutoReplenish extends Module {
   public static final List<Block> shulkers = Arrays.asList(
      Blocks.BLACK_SHULKER_BOX,
      Blocks.BLUE_SHULKER_BOX,
      Blocks.BROWN_SHULKER_BOX,
      Blocks.CYAN_SHULKER_BOX,
      Blocks.GRAY_SHULKER_BOX,
      Blocks.GREEN_SHULKER_BOX,
      Blocks.LIGHT_BLUE_SHULKER_BOX,
      Blocks.LIME_SHULKER_BOX,
      Blocks.MAGENTA_SHULKER_BOX,
      Blocks.ORANGE_SHULKER_BOX,
      Blocks.PINK_SHULKER_BOX,
      Blocks.PURPLE_SHULKER_BOX,
      Blocks.RED_SHULKER_BOX,
      Blocks.SILVER_SHULKER_BOX,
      Blocks.WHITE_SHULKER_BOX,
      Blocks.YELLOW_SHULKER_BOX
   );
   private final Setting<Boolean> autoDisable = this.add(new Setting<>("AutoDisable", true));
   public final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
   public final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
   private final Setting<Boolean> place = this.add(new Setting<>("Place", true));
   private final Setting<Boolean> open = this.add(new Setting<>("Open", true));
   private final Setting<Float> range = this.add(new Setting<>("Range", 4.0F, 0.0F, 6.0F));
   private final Setting<Float> minRange = this.add(new Setting<>("MinRange", 1.0F, 0.0F, 3.0F));
   private final Setting<Boolean> take = this.add(new Setting<>("Take", true).setParent());
   private final Setting<Boolean> smart = this.add(new Setting<>("Smart", true, v -> this.take.isOpen()).setParent());
   private final Setting<Integer> crystal = this.add(new Setting<>("Crystal", 6, 0, 20, v -> this.take.isOpen() && this.smart.isOpen()));
   private final Setting<Integer> exp = this.add(new Setting<>("Exp", 6, 0, 20, v -> this.take.isOpen() && this.smart.isOpen()));
   private final Setting<Integer> totem = this.add(new Setting<>("Totem", 6, 0, 30, v -> this.take.isOpen() && this.smart.isOpen()));
   private final Setting<Integer> gapple = this.add(new Setting<>("Gapple", 3, 0, 10, v -> this.take.isOpen() && this.smart.isOpen()));
   private final Setting<Integer> endChest = this.add(new Setting<>("EndChest", 1, 0, 5, v -> this.take.isOpen() && this.smart.isOpen()));
   private final Setting<Integer> web = this.add(new Setting<>("Web", 1, 0, 5, v -> this.take.isOpen() && this.smart.isOpen()));
   private final Setting<Integer> redStoneBlock = this.add(new Setting<>("RedStoneBlock", 1, 0, 5, v -> this.take.isOpen() && this.smart.isOpen()));
   private final Setting<Integer> piston = this.add(new Setting<>("Piston", 1, 0, 5, v -> this.take.isOpen() && this.smart.isOpen()));
   int[] stealCountList = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
   BlockPos placePos = null;

   public AutoReplenish() {
      super("AutoReplenish", "Auto place shulker and replenish", Category.COMBAT);
   }

   public int findShulker() {
      AtomicInteger atomicInteger = new AtomicInteger(-1);
      shulkers.forEach(e -> {
         if (InventoryUtil.findHotbarBlock(e) != -1) {
            atomicInteger.set(InventoryUtil.findHotbarBlock(e));
         }
      });
      return atomicInteger.get();
   }

   @Override
   public void onEnable() {
      this.placePos = null;
      int oldSlot = mc.player.inventory.currentItem;
      if (this.place.getValue()) {
         if (this.findShulker() == -1) {
            this.sendMessage(ChatFormatting.RED + "No Shulker Found");
         } else {
            InventoryUtil.doSwap(this.findShulker());
            double distance = 100.0;
            BlockPos bestPos = null;

            for(BlockPos pos : BlockUtil.getBox(this.range.getValue())) {
               if (BlockUtil.isAir(pos.up())
                  && !(
                     mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5)
                        < (double)this.minRange.getValue().floatValue()
                  )
                  && BlockUtil.canPlaceShulker(pos)
                  && (
                     bestPos == null
                        || mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5)
                           < distance
                  )) {
                  distance = mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5);
                  bestPos = pos;
               }
            }

            if (bestPos != null) {
               placeShulker(bestPos, this.rotate.getValue(), this.packet.getValue());
               this.placePos = bestPos;
            }

            InventoryUtil.doSwap(oldSlot);
         }
      }
   }

   private void update() {
      this.stealCountList[0] = this.crystal.getValue() - this.getItemCount(Items.END_CRYSTAL);
      this.stealCountList[1] = this.exp.getValue() - this.getItemCount(Items.EXPERIENCE_BOTTLE);
      this.stealCountList[2] = this.totem.getValue() - this.getItemCount(Items.TOTEM_OF_UNDYING);
      this.stealCountList[3] = this.gapple.getValue() - this.getItemCount(Items.GOLDEN_APPLE);
      this.stealCountList[4] = this.endChest.getValue() - this.getItemCount(Item.getItemFromBlock(Blocks.ENDER_CHEST));
      this.stealCountList[5] = this.web.getValue() - this.getItemCount(Item.getItemFromBlock(Blocks.WEB));
      this.stealCountList[6] = this.redStoneBlock.getValue() - this.getItemCount(Item.getItemFromBlock(Blocks.REDSTONE_BLOCK));
      this.stealCountList[7] = this.piston.getValue() - this.getPistonCount();
   }

   @Override
   public void onTick() {
      this.update();
      if (mc.currentScreen instanceof GuiShulkerBox) {
         if (!this.take.getValue()) {
            if (this.autoDisable.getValue()) {
               this.disable();
            }
         } else {
            GuiShulkerBox l_Chest = (GuiShulkerBox)mc.currentScreen;
            IInventory inventory = ((IGuiShulkerBox)l_Chest).getInventory();

            for(int l_I = 0; l_I < ((IInventory)Objects.requireNonNull(inventory)).getSizeInventory(); ++l_I) {
               ItemStack stack = inventory.getStackInSlot(l_I);
               if (!stack.isEmpty) {
                  if (!this.smart.getValue()) {
                     mc.playerController.windowClick(l_Chest.inventorySlots.windowId, l_I, 0, ClickType.QUICK_MOVE, mc.player);
                  }

                  if (this.needSteal(((IGuiShulkerBox)l_Chest).getInventory().getStackInSlot(l_I).getItem())) {
                     mc.playerController.windowClick(l_Chest.inventorySlots.windowId, l_I, 0, ClickType.QUICK_MOVE, mc.player);
                  }
               }
            }

            if (this.autoDisable.getValue()) {
               this.disable();
            }
         }
      } else {
         if (this.open.getValue()) {
            if (this.placePos != null) {
               if (shulkers.contains(mc.world.getBlockState(this.placePos).getBlock())) {
                  openShulker(this.placePos, this.rotate.getValue(), this.packet.getValue());
               }
            } else {
               for(BlockPos pos : BlockUtil.getBox(this.range.getValue())) {
                  if (BlockUtil.isAir(pos.up()) && shulkers.contains(mc.world.getBlockState(pos).getBlock())) {
                     openShulker(pos, this.rotate.getValue(), this.packet.getValue());
                     break;
                  }
               }
            }
         } else if (!this.take.getValue()) {
            if (this.autoDisable.getValue()) {
               this.disable();
            }

            return;
         }
      }
   }

   private boolean needSteal(Item i) {
      if (i.equals(Items.END_CRYSTAL) && this.stealCountList[0] > 0) {
         int[] stealCountList = this.stealCountList;
         stealCountList[0]--;
         return true;
      } else if (i.equals(Items.EXPERIENCE_BOTTLE) && this.stealCountList[1] > 0) {
         int[] stealCountList2 = this.stealCountList;
         stealCountList2[1]--;
         return true;
      } else if (i.equals(Items.TOTEM_OF_UNDYING) && this.stealCountList[2] > 0) {
         int[] stealCountList3 = this.stealCountList;
         stealCountList3[2]--;
         return true;
      } else if (i.equals(Items.GOLDEN_APPLE) && this.stealCountList[3] > 0) {
         int[] stealCountList4 = this.stealCountList;
         stealCountList4[3]--;
         return true;
      } else if (i.equals(Item.getItemFromBlock(Blocks.ENDER_CHEST)) && this.stealCountList[4] > 0) {
         int[] stealCountList5 = this.stealCountList;
         stealCountList5[4]--;
         return true;
      } else if (i.equals(Item.getItemFromBlock(Blocks.WEB)) && this.stealCountList[5] > 0) {
         int[] stealCountList6 = this.stealCountList;
         stealCountList6[5]--;
         return true;
      } else if (i.equals(Item.getItemFromBlock(Blocks.REDSTONE_BLOCK)) && this.stealCountList[6] > 0) {
         int[] stealCountList7 = this.stealCountList;
         stealCountList7[6]--;
         return true;
      } else if ((i == Item.getItemFromBlock(Blocks.PISTON) || i == Item.getItemFromBlock(Blocks.STICKY_PISTON)) && this.stealCountList[7] > 0) {
         int[] stealCountList8 = this.stealCountList;
         stealCountList8[7]--;
         return true;
      } else {
         return false;
      }
   }

   public static void placeShulker(BlockPos pos, boolean rotate, boolean packet) {
      EnumFacing side = EnumFacing.DOWN;
      BlockPos neighbour = pos.offset(side);
      EnumFacing opposite = side.getOpposite();
      Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
      Block neighbourBlock = mc.world.getBlockState(neighbour).getBlock();
      boolean sneaking = false;
      if (!SneakManager.isSneaking && (BlockUtil.canUseList.contains(neighbourBlock) || BlockUtil.shulkerList.contains(neighbourBlock))) {
         mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
         sneaking = true;
      }

      if (rotate) {
         EntityUtil.faceVector(hitVec);
      }

      PlaceRender.PlaceMap.put(pos, new PlaceRender.placePosition(pos));
      BlockUtil.rightClickBlock(neighbour, hitVec, EnumHand.MAIN_HAND, opposite, packet);
      if (sneaking) {
         mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
      }
   }

   public static void openShulker(BlockPos pos, boolean rotate, boolean packet) {
      EnumFacing side = EnumFacing.DOWN;
      pos = pos.up();
      BlockPos neighbour = pos.offset(side);
      EnumFacing opposite = side.getOpposite();
      Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
      if (rotate) {
         EntityUtil.faceVector(hitVec);
      }

      mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
      BlockUtil.rightClickBlock(neighbour, hitVec, EnumHand.MAIN_HAND, opposite, packet);
   }

   private int getItemCount(Item item) {
      int count = 0;
      if (mc.player.getHeldItemOffhand().getItem() == item) {
         ++count;
      }

      for(int i = 1; i < 5; ++i) {
         ItemStack itemStack = ((Slot)mc.player.inventoryContainer.inventorySlots.get(i)).getStack();
         if (itemStack.getItem() == item) {
            ++count;
         }
      }

      for(Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
         if (((ItemStack)entry.getValue()).getItem() == item && entry.getKey() != 45) {
            ++count;
         }
      }

      return count;
   }

   private int getPistonCount() {
      int count = 0;
      if (mc.player.getHeldItemOffhand().getItem() == Item.getItemFromBlock(Blocks.PISTON)
         || mc.player.getHeldItemOffhand().getItem() == Item.getItemFromBlock(Blocks.STICKY_PISTON)) {
         ++count;
      }

      for(int i = 1; i < 5; ++i) {
         ItemStack itemStack = ((Slot)mc.player.inventoryContainer.inventorySlots.get(i)).getStack();
         if (itemStack.getItem() == Item.getItemFromBlock(Blocks.STICKY_PISTON) || itemStack.getItem() == Item.getItemFromBlock(Blocks.STICKY_PISTON)) {
            ++count;
         }
      }

      for(Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
         if ((
               ((ItemStack)entry.getValue()).getItem() == Item.getItemFromBlock(Blocks.STICKY_PISTON)
                  || ((ItemStack)entry.getValue()).getItem() == Item.getItemFromBlock(Blocks.PISTON)
            )
            && entry.getKey() != 45) {
            ++count;
         }
      }

      return count;
   }
}
