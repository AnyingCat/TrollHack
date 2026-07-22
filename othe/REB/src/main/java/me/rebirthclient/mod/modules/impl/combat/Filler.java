//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat;

import java.util.Iterator;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class Filler extends Module {
   private final Timer timer = new Timer();
   private final Setting<Integer> multiPlace = this.add(new Setting<>("MultiPlace", 1, 0, 8));
   private final Setting<Integer> delay = this.add(new Setting<>("Delay", 50, 0, 500));
   private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
   private final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
   private final Setting<Boolean> holeCheck = this.add(new Setting<>("HoleCheck", true).setParent());
   private final Setting<Float> holeRange = this.add(new Setting<>("HoleRange", 2.0F, 0.5F, 3.0F, v -> this.holeCheck.isOpen()));
   private final Setting<Integer> check = this.add(new Setting<>("Check", 3, 2, 4, v -> this.holeCheck.isOpen()));
   private final Setting<Boolean> anyBlock = this.add(new Setting<>("anyBlock", true, v -> this.holeCheck.isOpen()));
   private final Setting<Boolean> onlyCanStand = this.add(new Setting<>("OnlyCanStand", false, v -> this.holeCheck.isOpen()));
   private final Setting<Boolean> allowUp = this.add(new Setting<>("AllowUp", false, v -> this.holeCheck.isOpen()));
   private final Setting<Float> minSelfRange = this.add(new Setting<>("MinSelfRange", 2.0F, 1.0F, 4.0F, v -> this.holeCheck.isOpen()));
   private final Setting<Boolean> raytrace = this.add(new Setting<>("Raytrace", false));
   private final Setting<Boolean> web = this.add(new Setting<>("Web", true));
   private final Setting<Boolean> noInWeb = this.add(new Setting<>("NoInWeb", false));
   private final Setting<Float> range = this.add(new Setting<>("Range", 5.0F, 1.0F, 6.0F));
   private final Setting<Float> placeRange = this.add(new Setting<>("PlaceRange", 4.0F, 1.0F, 6.0F));
   private final Setting<Double> maxSelfSpeed = this.add(new Setting<>("MaxSelfSpeed", 20.0, 1.0, 30.0));
   private final Setting<Double> minTargetSpeed = this.add(new Setting<>("MinTargetSpeed", 6.0, 0.0, 20.0));
   private final Setting<Boolean> selfGround = this.add(new Setting<>("SelfGround", false));
   public EntityPlayer target;
   int progress = 0;

   public Filler() {
      super("Filler", "Fills all safe spots in radius", Category.COMBAT);
   }

   @Override
   public void onUpdate() {
      if (this.timer.passedMs((long)this.delay.getValue().intValue())) {
         this.progress = 0;
         if (this.selfGround.getValue() && !mc.player.onGround) {
            this.target = null;
         } else if (Managers.SPEED.getPlayerSpeed(mc.player) > this.maxSelfSpeed.getValue()) {
            this.target = null;
         } else {
            boolean found = false;

            label134:
            for(EntityPlayer player : mc.world.playerEntities) {
               if (this.progress >= this.multiPlace.getValue()) {
                  return;
               }

               if (!EntityUtil.invalid(player, (double)this.range.getValue().floatValue())
                  && (!AutoWeb.isInWeb(player) || !this.noInWeb.getValue())
                  && !(Managers.SPEED.getPlayerSpeed(player) < this.minTargetSpeed.getValue())) {
                  this.target = player;
                  found = true;
                  if (this.holeCheck.getValue()) {
                     Iterator var11 = BlockUtil.getBox(this.holeRange.getValue() + 2.0F, EntityUtil.getEntityPos(player).down()).iterator();

                     while(true) {
                        BlockPos pos;
                        while(true) {
                           if (!var11.hasNext()) {
                              continue label134;
                           }

                           pos = (BlockPos)var11.next();
                           if ((pos.getY() < EntityUtil.getEntityPos(player).getY() || this.allowUp.getValue())
                              && !pos.equals(EntityUtil.getEntityPos(player))) {
                              if (!this.allowUp.getValue()) {
                                 break;
                              }

                              boolean skip = false;

                              for(EnumFacing side : EnumFacing.values()) {
                                 if (side != EnumFacing.UP && side != EnumFacing.DOWN && pos.equals(EntityUtil.getEntityPos(player).offset(side))) {
                                    skip = true;
                                    break;
                                 }
                              }

                              if (!skip) {
                                 break;
                              }
                           }
                        }

                        if ((
                              !(
                                    player.getDistance((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5)
                                       > (double)this.holeRange.getValue().floatValue()
                                 )
                                 || !(
                                    player.getDistance(
                                          (double)pos.getX() + 0.5, (double)(pos.getY() + 1), (double)pos.getZ() + 0.5
                                       )
                                       > (double)this.holeRange.getValue().floatValue()
                                 )
                           )
                           && !(
                              mc.player
                                    .getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5)
                                 > (double)this.placeRange.getValue().floatValue()
                           )) {
                           this.placeBlock(pos);
                        }
                     }
                  } else {
                     BlockPos feet = new BlockPos(this.target.posX, this.target.posY + 0.5, this.target.posZ);
                     this.placeBlock(feet.down());

                     for(EnumFacing side : EnumFacing.values()) {
                        if (side != EnumFacing.UP && side != EnumFacing.DOWN) {
                           this.placeBlock(feet.offset(side).down());
                        }
                     }
                  }
               }
            }

            if (!found) {
               this.target = null;
            }
         }
      }
   }

   private void placeBlock(BlockPos pos) {
      if (this.progress < this.multiPlace.getValue()) {
         if (!(
            mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5)
               > (double)this.placeRange.getValue().floatValue()
         )) {
            if (!(
               mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5)
                  <= (double)this.minSelfRange.getValue().floatValue()
            )) {
               if (!this.holeCheck.getValue() || CombatUtil.isHole(pos, this.anyBlock.getValue(), this.check.getValue(), this.onlyCanStand.getValue())) {
                  if (this.canPlace(pos)) {
                     int old = mc.player.inventory.currentItem;
                     if (this.web.getValue() && InventoryUtil.findHotbarClass(BlockWeb.class) != -1) {
                        InventoryUtil.doSwap(InventoryUtil.findHotbarClass(BlockWeb.class));
                        BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
                        InventoryUtil.doSwap(old);
                     } else if (InventoryUtil.findHotbarClass(BlockObsidian.class) != -1) {
                        InventoryUtil.doSwap(InventoryUtil.findHotbarClass(BlockObsidian.class));
                        BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
                        InventoryUtil.doSwap(old);
                     }

                     this.timer.reset();
                     ++this.progress;
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

   private boolean canPlace(BlockPos pos) {
      if (!BlockUtil.canBlockFacing(pos)) {
         return false;
      } else if (!BlockUtil.canReplace(pos)) {
         return false;
      } else if (!this.strictPlaceCheck(pos)) {
         return false;
      } else if (this.web.getValue() && InventoryUtil.findHotbarBlock(Blocks.WEB) != -1) {
         return true;
      } else {
         return !BlockUtil.checkEntity(pos);
      }
   }

   private boolean strictPlaceCheck(BlockPos pos) {
      if (!CombatSetting.INSTANCE.strictPlace.getValue() && this.raytrace.getValue()) {
         return true;
      } else {
         for(EnumFacing side : BlockUtil.getPlacableFacings(pos, true, !this.raytrace.getValue() || CombatSetting.INSTANCE.checkRaytrace.getValue())) {
            if (BlockUtil.canClick(pos.offset(side))) {
               return true;
            }
         }

         return false;
      }
   }
}
