//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import java.util.ArrayList;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.util.FadeUtils;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ExplosionSpawn extends Module {
   public static final ArrayList<ExplosionSpawn.Pos> spawnList = new ArrayList<>();
   public final Setting<Color> color = this.add(new Setting<>("Color", new Color(-557395713, true)));
   public final Setting<Float> size = this.add(new Setting<>("Max Size", 0.5F, 0.1F, 5.0F));
   public final Setting<Double> minSize = this.add(new Setting<>("Min Size", 0.1, 0.0, 1.0));
   public final Setting<Double> up = this.add(new Setting<>("Up", 0.1, 0.0, 1.0));
   public final Setting<Double> height = this.add(new Setting<>("Height", 0.5, -1.0, 1.0));
   private final Setting<Boolean> extra = this.add(new Setting<>("Extra", true).setParent());
   private final Setting<Integer> extraCount = this.add(new Setting<>("Counts", 5, 1, 10, v -> this.extra.isOpen()));
   private final Setting<Integer> distance = this.add(new Setting<>("Distance", 10, 0, 50, v -> this.extra.isOpen()));
   private final Setting<Integer> delay = this.add(new Setting<>("Delay", 300, 0, 1000));
   private final Setting<Integer> time = this.add(new Setting<>("Time", 500, 0, 5000));
   private final Setting<Integer> animationTime = this.add(new Setting<>("animationTime", 500, 0, 5000));
   private final Setting<Integer> fadeTime = this.add(new Setting<>("FadeTime", 200, 0, 5000));
   private final Timer timer = new Timer();

   public ExplosionSpawn() {
      super("ExplosionSpawn", "Draws a circle when a crystal spawn", Category.RENDER);
   }

   @SubscribeEvent
   public void onPacketReceive(PacketEvent.Send event) {
      if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock && this.timer.passedMs((long)this.delay.getValue().intValue())) {
         this.timer.reset();
         spawnList.add(
            new ExplosionSpawn.Pos(
               ((CPacketPlayerTryUseItemOnBlock)event.getPacket()).getPos(),
               this.animationTime.getValue(),
               this.time.getValue(),
               this.fadeTime.getValue()
            )
         );
      }
   }

   @Override
   public void onEnable() {
      spawnList.clear();
   }

   @Override
   public void onRender3D(Render3DEvent event) {
      if (spawnList.size() != 0) {
         boolean canClear = true;

         for(ExplosionSpawn.Pos spawnPos : spawnList) {
            if (!(spawnPos.time.easeOutQuad() >= 1.0)) {
               Color color = ColorUtil.injectAlpha(
                  this.color.getValue(), (int)((double)this.color.getValue().getAlpha() * Math.abs(1.0 - spawnPos.fadeTime.easeOutQuad()))
               );
               canClear = false;
               if (this.extra.getValue()) {
                  for(float i = 0.0F;
                     i < (float)this.extraCount.getValue().intValue() * 0.001F * (float)this.distance.getValue().intValue();
                     i += 0.001F * (float)this.distance.getValue().intValue()
                  ) {
                     RenderUtil.drawCircle(
                        (float)spawnPos.pos.getX(),
                        (float)(
                           (double)(spawnPos.pos.getY() + 1) + 1.0 * spawnPos.firstFade.easeOutQuad() * this.up.getValue() + this.height.getValue()
                        ),
                        (float)spawnPos.pos.getZ(),
                        (float)((double)this.size.getValue().floatValue() * spawnPos.firstFade.easeOutQuad() + this.minSize.getValue() - (double)i),
                        color
                     );
                  }
               } else {
                  RenderUtil.drawCircle(
                     (float)spawnPos.pos.getX(),
                     (float)((double)(spawnPos.pos.getY() + 1) + 1.0 * spawnPos.firstFade.easeOutQuad() * this.up.getValue() + this.height.getValue()),
                     (float)spawnPos.pos.getZ(),
                     (float)((double)this.size.getValue().floatValue() * spawnPos.firstFade.easeOutQuad() + this.minSize.getValue()),
                     color
                  );
               }
            }
         }

         if (canClear) {
            spawnList.clear();
         }
      }
   }

   private static class Pos {
      public final BlockPos pos;
      public final FadeUtils firstFade;
      public final FadeUtils time;
      public final FadeUtils fadeTime;

      public Pos(BlockPos pos, int animTime, int time, int fadeTime) {
         this.firstFade = new FadeUtils((long)animTime);
         this.time = new FadeUtils((long)time);
         this.fadeTime = new FadeUtils((long)fadeTime);
         this.pos = pos;
      }
   }
}
