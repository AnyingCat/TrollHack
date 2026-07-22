package me.rebirthclient.mod.modules.impl.hud;

import java.awt.Color;
import java.util.ArrayList;
import me.rebirthclient.api.events.impl.Render2DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.FadeUtils;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;

public class Notifications extends Module {
   public static final ArrayList<Notifications.Notifys> notifyList = new ArrayList<>();
   private final Setting<Color> color = this.add(new Setting<>("Color", new Color(255, 255, 255)));
   private final Setting<Integer> notifyY = this.add(new Setting<>("Y", 18, 25, 500));

   public Notifications() {
      super("Notifications", "Notify toggle module", Category.HUD);
   }

   public static void add(String string) {
      notifyList.add(new Notifications.Notifys(string));
   }

   @Override
   public void onRender2D(Render2DEvent event) {
      int y = Managers.TEXT.scaledHeight - this.notifyY.getValue();
      int x = Managers.TEXT.scaledWidth;
      boolean canClear = true;
      int color = ColorUtil.toRGBA(this.color.getValue().getRed(), this.color.getValue().getGreen(), this.color.getValue().getBlue(), 255);
      if (!notifyList.isEmpty()) {
         for(Notifications.Notifys notifys : notifyList) {
            if (notifys != null && notifys.first != null && notifys.delayed >= 1) {
               canClear = false;
               if (notifys.delayed < 5 && !notifys.end) {
                  notifys.end = true;
                  notifys.endFade.reset();
               }

               y = (int)((double)y - 18.0 * notifys.yFade.easeOutQuad());
               String string = notifys.first;
               double x2;
               if (notifys.delayed < 5) {
                  x2 = (double)x - (double)(Managers.TEXT.getStringWidth(string) + 10) * (1.0 - notifys.endFade.easeOutQuad());
               } else {
                  x2 = (double)x - (double)(Managers.TEXT.getStringWidth(string) + 10) * notifys.firstFade.easeOutQuad();
               }

               if (this.color.getValue().getAlpha() > 5) {
                  RenderUtil.drawRectangleCorrectly(
                     (int)x2, y, 10 + Managers.TEXT.getStringWidth(string), 15, ColorUtil.toRGBA(20, 20, 20, this.color.getValue().getAlpha())
                  );
               }

               Managers.TEXT.drawString(string, (float)(5 + (int)x2), (float)(4 + y), this.color.getValue().getRGB(), true);
               if (notifys.delayed < 5) {
                  y = (int)((double)y + 18.0 * notifys.yFade.easeOutQuad() - 18.0 * (1.0 - notifys.endFade.easeOutQuad()));
               } else {
                  RenderUtil.drawRectangleCorrectly((int)x2, y + 14, (10 + Managers.TEXT.getStringWidth(string)) * (notifys.delayed - 4) / 62, 1, color);
               }
            }
         }

         if (canClear) {
            notifyList.clear();
         }
      }
   }

   @Override
   public void onUpdate() {
      for(Notifications.Notifys notifys : notifyList) {
         if (notifys != null && notifys.first != null) {
            --notifys.delayed;
         }
      }
   }

   @Override
   public void onDisable() {
      notifyList.clear();
   }

   @Override
   public void onEnable() {
      notifyList.clear();
   }

   public static class Notifys {
      public final FadeUtils firstFade = new FadeUtils(500L);
      public final FadeUtils endFade;
      public final FadeUtils yFade = new FadeUtils(500L);
      public final String first;
      public int delayed;
      public boolean end;

      public Notifys(String string) {
         this.endFade = new FadeUtils(350L);
         this.delayed = 55;
         this.first = string;
         this.firstFade.reset();
         this.yFade.reset();
         this.endFade.reset();
         this.end = false;
      }
   }
}
