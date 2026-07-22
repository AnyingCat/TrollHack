//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.movement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.exploit.Blink;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.Timer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HoleSnap extends Module {
   public static final List<BlockPos> holeBlocks = Arrays.asList(
      new BlockPos(0, -1, 0), new BlockPos(0, 0, -1), new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0), new BlockPos(0, 0, 1)
   );
   private static final BlockPos[] surroundOffset = new BlockPos[]{
      new BlockPos(0, -1, 0), new BlockPos(0, 0, -1), new BlockPos(1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(-1, 0, 0)
   };
   public static HoleSnap INSTANCE;
   private final Setting<Boolean> blink = this.add(new Setting<>("Blink", false));
   private final Setting<Integer> range = this.add(new Setting<>("Range", 5, 1, 50));
   private final Setting<Float> timer = this.add(new Setting<>("Timer", 1.6F, 1.0F, 8.0F));
   private final Setting<Integer> timeoutTicks = this.add(new Setting<>("TimeOutTicks", 10, 0, 30));
   boolean resetMove = false;
   private BlockPos holePos;
   private int stuckTicks;
   private int enabledTicks;

   public HoleSnap() {
      super("HoleSnap", "IQ", Category.MOVEMENT);
      INSTANCE = this;
   }

   public static boolean is2HoleB(BlockPos pos) {
      return is2Hole(pos) != null;
   }

   public static BlockPos is2Hole(BlockPos pos) {
      if (isHole(pos)) {
         return null;
      } else {
         BlockPos blockpos2 = null;
         int size = 0;
         int size2 = 0;
         if (mc.world.getBlockState(pos).getBlock() != Blocks.AIR) {
            return null;
         } else {
            for(BlockPos bPos : holeBlocks) {
               if (mc.world.getBlockState(pos.add(bPos)).getBlock() == Blocks.AIR
                  && !pos.add(bPos).equals(new BlockPos(bPos.getX(), bPos.getY() - 1, bPos.getZ()))) {
                  blockpos2 = pos.add(bPos);
                  ++size;
               }
            }

            if (size == 1) {
               for(BlockPos bPoss : holeBlocks) {
                  if (mc.world.getBlockState(pos.add(bPoss)).getBlock() == Blocks.BEDROCK
                     || mc.world.getBlockState(pos.add(bPoss)).getBlock() == Blocks.OBSIDIAN) {
                     ++size2;
                  }
               }

               for(BlockPos bPoss : holeBlocks) {
                  if (mc.world.getBlockState(blockpos2.add(bPoss)).getBlock() == Blocks.BEDROCK
                     || mc.world.getBlockState(blockpos2.add(bPoss)).getBlock() == Blocks.OBSIDIAN) {
                     ++size2;
                  }
               }
            }

            return size2 == 8 ? blockpos2 : null;
         }
      }
   }

   public static boolean isHole(BlockPos blockPos) {
      return getBlockResistance(blockPos.add(0, 1, 0)) == HoleSnap.BlockResistance.Blank
         && getBlockResistance(blockPos.add(0, 0, 0)) == HoleSnap.BlockResistance.Blank
         && getBlockResistance(blockPos.add(0, 2, 0)) == HoleSnap.BlockResistance.Blank
         && (
            getBlockResistance(blockPos.add(0, 0, -1)) == HoleSnap.BlockResistance.Resistant
               || getBlockResistance(blockPos.add(0, 0, -1)) == HoleSnap.BlockResistance.Unbreakable
         )
         && (
            getBlockResistance(blockPos.add(1, 0, 0)) == HoleSnap.BlockResistance.Resistant
               || getBlockResistance(blockPos.add(1, 0, 0)) == HoleSnap.BlockResistance.Unbreakable
         )
         && (
            getBlockResistance(blockPos.add(-1, 0, 0)) == HoleSnap.BlockResistance.Resistant
               || getBlockResistance(blockPos.add(-1, 0, 0)) == HoleSnap.BlockResistance.Unbreakable
         )
         && (
            getBlockResistance(blockPos.add(0, 0, 1)) == HoleSnap.BlockResistance.Resistant
               || getBlockResistance(blockPos.add(0, 0, 1)) == HoleSnap.BlockResistance.Unbreakable
         )
         && getBlockResistance(blockPos.add(0.5, 0.5, 0.5)) == HoleSnap.BlockResistance.Blank
         && (
            getBlockResistance(blockPos.add(0, -1, 0)) == HoleSnap.BlockResistance.Resistant
               || getBlockResistance(blockPos.add(0, -1, 0)) == HoleSnap.BlockResistance.Unbreakable
         );
   }

   public static HoleSnap.BlockResistance getBlockResistance(BlockPos block) {
      if (mc.world.isAirBlock(block)) {
         return HoleSnap.BlockResistance.Blank;
      } else if (mc.world.getBlockState(block).getBlock().getBlockHardness(mc.world.getBlockState(block), mc.world, block) != -1.0F
         && !mc.world.getBlockState(block).getBlock().equals(Blocks.OBSIDIAN)
         && !mc.world.getBlockState(block).getBlock().equals(Blocks.ANVIL)
         && !mc.world.getBlockState(block).getBlock().equals(Blocks.ENCHANTING_TABLE)
         && !mc.world.getBlockState(block).getBlock().equals(Blocks.ENDER_CHEST)) {
         return HoleSnap.BlockResistance.Breakable;
      } else if (mc.world.getBlockState(block).getBlock().equals(Blocks.OBSIDIAN)
         || mc.world.getBlockState(block).getBlock().equals(Blocks.ANVIL)
         || mc.world.getBlockState(block).getBlock().equals(Blocks.ENCHANTING_TABLE)
         || mc.world.getBlockState(block).getBlock().equals(Blocks.ENDER_CHEST)) {
         return HoleSnap.BlockResistance.Resistant;
      } else {
         return mc.world.getBlockState(block).getBlock().equals(Blocks.BEDROCK) ? HoleSnap.BlockResistance.Unbreakable : null;
      }
   }

   @Override
   public void onEnable() {
      if (this.blink.getValue() && Blink.INSTANCE.isOff()) {
         Blink.INSTANCE.enable();
      }

      this.resetMove = false;
   }

   @Override
   public void onDisable() {
      if (this.blink.getValue() && Blink.INSTANCE.isOn()) {
         Blink.INSTANCE.disable();
      }

      if (this.resetMove) {
         mc.player.motionX = 0.0;
         mc.player.motionZ = 0.0;
      }

      this.holePos = null;
      this.stuckTicks = 0;
      this.enabledTicks = 0;
      mc.timer.tickLength = 50.0F;
   }

   public double getSpeed(Entity entity) {
      return Math.hypot(entity.motionX, entity.motionZ);
   }

   private boolean isFlying(EntityPlayer player) {
      return player.isElytraFlying() || player.capabilities.isFlying;
   }

   @SubscribeEvent
   public void onReceivePacket(PacketEvent.Receive event) {
      if (event.getPacket() instanceof SPacketPlayerPosLook) {
         this.disable();
      }
   }

   @SubscribeEvent
   public void onInput(InputUpdateEvent event) {
      if (event.getMovementInput() instanceof MovementInputFromOptions && this.holePos != null) {
         MovementInput movementInput = event.getMovementInput();
         this.resetMove(movementInput);
      }
   }

   private void resetMove(MovementInput movementInput) {
      movementInput.moveForward = 0.0F;
      movementInput.moveStrafe = 0.0F;
      movementInput.forwardKeyDown = false;
      movementInput.backKeyDown = false;
      movementInput.leftKeyDown = false;
      movementInput.rightKeyDown = false;
   }

   private boolean isCentered(Entity entity, BlockPos pos) {
      double d = (double)pos.getX() + 0.31;
      double d2 = (double)pos.getX() + 0.69;
      double d3 = entity.posX;
      if (d > d3) {
         return false;
      } else if (d3 > d2) {
         return false;
      } else {
         d = (double)pos.getZ() + 0.31;
         d2 = (double)pos.getZ() + 0.69;
         d3 = entity.posZ;
         return d <= d3 && d3 <= d2;
      }
   }

   @Override
   public void onTick() {
      ++this.enabledTicks;
      if (this.enabledTicks > this.timeoutTicks.getValue() - 1) {
         this.disable();
      } else {
         if (mc.player.isEntityAlive()) {
            if (!this.isFlying(mc.player)) {
               EntityPlayerSP entityPlayerSP = mc.player;
               double currentSpeed = this.getSpeed(entityPlayerSP);
               if (this.shouldDisable(currentSpeed)) {
                  this.disable();
                  return;
               }

               BlockPos blockPos = this.getHole();
               if (blockPos != null) {
                  if (mc.player.posY - (double)blockPos.getY() < 0.5
                     && mc.player.posX - (double)blockPos.getX() <= 0.65
                     && mc.player.posX - (double)blockPos.getX() >= 0.35
                     && mc.player.posZ - (double)blockPos.getZ() <= 0.65
                     && mc.player.posZ - (double)blockPos.getZ() >= 0.35) {
                     this.disable();
                     return;
                  }

                  if (mc.player.posX - (double)blockPos.getX() <= 0.65
                     && mc.player.posX - (double)blockPos.getX() >= 0.35
                     && mc.player.posZ - (double)blockPos.getZ() <= 0.65
                     && mc.player.posZ - (double)blockPos.getZ() >= 0.35) {
                     mc.player.motionX = 0.0;
                     mc.player.motionZ = 0.0;
                     return;
                  }

                  this.resetMove = true;
                  Timer timer = mc.timer;
                  Float f = this.timer.getValue();
                  timer.tickLength = 50.0F / f;
                  if (!this.isCentered(mc.player, blockPos)) {
                     Vec3d playerPos = mc.player.getPositionVector();
                     Vec3d targetPos = new Vec3d(
                        (double)blockPos.getX() + 0.5, mc.player.posY, (double)blockPos.getZ() + 0.5
                     );
                     float rotation = this.getRotationTo(playerPos, targetPos).x;
                     float yawRad = rotation / 180.0F * (float) Math.PI;
                     double dist = playerPos.distanceTo(targetPos);
                     EntityPlayerSP entityPlayerSP2 = mc.player;
                     double baseSpeed = this.applySpeedPotionEffects(entityPlayerSP2);
                     double speed = mc.player.onGround ? baseSpeed : Math.max(currentSpeed + 0.02, baseSpeed);
                     double cappedSpeed = Math.min(speed, dist);
                     mc.player.motionX = (double)(-((float)Math.sin((double)yawRad))) * cappedSpeed;
                     mc.player.motionZ = (double)((float)Math.cos((double)yawRad)) * cappedSpeed;
                     if (mc.player.collidedHorizontally) {
                        ++this.stuckTicks;
                     } else {
                        this.stuckTicks = 0;
                     }
                  }
               } else {
                  this.disable();
               }
            } else {
               this.disable();
            }
         } else {
            this.disable();
         }
      }
   }

   public double applySpeedPotionEffects(EntityLivingBase entityLivingBase) {
      PotionEffect potionEffect = entityLivingBase.getActivePotionEffect(MobEffects.SPEED);
      double d;
      if (potionEffect == null) {
         d = 0.2873;
      } else {
         d = 0.2873 * this.getSpeedEffectMultiplier(entityLivingBase);
      }

      return d;
   }

   private double getSpeedEffectMultiplier(EntityLivingBase entityLivingBase) {
      PotionEffect potionEffect = entityLivingBase.getActivePotionEffect(MobEffects.SPEED);
      double d;
      if (potionEffect == null) {
         d = 1.0;
      } else {
         d = 1.0 + ((double)potionEffect.getAmplifier() + 1.0) * 0.2;
      }

      return d;
   }

   private Vec2f getRotationTo(Vec3d posFrom, Vec3d posTo) {
      Vec3d vec3d = posTo.subtract(posFrom);
      return this.getRotationFromVec(vec3d);
   }

   private Vec2f getRotationFromVec(Vec3d vec) {
      double d = vec.x;
      double d2 = vec.z;
      double xz = Math.hypot(d, d2);
      d2 = vec.z;
      double d3 = vec.x;
      double yaw = this.normalizeAngle(Math.toDegrees(Math.atan2(d2, d3)) - 90.0);
      double pitch = this.normalizeAngle(Math.toDegrees(-Math.atan2(vec.y, xz)));
      return new Vec2f((float)yaw, (float)pitch);
   }

   private double normalizeAngle(double angleIn) {
      double angle;
      if ((angle = angleIn % 360.0) >= 180.0) {
         angle -= 360.0;
      }

      if (angle < -180.0) {
         angle += 360.0;
      }

      return angle;
   }

   private boolean shouldDisable(double currentSpeed) {
      BlockPos blockPos = this.holePos;
      if (blockPos != null) {
         if (mc.player.posY < (double)blockPos.getY()) {
            return true;
         }

         if (is2HoleB(blockPos)) {
            BlockPos blockPos2 = mc.player.getPosition();
            if (this.toBlockPos(this.toVec3dCenter(blockPos2)).equals(blockPos)) {
               return true;
            }
         }
      }

      if (this.stuckTicks > 5 && currentSpeed < 0.1) {
         return true;
      } else if (currentSpeed >= 0.01) {
         return false;
      } else {
         EntityPlayerSP entityPlayerSP = mc.player;
         return this.checkHole(entityPlayerSP) != HoleSnap.HoleType.NONE;
      }
   }

   private BlockPos getHole() {
      if (mc.player.ticksExisted % 10 == 0) {
         EntityPlayerSP entityPlayerSP = mc.player;
         if (!this.getFlooredPosition(entityPlayerSP).equals(this.holePos)) {
            return this.findHole();
         }
      }

      BlockPos blockPos2 = this.holePos;
      return blockPos2 != null ? blockPos2 : this.findHole();
   }

   private BlockPos findHole() {
      HoleSnap.Pair<Double, BlockPos> closestHole = new HoleSnap.Pair<>(69.69, BlockPos.ORIGIN);
      EntityPlayerSP entityPlayerSP = mc.player;
      BlockPos playerPos = this.getFlooredPosition(entityPlayerSP);
      Integer ceilRange = this.range.getValue();
      BlockPos blockPos2 = playerPos.add(ceilRange, -1, ceilRange);
      BlockPos object = playerPos.add(-ceilRange, -1, -ceilRange);

      for(BlockPos posXZ : this.getBlockPositionsInArea(blockPos2, object)) {
         EntityPlayerSP entityPlayerSP2 = mc.player;
         double dist = this.distanceTo(entityPlayerSP2, posXZ);
         Integer n = this.range.getValue();
         if (dist <= Double.valueOf((double)n.intValue()) && !(dist > closestHole.getLeft())) {
            int n2 = 0;

            BlockPos pos;
            while(n2 < 6 && mc.world.isAirBlock((pos = posXZ.add(0, -(n2++), 0)).up())) {
               if (is2HoleB(pos) || this.checkHole(pos) != HoleSnap.HoleType.NONE) {
                  closestHole = new HoleSnap.Pair<>(dist, pos);
               }
            }
         }
      }

      BlockPos blockPos3;
      if (closestHole.getRight() != BlockPos.ORIGIN) {
         object = (BlockPos)closestHole.getRight();
         this.holePos = object;
         blockPos3 = object;
      } else {
         blockPos3 = null;
      }

      return blockPos3;
   }

   public BlockPos getFlooredPosition(Entity entity) {
      return new BlockPos((int)Math.floor(entity.posX), (int)Math.floor(entity.posY), (int)Math.floor(entity.posZ));
   }

   public HoleSnap.HoleType checkHole(Entity entity) {
      return this.checkHole(this.getFlooredPosition(entity));
   }

   public HoleSnap.HoleType checkHole(BlockPos pos) {
      if (mc.world.isAirBlock(pos)
         && mc.world.isAirBlock(pos.up())
         && mc.world.isAirBlock(pos.up().up())) {
         HoleSnap.HoleType type = HoleSnap.HoleType.BEDROCK;

         for(BlockPos offset : surroundOffset) {
            Block block = mc.world.getBlockState(pos.add(offset)).getBlock();
            if (!this.checkBlock(block)) {
               type = HoleSnap.HoleType.NONE;
               break;
            }
         }

         return type;
      } else {
         return HoleSnap.HoleType.NONE;
      }
   }

   private boolean checkBlock(Block block) {
      return block == Blocks.BEDROCK || block == Blocks.OBSIDIAN || block == Blocks.ENDER_CHEST || block == Blocks.ANVIL;
   }

   public double distanceTo(Entity entity, Vec3i vec3i) {
      double xDiff = (double)vec3i.getX() + 0.5 - entity.posX;
      double yDiff = (double)vec3i.getY() + 0.5 - entity.posY;
      double zDiff = (double)vec3i.getZ() + 0.5 - entity.posZ;
      return Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
   }

   public BlockPos toBlockPos(Vec3d vec) {
      int n = (int)Math.floor(vec.x);
      int n2 = (int)Math.floor(vec.y);
      return new BlockPos(n, n2, (int)Math.floor(vec.z));
   }

   public Vec3d toVec3dCenter(Vec3i vec3i) {
      return this.toVec3dCenter(vec3i, 0.0, 0.0, 0.0);
   }

   public Vec3d toVec3dCenter(Vec3i vec3i, double xOffset, double yOffset, double zOffset) {
      return new Vec3d(
         (double)vec3i.getX() + 0.5 + xOffset, (double)vec3i.getY() + 0.5 + yOffset, (double)vec3i.getZ() + 0.5 + zOffset
      );
   }

   public List<BlockPos> getBlockPositionsInArea(BlockPos pos1, BlockPos pos2) {
      int minX = Math.min(pos1.getX(), pos2.getX());
      int maxX = Math.max(pos1.getX(), pos2.getX());
      int minY = Math.min(pos1.getY(), pos2.getY());
      int maxY = Math.max(pos1.getY(), pos2.getY());
      int minZ = Math.min(pos1.getZ(), pos2.getZ());
      int maxZ = Math.max(pos1.getZ(), pos2.getZ());
      return this.getBlockPos(minX, maxX, minY, maxY, minZ, maxZ);
   }

   private List<BlockPos> getBlockPos(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
      ArrayList<BlockPos> returnList = new ArrayList();
      int n = minX;
      int x;
      if (minX <= maxX) {
         do {
            x = n++;
            int n2 = minZ;
            int z;
            if (minZ <= maxZ) {
               do {
                  z = n2++;
                  int n3 = minY;
                  int y;
                  if (minY <= maxY) {
                     do {
                        y = n3++;
                        returnList.add(new BlockPos(x, y, z));
                     } while(y != maxY);
                  }
               } while(z != maxZ);
            }
         } while(x != maxX);
      }

      return returnList;
   }

   public static enum BlockResistance {
      Blank,
      Breakable,
      Resistant,
      Unbreakable;
   }

   public static enum HoleType {
      NONE,
      OBBY,
      BEDROCK;
   }

   public static class Pair<L, R> {
      L left;
      R right;

      public Pair(L l, R r) {
         this.left = l;
         this.right = r;
      }

      public L getLeft() {
         return this.left;
      }

      public HoleSnap.Pair<L, R> setLeft(L left) {
         this.left = left;
         return this;
      }

      public R getRight() {
         return this.right;
      }

      public HoleSnap.Pair<L, R> setRight(R right) {
         this.right = right;
         return this;
      }
   }
}
