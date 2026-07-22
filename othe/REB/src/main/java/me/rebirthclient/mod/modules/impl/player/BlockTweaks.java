//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BlockTweaks extends Module {
   public static BlockTweaks INSTANCE;
   public Setting<Boolean> noFriendAttack = this.add(new Setting<>("NoFriendAttack", false));
   public Setting<Boolean> autoTool = this.add(new Setting<>("AutoTool", false));
   public Setting<Boolean> noGhost = this.add(new Setting<>("NoGlitchBlocks", false));
   private int lastHotbarSlot = -1;
   private boolean switched = false;
   private int currentTargetSlot = -1;

   public BlockTweaks() {
      super("BlockTweaks", "Some tweaks for blocks", Category.PLAYER);
      INSTANCE = this;
   }

   @Override
   public void onDisable() {
      if (this.switched) {
         this.equip(this.lastHotbarSlot, false);
      }

      this.lastHotbarSlot = -1;
      this.currentTargetSlot = -1;
   }

   @SubscribeEvent
   public void onBlockInteract(LeftClickBlock leftClickBlock) {
      if (this.autoTool.getValue() && !fullNullCheck()) {
         this.equipBestTool(mc.world.getBlockState(leftClickBlock.getPos()));
      }
   }

   private void equipBestTool(IBlockState blockState) {
      int n = -1;
      double n2 = 0.0;

      for(int i = 0; i < 9; ++i) {
         ItemStack getStackInSlot = mc.player.inventory.getStackInSlot(i);
         float getDestroySpeed;
         int getEnchantmentLevel;
         float n3;
         if (!getStackInSlot.isEmpty
            && (getDestroySpeed = getStackInSlot.getDestroySpeed(blockState)) > 1.0F
            && (double)(
                  n3 = (float)(
                     (double)getDestroySpeed
                        + (
                           (getEnchantmentLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, getStackInSlot)) > 0
                              ? Math.pow((double)getEnchantmentLevel, 2.0) + 1.0
                              : 0.0
                        )
                  )
               )
               > n2) {
            n2 = (double)n3;
            n = i;
         }
      }

      this.equip(n, true);
   }

   private void equip(int n, boolean switched) {
      if (n != -1) {
         if (n != mc.player.inventory.currentItem) {
            this.lastHotbarSlot = mc.player.inventory.currentItem;
         }

         this.currentTargetSlot = n;
         mc.player.inventory.currentItem = n;
         this.switched = switched;
      }
   }

   @SubscribeEvent
   public void onPacketSend(PacketEvent.Send send) {
      if (!fullNullCheck()) {
         Entity getEntityFromWorld;
         if (this.noFriendAttack.getValue()
            && send.getPacket() instanceof CPacketUseEntity
            && (getEntityFromWorld = ((CPacketUseEntity)send.getPacket()).getEntityFromWorld(mc.world)) != null
            && Managers.FRIENDS.isFriend(getEntityFromWorld.getName())) {
            send.setCanceled(true);
         }
      }
   }

   private void removeGlitchBlocks(BlockPos blockPos) {
      for(int i = -4; i <= 4; ++i) {
         for(int j = -4; j <= 4; ++j) {
            for(int k = -4; k <= 4; ++k) {
               BlockPos blockPos2 = new BlockPos(blockPos.getX() + i, blockPos.getY() + j, blockPos.getZ() + k);
               if (mc.world.getBlockState(blockPos2).getBlock().equals(Blocks.AIR)) {
                  mc.playerController.processRightClickBlock(mc.player, mc.world, blockPos2, EnumFacing.DOWN, new Vec3d(0.5, 0.5, 0.5), EnumHand.MAIN_HAND);
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onBreak(BreakEvent breakEvent) {
      if (!fullNullCheck() && this.noGhost.getValue()) {
         if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock)) {
            this.removeGlitchBlocks(mc.player.getPosition());
         }
      }
   }

   @Override
   public void onUpdate() {
      if (mc.player.inventory.currentItem != this.lastHotbarSlot && mc.player.inventory.currentItem != this.currentTargetSlot) {
         this.lastHotbarSlot = mc.player.inventory.currentItem;
      }

      if (!mc.gameSettings.keyBindAttack.isKeyDown() && this.switched) {
         this.equip(this.lastHotbarSlot, false);
      }
   }
}
