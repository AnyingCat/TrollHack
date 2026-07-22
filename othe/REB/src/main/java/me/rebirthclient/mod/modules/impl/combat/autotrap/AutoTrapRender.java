//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat.autotrap;

import java.awt.Color;
import java.util.HashMap;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.FadeUtils;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.RenderModule;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class AutoTrapRender extends RenderModule {
   public static HashMap<BlockPos, AutoTrapRender.placePosition> PlaceMap = new HashMap<>();

   public static void addBlock(BlockPos pos) {
      if (BlockUtil.canPlace2(pos)) {
         PlaceMap.put(pos, new AutoTrapRender.placePosition(pos));
      }
   }

   private void drawBlock(BlockPos pos, double alpha, Color color) {
      if (AutoTrap.INSTANCE.sync.getValue()) {
         color = AutoTrap.INSTANCE.color.getValue();
      }

      AxisAlignedBB axisAlignedBB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos).grow(0.002);
      RenderUtil.testESP(
         axisAlignedBB,
         color,
         AutoTrap.INSTANCE.lineWidth.getValue(),
         AutoTrap.INSTANCE.outline.getValue(),
         AutoTrap.INSTANCE.box.getValue(),
         (int)((double)color.getAlpha() * -alpha),
         AutoTrap.INSTANCE.depth.getValue()
      );
   }

   @Override
   public void onRender3D(Render3DEvent event) {
      if (AutoTrap.INSTANCE.render.getValue()) {
         if (!PlaceMap.isEmpty()) {
            boolean shouldClear = true;

            for(AutoTrapRender.placePosition placePosition : PlaceMap.values()) {
               if (!BlockUtil.canPlace2(placePosition.pos)) {
                  placePosition.isAir = false;
               }

               if (!placePosition.timer.passedMs((long)(AutoTrap.INSTANCE.delay.getValue() + 100)) && placePosition.isAir) {
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
      public final FadeUtils firstFade = new FadeUtils((long)AutoTrap.INSTANCE.fadeTime.getValue().intValue());
      public BlockPos pos;
      public Color posColor;
      public final Timer timer;
      public boolean isAir;

      public placePosition(BlockPos placePos) {
         this.pos = placePos;
         this.posColor = AutoTrap.INSTANCE.color.getValue();
         this.timer = new Timer();
         this.isAir = true;
         this.timer.reset();
      }
   }
}
