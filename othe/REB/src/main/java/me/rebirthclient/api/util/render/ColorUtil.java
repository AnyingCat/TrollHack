package me.rebirthclient.api.util.render;

import java.awt.Color;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.modules.impl.client.ClickGui;

public class ColorUtil {
   public static int toARGB(int r, int g, int b, int a) {
      return new Color(r, g, b, a).getRGB();
   }

   public static int toRGBA(int r, int g, int b) {
      return toRGBA(r, g, b, 255);
   }

   public static Color getGradientOffset(Color color, Color color2, double d) {
      if (d > 1.0) {
         double d2 = d % 1.0;
         int n = (int)d;
         d = n % 2 == 0 ? d2 : 1.0 - d2;
      }

      double d2 = 1.0 - d;
      int n = (int)((double)color.getRed() * d2 + (double)color2.getRed() * d);
      int n2 = (int)((double)color.getGreen() * d2 + (double)color2.getGreen() * d);
      int n3 = (int)((double)color.getBlue() * d2 + (double)color2.getBlue() * d);
      return new Color(n, n2, n3);
   }

   public static int toRGBA(int r, int g, int b, int a) {
      return (r << 16) + (g << 8) + b + (a << 24);
   }

   public static int toRGBA(float r, float g, float b, float a) {
      return toRGBA((int)(r * 255.0F), (int)(g * 255.0F), (int)(b * 255.0F), (int)(a * 255.0F));
   }

   public static float[] toArray(int color) {
      return new float[]{
         (float)(color >> 16 & 0xFF) / 255.0F, (float)(color >> 8 & 0xFF) / 255.0F, (float)(color & 0xFF) / 255.0F, (float)(color >> 24 & 0xFF) / 255.0F
      };
   }

   public static int toHex(int r, int g, int b) {
      return 0xFF000000 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
   }

   public static Color injectAlpha(Color color, int alpha) {
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
   }

   public static int injectAlpha(int color, int alpha) {
      return toRGBA(new Color(color).getRed(), new Color(color).getGreen(), new Color(color).getBlue(), alpha);
   }

   public static Color rainbow(int delay) {
      double rainbowState = Math.ceil(
         (double)((ClickGui.INSTANCE.rainbowSpeed.getValue() == 0 ? System.currentTimeMillis() : (long)Managers.COLORS.rainbowProgress) + (long)delay) / 20.0
      );
      if (ClickGui.INSTANCE.rainbowMode.getValue() == ClickGui.Rainbow.DOUBLE) {
         return gradientColor(
            ClickGui.INSTANCE.color.getValue(),
            ClickGui.INSTANCE.secondColor.getValue(),
            (double)Math.abs(
               (
                        (float)((ClickGui.INSTANCE.rainbowSpeed.getValue() == 0 ? System.currentTimeMillis() : (long)Managers.COLORS.rainbowProgress) % 2000L)
                              / 1000.0F
                           + 20.0F / (float)(delay / 15 * 2 + 10) * 2.0F
                     )
                     % 2.0F
                  - 1.0F
            )
         );
      } else {
         return ClickGui.INSTANCE.rainbowMode.getValue() == ClickGui.Rainbow.PLAIN
            ? pulseColor(ClickGui.INSTANCE.color.getValue(), 50, delay)
            : Color.getHSBColor((float)(rainbowState % 360.0 / 360.0), ClickGui.INSTANCE.rainbowSaturation.getValue() / 255.0F, 1.0F);
      }
   }

   public static Color pulseColor(Color color, int index, int count) {
      float[] hsb = new float[3];
      Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
      float brightness = Math.abs(
         (
                  (float)(System.currentTimeMillis() % 2000L) / Float.intBitsToFloat(Float.floatToIntBits(0.0013786979F) ^ 2127476077)
                     + (float)index / (float)count * Float.intBitsToFloat(Float.floatToIntBits(0.09192204F) ^ 2109489567)
               )
               % Float.intBitsToFloat(Float.floatToIntBits(0.7858098F) ^ 2135501525)
            - Float.intBitsToFloat(Float.floatToIntBits(6.46708F) ^ 2135880274)
      );
      brightness = Float.intBitsToFloat(Float.floatToIntBits(18.996923F) ^ 2123889075)
         + Float.intBitsToFloat(Float.floatToIntBits(2.7958195F) ^ 2134044341) * brightness;
      hsb[2] = brightness % Float.intBitsToFloat(Float.floatToIntBits(0.8992331F) ^ 2137404452);
      return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
   }

   public static Color gradientColor(Color color1, Color color2, double offset) {
      if (offset > 1.0) {
         double left = offset % 1.0;
         int off = (int)offset;
         offset = off % 2 == 0 ? left : 1.0 - left;
      }

      double inverse_percent = 1.0 - offset;
      int redPart = (int)((double)color1.getRed() * inverse_percent + (double)color2.getRed() * offset);
      int greenPart = (int)((double)color1.getGreen() * inverse_percent + (double)color2.getGreen() * offset);
      int bluePart = (int)((double)color1.getBlue() * inverse_percent + (double)color2.getBlue() * offset);
      return new Color(redPart, greenPart, bluePart);
   }

   public static int toRGBA(Color color) {
      return toRGBA(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
   }

   public static Color interpolate(float value, Color start, Color end) {
      float sr = (float)start.getRed() / 255.0F;
      float sg = (float)start.getGreen() / 255.0F;
      float sb = (float)start.getBlue() / 255.0F;
      float sa = (float)start.getAlpha() / 255.0F;
      float er = (float)end.getRed() / 255.0F;
      float eg = (float)end.getGreen() / 255.0F;
      float eb = (float)end.getBlue() / 255.0F;
      float ea = (float)end.getAlpha() / 255.0F;
      float r = sr * value + er * (1.0F - value);
      float g = sg * value + eg * (1.0F - value);
      float b = sb * value + eb * (1.0F - value);
      float a = sa * value + ea * (1.0F - value);
      return new Color(r, g, b, a);
   }

   public static class Colors {
      public static final int WHITE = ColorUtil.toRGBA(255, 255, 255, 255);
      public static final int BLACK = ColorUtil.toRGBA(0, 0, 0, 255);
      public static final int RED = ColorUtil.toRGBA(255, 0, 0, 255);
      public static final int GREEN = ColorUtil.toRGBA(0, 255, 0, 255);
      public static final int BLUE = ColorUtil.toRGBA(0, 0, 255, 255);
      public static final int ORANGE = ColorUtil.toRGBA(255, 128, 0, 255);
      public static final int PURPLE = ColorUtil.toRGBA(163, 73, 163, 255);
      public static final int GRAY = ColorUtil.toRGBA(127, 127, 127, 255);
      public static final int DARK_RED = ColorUtil.toRGBA(64, 0, 0, 255);
      public static final int YELLOW = ColorUtil.toRGBA(255, 255, 0, 255);
      public static final int RAINBOW = Integer.MIN_VALUE;
   }
}
