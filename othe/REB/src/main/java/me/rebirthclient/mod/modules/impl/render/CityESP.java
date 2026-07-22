//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class CityESP extends Module {
   private final Setting<Boolean> onlyBurrow = this.add(new Setting<>("OnlyBurrow", false));
   private final Setting<Boolean> outline = this.add(new Setting<>("Outline", true).setParent());
   private final Setting<Integer> outlineAlpha = this.add(new Setting<>("OutlineAlpha", 150, 0, 255, v -> this.outline.isOpen()));
   private final Setting<Boolean> box = this.add(new Setting<>("Box", true).setParent());
   private final Setting<Integer> boxAlpha = this.add(new Setting<>("BoxAlpha", 70, 0, 255, v -> this.box.isOpen()));
   private final Setting<Float> range = this.add(new Setting<>("Range", 7.0F, 1.0F, 12.0F));
   private final Setting<Color> canAttackColor = this.add(new Setting<>("AttackColor", new Color(255, 147, 147)).hideAlpha());
   private final Setting<Color> breakColor = this.add(new Setting<>("Color", new Color(118, 118, 255)).hideAlpha());
   private final Setting<Color> burrowColor = this.add(new Setting<>("BurrowColor", new Color(255, 255, 255)).hideAlpha());
   private final List<BlockPos> burrowPos = new ArrayList();

   public CityESP() {
      super("CityESP", "CityESP", Category.RENDER);
   }

   @Override
   public void onRender3D(Render3DEvent event) {
      this.burrowPos.clear();

      for(EntityPlayer player : mc.world.playerEntities) {
         if (!EntityUtil.invalid(player, (double)this.range.getValue().floatValue())) {
            this.doRender(player);
         }
      }
   }

   private void drawBurrow(BlockPos pos) {
      if (!this.burrowPos.contains(pos)) {
         this.burrowPos.add(pos);
         if (this.getBlock(pos).getBlock() != Blocks.AIR && this.getBlock(pos).getBlock() != Blocks.BEDROCK) {
            AxisAlignedBB axisAlignedBB = mc.world.getBlockState(new BlockPos(pos)).getSelectedBoundingBox(mc.world, new BlockPos(pos));
            if (this.box.getValue()) {
               RenderUtil.drawBBFill(axisAlignedBB, this.burrowColor.getValue(), this.boxAlpha.getValue());
            }

            if (this.outline.getValue()) {
               RenderUtil.drawBBBox(axisAlignedBB, this.burrowColor.getValue(), this.outlineAlpha.getValue());
            }
         }
      }
   }

   private void doRender(EntityPlayer target) {
      BlockPos pos = EntityUtil.getEntityPos(target);
      this.drawBurrow(new BlockPos(target.posX + 0.1, target.posY + 0.5, target.posZ + 0.1));
      this.drawBurrow(new BlockPos(target.posX - 0.1, target.posY + 0.5, target.posZ + 0.1));
      this.drawBurrow(new BlockPos(target.posX + 0.1, target.posY + 0.5, target.posZ - 0.1));
      this.drawBurrow(new BlockPos(target.posX - 0.1, target.posY + 0.5, target.posZ - 0.1));
      if (!this.onlyBurrow.getValue()) {
         if (this.getBlock(pos.add(-1, 0, 0)).getBlock() != Blocks.AIR
            && this.getBlock(pos.add(-1, 0, 0)).getBlock() != Blocks.BEDROCK) {
            if (this.getBlock(pos.add(-2, 0, 0)).getBlock() == Blocks.AIR
               && this.getBlock(pos.add(-2, 1, 0)).getBlock() == Blocks.AIR) {
               this.drawBlock(pos, -1.0, 0.0, 0.0, true);
            } else if (this.getBlock(pos.add(-2, 0, 0)).getBlock() != Blocks.BEDROCK
               && this.getBlock(pos.add(-2, 1, 0)).getBlock() != Blocks.BEDROCK) {
               this.drawBlock(pos, -1.0, 0.0, 0.0, false);
            }
         }

         if (this.getBlock(pos.add(1, 0, 0)).getBlock() != Blocks.AIR
            && this.getBlock(pos.add(1, 0, 0)).getBlock() != Blocks.BEDROCK) {
            if (this.getBlock(pos.add(2, 0, 0)).getBlock() == Blocks.AIR
               && this.getBlock(pos.add(2, 1, 0)).getBlock() == Blocks.AIR) {
               this.drawBlock(pos, 1.0, 0.0, 0.0, true);
            } else if (this.getBlock(pos.add(2, 0, 0)).getBlock() != Blocks.BEDROCK
               && this.getBlock(pos.add(2, 1, 0)).getBlock() != Blocks.BEDROCK) {
               this.drawBlock(pos, 1.0, 0.0, 0.0, false);
            }
         }

         if (this.getBlock(pos.add(-1, 0, 0)).getBlock() != Blocks.BEDROCK
            && this.getBlock(pos.add(-2, 1, 0)).getBlock() != Blocks.BEDROCK
            && this.getBlock(pos.add(-2, 0, 0)).getBlock() != Blocks.AIR
            && this.getBlock(pos.add(-2, 0, 0)).getBlock() != Blocks.BEDROCK) {
            this.drawBlock(
               pos,
               -2.0,
               0.0,
               0.0,
               this.getBlock(pos.add(-1, 0, 0)).getBlock() == Blocks.AIR
                  && this.getBlock(pos.add(-2, 1, 0)).getBlock() == Blocks.AIR
            );
         }

         if (this.getBlock(pos.add(-1, 0, 0)).getBlock() != Blocks.BEDROCK
            && this.getBlock(pos.add(-2, 0, 0)).getBlock() != Blocks.BEDROCK
            && this.getBlock(pos.add(-2, 1, 0)).getBlock() != Blocks.AIR
            && this.getBlock(pos.add(-2, 1, 0)).getBlock() != Blocks.BEDROCK) {
            this.drawBlock(
               pos,
               -2.0,
               1.0,
               0.0,
               this.getBlock(pos.add(-1, 0, 0)).getBlock() == Blocks.AIR
                  && this.getBlock(pos.add(-2, 0, 0)).getBlock() == Blocks.AIR
            );
         }

         if (this.getBlock(pos.add(1, 0, 0)).getBlock() != Blocks.BEDROCK
            && this.getBlock(pos.add(2, 1, 0)).getBlock() != Blocks.BEDROCK
            && this.getBlock(pos.add(2, 0, 0)).getBlock() != Blocks.AIR
            && this.getBlock(pos.add(2, 0, 0)).getBlock() != Blocks.BEDROCK) {
            this.drawBlock(
               pos,
               2.0,
               0.0,
               0.0,
               this.getBlock(pos.add(1, 0, 0)).getBlock() == Blocks.AIR
                  && this.getBlock(pos.add(2, 1, 0)).getBlock() == Blocks.AIR
            );
         }

         if (this.getBlock(pos.add(1, 0, 0)).getBlock() != Blocks.BEDROCK
            && this.getBlock(pos.add(2, 0, 0)).getBlock() != Blocks.BEDROCK
            && this.getBlock(pos.add(2, 1, 0)).getBlock() != Blocks.AIR
            && this.getBlock(pos.add(2, 1, 0)).getBlock() != Blocks.BEDROCK) {
            this.drawBlock(
               pos,
               2.0,
               1.0,
               0.0,
               this.getBlock(pos.add(1, 0, 0)).getBlock() == Blocks.AIR
                  && this.getBlock(pos.add(2, 0, 0)).getBlock() == Blocks.AIR
            );
         }

         if (this.getBlock(pos.add(0, 0, 1)).getBlock() != Blocks.AIR
            && this.getBlock(pos.add(0, 0, 1)).getBlock() != Blocks.BEDROCK) {
            if (this.getBlock(pos.add(0, 0, 2)).getBlock() == Blocks.AIR
               && this.getBlock(pos.add(0, 1, 2)).getBlock() == Blocks.AIR) {
               this.drawBlock(pos, 0.0, 0.0, 1.0, true);
            } else if (this.getBlock(pos.add(0, 0, 2)).getBlock() != Blocks.BEDROCK
               && this.getBlock(pos.add(0, 1, 2)).getBlock() != Blocks.BEDROCK) {
               this.drawBlock(pos, 0.0, 0.0, 1.0, false);
            }
         }

         if (this.getBlock(pos.add(0, 0, -1)).getBlock() != Blocks.AIR
            && this.getBlock(pos.add(0, 0, -1)).getBlock() != Blocks.BEDROCK) {
            if (this.getBlock(pos.add(0, 0, -2)).getBlock() == Blocks.AIR
               && this.getBlock(pos.add(0, 1, -2)).getBlock() == Blocks.AIR) {
               this.drawBlock(pos, 0.0, 0.0, -1.0, true);
            } else if (this.getBlock(pos.add(0, 0, -2)).getBlock() != Blocks.BEDROCK
               && this.getBlock(pos.add(0, 1, -2)).getBlock() != Blocks.BEDROCK) {
               this.drawBlock(pos, 0.0, 0.0, -1.0, false);
            }
         }

         if (this.getBlock(pos.add(0, 0, 1)).getBlock() != Blocks.BEDROCK
            && this.getBlock(pos.add(0, 1, 2)).getBlock() != Blocks.BEDROCK
            && this.getBlock(pos.add(0, 0, 2)).getBlock() != Blocks.AIR
            && this.getBlock(pos.add(0, 0, 2)).getBlock() != Blocks.BEDROCK) {
            this.drawBlock(
               pos,
               0.0,
               0.0,
               2.0,
               this.getBlock(pos.add(0, 0, 1)).getBlock() == Blocks.AIR
                  && this.getBlock(pos.add(0, 1, 2)).getBlock() == Blocks.AIR
            );
         }

         if (this.getBlock(pos.add(0, 0, 1)).getBlock() != Blocks.BEDROCK
            && this.getBlock(pos.add(0, 0, 2)).getBlock() != Blocks.BEDROCK
            && this.getBlock(pos.add(0, 1, 2)).getBlock() != Blocks.AIR
            && this.getBlock(pos.add(0, 1, 2)).getBlock() != Blocks.BEDROCK) {
            this.drawBlock(
               pos,
               0.0,
               1.0,
               2.0,
               this.getBlock(pos.add(0, 0, 1)).getBlock() == Blocks.AIR
                  && this.getBlock(pos.add(0, 0, 2)).getBlock() == Blocks.AIR
            );
         }

         if (this.getBlock(pos.add(0, 0, -1)).getBlock() != Blocks.BEDROCK
            && this.getBlock(pos.add(0, 1, -2)).getBlock() != Blocks.BEDROCK
            && this.getBlock(pos.add(0, 0, -2)).getBlock() != Blocks.AIR
            && this.getBlock(pos.add(0, 0, -2)).getBlock() != Blocks.BEDROCK) {
            this.drawBlock(
               pos,
               0.0,
               0.0,
               -2.0,
               this.getBlock(pos.add(0, 0, -1)).getBlock() == Blocks.AIR
                  && this.getBlock(pos.add(0, 1, -2)).getBlock() == Blocks.AIR
            );
         }

         if (this.getBlock(pos.add(0, 0, -1)).getBlock() != Blocks.BEDROCK
            && this.getBlock(pos.add(0, 0, -2)).getBlock() != Blocks.BEDROCK
            && this.getBlock(pos.add(0, 1, -2)).getBlock() != Blocks.AIR
            && this.getBlock(pos.add(0, 1, -2)).getBlock() != Blocks.BEDROCK) {
            this.drawBlock(
               pos,
               0.0,
               1.0,
               -2.0,
               this.getBlock(pos.add(0, 0, -1)).getBlock() == Blocks.AIR
                  && this.getBlock(pos.add(0, 0, -2)).getBlock() == Blocks.AIR
            );
         }
      }
   }

   private void drawBlock(BlockPos pos, double x, double y, double z, boolean red) {
      pos = pos.add(x, y, z);
      if (mc.world.getBlockState(pos).getBlock() != Blocks.AIR) {
         if (mc.world.getBlockState(pos).getBlock() != Blocks.FIRE) {
            AxisAlignedBB axisAlignedBB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
            if (red) {
               if (this.outline.getValue()) {
                  RenderUtil.drawBBBox(axisAlignedBB, this.canAttackColor.getValue(), this.outlineAlpha.getValue());
               }

               if (this.box.getValue()) {
                  RenderUtil.drawBBFill(axisAlignedBB, this.canAttackColor.getValue(), this.boxAlpha.getValue());
               }
            } else {
               if (this.outline.getValue()) {
                  RenderUtil.drawBBBox(axisAlignedBB, this.breakColor.getValue(), this.outlineAlpha.getValue());
               }

               if (this.box.getValue()) {
                  RenderUtil.drawBBFill(axisAlignedBB, this.breakColor.getValue(), this.boxAlpha.getValue());
               }
            }
         }
      }
   }

   private IBlockState getBlock(BlockPos block) {
      return mc.world.getBlockState(block);
   }
}
