//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.gui.screen;

import java.awt.Color;
import java.util.ArrayList;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.commands.Command;
import me.rebirthclient.mod.modules.impl.misc.NoCom;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiScanner extends GuiScreen {
   public static boolean neartrack = false;
   public static boolean track = false;
   public static boolean busy = false;
   private static GuiScanner INSTANCE = new GuiScanner();
   public ArrayList<NoCom.cout> consoleout = new ArrayList<>();
   int radarx = 0;
   int radary = 0;
   int radarx1 = 0;
   int radary1 = 0;
   int centerx = 0;
   int centery = 0;
   int consolex = 0;
   int consoley = 0;
   int consolex1 = 0;
   int consoley1 = 0;
   int hovery = 0;
   int hoverx = 0;
   int searchx = 0;
   int searchy = 0;
   int wheely = 0;

   public GuiScanner() {
      this.setInstance();
      this.load();
   }

   public static GuiScanner getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new GuiScanner();
      }

      return INSTANCE;
   }

   public static GuiScanner getGuiScanner() {
      return getInstance();
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   private void load() {
   }

   private void setInstance() {
      INSTANCE = this;
   }

   public float getscale() {
      if (NoCom.INSTANCE.scale.getValue() == 1) {
         return 500.0F;
      } else if (NoCom.INSTANCE.scale.getValue() == 2) {
         return 250.0F;
      } else if (NoCom.INSTANCE.scale.getValue() == 3) {
         return 125.0F;
      } else {
         return NoCom.INSTANCE.scale.getValue() == 4 ? 75.0F : 705.0F;
      }
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      ScaledResolution sr = new ScaledResolution(this.mc);
      this.checkMouseWheel(mouseX, mouseY);
      this.radarx = sr.getScaledWidth() / 8;
      this.radarx1 = sr.getScaledWidth() * 5 / 8;
      this.radary = sr.getScaledHeight() / 2 - (this.radarx1 - this.radarx) / 2;
      this.radary1 = sr.getScaledHeight() / 2 + (this.radarx1 - this.radarx) / 2;
      this.centerx = (this.radarx + this.radarx1) / 2;
      this.centery = (this.radary + this.radary1) / 2;
      this.consolex = (int)((float)sr.getScaledWidth() * 5.5F / 8.0F);
      this.consolex1 = sr.getScaledWidth() - 50;
      this.consoley = this.radary;
      this.consoley1 = this.radary1 - 50;
      RenderUtil.drawOutlineRect(
         (float)this.consolex,
         (float)this.consoley,
         (float)(this.consolex1 - this.consolex),
         (float)(this.consoley1 - this.consoley),
         4.0F,
         new Color(-844584792, true).getRGB()
      );
      RenderUtil.drawRect2((double)this.consolex, (double)this.consoley, (double)this.consolex1, (double)this.consoley1, new Color(-150205428, true).getRGB());
      RenderUtil.drawOutlineRect(
         (float)this.consolex, (float)(this.consoley1 + 3), (float)(this.consolex1 - this.consolex), 15.0F, 4.0F, new Color(-844584792, true).getRGB()
      );
      RenderUtil.drawRect2(
         (double)this.consolex, (double)(this.consoley1 + 3), (double)this.consolex1, (double)(this.consoley1 + 17), new Color(-150205428, true).getRGB()
      );
      Managers.TEXT
         .drawString("cursor pos: " + this.hoverx * 64 + "x  " + this.hovery * 64 + "z", (float)(this.consolex + 4), (float)(this.consoley1 + 6), -1, false);
      RenderUtil.drawOutlineRect(
         (float)this.consolex, (float)(this.consoley1 + 20), (float)(this.consolex1 - this.consolex), 15.0F, 4.0F, new Color(-844584792, true).getRGB()
      );
      if (!track) {
         RenderUtil.drawRect2(
            (double)this.consolex, (double)(this.consoley1 + 20), (double)this.consolex1, (double)(this.consoley1 + 35), new Color(-150205428, true).getRGB()
         );
         Managers.TEXT.drawString("tracker off", (float)(this.consolex + 4), (float)(this.consoley1 + 26), -1, false);
      } else {
         RenderUtil.drawRect2(
            (double)this.consolex, (double)(this.consoley1 + 20), (double)this.consolex1, (double)(this.consoley1 + 35), new Color(-144810402, true).getRGB()
         );
         Managers.TEXT.drawString("tracker on", (float)(this.consolex + 4), (float)(this.consoley1 + 26), -1, false);
      }

      RenderUtil.drawOutlineRect(
         (float)this.radarx,
         (float)this.radary,
         (float)(this.radarx1 - this.radarx),
         (float)(this.radary1 - this.radary),
         4.0F,
         new Color(-844584792, true).getRGB()
      );
      RenderUtil.drawRect2((double)this.radarx, (double)this.radary, (double)this.radarx1, (double)this.radary1, new Color(-535489259, true).getRGB());

      try {
         for(NoCom.Dot point : NoCom.dots) {
            if (point.type == NoCom.DotType.Searched) {
               RenderUtil.drawRect2(
                  (double)((float)point.posX / 4.0F + (float)this.centerx),
                  (double)((float)point.posY / 4.0F + (float)this.centery),
                  (double)((float)point.posX / 4.0F + (float)(this.radarx1 - this.radarx) / this.getscale() + (float)this.centerx),
                  (double)((float)point.posY / 4.0F + (float)(this.radary1 - this.radary) / this.getscale() + (float)this.centery),
                  new Color(-408377176, true).getRGB()
               );
            } else {
               RenderUtil.drawRect2(
                  (double)((float)point.posX / 4.0F + (float)this.centerx),
                  (double)((float)point.posY / 4.0F + (float)this.centery),
                  (double)((float)point.posX / 4.0F + (float)(this.radarx1 - this.radarx) / this.getscale() + (float)this.centerx),
                  (double)((float)point.posY / 4.0F + (float)(this.radary1 - this.radary) / this.getscale() + (float)this.centery),
                  new Color(3991304).getRGB()
               );
            }
         }
      } catch (Exception var8) {
      }

      RenderUtil.drawRect2(
         (double)((float)this.centerx - 1.0F),
         (double)((float)this.centery - 1.0F),
         (double)((float)this.centerx + 1.0F),
         (double)((float)this.centery + 1.0F),
         new Color(16712451).getRGB()
      );
      RenderUtil.drawRect2(
         this.mc.player.posX / 16.0 / 4.0 + (double)this.centerx,
         this.mc.player.posZ / 16.0 / 4.0 + (double)this.centery,
         this.mc.player.posX / 16.0 / 4.0 + (double)((float)(this.radarx1 - this.radarx) / this.getscale()) + (double)this.centerx,
         this.mc.player.posZ / 16.0 / 4.0 + (double)((float)(this.radary1 - this.radary) / this.getscale()) + (double)this.centery,
         new Color(4863).getRGB()
      );
      if (mouseX > this.radarx && mouseX < this.radarx1 && mouseY > this.radary && mouseY < this.radary1) {
         this.hoverx = mouseX - this.centerx;
         this.hovery = mouseY - this.centery;
      }

      RenderUtil.glScissor((float)this.consolex, (float)this.consoley, (float)this.consolex1, (float)(this.consoley1 - 10), sr);
      GL11.glEnable(3089);

      try {
         for(NoCom.cout out : this.consoleout) {
            Managers.TEXT.drawString(out.string, (float)(this.consolex + 4), (float)(this.consoley + 6 + out.posY * 11 + this.wheely), -1, false);
         }
      } catch (Exception var7) {
      }

      GL11.glDisable(3089);
      Managers.TEXT.drawString("X+", (float)(this.radarx1 + 5), (float)this.centery, -1, false);
      Managers.TEXT.drawString("X-", (float)(this.radarx - 15), (float)this.centery, -1, false);
      Managers.TEXT.drawString("Y+", (float)this.centerx, (float)(this.radary1 + 5), -1, false);
      Managers.TEXT.drawString("Y-", (float)this.centerx, (float)(this.radary - 8), -1, false);
   }

   public void mouseClicked(int mouseX, int mouseY, int clickedButton) {
      if (mouseX > this.radarx && mouseX < this.radarx1 && mouseY > this.radary && mouseY < this.radary1) {
         busy = true;
         this.searchx = mouseX - this.centerx;
         this.searchy = mouseY - this.centery;
         Command.sendMessage(this.searchx * 64 + " " + this.searchy * 64);
         NoCom.rerun(this.searchx * 64, this.searchy * 64);
         getInstance().consoleout.add(new NoCom.cout(NoCom.INSTANCE.couti, "Selected pos " + this.searchx * 65 + "x " + this.searchy * 64 + "z "));
         ++NoCom.INSTANCE.couti;
      }

      if (mouseX > this.consolex && mouseX < this.consolex1 && mouseY > this.consoley1 + 20 && mouseY < this.consoley1 + 36) {
         track = !track;
      }
   }

   public void checkMouseWheel(int mouseX, int mouseY) {
      int dWheel = Mouse.getDWheel();
      if (dWheel < 0) {
         this.wheely -= 20;
      } else if (dWheel > 0) {
         this.wheely += 20;
      }
   }
}
