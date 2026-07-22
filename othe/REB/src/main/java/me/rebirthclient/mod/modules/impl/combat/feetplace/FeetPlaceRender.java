//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat.feetplace;

import java.awt.Color;
import java.util.HashMap;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.FadeUtils;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.RenderModule;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class FeetPlaceRender extends RenderModule {
   public static HashMap<BlockPos, FeetPlaceRender.placePosition> PlaceMap = new HashMap<>();
   private BlockPos lastPos = null;

   public static void addBlock(BlockPos pos) {
      if (BlockUtil.canPlace2(pos)) {
         PlaceMap.put(pos, new FeetPlaceRender.placePosition(pos));
      }
   }

   private void drawBlock(BlockPos pos, double alpha, Color color) {
      if (FeetPlace.INSTANCE.sync.getValue()) {
         color = FeetPlace.INSTANCE.color.getValue();
      }

      AxisAlignedBB axisAlignedBB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos).grow(0.002);
      RenderUtil.testESP(
         axisAlignedBB,
         color,
         FeetPlace.INSTANCE.lineWidth.getValue(),
         FeetPlace.INSTANCE.outline.getValue(),
         FeetPlace.INSTANCE.box.getValue(),
         (int)((double)color.getAlpha() * -alpha),
         FeetPlace.INSTANCE.depth.getValue()
      );
   }

   @Override
   public void onRender3D(Render3DEvent event) {
      if (FeetPlace.INSTANCE.render.getValue()) {
         if (FeetPlace.INSTANCE.moveReset.getValue() && !EntityUtil.getPlayerPos().equals(this.lastPos)) {
            this.lastPos = EntityUtil.getPlayerPos();
            PlaceMap.clear();
         }

         if (FeetPlace.INSTANCE.isOn() && (!FeetPlace.INSTANCE.onlyGround.getValue() || mc.player.onGround)) {
            BlockPos pos = EntityUtil.getPlayerPos();

            for(EnumFacing i : EnumFacing.VALUES) {
               if (i != EnumFacing.UP && !FeetPlace.INSTANCE.isGod(pos.offset(i), i)) {
                  BlockPos offsetPos = pos.offset(i);
                  addBlock(offsetPos);
                  if (!BlockUtil.canPlaceEnum(offsetPos) && BlockUtil.canReplace(offsetPos)) {
                     addBlock(offsetPos.down());
                  }

                  if (FeetPlace.checkSelf(offsetPos) && FeetPlace.INSTANCE.extend.getValue()) {
                     for(EnumFacing i2 : EnumFacing.VALUES) {
                        if (i2 != EnumFacing.UP) {
                           BlockPos offsetPos2 = offsetPos.offset(i2);
                           if (FeetPlace.checkSelf(offsetPos2)) {
                              for(EnumFacing i3 : EnumFacing.VALUES) {
                                 if (i3 != EnumFacing.UP) {
                                    addBlock(offsetPos2);
                                    BlockPos offsetPos3 = offsetPos2.offset(i3);
                                    addBlock(offsetPos3);
                                    if (!BlockUtil.canPlaceEnum(offsetPos3)) {
                                       addBlock(offsetPos3.down());
                                    }
                                 }
                              }
                           }

                           addBlock(offsetPos2);
                           if (!BlockUtil.canPlaceEnum(offsetPos2)) {
                              addBlock(offsetPos2.down());
                           }
                        }
                     }
                  }
               }
            }
         }

         if (!PlaceMap.isEmpty()) {
            boolean shouldClear = true;

            for(FeetPlaceRender.placePosition placePosition : PlaceMap.values()) {
               if (!BlockUtil.canPlace2(placePosition.pos)) {
                  placePosition.isAir = false;
               }

               if (!placePosition.timer.passedMs((long)(FeetPlace.INSTANCE.delay.getValue() + 100)) && placePosition.isAir) {
                  placePosition.firstFade.reset();
               }

               if (placePosition.firstFade.easeOutQuad() != 1.0) {
                  shouldClear = false;
                  this.drawBlock(placePosition.pos, placePosition.firstFade.easeOutQuad() - 1.0, placePosition.posColor);
               }
            }

            if (shouldClear) {
               PlaceMap.clear();
            }
         }
      }
   }

   public static class placePosition {
      public final FadeUtils firstFade = new FadeUtils((long)FeetPlace.INSTANCE.fadeTime.getValue().intValue());
      public BlockPos pos;
      public Color posColor;
      public final Timer timer;
      public boolean isAir;

      public placePosition(BlockPos placePos) {
         this.pos = placePos;
         this.posColor = FeetPlace.INSTANCE.color.getValue();
         this.timer = new Timer();
         this.isAir = true;
         this.timer.reset();
      }
   }
}
