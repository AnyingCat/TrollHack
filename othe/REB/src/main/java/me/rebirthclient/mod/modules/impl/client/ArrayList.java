package me.rebirthclient.mod.modules.impl.client;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import me.rebirthclient.api.events.impl.Render2DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.FadeUtils;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.gui.font.CustomFont;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;

public class ArrayList extends Module {
   private final Setting<Integer> listX = this.add(new Setting<>("X", 0, 0, 2000));
   private final Setting<Integer> listY = this.add(new Setting<>("Y", 10, 1, 2000));
   private final Setting<Integer> animationTime = this.add(new Setting<>("AnimationTime", 300, 0, 1000));
   private final Setting<Boolean> forgeHax = this.add(new Setting<>("ForgeHax", true));
   private final Setting<Boolean> reverse = this.add(new Setting<>("Reverse", false));
   private final Setting<Boolean> down = this.add(new Setting<>("Down", false));
   private final Setting<Boolean> fps = this.add(new Setting<>("Fps", true));
   private final Setting<Boolean> onlyDrawn = this.add(new Setting<>("OnlyDrawn", true));
   private final Setting<Boolean> onlyBind = this.add(new Setting<>("OnlyBind", false));
   private final Setting<Boolean> animationY = this.add(new Setting<>("AnimationY", true));
   public final Setting<ArrayList.ColorMode> colorMode = this.add(new Setting<>("ColorMode", ArrayList.ColorMode.Pulse));
   private final Setting<Integer> rainbowSpeed = this.register(
      new Setting<>(
         "RainbowSpeed",
         200,
         1,
         400,
         v -> this.colorMode.getValue() == ArrayList.ColorMode.Rainbow || this.colorMode.getValue() == ArrayList.ColorMode.PulseRainbow
      )
   );
   private final Setting<Float> saturation = this.register(
      new Setting<>(
         "Saturation",
         130.0F,
         1.0F,
         255.0F,
         v -> this.colorMode.getValue() == ArrayList.ColorMode.Rainbow || this.colorMode.getValue() == ArrayList.ColorMode.PulseRainbow
      )
   );
   private final Setting<Integer> pulseSpeed = this.register(
      new Setting<>(
         "PulseSpeed",
         100,
         1,
         400,
         v -> this.colorMode.getValue() == ArrayList.ColorMode.Pulse || this.colorMode.getValue() == ArrayList.ColorMode.PulseRainbow
      )
   );
   public final Setting<Integer> rainbowDelay = this.add(new Setting<>("Delay", 350, 0, 600, v -> this.colorMode.getValue() == ArrayList.ColorMode.Rainbow));
   private final Setting<Color> color = this.add(
      new Setting<>("Color", new Color(255, 255, 255, 255), v -> this.colorMode.getValue() != ArrayList.ColorMode.Rainbow).hideAlpha()
   );
   private final Setting<Boolean> rect = this.add(new Setting<>("Rect", true));
   private final Setting<Boolean> backGround = this.add(new Setting<>("BackGround", true).setParent());
   private final Setting<Boolean> bgSync = this.add(new Setting<>("Sync", false, v -> this.backGround.isOpen()));
   private final Setting<Color> bgColor = this.add(new Setting<>("BGColor", new Color(0, 0, 0, 100), v -> this.backGround.isOpen()));
   private List<ArrayList.Modules> Map = new java.util.ArrayList<>();
   private static boolean needUpdate = false;
   private boolean font = false;
   private CustomFont customFont;
   int progress = 0;
   int pulseProgress = 0;

   public ArrayList() {
      super("ArrayList", "", Category.CLIENT);
   }

   @Override
   public void onLoad() {
      for(Module module : Managers.MODULES.getModules()) {
         this.Map.add(new ArrayList.Modules(module));
      }
   }

   @Override
   public void onLogin() {
      needUpdate = true;
   }

   @Override
   public void onTick() {
      this.progress += this.rainbowSpeed.getValue();
      this.pulseProgress += this.pulseSpeed.getValue();
   }

   @SubscribeEvent
   public void onRender(RenderTickEvent event) {
      if (!this.fps.getValue()) {
         this.doRender();
      }
   }

   @Override
   public void onRender2D(Render2DEvent event) {
      if (this.fps.getValue()) {
         this.doRender();
      }
   }

   private void doRender() {
      if (!fullNullCheck()) {
         if (this.customFont != Managers.TEXT.customFont) {
            this.customFont = Managers.TEXT.customFont;
            needUpdate = true;
         }

         if (this.font) {
            if (FontMod.INSTANCE.isOff()) {
               this.font = false;
               needUpdate = true;
            }
         } else if (FontMod.INSTANCE.isOn()) {
            this.font = true;
            needUpdate = true;
         }

         if (needUpdate) {
            needUpdate = false;
            this.Map = this.Map
               .stream()
               .sorted(Comparator.comparing(module -> Managers.TEXT.getStringWidth(module.module.getArrayListInfo()) * -1))
               .collect(Collectors.toList());
         }

         int lastY = this.down.getValue() ? Managers.TEXT.scaledHeight - this.listY.getValue() - Managers.TEXT.getFontHeight() : this.listY.getValue();
         int counter = 20;

         for(ArrayList.Modules modules : this.Map) {
            if ((modules.module.isDrawn() || !this.onlyDrawn.getValue()) && (!this.onlyBind.getValue() || modules.module.getBind().getKey() != -1)) {
               modules.fade.setLength((long)this.animationTime.getValue().intValue());
               if (modules.module.isOn()) {
                  modules.enable();
               } else {
                  modules.disable();
               }

               modules.updateName();
               int x;
               int y;
               if (!this.reverse.getValue()) {
                  if (modules.isEnabled) {
                     double size = modules.fade.easeOutQuad();
                     x = (int)((double)Managers.TEXT.getStringWidth(modules.module.getArrayListInfo() + this.getSuffix()) * size);
                     y = (int)((double)Managers.TEXT.getFontHeight2() * size);
                     modules.lastY = y;
                     modules.lastX = x;
                  } else {
                     double size = Math.abs(modules.fade.easeOutQuad() - 1.0);
                     x = (int)((double)modules.lastX * size);
                     y = (int)((double)modules.lastY * size);
                     if (size <= 0.0) {
                        continue;
                     }
                  }
               } else if (modules.isEnabled) {
                  double size = Math.abs(modules.fade.easeOutQuad() - 1.0);
                  x = (int)((double)Managers.TEXT.getStringWidth(modules.module.getArrayListInfo() + this.getSuffix()) * size);
                  size = modules.fade.easeOutQuad();
                  y = (int)((double)Managers.TEXT.getFontHeight2() * size);
                  modules.lastY = y;
                  modules.lastX = x;
               } else {
                  double size = modules.fade.easeOutQuad();
                  x = (int)((double)Managers.TEXT.getStringWidth(modules.module.getArrayListInfo() + this.getSuffix()) * size) + modules.lastX;
                  size = Math.abs(modules.fade.easeOutQuad() - 1.0);
                  y = (int)((double)modules.lastY * size);
                  if (size <= 0.0 || x >= Managers.TEXT.getStringWidth(modules.module.getArrayListInfo() + this.getSuffix())) {
                     continue;
                  }
               }

               x = (int)((double)x + 20.0 * Math.abs(modules.change.easeOutQuad() - 1.0));
               ++counter;
               int showX;
               if (!this.reverse.getValue()) {
                  showX = Managers.TEXT.scaledWidth - x - this.listX.getValue() - (this.rect.getValue() ? 2 : 0);
                  if (this.backGround.getValue()) {
                     RenderUtil.drawRect(
                        (float)showX,
                        (float)(lastY - (this.animationY.getValue() ? Math.abs(y - Managers.TEXT.getFontHeight2()) : 0) - 1),
                        (float)(Managers.TEXT.scaledWidth - this.listX.getValue()),
                        (float)(lastY - (this.animationY.getValue() ? Math.abs(y - Managers.TEXT.getFontHeight2()) : 0) + Managers.TEXT.getFontHeight2() - 1),
                        this.bgSync.getValue()
                           ? ColorUtil.injectAlpha(this.getColor(counter), this.bgColor.getValue().getAlpha())
                           : this.bgColor.getValue().getRGB()
                     );
                  }

                  if (this.rect.getValue()) {
                     RenderUtil.drawRect(
                        (float)(Managers.TEXT.scaledWidth - this.listX.getValue() - 1),
                        (float)(lastY - (this.animationY.getValue() ? Math.abs(y - Managers.TEXT.getFontHeight2()) : 0) - 1),
                        (float)(Managers.TEXT.scaledWidth - this.listX.getValue()),
                        (float)(lastY - (this.animationY.getValue() ? Math.abs(y - Managers.TEXT.getFontHeight2()) : 0) + Managers.TEXT.getFontHeight2()),
                        this.getColor(counter)
                     );
                  }
               } else {
                  showX = -x + this.listX.getValue() + (this.rect.getValue() ? 2 : 0);
                  if (this.rect.getValue()) {
                     RenderUtil.drawRect(
                        (float)this.listX.getValue().intValue(),
                        (float)(lastY - (this.animationY.getValue() ? Math.abs(y - Managers.TEXT.getFontHeight2()) : 0) - 1),
                        (float)(this.listX.getValue() + 1),
                        (float)(lastY - (this.animationY.getValue() ? Math.abs(y - Managers.TEXT.getFontHeight2()) : 0) + Managers.TEXT.getFontHeight2() - 1),
                        this.getColor(counter)
                     );
                  }

                  if (this.backGround.getValue()) {
                     RenderUtil.drawRect(
                        (float)this.listX.getValue().intValue(),
                        (float)(lastY - (this.animationY.getValue() ? Math.abs(y - Managers.TEXT.getFontHeight2()) : 0) - 1),
                        (float)(
                           Math.abs(x - Managers.TEXT.getStringWidth(modules.module.getArrayListInfo() + this.getSuffix())) + (this.rect.getValue() ? 2 : 0)
                        ),
                        (float)(lastY - (this.animationY.getValue() ? Math.abs(y - Managers.TEXT.getFontHeight2()) : 0) + Managers.TEXT.getFontHeight2() - 1),
                        this.bgSync.getValue()
                           ? ColorUtil.injectAlpha(this.getColor(counter), this.bgColor.getValue().getAlpha())
                           : this.bgColor.getValue().getRGB()
                     );
                  }
               }

               Managers.TEXT
                  .drawString(
                     modules.module.getArrayListInfo() + this.getSuffix(),
                     (float)showX,
                     (float)(lastY - (this.animationY.getValue() ? Math.abs(y - Managers.TEXT.getFontHeight2()) : 0)),
                     this.getColor(counter),
                     true
                  );
               if (this.down.getValue()) {
                  lastY -= y;
               } else {
                  lastY += y;
               }
            }
         }
      }
   }

   private String getSuffix() {
      return this.forgeHax.getValue() ? "§r<" : "";
   }

   private int getColor(int counter) {
      return this.colorMode.getValue() != ArrayList.ColorMode.Custom ? this.rainbow(counter).getRGB() : this.color.getValue().getRGB();
   }

   private Color rainbow(int delay) {
      double rainbowState = Math.ceil((double)(this.progress + delay * this.rainbowDelay.getValue()) / 20.0);
      if (this.colorMode.getValue() == ArrayList.ColorMode.Pulse) {
         return this.pulseColor(this.color.getValue(), delay);
      } else {
         return this.colorMode.getValue() == ArrayList.ColorMode.Rainbow
            ? Color.getHSBColor((float)(rainbowState % 360.0 / 360.0), this.saturation.getValue() / 255.0F, 1.0F)
            : this.pulseColor(Color.getHSBColor((float)(rainbowState % 360.0 / 360.0), this.saturation.getValue() / 255.0F, 1.0F), delay);
      }
   }

   private Color pulseColor(Color color, int index) {
      float[] hsb = new float[3];
      Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
      float brightness = Math.abs(
         (
                  (float)((long)this.pulseProgress % 2000L) / Float.intBitsToFloat(Float.floatToIntBits(0.0013786979F) ^ 2127476077)
                     + (float)index / 14.0F * Float.intBitsToFloat(Float.floatToIntBits(0.09192204F) ^ 2109489567)
               )
               % Float.intBitsToFloat(Float.floatToIntBits(0.7858098F) ^ 2135501525)
            - Float.intBitsToFloat(Float.floatToIntBits(6.46708F) ^ 2135880274)
      );
      brightness = Float.intBitsToFloat(Float.floatToIntBits(18.996923F) ^ 2123889075)
         + Float.intBitsToFloat(Float.floatToIntBits(2.7958195F) ^ 2134044341) * brightness;
      hsb[2] = brightness % Float.intBitsToFloat(Float.floatToIntBits(0.8992331F) ^ 2137404452);
      return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
   }

   private static enum ColorMode {
      Custom,
      Pulse,
      Rainbow,
      PulseRainbow;
   }

   public static class Modules {
      public final FadeUtils fade;
      public final FadeUtils change;
      public boolean isEnabled = false;
      public Module module;
      public int lastX = 0;
      public int lastY = 0;
      public String lastName;

      public Modules(Module module) {
         this.module = module;
         this.fade = new FadeUtils(500L);
         this.change = new FadeUtils(200L);
         this.lastName = module.getArrayListInfo();
      }

      public void enable() {
         if (!this.isEnabled) {
            this.isEnabled = true;
            this.fade.reset();
         }
      }

      public void disable() {
         if (this.isEnabled) {
            this.isEnabled = false;
            this.fade.reset();
         }
      }

      public void updateName() {
         if (!this.lastName.equals(this.module.getArrayListInfo())) {
            ArrayList.needUpdate = true;
            this.lastName = this.module.getArrayListInfo();
            this.change.reset();
         }
      }
   }
}
