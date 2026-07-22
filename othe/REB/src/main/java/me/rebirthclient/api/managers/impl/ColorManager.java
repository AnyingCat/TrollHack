package me.rebirthclient.api.managers.impl;

import java.awt.Color;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.mod.gui.click.Component;
import me.rebirthclient.mod.modules.impl.client.ClickGui;

public class ColorManager {
   public int rainbowProgress = 1;
   private Color current = new Color(-1);

   public boolean isRainbow() {
      return ClickGui.INSTANCE.rainbow.getValue();
   }

   public Color getCurrent() {
      return this.isRainbow() ? this.getRainbow() : this.current;
   }

   public Color getNormalCurrent() {
      return this.current;
   }

   public void setCurrent(Color color) {
      this.current = color;
   }

   public int getCurrentWithAlpha(int alpha) {
      return this.isRainbow()
         ? ColorUtil.toRGBA(ColorUtil.injectAlpha(this.getRainbow(), alpha))
         : ColorUtil.toRGBA(ColorUtil.injectAlpha(this.current, alpha));
   }

   public int getCurrentGui(int alpha) {
      return this.isRainbow()
         ? ColorUtil.rainbow(Component.counter1[0] * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB()
         : ColorUtil.toRGBA(ColorUtil.injectAlpha(this.current, alpha));
   }

   public Color getRainbow() {
      return ColorUtil.rainbow(ClickGui.INSTANCE.rainbowDelay.getValue());
   }

   public Color getFriendColor(int alpha) {
      return new Color(0, 191, 255, alpha);
   }
}
