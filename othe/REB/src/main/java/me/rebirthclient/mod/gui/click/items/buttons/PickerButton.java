//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.gui.click.items.buttons;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Objects;
import me.rebirthclient.Rebirth;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.commands.Command;
import me.rebirthclient.mod.gui.screen.Gui;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.SoundEvents;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class PickerButton extends Button {
   public static final Tessellator tessellator = Tessellator.getInstance();
   public static final BufferBuilder builder = tessellator.getBuffer();
   final Setting setting;
   boolean pickingColor;
   boolean pickingHue;
   boolean pickingAlpha;
   private Color finalColor;
   private float hueX;
   private float prevHueX;
   private float alphaX;
   private float prevAlphaX;

   public PickerButton(Setting setting) {
      super(setting.getName());
      this.setting = setting;
      this.finalColor = (Color)setting.getValue();
   }

   public static boolean mouseOver(int minX, int minY, int maxX, int maxY, int mX, int mY) {
      return mX >= minX && mY >= minY && mX <= maxX && mY <= maxY;
   }

   public static Color getColor(Color color, float alpha) {
      float red = (float)color.getRed() / 255.0F;
      float green = (float)color.getGreen() / 255.0F;
      float blue = (float)color.getBlue() / 255.0F;
      return new Color(red, green, blue, alpha);
   }

   public static void drawPickerBase(int pickerX, int pickerY, int pickerWidth, int pickerHeight, float red, float green, float blue, float alpha) {
      GL11.glEnable(3042);
      GL11.glDisable(3553);
      GL11.glBlendFunc(770, 771);
      GL11.glShadeModel(7425);
      GL11.glBegin(9);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glVertex2f((float)pickerX, (float)pickerY);
      GL11.glVertex2f((float)pickerX, (float)(pickerY + pickerHeight));
      GL11.glColor4f(red, green, blue, alpha);
      GL11.glVertex2f((float)(pickerX + pickerWidth), (float)(pickerY + pickerHeight));
      GL11.glVertex2f((float)(pickerX + pickerWidth), (float)pickerY);
      GL11.glEnd();
      GL11.glDisable(3008);
      GL11.glBegin(9);
      GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
      GL11.glVertex2f((float)pickerX, (float)pickerY);
      GL11.glColor4f(0.0F, 0.0F, 0.0F, 1.0F);
      GL11.glVertex2f((float)pickerX, (float)(pickerY + pickerHeight));
      GL11.glVertex2f((float)(pickerX + pickerWidth), (float)(pickerY + pickerHeight));
      GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
      GL11.glVertex2f((float)(pickerX + pickerWidth), (float)pickerY);
      GL11.glEnd();
      GL11.glEnable(3008);
      GL11.glShadeModel(7424);
      GL11.glEnable(3553);
      GL11.glDisable(3042);
   }

   public static void drawGradientRect(double leftpos, double top, double right, double bottom, int col1, int col2) {
      float f = (float)(col1 >> 24 & 0xFF) / 255.0F;
      float f2 = (float)(col1 >> 16 & 0xFF) / 255.0F;
      float f3 = (float)(col1 >> 8 & 0xFF) / 255.0F;
      float f4 = (float)(col1 & 0xFF) / 255.0F;
      float f5 = (float)(col2 >> 24 & 0xFF) / 255.0F;
      float f6 = (float)(col2 >> 16 & 0xFF) / 255.0F;
      float f7 = (float)(col2 >> 8 & 0xFF) / 255.0F;
      float f8 = (float)(col2 & 0xFF) / 255.0F;
      GL11.glEnable(3042);
      GL11.glDisable(3553);
      GL11.glBlendFunc(770, 771);
      GL11.glEnable(2848);
      GL11.glShadeModel(7425);
      GL11.glPushMatrix();
      GL11.glBegin(7);
      GL11.glColor4f(f2, f3, f4, f);
      GL11.glVertex2d(leftpos, top);
      GL11.glVertex2d(leftpos, bottom);
      GL11.glColor4f(f6, f7, f8, f5);
      GL11.glVertex2d(right, bottom);
      GL11.glVertex2d(right, top);
      GL11.glEnd();
      GL11.glPopMatrix();
      GL11.glEnable(3553);
      GL11.glDisable(3042);
      GL11.glDisable(2848);
      GL11.glShadeModel(7424);
   }

   public static void drawLeftGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
      GlStateManager.shadeModel(7425);
      builder.begin(7, DefaultVertexFormats.POSITION_COLOR);
      builder.pos((double)right, (double)top, 0.0)
         .color(
            (float)(endColor >> 24 & 0xFF) / 255.0F,
            (float)(endColor >> 16 & 0xFF) / 255.0F,
            (float)(endColor >> 8 & 0xFF) / 255.0F,
            (float)(endColor >> 24 & 0xFF) / 255.0F
         )
         .endVertex();
      builder.pos((double)left, (double)top, 0.0)
         .color(
            (float)(startColor >> 16 & 0xFF) / 255.0F,
            (float)(startColor >> 8 & 0xFF) / 255.0F,
            (float)(startColor & 0xFF) / 255.0F,
            (float)(startColor >> 24 & 0xFF) / 255.0F
         )
         .endVertex();
      builder.pos((double)left, (double)bottom, 0.0)
         .color(
            (float)(startColor >> 16 & 0xFF) / 255.0F,
            (float)(startColor >> 8 & 0xFF) / 255.0F,
            (float)(startColor & 0xFF) / 255.0F,
            (float)(startColor >> 24 & 0xFF) / 255.0F
         )
         .endVertex();
      builder.pos((double)right, (double)bottom, 0.0)
         .color(
            (float)(endColor >> 24 & 0xFF) / 255.0F,
            (float)(endColor >> 16 & 0xFF) / 255.0F,
            (float)(endColor >> 8 & 0xFF) / 255.0F,
            (float)(endColor >> 24 & 0xFF) / 255.0F
         )
         .endVertex();
      tessellator.draw();
      GlStateManager.shadeModel(7424);
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
   }

   public static void gradient(int minX, int minY, int maxX, int maxY, int startColor, int endColor, boolean left) {
      if (left) {
         float startA = (float)(startColor >> 24 & 0xFF) / 255.0F;
         float startR = (float)(startColor >> 16 & 0xFF) / 255.0F;
         float startG = (float)(startColor >> 8 & 0xFF) / 255.0F;
         float startB = (float)(startColor & 0xFF) / 255.0F;
         float endA = (float)(endColor >> 24 & 0xFF) / 255.0F;
         float endR = (float)(endColor >> 16 & 0xFF) / 255.0F;
         float endG = (float)(endColor >> 8 & 0xFF) / 255.0F;
         float endB = (float)(endColor & 0xFF) / 255.0F;
         GL11.glEnable(3042);
         GL11.glDisable(3553);
         GL11.glBlendFunc(770, 771);
         GL11.glShadeModel(7425);
         GL11.glBegin(9);
         GL11.glColor4f(startR, startG, startB, startA);
         GL11.glVertex2f((float)minX, (float)minY);
         GL11.glVertex2f((float)minX, (float)maxY);
         GL11.glColor4f(endR, endG, endB, endA);
         GL11.glVertex2f((float)maxX, (float)maxY);
         GL11.glVertex2f((float)maxX, (float)minY);
         GL11.glEnd();
         GL11.glShadeModel(7424);
         GL11.glEnable(3553);
         GL11.glDisable(3042);
      } else {
         drawGradientRect((double)minX, (double)minY, (double)maxX, (double)maxY, startColor, endColor);
      }
   }

   public static int gradientColor(int color, int percentage) {
      int r = ((color & 0xFF0000) >> 16) * (100 + percentage) / 100;
      int g = ((color & 0xFF00) >> 8) * (100 + percentage) / 100;
      int b = (color & 0xFF) * (100 + percentage) / 100;
      return new Color(r, g, b).hashCode();
   }

   public static void drawGradientRect(float left, float top, float right, float bottom, int startColor, int endColor, boolean hovered) {
      if (hovered) {
         startColor = gradientColor(startColor, -20);
         endColor = gradientColor(endColor, -20);
      }

      float c = (float)(startColor >> 24 & 0xFF) / 255.0F;
      float c1 = (float)(startColor >> 16 & 0xFF) / 255.0F;
      float c2 = (float)(startColor >> 8 & 0xFF) / 255.0F;
      float c3 = (float)(startColor & 0xFF) / 255.0F;
      float c4 = (float)(endColor >> 24 & 0xFF) / 255.0F;
      float c5 = (float)(endColor >> 16 & 0xFF) / 255.0F;
      float c6 = (float)(endColor >> 8 & 0xFF) / 255.0F;
      float c7 = (float)(endColor & 0xFF) / 255.0F;
      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
      GlStateManager.shadeModel(7425);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos((double)right, (double)top, 0.0).color(c1, c2, c3, c).endVertex();
      bufferbuilder.pos((double)left, (double)top, 0.0).color(c1, c2, c3, c).endVertex();
      bufferbuilder.pos((double)left, (double)bottom, 0.0).color(c5, c6, c7, c4).endVertex();
      bufferbuilder.pos((double)right, (double)bottom, 0.0).color(c5, c6, c7, c4).endVertex();
      tessellator.draw();
      GlStateManager.shadeModel(7424);
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
   }

   public static String readClipboard() {
      try {
         return (String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
      } catch (IOException | UnsupportedFlavorException var1) {
         return null;
      }
   }

   public static void drawOutlineRect(double left, double top, double right, double bottom, Color color, float lineWidth) {
      if (left < right) {
         double i = left;
         left = right;
         right = i;
      }

      if (top < bottom) {
         double j = top;
         top = bottom;
         bottom = j;
      }

      float f3 = (float)(color.getRGB() >> 24 & 0xFF) / 255.0F;
      float f = (float)(color.getRGB() >> 16 & 0xFF) / 255.0F;
      float f1 = (float)(color.getRGB() >> 8 & 0xFF) / 255.0F;
      float f2 = (float)(color.getRGB() & 0xFF) / 255.0F;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      GlStateManager.enableBlend();
      GL11.glPolygonMode(1032, 6913);
      GL11.glLineWidth(lineWidth);
      GlStateManager.disableTexture2D();
      GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
      GlStateManager.color(f, f1, f2, f3);
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
      bufferbuilder.pos(left, bottom, 0.0).endVertex();
      bufferbuilder.pos(right, bottom, 0.0).endVertex();
      bufferbuilder.pos(right, top, 0.0).endVertex();
      bufferbuilder.pos(left, top, 0.0).endVertex();
      tessellator.draw();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GL11.glPolygonMode(1032, 6914);
   }

   @Override
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      boolean newStyle = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.NEW;
      boolean future = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.FUTURE;
      boolean dotgod = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.DOTGOD;
      RenderUtil.drawRect(
         this.x,
         this.y,
         this.x + (float)this.width + 7.4F,
         this.y + (float)this.height - 0.5F,
         !future && !dotgod
            ? (
               this.setting.hasBoolean
                  ? (
                     this.setting.booleanValue
                        ? (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(120) : Managers.COLORS.getCurrentWithAlpha(200))
                        : (!this.isHovering(mouseX, mouseY) ? 290805077 : -2007673515)
                  )
                  : (!this.isHovering(mouseX, mouseY) ? 290805077 : -2007673515)
            )
            : (
               this.setting.hasBoolean && this.setting.booleanValue
                  ? (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(65) : Managers.COLORS.getCurrentWithAlpha(90))
                  : (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(26) : Managers.COLORS.getCurrentWithAlpha(55))
            )
      );

      try {
         RenderUtil.drawRect(
            this.x - 1.5F + (float)this.width + 0.6F - 0.5F,
            this.y + 5.0F,
            this.x + (float)this.width + 7.0F - 2.5F,
            this.y + (float)this.height - 4.0F,
            this.setting.isRainbow && !this.setting.noRainbow
               ? ColorUtil.injectAlpha(Managers.COLORS.getRainbow(), this.finalColor.getAlpha()).getRGB()
               : this.finalColor.getRGB()
         );
      } catch (Exception var8) {
         var8.printStackTrace();
      }

      Managers.TEXT
         .drawStringWithShadow(
            !newStyle && !dotgod ? this.getName() : this.getName().toLowerCase(),
            this.x + 2.3F,
            this.y - 1.7F - (float)Gui.INSTANCE.getTextOffset(),
            dotgod && this.setting.hasBoolean && this.setting.booleanValue ? Managers.COLORS.getCurrentGui(240) : (dotgod ? 11579568 : -1)
         );
      if (this.setting.open) {
         this.drawPicker(
            this.setting,
            (int)this.x,
            (int)this.y + 15,
            (int)this.x,
            this.setting.hideAlpha ? (int)this.y + 100 : (int)this.y + 103,
            (int)this.x,
            (int)this.y + 95,
            mouseX,
            mouseY
         );
         Managers.TEXT.drawStringWithShadow("copy", this.x + 2.3F, this.y + 113.0F, this.isInsideCopy(mouseX, mouseY) ? -1 : -5592406);
         Managers.TEXT
            .drawStringWithShadow(
               "paste",
               this.x + (float)this.width - 2.3F - (float)Managers.TEXT.getStringWidth("paste") + 11.7F - 4.6F,
               this.y + 113.0F,
               this.isInsidePaste(mouseX, mouseY) ? -1 : -5592406
            );
         if (!this.setting.noRainbow) {
            Managers.TEXT
               .drawStringWithShadow(
                  "rainbow",
                  this.x + 2.3F,
                  this.y + 124.0F,
                  this.setting.isRainbow ? Managers.COLORS.getRainbow().getRGB() : (this.isInsideRainbow(mouseX, mouseY) ? -1 : -5592406)
               );
         }

         this.setting.setValue(this.finalColor);
      }
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
      if (mouseButton == 1 && this.isHovering(mouseX, mouseY)) {
         mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
         this.setting.open = !this.setting.open;
      }

      if (mouseButton == 0 && this.isHovering(mouseX, mouseY)) {
         mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
         this.setting.booleanValue = !this.setting.booleanValue;
      }

      if (mouseButton == 0 && this.isInsideRainbow(mouseX, mouseY) && this.setting.open) {
         this.setting.isRainbow = !this.setting.isRainbow;
      }

      if (mouseButton == 0 && this.isInsideCopy(mouseX, mouseY) && this.setting.open) {
         mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
         String hex = String.format(
            "#%02x%02x%02x%02x", this.finalColor.getAlpha(), this.finalColor.getRed(), this.finalColor.getGreen(), this.finalColor.getBlue()
         );
         StringSelection selection = new StringSelection(hex);
         Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
         clipboard.setContents(selection, selection);
         Command.sendMessage("Copied the color to your clipboard.");
      }

      if (mouseButton == 0 && this.isInsidePaste(mouseX, mouseY) && this.setting.open) {
         try {
            if (readClipboard() != null) {
               if (Objects.requireNonNull(readClipboard()).startsWith("#")) {
                  String hex = Objects.requireNonNull(readClipboard());
                  int a = Integer.valueOf(hex.substring(1, 3), 16);
                  int r = Integer.valueOf(hex.substring(3, 5), 16);
                  int g = Integer.valueOf(hex.substring(5, 7), 16);
                  int b = Integer.valueOf(hex.substring(7, 9), 16);
                  if (this.setting.hideAlpha) {
                     this.setting.setValue(new Color(r, g, b));
                  } else {
                     this.setting.setValue(new Color(r, g, b, a));
                  }
               } else {
                  String[] color = readClipboard().split(",");
                  this.setting.setValue(new Color(Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2])));
               }
            }
         } catch (NumberFormatException var9) {
            Command.sendMessage("Bad color format! Use Hex (#FFFFFFFF)");
         }
      }
   }

   @Override
   public void update() {
      this.setHidden(!this.setting.isVisible());
   }

   @Override
   public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
      this.pickingAlpha = false;
      this.pickingHue = false;
      this.pickingColor = false;
   }

   public boolean isInsideCopy(int mouseX, int mouseY) {
      return mouseOver(
         (int)((float)((int)this.x) + 2.3F),
         (int)this.y + 113,
         (int)((float)((int)this.x) + 2.3F) + Managers.TEXT.getStringWidth("copy"),
         (int)(this.y + 112.0F) + Managers.TEXT.getFontHeight(),
         mouseX,
         mouseY
      );
   }

   public boolean isInsideRainbow(int mouseX, int mouseY) {
      return mouseOver(
         (int)((float)((int)this.x) + 2.3F),
         (int)this.y + 124,
         (int)((float)((int)this.x) + 2.3F) + Managers.TEXT.getStringWidth("rainbow"),
         (int)(this.y + 123.0F) + Managers.TEXT.getFontHeight(),
         mouseX,
         mouseY
      );
   }

   public boolean isInsidePaste(int mouseX, int mouseY) {
      return mouseOver(
         (int)(this.x + (float)this.width - 2.3F - (float)Managers.TEXT.getStringWidth("paste") + 11.7F - 4.6F),
         (int)this.y + 113,
         (int)(this.x + (float)this.width - 2.3F - (float)Managers.TEXT.getStringWidth("paste") + 11.7F - 4.6F) + Managers.TEXT.getStringWidth("paste"),
         (int)(this.y + 112.0F) + Managers.TEXT.getFontHeight(),
         mouseX,
         mouseY
      );
   }

   public void drawPicker(
      Setting setting, int pickerX, int pickerY, int hueSliderX, int hueSliderY, int alphaSliderX, int alphaSliderY, int mouseX, int mouseY
   ) {
      float[] color = new float[]{0.0F, 0.0F, 0.0F, 0.0F};

      try {
         color = new float[]{
            Color.RGBtoHSB(((Color)setting.getValue()).getRed(), ((Color)setting.getValue()).getGreen(), ((Color)setting.getValue()).getBlue(), null)[0],
            Color.RGBtoHSB(((Color)setting.getValue()).getRed(), ((Color)setting.getValue()).getGreen(), ((Color)setting.getValue()).getBlue(), null)[1],
            Color.RGBtoHSB(((Color)setting.getValue()).getRed(), ((Color)setting.getValue()).getGreen(), ((Color)setting.getValue()).getBlue(), null)[2],
            (float)((Color)setting.getValue()).getAlpha() / 255.0F
         };
      } catch (Exception var23) {
         Rebirth.LOGGER.info("rebirth color picker says it's a bad color!");
      }

      int pickerWidth = (int)((float)this.width + 7.4F);
      int pickerHeight = 78;
      int hueSliderWidth = pickerWidth + 3;
      int hueSliderHeight = 7;
      int alphaSliderHeight = 7;
      if (this.pickingColor && (!Mouse.isButtonDown(0) || !mouseOver(pickerX, pickerY, pickerX + pickerWidth, pickerY + pickerHeight, mouseX, mouseY))) {
         this.pickingColor = false;
      }

      if (this.pickingHue
         && (!Mouse.isButtonDown(0) || !mouseOver(hueSliderX, hueSliderY, hueSliderX + hueSliderWidth, hueSliderY + hueSliderHeight, mouseX, mouseY))) {
         this.pickingHue = false;
      }

      if (this.pickingAlpha
         && (!Mouse.isButtonDown(0) || !mouseOver(alphaSliderX, alphaSliderY, alphaSliderX + pickerWidth, alphaSliderY + alphaSliderHeight, mouseX, mouseY))) {
         this.pickingAlpha = false;
      }

      if (Mouse.isButtonDown(0) && mouseOver(pickerX, pickerY, pickerX + pickerWidth, pickerY + pickerHeight, mouseX, mouseY)) {
         this.pickingColor = true;
      }

      if (Mouse.isButtonDown(0) && mouseOver(hueSliderX, hueSliderY, hueSliderX + hueSliderWidth, hueSliderY + hueSliderHeight, mouseX, mouseY)) {
         this.pickingHue = true;
      }

      if (Mouse.isButtonDown(0) && mouseOver(alphaSliderX, alphaSliderY, alphaSliderX + pickerWidth, alphaSliderY + alphaSliderHeight, mouseX, mouseY)) {
         this.pickingAlpha = true;
      }

      if (this.pickingHue) {
         float restrictedX = (float)Math.min(Math.max(hueSliderX, mouseX), hueSliderX + hueSliderWidth);
         color[0] = (restrictedX - (float)hueSliderX) / (float)hueSliderWidth;
      }

      if (this.pickingAlpha && !setting.hideAlpha) {
         float restrictedX = (float)Math.min(Math.max(alphaSliderX, mouseX), alphaSliderX + pickerWidth);
         color[3] = 1.0F - (restrictedX - (float)alphaSliderX) / (float)pickerWidth;
      }

      if (this.pickingColor) {
         float restrictedX = (float)Math.min(Math.max(pickerX, mouseX), pickerX + pickerWidth);
         float restrictedY = (float)Math.min(Math.max(pickerY, mouseY), pickerY + pickerHeight);
         color[1] = (restrictedX - (float)pickerX) / (float)pickerWidth;
         color[2] = 1.0F - (restrictedY - (float)pickerY) / (float)pickerHeight;
      }

      int selectedColor = Color.HSBtoRGB(color[0], 1.0F, 1.0F);
      float selectedRed = (float)(selectedColor >> 16 & 0xFF) / 255.0F;
      float selectedGreen = (float)(selectedColor >> 8 & 0xFF) / 255.0F;
      float selectedBlue = (float)(selectedColor & 0xFF) / 255.0F;
      drawPickerBase(pickerX, pickerY, pickerWidth, pickerHeight, selectedRed, selectedGreen, selectedBlue, color[3]);
      this.drawHueSlider(hueSliderX, hueSliderY, pickerWidth + 1, hueSliderHeight, color[0]);
      int cursorX = (int)((float)pickerX + color[1] * (float)pickerWidth);
      int cursorY = (int)((float)(pickerY + pickerHeight) - color[2] * (float)pickerHeight);
      if (this.pickingColor) {
         RenderUtil.drawCircle((float)cursorX, (float)cursorY, 6.4F, Color.BLACK.getRGB());
         RenderUtil.drawCircle(
            (float)cursorX, (float)cursorY, 6.0F, ColorUtil.toARGB(this.finalColor.getRed(), this.finalColor.getGreen(), this.finalColor.getBlue(), 255)
         );
      } else {
         RenderUtil.drawCircle((float)cursorX, (float)cursorY, 3.4F, Color.BLACK.getRGB());
         RenderUtil.drawCircle((float)cursorX, (float)cursorY, 3.0F, -1);
      }

      if (!setting.hideAlpha) {
         this.drawAlphaSlider(alphaSliderX, alphaSliderY, pickerWidth - 1, alphaSliderHeight, selectedRed, selectedGreen, selectedBlue, color[3]);
      }

      this.finalColor = getColor(new Color(Color.HSBtoRGB(color[0], color[1], color[2])), color[3]);
   }

   public void drawHueSlider(int x, int y, int width, int height, float hue) {
      int step = 0;
      if (height > width) {
         RenderUtil.drawRect((float)x, (float)y, (float)(x + width), (float)(y + 4), -65536);
         y += 4;

         for(int colorIndex = 0; colorIndex < 6; ++colorIndex) {
            int previousStep = Color.HSBtoRGB((float)step / 6.0F, 1.0F, 1.0F);
            int nextStep = Color.HSBtoRGB((float)(step + 1) / 6.0F, 1.0F, 1.0F);
            drawGradientRect(
               (float)x,
               (float)y + (float)step * ((float)height / 6.0F),
               (float)(x + width),
               (float)y + (float)(step + 1) * ((float)height / 6.0F),
               previousStep,
               nextStep,
               false
            );
            ++step;
         }

         int sliderMinY = (int)((float)y + (float)height * hue) - 4;
         RenderUtil.drawRect((float)x, (float)(sliderMinY - 1), (float)(x + width), (float)(sliderMinY + 1), -1);
         drawOutlineRect((double)x, (double)(sliderMinY - 1), (double)(x + width), (double)(sliderMinY + 1), Color.BLACK, 1.0F);
      } else {
         for(int colorIndex = 0; colorIndex < 6; ++colorIndex) {
            int previousStep = Color.HSBtoRGB((float)step / 6.0F, 1.0F, 1.0F);
            int nextStep = Color.HSBtoRGB((float)(step + 1) / 6.0F, 1.0F, 1.0F);
            gradient(x + step * (width / 6), y, x + (step + 1) * (width / 6) + 3, y + height, previousStep, nextStep, true);
            ++step;
         }

         int sliderMinX = (int)((float)x + (float)width * hue);
         RenderUtil.drawRect((float)(sliderMinX - 1), (float)y - 1.2F, (float)(sliderMinX + 1), (float)(y + height) + 1.2F, -1);
         drawOutlineRect((double)sliderMinX - 1.2, (double)y - 1.2, (double)sliderMinX + 1.2, (double)(y + height) + 1.2, Color.BLACK, 0.1F);
      }
   }

   public float getHueX() {
      if (Managers.FPS.getFPS() < 20) {
         return this.hueX;
      } else {
         this.hueX = this.prevHueX + (this.hueX - this.prevHueX) * mc.getRenderPartialTicks() / (8.0F * ((float)Math.min(240, Managers.FPS.getFPS()) / 240.0F));
         return this.hueX;
      }
   }

   public void setHueX(float x) {
      if (this.hueX != x) {
         this.prevHueX = this.hueX;
         this.hueX = x;
      }
   }

   public void drawAlphaSlider(int x, int y, int width, int height, float red, float green, float blue, float alpha) {
      boolean left = true;
      int checkerBoardSquareSize = height / 2;

      for(int squareIndex = -checkerBoardSquareSize; squareIndex < width; squareIndex += checkerBoardSquareSize) {
         if (!left) {
            RenderUtil.drawRect((float)(x + squareIndex), (float)y, (float)(x + squareIndex + checkerBoardSquareSize), (float)(y + height), -1);
            RenderUtil.drawRect(
               (float)(x + squareIndex), (float)(y + checkerBoardSquareSize), (float)(x + squareIndex + checkerBoardSquareSize), (float)(y + height), -7303024
            );
            if (squareIndex < width - checkerBoardSquareSize) {
               int minX = x + squareIndex + checkerBoardSquareSize;
               int maxX = Math.min(x + width, x + squareIndex + checkerBoardSquareSize * 2);
               RenderUtil.drawRect((float)minX, (float)y, (float)maxX, (float)(y + height), -7303024);
               RenderUtil.drawRect((float)minX, (float)(y + checkerBoardSquareSize), (float)maxX, (float)(y + height), -1);
            }
         }

         left = !left;
      }

      drawLeftGradientRect(x, y, x + width, y + height, new Color(red, green, blue, 1.0F).getRGB(), 0);
      int sliderMinX = (int)((float)(x + width) - (float)width * alpha);
      RenderUtil.drawRect((float)(sliderMinX - 1), (float)y - 1.2F, (float)(sliderMinX + 1), (float)(y + height) + 1.2F, -1);
      drawOutlineRect((double)sliderMinX - 1.2, (double)y - 1.2, (double)sliderMinX + 1.2, (double)(y + height) + 1.2, Color.BLACK, 0.1F);
   }

   public float getAlphaX() {
      if (Managers.FPS.getFPS() < 20) {
         return this.alphaX;
      } else {
         this.alphaX = this.prevAlphaX
            + (this.alphaX - this.prevAlphaX) * mc.getRenderPartialTicks() / (8.0F * ((float)Math.min(240, Managers.FPS.getFPS()) / 240.0F));
         return this.alphaX;
      }
   }

   public void setAlphaX(float x) {
      if (this.alphaX != x) {
         this.prevAlphaX = this.alphaX;
         this.alphaX = x;
      }
   }
}
