//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HoleFiller extends Module {
   private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", false));
   private final Setting<Boolean> packet = this.add(new Setting<>("Packet", false));
   private final Setting<Boolean> webs = this.add(new Setting<>("Webs", false));
   private final Setting<Boolean> autoDisable = this.add(new Setting<>("AutoDisable", true));
   private final Setting<Double> range = this.add(new Setting<>("Radius", 4.0, 0.0, 6));
   private final Setting<Boolean> smart = this.add(new Setting<>("Smart", false).setParent());
   private final Setting<HoleFiller.Logic> logic = this.add(new Setting<>("Logic", HoleFiller.Logic.PLAYER, v -> this.smart.isOpen()));
   private final Setting<Integer> smartRange = this.add(new Setting<>("EnemyRange", 4, 0, 6, v -> this.smart.isOpen()));
   private final Setting<Boolean> render = this.add(new Setting<>("Render", true).setParent());
   private final Setting<Boolean> box = this.add(new Setting<>("Box", true, v -> this.render.isOpen()));
   private final Setting<Boolean> line = this.add(new Setting<>("Line", true, v -> this.render.isOpen()));
   private final Map<BlockPos, Long> renderBlocks = new ConcurrentHashMap<>();
   private final Timer renderTimer = new Timer();
   private EntityPlayer closestTarget;

   public HoleFiller() {
      super("HoleFiller", "Fills all safe spots in radius", Category.COMBAT);
   }

   @Override
   public void onDisable() {
      this.closestTarget = null;
      Managers.ROTATIONS.resetRotationsPacket();
   }

   @Override
   public String getInfo() {
      return this.smart.getValue() ? Managers.TEXT.normalizeCases(this.logic.getValue()) : "Normal";
   }

   @Override
   public void onRender3D(Render3DEvent event) {
      if (this.render.getValue()) {
         this.renderTimer.reset();
         this.renderBlocks
            .forEach(
               (pos, time) -> {
                  int lineA = 255;
                  int fillA = 80;
                  if (System.currentTimeMillis() - time > 400L) {
                     this.renderTimer.reset();
                     this.renderBlocks.remove(pos);
                  } else {
                     long endTime = System.currentTimeMillis() - time - 100L;
                     double normal = MathUtil.normalize((double)endTime, 0.0, 500.0);
                     normal = MathHelper.clamp(normal, 0.0, 1.0);
                     normal = -normal + 1.0;
                     int firstAl = (int)(normal * (double)lineA);
                     int secondAl = (int)(normal * (double)fillA);
                     RenderUtil.drawBoxESP(
                        new BlockPos(pos),
                        Managers.COLORS.getCurrent(),
                        true,
                        new Color(255, 255, 255, firstAl),
                        0.7F,
                        this.line.getValue(),
                        this.box.getValue(),
                        secondAl,
                        true,
                        0.0
                     );
                  }
               }
            );
      }
   }

   @Override
   public void onUpdate() {
      if (mc.world != null) {
         if (this.smart.getValue()) {
            this.findClosestTarget();
         }

         List<BlockPos> blocks = this.getPlacePositions();
         BlockPos q = null;
         int obbySlot = InventoryUtil.findHotbarClass(BlockObsidian.class);
         int eChestSlot = InventoryUtil.findHotbarClass(BlockEnderChest.class);
         int webSlot = InventoryUtil.findHotbarClass(BlockWeb.class);
         if (this.webs.getValue() || obbySlot != -1 || eChestSlot != -1) {
            if (!this.webs.getValue() || webSlot != -1 || obbySlot != -1 || eChestSlot != -1) {
               int originalSlot = mc.player.inventory.currentItem;

               for(BlockPos blockPos : blocks) {
                  if (mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(blockPos)).isEmpty()) {
                     if (this.smart.getValue() && this.isInRange(blockPos)) {
                        q = blockPos;
                     } else if (this.smart.getValue()
                        && this.isInRange(blockPos)
                        && this.logic.getValue() == HoleFiller.Logic.HOLE
                        && this.closestTarget.getDistanceSq(blockPos) <= (double)this.smartRange.getValue().intValue()) {
                        q = blockPos;
                     } else {
                        q = blockPos;
                     }
                  }
               }

               if (q != null && mc.player.onGround) {
                  InventoryUtil.doSwap(
                     this.webs.getValue() ? (webSlot == -1 ? (obbySlot == -1 ? eChestSlot : obbySlot) : webSlot) : (obbySlot == -1 ? eChestSlot : obbySlot)
                  );
                  this.renderBlocks.put(q, System.currentTimeMillis());
                  Managers.INTERACTIONS.placeBlock(q, this.rotate.getValue(), this.packet.getValue(), false);
                  if (mc.player.inventory.currentItem != originalSlot) {
                     InventoryUtil.doSwap(originalSlot);
                  }

                  mc.player.swingArm(EnumHand.MAIN_HAND);
                  mc.player.inventory.currentItem = originalSlot;
               }

               if (q == null && this.autoDisable.getValue() && !this.smart.getValue()) {
                  this.disable();
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onReceivePacket(PacketEvent.Receive event) {
      if (!event.isCanceled()) {
         if (event.getPacket() instanceof SPacketBlockChange && this.renderBlocks.containsKey(((SPacketBlockChange)event.getPacket()).getBlockPosition())) {
            this.renderTimer.reset();
            if (((SPacketBlockChange)event.getPacket()).getBlockState().getBlock() != Blocks.AIR && this.renderTimer.passedMs(400L)) {
               this.renderBlocks.remove(((SPacketBlockChange)event.getPacket()).getBlockPosition());
            }
         }
      }
   }

   private boolean isHole(BlockPos pos) {
      BlockPos boost = pos.add(0, 1, 0);
      BlockPos boost2 = pos.add(0, 0, 0);
      BlockPos boost3 = pos.add(0, 0, -1);
      BlockPos boost4 = pos.add(1, 0, 0);
      BlockPos boost5 = pos.add(-1, 0, 0);
      BlockPos boost6 = pos.add(0, 0, 1);
      BlockPos boost7 = pos.add(0, 2, 0);
      BlockPos boost8 = pos.add(0.5, 0.5, 0.5);
      BlockPos boost9 = pos.add(0, -1, 0);
      return mc.world.getBlockState(boost).getBlock() == Blocks.AIR
         && mc.world.getBlockState(boost2).getBlock() == Blocks.AIR
         && mc.world.getBlockState(boost7).getBlock() == Blocks.AIR
         && (
            mc.world.getBlockState(boost3).getBlock() == Blocks.OBSIDIAN
               || mc.world.getBlockState(boost3).getBlock() == Blocks.BEDROCK
         )
         && (
            mc.world.getBlockState(boost4).getBlock() == Blocks.OBSIDIAN
               || mc.world.getBlockState(boost4).getBlock() == Blocks.BEDROCK
         )
         && (
            mc.world.getBlockState(boost5).getBlock() == Blocks.OBSIDIAN
               || mc.world.getBlockState(boost5).getBlock() == Blocks.BEDROCK
         )
         && (
            mc.world.getBlockState(boost6).getBlock() == Blocks.OBSIDIAN
               || mc.world.getBlockState(boost6).getBlock() == Blocks.BEDROCK
         )
         && mc.world.getBlockState(boost8).getBlock() == Blocks.AIR
         && (
            mc.world.getBlockState(boost9).getBlock() == Blocks.OBSIDIAN
               || mc.world.getBlockState(boost9).getBlock() == Blocks.BEDROCK
         );
   }

   private BlockPos getPlayerPos() {
      return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
   }

   private BlockPos getClosestTargetPos(EntityPlayer target) {
      return new BlockPos(Math.floor(target.posX), Math.floor(target.posY), Math.floor(target.posZ));
   }

   private void findClosestTarget() {
      List<EntityPlayer> playerList = mc.world.playerEntities;
      this.closestTarget = null;

      for(EntityPlayer target : playerList) {
         if (target != mc.player
            && !Managers.FRIENDS.isFriend(target.getName())
            && EntityUtil.isLiving(target)
            && !(target.getHealth() <= 0.0F)) {
            if (this.closestTarget == null) {
               this.closestTarget = target;
            } else if (mc.player.getDistance(target) < mc.player.getDistance(this.closestTarget)) {
               this.closestTarget = target;
            }
         }
      }
   }

   private boolean isInRange(BlockPos blockPos) {
      NonNullList positions = NonNullList.create();
      positions.addAll(this.getSphere(this.getPlayerPos(), this.range.getValue().floatValue()).stream().filter(this::isHole).collect(Collectors.toList()));
      return positions.contains(blockPos);
   }

   private List<BlockPos> getPlacePositions() {
      NonNullList positions = NonNullList.create();
      if (this.smart.getValue() && this.closestTarget != null) {
         positions.addAll(
            this.getSphere(this.getClosestTargetPos(this.closestTarget), this.smartRange.getValue().floatValue())
               .stream()
               .filter(this::isHole)
               .filter(this::isInRange)
               .collect(Collectors.toList())
         );
      } else if (!this.smart.getValue()) {
         positions.addAll(this.getSphere(this.getPlayerPos(), this.range.getValue().floatValue()).stream().filter(this::isHole).collect(Collectors.toList()));
      }

      return positions;
   }

   private List<BlockPos> getSphere(BlockPos loc, float r) {
      ArrayList<BlockPos> circleBlocks = new ArrayList();
      int cx = loc.getX();
      int cy = loc.getY();
      int cz = loc.getZ();

      for(int x = cx - (int)r; (float)x <= (float)cx + r; ++x) {
         for(int z = cz - (int)r; (float)z <= (float)cz + r; ++z) {
            int y = cy - (int)r;

            while(true) {
               float f = (float)y;
               float f2 = (float)cy + r;
               if (!(f < f2)) {
                  break;
               }

               double dist = (double)((cx - x) * (cx - x) + (cz - z) * (cz - z) + (cy - y) * (cy - y));
               if (dist < (double)(r * r)) {
                  BlockPos l = new BlockPos(x, y, z);
                  circleBlocks.add(l);
               }

               ++y;
            }
         }
      }

      return circleBlocks;
   }

   private static enum Logic {
      PLAYER,
      HOLE;
   }
}
