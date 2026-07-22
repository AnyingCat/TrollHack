//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.gui.click;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.Mod;
import me.rebirthclient.mod.gui.click.items.Item;
import me.rebirthclient.mod.gui.click.items.buttons.Button;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class Component extends Mod {
   public static int[] counter1 = new int[]{1};
   public final Map<Integer, Integer> colorMap = new HashMap<>();
   private final ArrayList<Item> items = new ArrayList<>();
   public boolean drag;
   private int x;
   private int y;
   private int x2;
   private int y2;
   private int width;
   private int height;
   private boolean open;
   private boolean hidden;
   private int angle = 180;

   public Component(String name, int x, int y, boolean open) {
      super(name);
      this.x = x;
      this.y = y;
      this.width = 88;
      this.height = 18;
      this.open = open;
      this.setupItems();
   }

   public static float calculateRotation(float var0) {
      if ((var0 = var0 % 360.0F) >= 180.0F) {
         var0 -= 360.0F;
      }

      if (var0 < -180.0F) {
         var0 += 360.0F;
      }

      return var0;
   }

   public void setupItems() {
   }

   private void drag(int mouseX, int mouseY) {
      if (this.drag) {
         this.x = this.x2 + mouseX;
         this.y = this.y2 + mouseY;
      }
   }

   private void drawOutline(int color) {
      float totalItemHeight = 0.0F;
      if (this.open) {
         totalItemHeight = this.getTotalItemHeight() - 2.0F;
      }

      RenderUtil.drawLine(
         (float)this.x,
         (float)this.y - 1.5F,
         (float)this.x,
         (float)(this.y + this.height) + totalItemHeight,
         1.0F,
         ClickGui.INSTANCE.rainbow.getValue() ? Managers.COLORS.getRainbow().getRGB() : color
      );
      RenderUtil.drawLine(
         (float)(this.x + this.width),
         (float)this.y - 1.5F,
         (float)(this.x + this.width),
         (float)(this.y + this.height) + totalItemHeight,
         1.0F,
         ClickGui.INSTANCE.rainbow.getValue() ? Managers.COLORS.getRainbow().getRGB() : color
      );
      RenderUtil.drawLine(
         (float)this.x,
         (float)this.y - 1.5F,
         (float)(this.x + this.width),
         (float)this.y - 1.5F,
         1.0F,
         ClickGui.INSTANCE.rainbow.getValue() ? Managers.COLORS.getRainbow().getRGB() : color
      );
      RenderUtil.drawLine(
         (float)this.x,
         (float)(this.y + this.height) + totalItemHeight,
         (float)(this.x + this.width),
         (float)(this.y + this.height) + totalItemHeight,
         1.0F,
         ClickGui.INSTANCE.rainbow.getValue() ? ColorUtil.rainbow(500).getRGB() : color
      );
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drag(mouseX, mouseY);
      counter1 = new int[]{1};
      float totalItemHeight = this.open ? this.getTotalItemHeight() - 2.0F : 0.0F;
      boolean future = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.FUTURE;
      int color = ColorUtil.toARGB(
         ClickGui.INSTANCE.color.getValue().getRed(),
         ClickGui.INSTANCE.color.getValue().getGreen(),
         ClickGui.INSTANCE.color.getValue().getBlue(),
         future ? 99 : 120
      );
      Gui.drawRect(
         this.x,
         this.y - 1,
         this.x + this.width,
         this.y + this.height - 6,
         ClickGui.INSTANCE.rainbow.getValue() ? Managers.COLORS.getCurrentWithAlpha(future ? 99 : 150) : color
      );
      if (future) {
         this.drawArrow();
      }

      if (this.open) {
         if (ClickGui.INSTANCE.line.getValue()) {
            if (ClickGui.INSTANCE.rainbow.getValue() && ClickGui.INSTANCE.rollingLine.getValue()) {
               float hue = (float)ClickGui.INSTANCE.rainbowDelay.getValue().intValue();
               int height = Managers.TEXT.scaledHeight;
               float tempHue = hue;

               for(int i2 = 0; i2 <= height; ++i2) {
                  this.colorMap.put(i2, Color.HSBtoRGB(tempHue, (float)ClickGui.INSTANCE.rainbowSaturation.getValue().intValue() / 255.0F, 1.0F));
                  tempHue += 1.0F / (float)height * 5.0F;
               }

               GL11.glLineWidth(1.0F);
               GlStateManager.disableTexture2D();
               GlStateManager.enableBlend();
               GlStateManager.disableAlpha();
               GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
               GlStateManager.shadeModel(7425);
               GL11.glBegin(1);
               Color currentColor = new Color(Managers.COLORS.getCurrentWithAlpha(150));
               GL11.glColor4f(
                  (float)currentColor.getRed() / 255.0F,
                  (float)currentColor.getGreen() / 255.0F,
                  (float)currentColor.getBlue() / 255.0F,
                  (float)currentColor.getAlpha() / 255.0F
               );
               GL11.glVertex3f((float)(this.x + this.width), (float)this.y - 1.5F, 0.0F);
               GL11.glVertex3f((float)this.x, (float)this.y - 1.5F, 0.0F);
               GL11.glVertex3f((float)this.x, (float)this.y - 1.5F, 0.0F);
               float currentHeight = (float)this.getHeight() - 1.5F;

               for(Item item : this.getItems()) {
                  currentColor = ClickGui.INSTANCE.rainbowMode.getValue() != ClickGui.Rainbow.NORMAL
                     ? ColorUtil.rainbow(
                        MathUtil.clamp((int)((float)this.y + (currentHeight += (float)item.getHeight() + 1.5F)), 0, Managers.TEXT.scaledHeight)
                     )
                     : new Color(
                        this.colorMap
                           .get(MathUtil.clamp((int)((float)this.y + (currentHeight += (float)item.getHeight() + 1.5F)), 0, Managers.TEXT.scaledHeight))
                     );
                  GL11.glColor4f(
                     (float)currentColor.getRed() / 255.0F,
                     (float)currentColor.getGreen() / 255.0F,
                     (float)currentColor.getBlue() / 255.0F,
                     (float)currentColor.getAlpha() / 255.0F
                  );
                  GL11.glVertex3f((float)this.x, (float)this.y + currentHeight, 0.0F);
                  GL11.glVertex3f((float)this.x, (float)this.y + currentHeight, 0.0F);
               }

               currentColor = ClickGui.INSTANCE.rainbowMode.getValue() != ClickGui.Rainbow.NORMAL
                  ? ColorUtil.rainbow(MathUtil.clamp((int)((float)(this.y + this.height) + totalItemHeight), 0, Managers.TEXT.scaledHeight))
                  : new Color(this.colorMap.get(MathUtil.clamp((int)((float)(this.y + this.height) + totalItemHeight), 0, Managers.TEXT.scaledHeight)));
               GL11.glColor4f(
                  (float)currentColor.getRed() / 255.0F,
                  (float)currentColor.getGreen() / 255.0F,
                  (float)currentColor.getBlue() / 255.0F,
                  (float)currentColor.getAlpha() / 255.0F
               );
               GL11.glVertex3f((float)(this.x + this.width), (float)(this.y + this.height) + totalItemHeight, 0.0F);
               GL11.glVertex3f((float)(this.x + this.width), (float)(this.y + this.height) + totalItemHeight, 0.0F);

               for(Item item : this.getItems()) {
                  currentColor = ClickGui.INSTANCE.rainbowMode.getValue() != ClickGui.Rainbow.NORMAL
                     ? ColorUtil.rainbow(
                        MathUtil.clamp((int)((float)this.y + (currentHeight -= (float)item.getHeight() + 1.5F)), 0, Managers.TEXT.scaledHeight)
                     )
                     : new Color(
                        this.colorMap
                           .get(MathUtil.clamp((int)((float)this.y + (currentHeight -= (float)item.getHeight() + 1.5F)), 0, Managers.TEXT.scaledHeight))
                     );
                  GL11.glColor4f(
                     (float)currentColor.getRed() / 255.0F,
                     (float)currentColor.getGreen() / 255.0F,
                     (float)currentColor.getBlue() / 255.0F,
                     (float)currentColor.getAlpha() / 255.0F
                  );
                  GL11.glVertex3f((float)(this.x + this.width), (float)this.y + currentHeight, 0.0F);
                  GL11.glVertex3f((float)(this.x + this.width), (float)this.y + currentHeight, 0.0F);
               }

               currentColor = new Color(Managers.COLORS.getCurrentWithAlpha(150));
               GL11.glColor4f(
                  (float)currentColor.getRed() / 255.0F,
                  (float)currentColor.getGreen() / 255.0F,
                  (float)currentColor.getBlue() / 255.0F,
                  (float)currentColor.getAlpha() / 255.0F
               );
               GL11.glVertex3f((float)(this.x + this.width), (float)this.y - 1.5F, 0.0F);
               GL11.glEnd();
               GlStateManager.shadeModel(7424);
               GlStateManager.disableBlend();
               GlStateManager.enableAlpha();
               GlStateManager.enableTexture2D();
            } else {
               this.drawOutline(color);
            }
         }

         if (ClickGui.INSTANCE.rect.getValue()) {
            int rectColor = ClickGui.INSTANCE.colorRect.getValue() ? Managers.COLORS.getCurrentWithAlpha(30) : ColorUtil.toARGB(10, 10, 10, 30);
            RenderUtil.drawRect((float)this.x, (float)this.y + 12.5F, (float)(this.x + this.width), (float)(this.y + this.height) + totalItemHeight, rectColor);
         }
      }

      if (ClickGui.INSTANCE.icon.getValue()) {
         String var15 = this.getName();
         switch(var15) {
            case "Combat":
               Managers.TEXT
                  .drawStringIcon("b", (float)this.x + 3.0F, (float)this.y - 5.0F - (float)me.rebirthclient.mod.gui.screen.Gui.INSTANCE.getTextOffset(), -1);
               break;
            case "Misc":
               Managers.TEXT
                  .drawStringIcon("[", (float)this.x + 3.0F, (float)this.y - 5.0F - (float)me.rebirthclient.mod.gui.screen.Gui.INSTANCE.getTextOffset(), -1);
               break;
            case "Render":
               Managers.TEXT
                  .drawStringIcon("a", (float)this.x + 3.0F, (float)this.y - 5.0F - (float)me.rebirthclient.mod.gui.screen.Gui.INSTANCE.getTextOffset(), -1);
               break;
            case "Movement":
               Managers.TEXT
                  .drawStringIcon("8", (float)this.x + 3.0F, (float)this.y - 5.0F - (float)me.rebirthclient.mod.gui.screen.Gui.INSTANCE.getTextOffset(), -1);
               break;
            case "Player":
               Managers.TEXT
                  .drawStringIcon("5", (float)this.x + 3.0F, (float)this.y - 5.0F - (float)me.rebirthclient.mod.gui.screen.Gui.INSTANCE.getTextOffset(), -1);
               break;
            case "Exploit":
               Managers.TEXT
                  .drawStringIcon("!", (float)this.x + 3.0F, (float)this.y - 5.0F - (float)me.rebirthclient.mod.gui.screen.Gui.INSTANCE.getTextOffset(), -1);
               break;
            case "Client":
            case "HUD":
               Managers.TEXT
                  .drawStringIcon("7", (float)this.x + 3.0F, (float)this.y - 5.0F - (float)me.rebirthclient.mod.gui.screen.Gui.INSTANCE.getTextOffset(), -1);
               break;
            case "Dev":
               Managers.TEXT
                  .drawStringIcon("6", (float)this.x + 3.0F, (float)this.y - 5.0F - (float)me.rebirthclient.mod.gui.screen.Gui.INSTANCE.getTextOffset(), -1);
         }
      }

      Managers.TEXT
         .drawStringWithShadow(
            this.getName(),
            ClickGui.INSTANCE.icon.getValue() ? (float)this.x + 17.0F : (float)this.x + 3.0F,
            (float)this.y - 4.0F - (float)me.rebirthclient.mod.gui.screen.Gui.INSTANCE.getTextOffset(),
            -1
         );
      if (this.open) {
         float y = (float)(this.getY() + this.getHeight()) - 3.0F;

         for(Item item : this.getItems()) {
            counter1[0]++;
            if (!item.isHidden()) {
               item.setLocation((float)this.x + 2.0F, y);
               item.setHeight(ClickGui.INSTANCE.getButtonHeight());
               item.setWidth(this.getWidth() - 4);
               item.drawScreen(mouseX, mouseY, partialTicks);
               y += (float)item.getHeight() + 1.5F;
            }
         }
      }
   }

   public void drawArrow() {
      if (!this.open) {
         if (this.angle > 0) {
            this.angle -= 6;
         }
      } else if (this.angle < 180) {
         this.angle += 6;
      }

      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      RenderUtil.glColor(new Color(255, 255, 255, 255));
      mc.getTextureManager().bindTexture(new ResourceLocation("textures/rebirth/arrow.png"));
      GlStateManager.translate((float)(this.getX() + this.getWidth() - 7), (float)(this.getY() + 6) - 0.3F, 0.0F);
      GlStateManager.rotate(calculateRotation((float)this.angle), 0.0F, 0.0F, 0.0F);
      RenderUtil.drawModalRect(-5, -5, 0.0F, 0.0F, 10, 10, 10, 10, 10.0F, 10.0F);
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
   }

   public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
      if (mouseButton == 0 && this.isHovering(mouseX, mouseY)) {
         this.x2 = this.x - mouseX;
         this.y2 = this.y - mouseY;
         me.rebirthclient.mod.gui.screen.Gui.INSTANCE.getComponents().forEach(component -> {
            if (component.drag) {
               component.drag = false;
            }
         });
         this.drag = true;
      } else if (mouseButton == 1 && this.isHovering(mouseX, mouseY)) {
         this.open = !this.open;
         mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      } else if (this.open) {
         this.getItems().forEach(item -> item.mouseClicked(mouseX, mouseY, mouseButton));
      }
   }

   public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
      if (releaseButton == 0) {
         this.drag = false;
      }

      if (this.open) {
         this.getItems().forEach(item -> item.mouseReleased(mouseX, mouseY, releaseButton));
      }
   }

   public void onKeyTyped(char typedChar, int keyCode) {
      if (this.open) {
         this.getItems().forEach(item -> item.onKeyTyped(typedChar, keyCode));
      }
   }

   public void addButton(Button button) {
      this.items.add(button);
   }

   public int getX() {
      return this.x;
   }

   public void setX(int x) {
      this.x = x;
   }

   public int getY() {
      return this.y;
   }

   public void setY(int y) {
      this.y = y;
   }

   public int getWidth() {
      return this.width;
   }

   public void setWidth(int width) {
      this.width = width;
   }

   public int getHeight() {
      return this.height;
   }

   public void setHeight(int height) {
      this.height = height;
   }

   public boolean isHidden() {
      return this.hidden;
   }

   public void setHidden(boolean hidden) {
      this.hidden = hidden;
   }

   public boolean isOpen() {
      return this.open;
   }

   public final ArrayList<Item> getItems() {
      return this.items;
   }

   private boolean isHovering(int mouseX, int mouseY) {
      return mouseX >= this.getX()
         && mouseX <= this.getX() + this.getWidth()
         && mouseY >= this.getY()
         && mouseY <= this.getY() + this.getHeight() - (this.open ? 2 : 0);
   }

   private float getTotalItemHeight() {
      float height = 0.0F;

      for(Item item : this.getItems()) {
         height += (float)item.getHeight() + 1.5F;
      }

      return height;
   }
}
