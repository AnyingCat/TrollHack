//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import me.rebirthclient.api.events.impl.BlockEvent;
import me.rebirthclient.api.events.impl.MotionEvent;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.FadeUtils;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.exploit.NoGround;
import me.rebirthclient.mod.modules.settings.Bind;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PacketMine extends Module {
   public static final List<Block> godBlocks = Arrays.asList(
      Blocks.COMMAND_BLOCK,
      Blocks.FLOWING_LAVA,
      Blocks.LAVA,
      Blocks.FLOWING_WATER,
      Blocks.WATER,
      Blocks.BEDROCK,
      Blocks.BARRIER
   );
   private final Setting<Integer> delay = this.add(new Setting<>("Delay", 100, 0, 1000));
   private final Setting<Float> damage = this.add(new Setting<>("Damage", 0.7F, 0.0F, 2.0F));
   private final Setting<Float> range = this.add(new Setting<>("Range", 7.0F, 3.0F, 10.0F));
   private final Setting<Integer> maxBreak = this.add(new Setting<>("MaxBreak", 2, 0, 20));
   private final Setting<Boolean> instant = this.add(new Setting<>("Instant", false));
   private final Setting<Boolean> wait = this.add(new Setting<>("Wait", true, v -> !this.instant.getValue()).setParent());
   private final Setting<Boolean> mineAir = this.add(new Setting<>("MineAir", true, v -> this.wait.isOpen()));
   public final Setting<Boolean> godCancel = this.add(new Setting<>("GodCancel", true));
   public final Setting<Boolean> hotBar = this.add(new Setting<>("HotBar", false));
   private final Setting<Boolean> tpsSync = this.add(new Setting<>("TpsSync", true));
   private final Setting<Boolean> checkGround = this.add(new Setting<>("CheckGround", true));
   private final Setting<Boolean> onlyGround = this.add(new Setting<>("OnlyGround", true).setParent());
   private final Setting<Boolean> allowWeb = this.add(new Setting<>("AllowWeb", true, v -> this.onlyGround.isOpen()));
   private final Setting<Boolean> doubleBreak = this.add(new Setting<>("DoubleBreak", true));
   private final Setting<Boolean> swing = this.add(new Setting<>("Swing", true));
   private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true).setParent());
   private final Setting<Integer> time = this.add(new Setting<>("Time", 100, 0, 2000, v -> this.rotate.isOpen()));
   private final Setting<Boolean> switchReset = this.add(new Setting<>("SwitchReset", false));
   private final Setting<Boolean> render = this.add(new Setting<>("Render", true).setParent());
   private final Setting<PacketMine.Mode> animationMode = this.add(new Setting<>("AnimationMode", PacketMine.Mode.Up, v -> this.render.isOpen()));
   private final Setting<Float> fillStart = this.add(
      new Setting<>("FillStart", 0.2F, 0.0F, 1.0F, v -> this.render.isOpen() && this.animationMode.getValue() == PacketMine.Mode.Custom)
   );
   private final Setting<Float> boxStart = this.add(
      new Setting<>("BoxStart", 0.4F, 0.0F, 1.0F, v -> this.render.isOpen() && this.animationMode.getValue() == PacketMine.Mode.Custom)
   );
   private final Setting<Float> boxExtend = this.add(
      new Setting<>("BoxExtend", 0.2F, 0.0F, 1.0F, v -> this.render.isOpen() && this.animationMode.getValue() == PacketMine.Mode.Custom)
   );
   private final Setting<Boolean> text = this.add(new Setting<>("Text", true, v -> this.render.isOpen()).setParent());
   private final Setting<PacketMine.TextMode> textMode = this.add(
      new Setting<>("TextMode", PacketMine.TextMode.Progress, v -> this.render.isOpen() && this.text.isOpen())
   );
   private final Setting<Boolean> showMax = this.add(
      new Setting<>(
         "ShowMax",
         true,
         v -> this.render.isOpen()
               && this.text.isOpen()
               && (this.textMode.getValue() == PacketMine.TextMode.Time || this.textMode.getValue() == PacketMine.TextMode.Tick)
      )
   );
   private final Setting<PacketMine.ColorMode> textColorMode = this.add(
      new Setting<>("TextColorMode", PacketMine.ColorMode.Progress, v -> this.render.isOpen() && this.text.isOpen())
   );
   private final Setting<Color> textColor = this.add(
      new Setting<>(
            "TextColor",
            new Color(255, 255, 255, 255),
            v -> this.render.isOpen() && this.text.isOpen() && this.textColorMode.getValue() == PacketMine.ColorMode.Custom
         )
         .hideAlpha()
   );
   private final Setting<Boolean> box = this.add(new Setting<>("Box", true, v -> this.render.isOpen()).setParent());
   private final Setting<Integer> boxAlpha = this.add(new Setting<>("BoxAlpha", 100, 0, 255, v -> this.box.isOpen() && this.render.isOpen()));
   private final Setting<Boolean> outline = this.add(new Setting<>("Outline", true, v -> this.render.isOpen()).setParent());
   private final Setting<Integer> outlineAlpha = this.add(new Setting<>("OutlineAlpha", 100, 0, 255, v -> this.outline.isOpen() && this.render.isOpen()));
   private final Setting<PacketMine.ColorMode> colorMode = this.add(new Setting<>("ColorMode", PacketMine.ColorMode.Progress, v -> this.render.isOpen()));
   private final Setting<Color> color = this.add(
      new Setting<>("Color", new Color(189, 212, 255), v -> this.render.isOpen() && this.colorMode.getValue() == PacketMine.ColorMode.Custom).hideAlpha()
   );
   private final Setting<Boolean> crystal = this.add(new Setting<>("Crystal", true).setParent());
   private final Setting<Boolean> waitPlace = this.add(new Setting<>("WaitPlace", false, v -> this.crystal.isOpen()));
   private final Setting<Boolean> fast = this.add(new Setting<>("Fast", false, v -> this.crystal.isOpen()));
   private final Setting<Boolean> afterBreak = this.add(new Setting<>("AfterBreak", true, v -> this.crystal.isOpen()));
   private final Setting<Boolean> checkDamage = this.add(new Setting<>("CheckDamage", true, v -> this.crystal.isOpen()));
   private final Setting<Float> crystalDamage = this.add(
      new Setting<>("CrystalDamage", 0.7F, 0.0F, 1.0F, v -> this.crystal.isOpen() && this.checkDamage.getValue())
   );
   private final Setting<Boolean> eatPause = this.add(new Setting<>("EatingPause", true, v -> this.crystal.isOpen()));
   private final Setting<Bind> enderChest = this.add(new Setting<>("EnderChest", new Bind(-1)));
   private final Setting<Bind> obsidian = this.add(new Setting<>("Obsidian", new Bind(-1)));
   private final Setting<Integer> placeDelay = this.add(new Setting<>("PlaceDelay", 300, 0, 1000));
   private final Setting<Boolean> debug = this.add(new Setting<>("Debug", false));
   public static PacketMine INSTANCE;
   public static BlockPos breakPos;
   private final Timer mineTimer = new Timer();
   private FadeUtils animationTime = new FadeUtils(1000L);
   private boolean startMine = false;
   private int breakNumber = 0;
   private final Timer placeTimer = new Timer();
   private final Timer delayTimer = new Timer();
   private final Timer firstTimer = new Timer();
   int lastSlot = -1;

   public PacketMine() {
      super("PacketMine", "1", Category.COMBAT);
      INSTANCE = this;
   }

   @Override
   public void onDisable() {
      this.startMine = false;
      breakPos = null;
   }

   @Override
   public void onTick() {
      this.update();
   }

   public void update() {
      if (breakPos == null) {
         this.breakNumber = 0;
         this.startMine = false;
      } else if (!mc.player.isCreative()
         && !(
            mc.player
                  .getDistance((double)breakPos.getX() + 0.5, (double)breakPos.getY() + 0.5, (double)breakPos.getZ() + 0.5)
               > (double)this.range.getValue().floatValue()
         )
         && (this.breakNumber <= this.maxBreak.getValue() - 1 || this.maxBreak.getValue() <= 0)
         && (this.wait.getValue() || !mc.world.isAirBlock(breakPos) || this.instant.getValue())) {
         if (godBlocks.contains(mc.world.getBlockState(breakPos).getBlock())) {
            if (this.godCancel.getValue()) {
               breakPos = null;
               this.startMine = false;
            }
         } else {
            int slot = this.getTool(breakPos);
            if (slot == -1) {
               slot = mc.player.inventory.currentItem + 36;
            }

            if (mc.world.isAirBlock(breakPos)) {
               if (this.crystal.getValue()) {
                  for(EnumFacing facing : EnumFacing.VALUES) {
                     CombatUtil.attackCrystal(breakPos.offset(facing), this.rotate.getValue(), this.eatPause.getValue());
                  }
               }

               if (this.placeTimer.passedMs((long)this.placeDelay.getValue().intValue()) && BlockUtil.canPlace(breakPos)) {
                  if (this.enderChest.getValue().isDown() && mc.currentScreen == null) {
                     int eChest = InventoryUtil.findHotbarBlock(Blocks.ENDER_CHEST);
                     if (eChest != -1) {
                        int oldSlot = mc.player.inventory.currentItem;
                        InventoryUtil.doSwap(eChest);
                        BlockUtil.placeBlock(breakPos, EnumHand.MAIN_HAND, this.rotate.getValue(), true);
                        InventoryUtil.doSwap(oldSlot);
                        this.placeTimer.reset();
                     }
                  } else if (this.obsidian.getValue().isDown() && mc.currentScreen == null) {
                     int obsidian = InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN);
                     if (obsidian != -1) {
                        boolean hasCrystal = false;
                        if (this.crystal.getValue()) {
                           for(Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(breakPos.up()))) {
                              if (entity instanceof EntityEnderCrystal) {
                                 hasCrystal = true;
                                 break;
                              }
                           }
                        }

                        if (!hasCrystal || this.fast.getValue()) {
                           int oldSlot = mc.player.inventory.currentItem;
                           InventoryUtil.doSwap(obsidian);
                           BlockUtil.placeBlock(breakPos, EnumHand.MAIN_HAND, this.rotate.getValue(), true);
                           InventoryUtil.doSwap(oldSlot);
                           this.placeTimer.reset();
                        }
                     }
                  }
               }

               this.breakNumber = 0;
            } else if (canPlaceCrystal(breakPos.up(), true) && this.crystal.getValue()) {
               if (this.placeTimer.passedMs((long)this.placeDelay.getValue().intValue())) {
                  if (this.checkDamage.getValue()) {
                     if ((float)this.mineTimer.getPassedTimeMs() / getBreakTime(breakPos, slot) >= this.crystalDamage.getValue()) {
                        int crystal = InventoryUtil.findItemInHotbar(Items.END_CRYSTAL);
                        if (crystal != -1) {
                           int oldSlot = mc.player.inventory.currentItem;
                           InventoryUtil.doSwap(crystal);
                           BlockUtil.placeCrystal(breakPos.up(), this.rotate.getValue());
                           InventoryUtil.doSwap(oldSlot);
                           this.placeTimer.reset();
                           if (this.waitPlace.getValue()) {
                              return;
                           }
                        }
                     }
                  } else {
                     int crystal = InventoryUtil.findItemInHotbar(Items.END_CRYSTAL);
                     if (crystal != -1) {
                        int oldSlot = mc.player.inventory.currentItem;
                        InventoryUtil.doSwap(crystal);
                        BlockUtil.placeCrystal(breakPos.up(), this.rotate.getValue());
                        InventoryUtil.doSwap(oldSlot);
                        this.placeTimer.reset();
                        if (this.waitPlace.getValue()) {
                           return;
                        }
                     }
                  }
               } else if (this.startMine) {
                  return;
               }
            }

            if (this.delayTimer.passedMs((long)this.delay.getValue().intValue())) {
               if (this.startMine) {
                  if (mc.world.isAirBlock(breakPos)) {
                     return;
                  }

                  if (this.onlyGround.getValue() && !mc.player.onGround && (!this.allowWeb.getValue() || !mc.player.isInWeb)) {
                     return;
                  }

                  if (PullCrystal.INSTANCE.isOn()
                     && breakPos.equals(PullCrystal.powerPos)
                     && PullCrystal.crystalPos != null
                     && !BlockUtil.posHasCrystal(PullCrystal.crystalPos)) {
                     return;
                  }

                  if (PistonCrystal.INSTANCE.isOn() && breakPos.equals(PistonCrystal.lastMine) && !PistonCrystal.shouldMine) {
                     return;
                  }

                  if (this.mineTimer.passedMs((long)getBreakTime(breakPos, slot))) {
                     int old = mc.player.inventory.currentItem;
                     boolean shouldSwitch = old + 36 != slot;
                     if (shouldSwitch) {
                        if (this.hotBar.getValue()) {
                           InventoryUtil.doSwap(slot - 36);
                        } else {
                           mc.playerController.windowClick(0, slot, old, ClickType.SWAP, mc.player);
                        }
                     }

                     if (this.rotate.getValue()) {
                        EntityUtil.facePosFacing(breakPos, BlockUtil.getRayTraceFacing(breakPos));
                     }

                     if (this.swing.getValue()) {
                        mc.player.swingArm(EnumHand.MAIN_HAND);
                     }

                     mc.player
                        .connection
                        .sendPacket(new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, breakPos, BlockUtil.getRayTraceFacing(breakPos)));
                     if (shouldSwitch) {
                        if (this.hotBar.getValue()) {
                           InventoryUtil.doSwap(old);
                        } else {
                           mc.playerController.windowClick(0, slot, old, ClickType.SWAP, mc.player);
                        }
                     }

                     ++this.breakNumber;
                     this.delayTimer.reset();
                     if (this.afterBreak.getValue() && this.crystal.getValue()) {
                        for(EnumFacing facing : EnumFacing.VALUES) {
                           CombatUtil.attackCrystal(breakPos.offset(facing), this.rotate.getValue(), this.eatPause.getValue());
                        }
                     }
                  }
               } else {
                  if (!this.mineAir.getValue() && mc.world.isAirBlock(breakPos)) {
                     return;
                  }

                  this.animationTime = new FadeUtils((long)getBreakTime(breakPos, slot));
                  this.mineTimer.reset();
                  if (this.swing.getValue()) {
                     mc.player.swingArm(EnumHand.MAIN_HAND);
                  }

                  mc.player
                     .connection
                     .sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, breakPos, BlockUtil.getRayTraceFacing(breakPos)));
                  this.delayTimer.reset();
               }
            }
         }
      } else {
         this.startMine = false;
         this.breakNumber = 0;
         breakPos = null;
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOW
   )
   public void onMotion(MotionEvent event) {
      if (!fullNullCheck()) {
         if (!this.onlyGround.getValue() || mc.player.onGround || this.allowWeb.getValue() && mc.player.isInWeb) {
            if (this.rotate.getValue() && breakPos != null && !mc.world.isAirBlock(breakPos) && this.time.getValue() > 0) {
               int slot = this.getTool(breakPos);
               if (slot == -1) {
                  slot = mc.player.inventory.currentItem + 36;
               }

               float breakTime = getBreakTime(breakPos, slot) - (float)this.time.getValue().intValue();
               if (breakTime <= 0.0F || this.mineTimer.passedMs((long)breakTime)) {
                  facePosFacing(breakPos, BlockUtil.getRayTraceFacing(breakPos), event);
               }
            }
         }
      }
   }

   public static float getBreakTime(BlockPos pos, int slot) {
      return 1.0F
         / getBlockStrength(pos, (ItemStack)mc.player.inventoryContainer.getInventory().get(slot))
         / 20.0F
         * 1000.0F
         * INSTANCE.damage.getValue()
         * (INSTANCE.tpsSync.getValue() ? Managers.SERVER.getTPS() / 20.0F : 1.0F);
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public void onSend(PacketEvent.Send event) {
      if (!fullNullCheck() && !mc.player.isCreative() && this.debug.getValue() && event.getPacket() instanceof CPacketPlayerDigging) {
         this.sendMessage(((CPacketPlayerDigging)event.getPacket()).getAction().name());
      }
   }

   @SubscribeEvent
   public void onPacketSend(PacketEvent.Send event) {
      if (!fullNullCheck() && !mc.player.isCreative()) {
         if (event.getPacket() instanceof CPacketHeldItemChange) {
            if (((CPacketHeldItemChange)event.getPacket()).getSlotId() != this.lastSlot) {
               this.lastSlot = ((CPacketHeldItemChange)event.getPacket()).getSlotId();
               if (this.switchReset.getValue()) {
                  this.startMine = false;
                  this.mineTimer.reset();
                  this.animationTime.reset();
               }
            }
         } else if (event.getPacket() instanceof CPacketPlayerDigging) {
            if (((CPacketPlayerDigging)event.getPacket()).getAction() == Action.START_DESTROY_BLOCK) {
               if (breakPos == null || !((CPacketPlayerDigging)event.getPacket()).getPosition().equals(breakPos)) {
                  event.setCanceled(true);
                  return;
               }

               this.startMine = true;
            } else if (((CPacketPlayerDigging)event.getPacket()).getAction() == Action.STOP_DESTROY_BLOCK) {
               if (breakPos == null || !((CPacketPlayerDigging)event.getPacket()).getPosition().equals(breakPos)) {
                  event.setCanceled(true);
                  return;
               }

               if (!this.instant.getValue()) {
                  this.startMine = false;
               }
            }
         }
      }
   }

   public static void facePosFacing(BlockPos pos, EnumFacing side, MotionEvent event) {
      Vec3d hitVec = new Vec3d(pos).add(0.5, 0.5, 0.5).add(new Vec3d(side.getDirectionVec()).scale(0.5));
      faceVector(hitVec, event);
   }

   private static void faceVector(Vec3d vec, MotionEvent event) {
      float[] rotations = EntityUtil.getLegitRotations(vec);
      event.setRotation(rotations[0], rotations[1]);
   }

   @Override
   public void onRender3D(Render3DEvent event) {
      this.update();
      if (!mc.player.isCreative() && breakPos != null) {
         if (this.debug.getValue()) {
            RenderUtil.drawBBFill(new AxisAlignedBB(breakPos.offset(BlockUtil.getRayTraceFacing(breakPos))), new Color(255, 255, 255), 70);
         }

         if (this.render.getValue()) {
            if (mc.world.isAirBlock(breakPos) && !this.wait.getValue() && !this.instant.getValue()) {
               return;
            }

            if (godBlocks.contains(mc.world.getBlockState(breakPos).getBlock())) {
               this.draw(breakPos, 1.0, this.colorMode.getValue() == PacketMine.ColorMode.Custom ? this.color.getValue() : new Color(255, 0, 0, 255), true);
               if (this.text.getValue()) {
                  AxisAlignedBB renderBB = mc.world.getBlockState(breakPos).getSelectedBoundingBox(mc.world, breakPos);
                  RenderUtil.drawText(renderBB, ChatFormatting.RED + "GodBlock");
               }
            } else {
               int slot = this.getTool(breakPos);
               if (slot == -1) {
                  slot = mc.player.inventory.currentItem + 36;
               }

               this.animationTime.setLength((long)getBreakTime(breakPos, slot));
               this.draw(
                  breakPos,
                  mc.world.isAirBlock(breakPos) ? 1.0 : this.animationTime.easeOutQuad(),
                  this.colorMode.getValue() == PacketMine.ColorMode.Custom
                     ? this.color.getValue()
                     : new Color((int)(255.0 * Math.abs(this.animationTime.easeOutQuad() - 1.0)), (int)(255.0 * this.animationTime.easeOutQuad()), 0),
                  mc.world.isAirBlock(breakPos)
               );
               if (this.text.getValue()) {
                  AxisAlignedBB renderBB = mc.world.getBlockState(breakPos).getSelectedBoundingBox(mc.world, breakPos);
                  if (!mc.world.isAirBlock(breakPos)) {
                     if (this.textMode.getValue() == PacketMine.TextMode.Progress) {
                        if ((float)((int)this.mineTimer.getPassedTimeMs()) < getBreakTime(breakPos, slot)) {
                           double num1 = (double)this.mineTimer.getPassedTimeMs()
                              / (double)(
                                 1.0F
                                    / getBlockStrength(breakPos, (ItemStack)mc.player.inventoryContainer.getInventory().get(slot))
                                    / 20.0F
                                    * 1000.0F
                                    * this.damage.getValue()
                                    / 100.0F
                              );
                           DecimalFormat df = new DecimalFormat("0.0");
                           RenderUtil.drawText(
                              renderBB,
                              df.format(num1) + "%",
                              this.textColorMode.getValue() == PacketMine.ColorMode.Progress
                                 ? new Color(
                                    (int)(255.0 * Math.abs(this.animationTime.easeOutQuad() - 1.0)), (int)(255.0 * this.animationTime.easeOutQuad()), 0, 255
                                 )
                                 : this.textColor.getValue()
                           );
                        } else {
                           RenderUtil.drawText(
                              renderBB,
                              "100.0%",
                              this.textColorMode.getValue() == PacketMine.ColorMode.Progress ? new Color(0, 255, 0, 255) : this.textColor.getValue()
                           );
                        }
                     } else if (this.textMode.getValue() == PacketMine.TextMode.Time) {
                        if ((float)((int)this.mineTimer.getPassedTimeMs()) < getBreakTime(breakPos, slot)) {
                           RenderUtil.drawText(
                              renderBB,
                              this.mineTimer.getPassedTimeMs() + (this.showMax.getValue() ? "/" + (int)getBreakTime(breakPos, slot) : ""),
                              this.textColorMode.getValue() == PacketMine.ColorMode.Progress ? new Color(0, 255, 0, 255) : this.textColor.getValue()
                           );
                        } else {
                           RenderUtil.drawText(
                              renderBB,
                              String.valueOf((int)getBreakTime(breakPos, slot)),
                              this.textColorMode.getValue() == PacketMine.ColorMode.Progress ? new Color(0, 255, 0, 255) : this.textColor.getValue()
                           );
                        }
                     } else if (this.textMode.getValue() == PacketMine.TextMode.Tick) {
                        if ((float)((int)this.mineTimer.getPassedTimeMs()) < getBreakTime(breakPos, slot)) {
                           RenderUtil.drawText(
                              renderBB,
                              (int)(this.mineTimer.getPassedTimeMs() / 50L)
                                 + (this.showMax.getValue() ? "/" + (int)(getBreakTime(breakPos, slot) / 50.0F) : ""),
                              this.textColorMode.getValue() == PacketMine.ColorMode.Progress ? new Color(0, 255, 0, 255) : this.textColor.getValue()
                           );
                        } else {
                           RenderUtil.drawText(
                              renderBB,
                              String.valueOf((int)(getBreakTime(breakPos, slot) / 50.0F)),
                              this.textColorMode.getValue() == PacketMine.ColorMode.Progress ? new Color(0, 255, 0, 255) : this.textColor.getValue()
                           );
                        }
                     }
                  } else {
                     RenderUtil.drawText(
                        renderBB,
                        "Waiting",
                        this.textColorMode.getValue() == PacketMine.ColorMode.Progress ? new Color(0, 255, 0, 255) : this.textColor.getValue()
                     );
                  }
               }
            }
         }
      }
   }

   public void draw(BlockPos pos, double size, Color color, boolean full) {
      if (this.animationMode.getValue() != PacketMine.Mode.Both && this.animationMode.getValue() != PacketMine.Mode.Custom) {
         AxisAlignedBB axisAlignedBB;
         if (full) {
            axisAlignedBB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
         } else if (this.animationMode.getValue() == PacketMine.Mode.InToOut) {
            axisAlignedBB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos).grow(size / 2.0 - 0.5);
         } else if (this.animationMode.getValue() == PacketMine.Mode.Up) {
            AxisAlignedBB bb = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
            axisAlignedBB = new AxisAlignedBB(
               bb.minX,
               bb.minY,
               bb.minZ,
               bb.maxX,
               bb.minY + (bb.maxY - bb.minY) * size,
               bb.maxZ
            );
         } else if (this.animationMode.getValue() == PacketMine.Mode.Down) {
            AxisAlignedBB bb = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
            axisAlignedBB = new AxisAlignedBB(
               bb.minX,
               bb.maxY - (bb.maxY - bb.minY) * size,
               bb.minZ,
               bb.maxX,
               bb.maxY,
               bb.maxZ
            );
         } else if (this.animationMode.getValue() == PacketMine.Mode.OutToIn) {
            axisAlignedBB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos).grow(-Math.abs(size / 2.0 - 1.0));
         } else if (this.animationMode.getValue() == PacketMine.Mode.None) {
            axisAlignedBB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
         } else {
            AxisAlignedBB bb = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos).grow(size / 2.0 - 0.5);
            AxisAlignedBB bb2 = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
            if (this.animationMode.getValue() == PacketMine.Mode.Horizontal) {
               axisAlignedBB = new AxisAlignedBB(
                  bb2.minX, bb.minY, bb2.minZ, bb2.maxX, bb.maxY, bb2.maxZ
               );
            } else {
               axisAlignedBB = new AxisAlignedBB(bb.minX, bb2.minY, bb.minZ, bb.maxX, bb2.maxY, bb.maxZ);
            }
         }

         if (this.outline.getValue()) {
            RenderUtil.drawBBBox(axisAlignedBB, color, this.outlineAlpha.getValue());
         }

         if (this.box.getValue()) {
            RenderUtil.drawBBFill(axisAlignedBB, color, this.boxAlpha.getValue());
         }
      } else if (this.animationMode.getValue() == PacketMine.Mode.Custom) {
         AxisAlignedBB axisAlignedBB;
         if (full) {
            axisAlignedBB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
            if (this.outline.getValue()) {
               RenderUtil.drawBBBox(axisAlignedBB, color, this.outlineAlpha.getValue());
            }
         } else {
            axisAlignedBB = mc.world
               .getBlockState(pos)
               .getSelectedBoundingBox(mc.world, pos)
               .grow((double)(-this.fillStart.getValue()) - size * (double)(1.0F - this.fillStart.getValue()));
            double boxSize = size + (double)this.boxExtend.getValue().floatValue();
            if (boxSize > 1.0) {
               boxSize = 1.0;
            }

            AxisAlignedBB axisAlignedBB2 = mc.world
               .getBlockState(pos)
               .getSelectedBoundingBox(mc.world, pos)
               .grow((double)(-this.boxStart.getValue()) - boxSize * (double)(1.0F - this.boxStart.getValue()));
            if (this.outline.getValue()) {
               RenderUtil.drawBBBox(axisAlignedBB2, color, this.outlineAlpha.getValue());
            }
         }

         if (this.box.getValue()) {
            RenderUtil.drawBBFill(axisAlignedBB, color, this.boxAlpha.getValue());
         }
      } else {
         AxisAlignedBB axisAlignedBB;
         if (full) {
            axisAlignedBB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
         } else {
            axisAlignedBB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos).grow(size / 2.0 - 0.5);
            if (this.outline.getValue()) {
               RenderUtil.drawBBBox(axisAlignedBB, color, this.outlineAlpha.getValue());
            }

            if (this.box.getValue()) {
               RenderUtil.drawBBFill(axisAlignedBB, color, this.boxAlpha.getValue());
            }

            axisAlignedBB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos).grow(-Math.abs(size / 2.0 - 1.0));
         }

         if (this.outline.getValue()) {
            RenderUtil.drawBBBox(axisAlignedBB, color, this.outlineAlpha.getValue());
         }

         if (this.box.getValue()) {
            RenderUtil.drawBBFill(axisAlignedBB, color, this.boxAlpha.getValue());
         }
      }
   }

   private int getTool(BlockPos pos) {
      if (this.hotBar.getValue()) {
         int index = -1;
         float CurrentFastest = 1.0F;

         for(int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY) {
               float digSpeed = (float)EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack);
               float destroySpeed = stack.getDestroySpeed(mc.world.getBlockState(pos));
               if (digSpeed + destroySpeed > CurrentFastest) {
                  CurrentFastest = digSpeed + destroySpeed;
                  index = 36 + i;
               }
            }
         }

         return index;
      } else {
         AtomicInteger slot = new AtomicInteger();
         slot.set(-1);
         float CurrentFastest = 1.0F;

         for(Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
            if (!(((ItemStack)entry.getValue()).getItem() instanceof ItemAir)) {
               float digSpeed = (float)EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, (ItemStack)entry.getValue());
               float destroySpeed = ((ItemStack)entry.getValue()).getDestroySpeed(mc.world.getBlockState(pos));
               if (digSpeed + destroySpeed > CurrentFastest) {
                  CurrentFastest = digSpeed + destroySpeed;
                  slot.set(entry.getKey());
               }
            }
         }

         return slot.get();
      }
   }

   @SubscribeEvent
   public void onClickBlock(BlockEvent event) {
      if (!fullNullCheck() && !mc.player.isCreative()) {
         event.setCanceled(true);
         if (!godBlocks.contains(mc.world.getBlockState(event.getBlockPos()).getBlock()) || !this.godCancel.getValue()) {
            if (!event.getBlockPos().equals(breakPos)) {
               breakPos = event.getBlockPos();
               this.mineTimer.reset();
               this.animationTime.reset();
               if (!godBlocks.contains(mc.world.getBlockState(event.getBlockPos()).getBlock())) {
                  this.firstTimer.reset();
                  mc.player
                     .connection
                     .sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, breakPos, BlockUtil.getRayTraceFacing(breakPos)));
                  if (this.doubleBreak.getValue()) {
                     mc.player
                        .connection
                        .sendPacket(new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, breakPos, BlockUtil.getRayTraceFacing(breakPos)));
                     mc.player
                        .connection
                        .sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, breakPos, BlockUtil.getRayTraceFacing(breakPos)));
                  }

                  this.breakNumber = 0;
               }
            }
         }
      }
   }

   private static boolean canBreak(BlockPos pos) {
      IBlockState blockState = mc.world.getBlockState(pos);
      Block block = blockState.getBlock();
      return block.getBlockHardness(blockState, mc.world, pos) != -1.0F;
   }

   public static float getBlockStrength(BlockPos position, ItemStack itemStack) {
      IBlockState state = mc.world.getBlockState(position);
      float hardness = state.getBlockHardness(mc.world, position);
      if (hardness < 0.0F) {
         return 0.0F;
      } else {
         return !canBreak(position) ? getDigSpeed(state, itemStack) / hardness / 100.0F : getDigSpeed(state, itemStack) / hardness / 30.0F;
      }
   }

   public static float getDigSpeed(IBlockState state, ItemStack itemStack) {
      float digSpeed = getDestroySpeed(state, itemStack);
      if (digSpeed > 1.0F) {
         int efficiencyModifier = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, itemStack);
         if (efficiencyModifier > 0 && !itemStack.isEmpty()) {
            digSpeed = (float)((double)digSpeed + StrictMath.pow((double)efficiencyModifier, 2.0) + 1.0);
         }
      }

      if (mc.player.isPotionActive(MobEffects.HASTE)) {
         digSpeed *= 1.0F + (float)(mc.player.getActivePotionEffect(MobEffects.HASTE).getAmplifier() + 1) * 0.2F;
      }

      if (mc.player.isPotionActive(MobEffects.MINING_FATIGUE)) {
         float fatigueScale;
         switch(mc.player.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
            case 0:
               fatigueScale = 0.3F;
               break;
            case 1:
               fatigueScale = 0.09F;
               break;
            case 2:
               fatigueScale = 0.0027F;
               break;
            case 3:
            default:
               fatigueScale = 8.1E-4F;
         }

         digSpeed *= fatigueScale;
      }

      if (mc.player.isInsideOfMaterial(Material.WATER) && !EnchantmentHelper.getAquaAffinityModifier(mc.player)) {
         digSpeed /= 5.0F;
      }

      if ((!mc.player.onGround || NoGround.INSTANCE.isOn()) && INSTANCE.checkGround.getValue()) {
         digSpeed /= 5.0F;
      }

      return digSpeed < 0.0F ? 0.0F : digSpeed;
   }

   public static float getDestroySpeed(IBlockState state, ItemStack itemStack) {
      float destroySpeed = 1.0F;
      if (itemStack != null && !itemStack.isEmpty()) {
         destroySpeed *= itemStack.getDestroySpeed(state);
      }

      return destroySpeed;
   }

   public static boolean canPlaceCrystal(BlockPos pos, boolean ignoreItem) {
      BlockPos obsPos = pos.down();
      BlockPos boost = obsPos.up();
      BlockPos boost2 = obsPos.up(2);
      return (getBlock(obsPos) == Blocks.BEDROCK || getBlock(obsPos) == Blocks.OBSIDIAN)
         && (getBlock(boost) == Blocks.AIR || getBlock(boost) == Blocks.FIRE && CombatSetting.INSTANCE.placeInFire.getValue())
         && noEntity(boost, ignoreItem)
         && getBlock(boost2) == Blocks.AIR
         && noEntity(boost2, ignoreItem);
   }

   public static boolean noEntity(BlockPos pos, boolean ignoreItem) {
      for(Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
         if (!(entity instanceof EntityItem) || !ignoreItem) {
            return false;
         }
      }

      return true;
   }

   public static Block getBlock(BlockPos pos) {
      return mc.world.getBlockState(pos).getBlock();
   }

   public static enum ColorMode {
      Custom,
      Progress;
   }

   public static enum Mode {
      Down,
      Up,
      InToOut,
      OutToIn,
      Both,
      Vertical,
      Horizontal,
      Custom,
      None;
   }

   public static enum TextMode {
      Time,
      Tick,
      Progress;
   }
}
