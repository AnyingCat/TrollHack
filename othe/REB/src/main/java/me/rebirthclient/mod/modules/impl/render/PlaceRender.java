//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import java.util.HashMap;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.util.FadeUtils;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class PlaceRender extends Module {
   public static PlaceRender INSTANCE;
   public static HashMap<BlockPos, PlaceRender.placePosition> PlaceMap = new HashMap<>();
   private final Setting<Boolean> line = this.add(new Setting<>("Line", true));
   private final Setting<Boolean> box = this.add(new Setting<>("Box", false));
   private final Setting<Boolean> depth = this.add(new Setting<>("Depth", true));
   private final Setting<Float> lineWidth = this.add(new Setting<>("LineWidth", 3.0F, 0.1F, 3.0F));
   private final Setting<Integer> fadeTime = this.add(new Setting<>("FadeTime", 500, 0, 5000));
   public final Setting<Color> color = this.add(new Setting<>("Color", new Color(255, 255, 255, 100)));
   private final Setting<Boolean> sync = this.add(new Setting<>("Sync", true));

   public PlaceRender() {
      super("PlaceRender", "test", Category.RENDER);
      INSTANCE = this;
   }

   @Override
   public void onRender3D(Render3DEvent event) {
      boolean shouldClear = true;

      for(PlaceRender.placePosition placePosition : PlaceMap.values()) {
         if (placePosition.firstFade.easeOutQuad() != 1.0) {
            shouldClear = false;
            this.drawBlock(placePosition.pos, placePosition.firstFade.easeOutQuad() - 1.0, placePosition.posColor);
         }
      }

      if (shouldClear) {
         PlaceMap.clear();
      }
   }

   private void drawBlock(BlockPos pos, double alpha, Color color) {
      if (this.sync.getValue()) {
         color = this.color.getValue();
      }

      AxisAlignedBB axisAlignedBB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos).grow(0.002);
      RenderUtil.testESP(
         axisAlignedBB,
         color,
         this.lineWidth.getValue(),
         this.line.getValue(),
         this.box.getValue(),
         (int)((double)color.getAlpha() * -alpha),
         this.depth.getValue()
      );
   }

   public static class placePosition {
      public final FadeUtils firstFade = new FadeUtils((long)PlaceRender.INSTANCE.fadeTime.getValue().intValue());
      public BlockPos pos;
      public Color posColor;

      public placePosition(BlockPos placePos) {
         this.pos = placePos;
         this.posColor = PlaceRender.INSTANCE.color.getValue();
      }
   }
}
