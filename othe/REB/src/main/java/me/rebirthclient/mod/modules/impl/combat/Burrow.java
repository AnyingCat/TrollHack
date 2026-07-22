//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Burrow extends Module {
   private final Setting<Boolean> placeDisable = this.add(new Setting<>("PlaceDisable", false));
   private final Setting<Boolean> wait = this.add(new Setting<>("Wait", true));
   private final Setting<Boolean> switchBypass = this.add(new Setting<>("SwitchBypass", false));
   private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
   private final Setting<Boolean> onlyGround = this.add(new Setting<>("OnlyGround", true).setParent());
   private final Setting<Boolean> airCheck = this.add(new Setting<>("AirCheck", true, v -> this.onlyGround.isOpen()));
   private final Setting<Boolean> aboveHead = this.add(new Setting<>("AboveHead", true).setParent());
   private final Setting<Boolean> center = this.add(new Setting<>("Center", false, v -> this.aboveHead.isOpen()));
   private final Setting<Boolean> test = this.add(new Setting<>("Test", false, v -> this.aboveHead.isOpen()));
   private final Setting<Boolean> breakCrystal = this.add(new Setting<>("BreakCrystal", true).setParent());
   public final Setting<Float> safeHealth = this.add(new Setting<>("SafeHealth", 16.0F, 0.0F, 36.0F, v -> this.breakCrystal.isOpen()));
   private final Setting<Integer> multiPlace = this.add(new Setting<>("MultiPlace", 1, 1, 4));
   private final Setting<Integer> timeOut = this.add(new Setting<>("TimeOut", 500, 0, 2000));
   private final Setting<Integer> delay = this.add(new Setting<>("delay", 300, 0, 1000));
   public final Setting<Burrow.LagMode> lagMode = this.add(new Setting<>("LagMode", Burrow.LagMode.China2B2T));
   private final Setting<Double> offsetX = this.add(new Setting<>("OffsetX", -7.0, -14.0, 14.0, v -> this.lagMode.getValue() == Burrow.LagMode.Custom));
   private final Setting<Double> offsetY = this.add(new Setting<>("OffsetY", -7.0, -14.0, 14.0, v -> this.lagMode.getValue() == Burrow.LagMode.Custom));
   private final Setting<Double> offsetZ = this.add(new Setting<>("OffsetZ", -7.0, -14.0, 14.0, v -> this.lagMode.getValue() == Burrow.LagMode.Custom));
   private final Setting<Boolean> debug = this.add(new Setting<>("Debug", false));
   int progress = 0;
   private final Timer timer = new Timer();
   private final Timer timedOut = new Timer();
   public static Burrow INSTANCE;
   private boolean shouldWait = false;

   public Burrow() {
      super("Burrow", "unknown", Category.COMBAT);
      INSTANCE = this;
   }

   private static boolean checkSelf(BlockPos pos) {
      Vec3d[] vec3dList = EntityUtil.getVarOffsets(0, 0, 0);

      for(Vec3d vec3d : vec3dList) {
         BlockPos position = new BlockPos(pos).add(vec3d.x, vec3d.y, vec3d.z);

         for(Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(position))) {
            if (entity == mc.player) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean isAir(BlockPos pos) {
      return mc.world.isAirBlock(pos);
   }

   private static boolean Trapped(BlockPos pos) {
      return !mc.world.isAirBlock(pos) && checkSelf(pos.down(2));
   }

   public static boolean canReplace(BlockPos pos) {
      return mc.world.getBlockState(pos).getMaterial().isReplaceable();
   }

   @Override
   public void onEnable() {
      this.timedOut.reset();
      this.shouldWait = this.wait.getValue();
   }

   @Override
   public void onDisable() {
      this.timer.reset();
      this.shouldWait = false;
   }

   @Override
   public void onUpdate() {
      this.progress = 0;
      int blockSlot;
      if (!this.switchBypass.getValue()) {
         if (InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)) != -1) {
            blockSlot = InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN));
         } else {
            blockSlot = InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST));
         }
      } else if (InventoryUtil.findItemInventorySlot(Item.getItemFromBlock(Blocks.OBSIDIAN), false, true) != -1) {
         blockSlot = InventoryUtil.findItemInventorySlot(Item.getItemFromBlock(Blocks.OBSIDIAN), false, true);
      } else {
         blockSlot = InventoryUtil.findItemInventorySlot(Item.getItemFromBlock(Blocks.ENDER_CHEST), false, true);
      }

      if (blockSlot == -1) {
         this.sendMessage(ChatFormatting.RED + "Obsidian/Ender Chest ?");
         this.disable();
      } else if (this.timedOut.passedMs((long)this.timeOut.getValue().intValue())) {
         this.disable();
      } else {
         BlockPos originalPos = EntityUtil.getPlayerPos();
         if (!this.canPlace(new BlockPos(mc.player.posX + 0.3, mc.player.posY + 0.5, mc.player.posZ + 0.3))
            && !this.canPlace(new BlockPos(mc.player.posX - 0.3, mc.player.posY + 0.5, mc.player.posZ + 0.3))
            && !this.canPlace(new BlockPos(mc.player.posX + 0.3, mc.player.posY + 0.5, mc.player.posZ - 0.3))
            && !this.canPlace(new BlockPos(mc.player.posX - 0.3, mc.player.posY + 0.5, mc.player.posZ - 0.3))) {
            if (this.debug.getValue()) {
               this.sendMessage("cant place");
            }

            if (!this.shouldWait) {
               this.disable();
            }
         } else if (!mc.player.isInLava() && !mc.player.isInWater() && !mc.player.isInWeb) {
            if (this.onlyGround.getValue()) {
               if (!mc.player.onGround) {
                  if (this.debug.getValue()) {
                     this.sendMessage("player not on ground");
                  }

                  return;
               }

               if (this.airCheck.getValue() && isAir(EntityUtil.getPlayerPos().down())) {
                  if (this.debug.getValue()) {
                     this.sendMessage("player in air");
                  }

                  return;
               }
            }

            if (this.timer.passedMs((long)this.delay.getValue().intValue())) {
               if (this.breakCrystal.getValue() && EntityUtil.getHealth(mc.player) >= this.safeHealth.getValue()) {
                  if (this.debug.getValue()) {
                     this.sendMessage("try break crystal");
                  }

                  CombatUtil.attackCrystal(originalPos, this.rotate.getValue(), false);
                  CombatUtil.attackCrystal(
                     new BlockPos(mc.player.posX + 0.3, mc.player.posY + 0.5, mc.player.posZ + 0.3),
                     this.rotate.getValue(),
                     false
                  );
                  CombatUtil.attackCrystal(
                     new BlockPos(mc.player.posX + 0.3, mc.player.posY + 0.5, mc.player.posZ - 0.3),
                     this.rotate.getValue(),
                     false
                  );
                  CombatUtil.attackCrystal(
                     new BlockPos(mc.player.posX - 0.3, mc.player.posY + 0.5, mc.player.posZ + 0.3),
                     this.rotate.getValue(),
                     false
                  );
                  CombatUtil.attackCrystal(
                     new BlockPos(mc.player.posX - 0.3, mc.player.posY + 0.5, mc.player.posZ - 0.3),
                     this.rotate.getValue(),
                     false
                  );
               }

               this.timer.reset();
               this.shouldWait = false;
               BlockPos headPos = EntityUtil.getPlayerPos().up(2);
               if (!Trapped(headPos)
                  && !Trapped(headPos.add(1, 0, 0))
                  && !Trapped(headPos.add(-1, 0, 0))
                  && !Trapped(headPos.add(0, 0, 1))
                  && !Trapped(headPos.add(0, 0, -1))
                  && !Trapped(headPos.add(1, 0, -1))
                  && !Trapped(headPos.add(-1, 0, -1))
                  && !Trapped(headPos.add(1, 0, 1))
                  && !Trapped(headPos.add(-1, 0, 1))) {
                  if (this.debug.getValue()) {
                     this.sendMessage("fake jump");
                  }

                  mc.player
                     .connection
                     .sendPacket(
                        new Position(
                           mc.player.posX, mc.player.posY + 0.4199999868869781, mc.player.posZ, false
                        )
                     );
                  mc.player
                     .connection
                     .sendPacket(
                        new Position(
                           mc.player.posX, mc.player.posY + 0.7531999805212017, mc.player.posZ, false
                        )
                     );
                  mc.player
                     .connection
                     .sendPacket(
                        new Position(
                           mc.player.posX, mc.player.posY + 0.9999957640154541, mc.player.posZ, false
                        )
                     );
                  mc.player
                     .connection
                     .sendPacket(
                        new Position(
                           mc.player.posX, mc.player.posY + 1.1661092609382138, mc.player.posZ, false
                        )
                     );
               } else {
                  if (!this.aboveHead.getValue()) {
                     if (!this.shouldWait) {
                        this.disable();
                     }

                     return;
                  }

                  boolean moved = false;
                  if (checkSelf(originalPos) && !canReplace(originalPos)) {
                     this.gotoPos(originalPos);
                     if (this.debug.getValue()) {
                        this.sendMessage(
                           "moved to center "
                              + ((double)originalPos.getX() + 0.5 - mc.player.posX)
                              + " "
                              + ((double)originalPos.getZ() + 0.5 - mc.player.posZ)
                        );
                     }
                  } else {
                     for(EnumFacing facing : EnumFacing.VALUES) {
                        if (facing != EnumFacing.UP && facing != EnumFacing.DOWN) {
                           BlockPos distance = originalPos.offset(facing);
                           if (checkSelf(distance) && !canReplace(distance)) {
                              this.gotoPos(distance);
                              moved = true;
                              if (this.debug.getValue()) {
                                 this.sendMessage(
                                    "moved to block "
                                       + ((double)distance.getX() + 0.5 - mc.player.posX)
                                       + " "
                                       + ((double)distance.getZ() + 0.5 - mc.player.posZ)
                                 );
                              }
                              break;
                           }
                        }
                     }

                     if (!moved) {
                        for(EnumFacing facing : EnumFacing.VALUES) {
                           if (facing != EnumFacing.UP && facing != EnumFacing.DOWN) {
                              BlockPos var11 = originalPos.offset(facing);
                              if (checkSelf(var11)) {
                                 this.gotoPos(var11);
                                 moved = true;
                                 if (this.debug.getValue()) {
                                    this.sendMessage(
                                       "moved to entity "
                                          + ((double)var11.getX() + 0.5 - mc.player.posX)
                                          + " "
                                          + ((double)var11.getZ() + 0.5 - mc.player.posZ)
                                    );
                                 }
                                 break;
                              }
                           }
                        }

                        if (!moved) {
                           if (!this.center.getValue()) {
                              if (!this.shouldWait) {
                                 this.disable();
                              }

                              return;
                           }

                           for(EnumFacing facing : EnumFacing.VALUES) {
                              if (facing != EnumFacing.UP && facing != EnumFacing.DOWN) {
                                 BlockPos var12 = originalPos.offset(facing);
                                 if (canReplace(var12) && canReplace(var12.up())) {
                                    this.gotoPos(var12);
                                    if (this.debug.getValue()) {
                                       this.sendMessage(
                                          "moved to air "
                                             + ((double)var12.getX() + 0.5 - mc.player.posX)
                                             + " "
                                             + ((double)var12.getZ() + 0.5 - mc.player.posZ)
                                       );
                                    }

                                    moved = true;
                                    break;
                                 }
                              }
                           }

                           if (!moved) {
                              if (!this.shouldWait) {
                                 this.disable();
                              }

                              return;
                           }
                        }
                     }
                  }
               }

               int oldSlot = mc.player.inventory.currentItem;
               if (!this.switchBypass.getValue()) {
                  InventoryUtil.doSwap(blockSlot);
               } else {
                  mc.playerController.windowClick(0, blockSlot, oldSlot, ClickType.SWAP, mc.player);
               }

               this.placeBlock(originalPos);
               this.placeBlock(new BlockPos(mc.player.posX + 0.3, mc.player.posY + 0.5, mc.player.posZ + 0.3));
               this.placeBlock(new BlockPos(mc.player.posX + 0.3, mc.player.posY + 0.5, mc.player.posZ - 0.3));
               this.placeBlock(new BlockPos(mc.player.posX - 0.3, mc.player.posY + 0.5, mc.player.posZ + 0.3));
               this.placeBlock(new BlockPos(mc.player.posX - 0.3, mc.player.posY + 0.5, mc.player.posZ - 0.3));
               if (!this.switchBypass.getValue()) {
                  InventoryUtil.doSwap(oldSlot);
               } else {
                  mc.playerController.windowClick(0, blockSlot, oldSlot, ClickType.SWAP, mc.player);
                  mc.player
                     .connection
                     .sendPacket(
                        new CPacketConfirmTransaction(
                           mc.player.inventoryContainer.windowId, mc.player.openContainer.getNextTransactionID(mc.player.inventory), true
                        )
                     );
               }

               if (this.lagMode.getValue() == Burrow.LagMode.China2B2T) {
                  double distance = 0.0;
                  BlockPos bestPos = null;

                  for(BlockPos pos : BlockUtil.getBox(6.0F)) {
                     if (this.canGoto(pos)
                        && !(
                           mc.player
                                 .getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5)
                              <= 3.0
                        )
                        && (
                           bestPos == null
                              || mc.player
                                    .getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5)
                                 < distance
                        )) {
                        bestPos = pos;
                        distance = mc.player
                           .getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
                     }
                  }

                  if (bestPos != null) {
                     mc.player
                        .connection
                        .sendPacket(
                           new Position((double)bestPos.getX() + 0.5, (double)bestPos.getY(), (double)bestPos.getZ() + 0.5, false)
                        );
                  }
               } else if (this.lagMode.getValue() == Burrow.LagMode.Custom) {
                  mc.player
                     .connection
                     .sendPacket(
                        new Position(
                           mc.player.posX + this.offsetX.getValue(),
                           mc.player.posY + this.offsetY.getValue(),
                           mc.player.posZ + this.offsetZ.getValue(),
                           false
                        )
                     );
               } else if (this.lagMode.getValue() == Burrow.LagMode.ToVoid) {
                  mc.player.connection.sendPacket(new Position(mc.player.posX, -7.0, mc.player.posZ, false));
               } else if (this.lagMode.getValue() == Burrow.LagMode.Strict) {
                  double distance = 0.0;
                  BlockPos bestPos = null;

                  for(int i = 0; i < 20; ++i) {
                     BlockPos pos = new BlockPos(
                        mc.player.posX, mc.player.posY + 0.5 + (double)i, mc.player.posZ
                     );
                     if (this.canGoto(pos)
                        && !(
                           mc.player
                                 .getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5)
                              <= 5.0
                        )
                        && (
                           bestPos == null
                              || mc.player
                                    .getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5)
                                 < distance
                        )) {
                        bestPos = pos;
                        distance = mc.player
                           .getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
                     }
                  }

                  if (bestPos != null) {
                     mc.player
                        .connection
                        .sendPacket(
                           new Position((double)bestPos.getX() + 0.5, (double)bestPos.getY(), (double)bestPos.getZ() + 0.5, false)
                        );
                  }
               } else if (this.lagMode.getValue() == Burrow.LagMode.Troll) {
                  mc.player
                     .connection
                     .sendPacket(
                        new Position(
                           mc.player.posX, mc.player.posY + 3.3400880035762786, mc.player.posZ, false
                        )
                     );
                  mc.player
                     .connection
                     .sendPacket(new Position(mc.player.posX, mc.player.posY - 1.0, mc.player.posZ, false));
               }

               if (this.placeDisable.getValue()) {
                  this.disable();
               }
            }
         } else {
            if (this.debug.getValue()) {
               this.sendMessage("player stuck");
            }
         }
      }
   }

   private void gotoPos(BlockPos offPos) {
      if (this.test.getValue()) {
         mc.player
            .connection
            .sendPacket(
               new Position(
                  mc.player.posX + ((double)offPos.getX() + 0.5 - mc.player.posX) * 0.42132,
                  mc.player.posY + 0.12160004615784,
                  mc.player.posZ + ((double)offPos.getZ() + 0.5 - mc.player.posZ) * 0.42132,
                  false
               )
            );
         mc.player
            .connection
            .sendPacket(
               new Position(
                  mc.player.posX + ((double)offPos.getX() + 0.5 - mc.player.posX) * 0.95,
                  mc.player.posY + 0.200000047683716,
                  mc.player.posZ + ((double)offPos.getZ() + 0.5 - mc.player.posZ) * 0.95,
                  false
               )
            );
         mc.player
            .connection
            .sendPacket(
               new Position(
                  mc.player.posX + ((double)offPos.getX() + 0.5 - mc.player.posX) * 1.03,
                  mc.player.posY + 0.200000047683716,
                  mc.player.posZ + ((double)offPos.getZ() + 0.5 - mc.player.posZ) * 1.03,
                  false
               )
            );
         mc.player
            .connection
            .sendPacket(
               new Position(
                  mc.player.posX + ((double)offPos.getX() + 0.5 - mc.player.posX) * 1.0933,
                  mc.player.posY + 0.12160004615784,
                  mc.player.posZ + ((double)offPos.getZ() + 0.5 - mc.player.posZ) * 1.0933,
                  false
               )
            );
      } else if (Math.abs((double)offPos.getX() + 0.5 - mc.player.posX)
         < Math.abs((double)offPos.getZ() + 0.5 - mc.player.posZ)) {
         mc.player
            .connection
            .sendPacket(
               new Position(
                  mc.player.posX,
                  mc.player.posY + 0.2,
                  mc.player.posZ + ((double)offPos.getZ() + 0.5 - mc.player.posZ),
                  true
               )
            );
      } else {
         mc.player
            .connection
            .sendPacket(
               new Position(
                  mc.player.posX + ((double)offPos.getX() + 0.5 - mc.player.posX),
                  mc.player.posY + 0.2,
                  mc.player.posZ,
                  true
               )
            );
      }
   }

   private boolean canGoto(BlockPos pos) {
      return isAir(pos) && isAir(pos.up());
   }

   private void placeBlock(BlockPos pos) {
      if (this.progress < this.multiPlace.getValue()) {
         if (this.canPlace(pos)) {
            BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), true, this.breakCrystal.getValue(), false);
            ++this.progress;
         }
      }
   }

   private boolean canPlace(BlockPos pos) {
      if (!BlockUtil.canBlockFacing(pos)) {
         return false;
      } else if (!canReplace(pos)) {
         return false;
      } else {
         return !this.checkEntity(pos);
      }
   }

   private boolean checkEntity(BlockPos pos) {
      for(Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
         if (!entity.isDead
            && !(entity instanceof EntityItem)
            && !(entity instanceof EntityXPOrb)
            && !(entity instanceof EntityExpBottle)
            && !(entity instanceof EntityArrow)) {
            if (entity instanceof EntityEnderCrystal) {
               if (!this.breakCrystal.getValue() || EntityUtil.getHealth(mc.player) < this.safeHealth.getValue()) {
                  return true;
               }
            } else if (entity != mc.player) {
               return true;
            }
         }
      }

      return false;
   }

   public static enum LagMode {
      China2B2T,
      ToVoid,
      Strict,
      Troll,
      Custom;
   }
}
