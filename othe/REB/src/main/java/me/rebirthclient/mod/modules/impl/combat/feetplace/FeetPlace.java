//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat.feetplace;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Color;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.MovementUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.movement.AutoCenter;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class FeetPlace extends Module {
   public static FeetPlace INSTANCE = new FeetPlace();
   public final Setting<Boolean> enableInHole = this.add(new Setting<>("EnableInHole", false));
   final Timer timer = new Timer();
   public final Setting<Integer> delay = this.add(new Setting<>("Delay", 50, 0, 500));
   private final Setting<Integer> multiPlace = this.add(new Setting<>("MultiPlace", 1, 1, 8));
   private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
   private final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
   private final Setting<Boolean> breakCrystal = this.add(new Setting<>("BreakCrystal", true).setParent());
   public final Setting<Float> safeHealth = this.add(new Setting<>("SafeHealth", 16.0F, 0.0F, 36.0F, v -> this.breakCrystal.isOpen()));
   private final Setting<Boolean> eatingPause = this.add(new Setting<>("EatingPause", true, v -> this.breakCrystal.isOpen()));
   private final Setting<Boolean> center = this.add(new Setting<>("Center", true));
   public final Setting<Boolean> extend = this.add(new Setting<>("Extend", true));
   public final Setting<Boolean> onlyGround = this.add(new Setting<>("OnlyGround", true));
   private final Setting<Boolean> moveDisable = this.add(new Setting<>("MoveDisable", true).setParent());
   private final Setting<Boolean> strictDisable = this.add(new Setting<>("StrictDisable", false, v -> this.moveDisable.isOpen()));
   private final Setting<Boolean> isMoving = this.add(new Setting<>("isMoving", true, v -> this.moveDisable.isOpen()));
   private final Setting<Boolean> jumpDisable = this.add(new Setting<>("JumpDisable", true).setParent());
   private final Setting<Boolean> inMoving = this.add(new Setting<>("inMoving", true, v -> this.jumpDisable.isOpen()));
   public final Setting<Boolean> render = this.add(new Setting<>("Render", true));
   public final Setting<Boolean> depth = this.add(new Setting<>("Depth", true, v -> this.render.getValue()));
   public final Setting<Boolean> box = this.add(new Setting<>("Box", true, v -> this.render.getValue()));
   public final Setting<Boolean> outline = this.add(new Setting<>("Outline", false, v -> this.render.getValue()));
   public final Setting<Float> lineWidth = this.add(new Setting<>("LineWidth", 3.0F, 0.1F, 3.0F, v -> this.render.getValue()));
   public final Setting<Integer> fadeTime = this.add(new Setting<>("FadeTime", 500, 0, 5000, v -> this.render.getValue()));
   public final Setting<Color> color = this.add(new Setting<>("Color", new Color(255, 255, 255, 100), v -> this.render.getValue()));
   public final Setting<Boolean> sync = this.add(new Setting<>("Sync", true, v -> this.render.getValue()));
   public final Setting<Boolean> moveReset = this.add(new Setting<>("MoveReset", true, v -> this.render.getValue()));
   double startX = 0.0;
   double startY = 0.0;
   double startZ = 0.0;
   int progress = 0;
   BlockPos startPos = null;

   public FeetPlace() {
      super("FeetPlace", "Surrounds you with Obsidian", Category.COMBAT);
      INSTANCE = this;
   }

   static boolean checkSelf(BlockPos pos) {
      Entity test = null;

      for(Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
         if (entity == mc.player && test == null) {
            test = entity;
         }
      }

      return test != null;
   }

   @Override
   public void onEnable() {
      this.startPos = EntityUtil.getPlayerPos();
      this.startX = mc.player.posX;
      this.startY = mc.player.posY;
      this.startZ = mc.player.posZ;
      if (this.center.getValue() && InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN) != -1) {
         AutoCenter.INSTANCE.enable();
      }
   }

   @Override
   public void onTick() {
      if (this.timer.passedMs((long)this.delay.getValue().intValue())) {
         this.progress = 0;
         BlockPos pos = EntityUtil.getPlayerPos();
         if (this.startPos != null
            && (
               EntityUtil.getPlayerPos().equals(this.startPos)
                  || !this.moveDisable.getValue()
                  || !this.strictDisable.getValue()
                  || this.isMoving.getValue() && !MovementUtil.isMoving()
            )
            && (
               !(mc.player.getDistance(this.startX, this.startY, this.startZ) > 1.3)
                  || !this.moveDisable.getValue()
                  || this.strictDisable.getValue()
                  || this.isMoving.getValue() && !MovementUtil.isMoving()
            )
            && (
               !this.jumpDisable.getValue()
                  || !(this.startY - mc.player.posY > 0.5) && !(this.startY - mc.player.posY < -0.5)
                  || this.inMoving.getValue() && !MovementUtil.isMoving()
            )) {
            if (InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN) == -1) {
               this.sendMessageWithID(ChatFormatting.RED + "Obsidian?", this.hashCode() + 1);
               this.disable();
            } else if (!this.onlyGround.getValue() || mc.player.onGround) {
               for(EnumFacing i : EnumFacing.VALUES) {
                  if (i != EnumFacing.UP && !this.isGod(pos.offset(i), i)) {
                     BlockPos offsetPos = pos.offset(i);
                     if (BlockUtil.canPlaceEnum(offsetPos)) {
                        this.placeBlock(offsetPos);
                     } else if (BlockUtil.canReplace(offsetPos)) {
                        this.placeBlock(offsetPos.down());
                     }

                     if (checkSelf(offsetPos) && this.extend.getValue()) {
                        for(EnumFacing i2 : EnumFacing.VALUES) {
                           if (i2 != EnumFacing.UP) {
                              BlockPos offsetPos2 = offsetPos.offset(i2);
                              if (checkSelf(offsetPos2)) {
                                 for(EnumFacing i3 : EnumFacing.VALUES) {
                                    if (i3 != EnumFacing.UP) {
                                       this.placeBlock(offsetPos2);
                                       BlockPos offsetPos3 = offsetPos2.offset(i3);
                                       this.placeBlock(BlockUtil.canPlaceEnum(offsetPos3) ? offsetPos3 : offsetPos3.down());
                                    }
                                 }
                              }

                              this.placeBlock(BlockUtil.canPlaceEnum(offsetPos2) ? offsetPos2 : offsetPos2.down());
                           }
                        }
                     }
                  }
               }
            }
         } else {
            this.disable();
         }
      }
   }

   public boolean isGod(BlockPos pos, EnumFacing facing) {
      if (mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK) {
         return true;
      } else {
         for(EnumFacing i : EnumFacing.VALUES) {
            if (i != facing.getOpposite() && mc.world.getBlockState(pos.offset(i)).getBlock() != Blocks.BEDROCK) {
               return false;
            }
         }

         return true;
      }
   }

   private void placeBlock(BlockPos pos) {
      if (BlockUtil.canPlace3(pos)) {
         if (this.breakCrystal.getValue() && EntityUtil.getHealth(mc.player) >= this.safeHealth.getValue() || BlockUtil.canPlace(pos)) {
            if (this.progress < this.multiPlace.getValue()) {
               int old = mc.player.inventory.currentItem;
               if (InventoryUtil.findHotbarClass(BlockObsidian.class) != -1) {
                  if (this.breakCrystal.getValue() && EntityUtil.getHealth(mc.player) >= this.safeHealth.getValue()) {
                     CombatUtil.attackCrystal(pos, this.rotate.getValue(), this.eatingPause.getValue());
                  }

                  InventoryUtil.doSwap(InventoryUtil.findHotbarClass(BlockObsidian.class));
                  BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), !this.render.getValue());
                  InventoryUtil.doSwap(old);
                  ++this.progress;
                  this.timer.reset();
                  FeetPlaceRender.addBlock(pos);
               }
            }
         }
      }
   }
}
