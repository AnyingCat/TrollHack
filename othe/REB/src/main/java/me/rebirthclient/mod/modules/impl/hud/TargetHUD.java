//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.hud;

import java.awt.Color;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import me.rebirthclient.api.events.impl.Render2DEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class TargetHUD extends Module {
   private final Setting<Integer> x = this.add(new Setting<>("X", 50, 0, 2000));
   private final Setting<Integer> y = this.add(new Setting<>("Y", 50, 0, 2000));
   private final Setting<Integer> backgroundAlpha = this.add(new Setting<>("Alpha", 80, 0, 255));
   EntityLivingBase target = mc.player;

   public TargetHUD() {
      super("TargetHUD", "description", Category.HUD);
   }

   private static double applyAsDouble(EntityLivingBase entityLivingBase) {
      return (double)entityLivingBase.getDistance(mc.player);
   }

   private static boolean checkIsNotPlayer(Entity entity) {
      return !entity.equals(mc.player);
   }

   @Override
   public synchronized void onTick() {
      List<EntityLivingBase> entities = new LinkedList();
      mc.world
         .loadedEntityList
         .stream()
         .filter(EntityPlayer.class::isInstance)
         .filter(TargetHUD::checkIsNotPlayer)
         .map(EntityLivingBase.class::cast)
         .sorted(Comparator.comparingDouble(TargetHUD::applyAsDouble))
         .forEach(entities::add);
      if (!entities.isEmpty()) {
         this.target = (EntityLivingBase)entities.get(0);
      } else {
         this.target = mc.player;
      }

      if (mc.currentScreen instanceof GuiChat) {
         this.target = mc.player;
      }
   }

   @Override
   public synchronized void onRender2D(Render2DEvent event) {
      if (this.target != null && !this.target.isDead) {
         FontRenderer fr = mc.fontRenderer;
         int color = this.target.getHealth() / this.target.getMaxHealth() > 0.66F
            ? -16711936
            : (this.target.getHealth() / this.target.getMaxHealth() > 0.33F ? -26368 : -65536);
         GlStateManager.color(1.0F, 1.0F, 1.0F);
         GuiInventory.drawEntityOnScreen(this.x.getValue() + 15, this.y.getValue() + 32, 15, 1.0F, 1.0F, this.target);
         List<ItemStack> armorList = new LinkedList();
         List<ItemStack> _armorList = new LinkedList();
         this.target.getArmorInventoryList().forEach(itemStack -> {
            if (!itemStack.isEmpty()) {
               _armorList.add(itemStack);
            }
         });

         for(int i = _armorList.size() - 1; i >= 0; --i) {
            armorList.add(_armorList.get(i));
         }

         int armorSize = 0;
         switch(armorList.size()) {
            case 0:
               if (!this.target.getHeldItemMainhand().isEmpty() && !this.target.getHeldItemOffhand().isEmpty()) {
                  mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemMainhand(), this.x.getValue() + 28, this.y.getValue() + 18);
                  mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemOffhand(), this.x.getValue() + 43, this.y.getValue() + 18);
                  armorSize += 45;
               } else if (!this.target.getHeldItemMainhand().isEmpty() || !this.target.getHeldItemOffhand().isEmpty()) {
                  mc.getRenderItem()
                     .renderItemAndEffectIntoGUI(
                        this.target.getHeldItemMainhand().isEmpty() ? this.target.getHeldItemOffhand() : this.target.getHeldItemMainhand(),
                        this.x.getValue() + 28,
                        this.y.getValue() + 18
                     );
                  armorSize += 30;
               }
               break;
            case 1:
               armorSize = 15;
               mc.getRenderItem().renderItemAndEffectIntoGUI((ItemStack)armorList.get(0), this.x.getValue() + 28, this.y.getValue() + 18);
               if (!this.target.getHeldItemMainhand().isEmpty() && !this.target.getHeldItemOffhand().isEmpty()) {
                  mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemMainhand(), this.x.getValue() + 43, this.y.getValue() + 18);
                  mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemOffhand(), this.x.getValue() + 58, this.y.getValue() + 18);
                  armorSize += 45;
               } else if (!this.target.getHeldItemMainhand().isEmpty() || !this.target.getHeldItemOffhand().isEmpty()) {
                  mc.getRenderItem()
                     .renderItemAndEffectIntoGUI(
                        this.target.getHeldItemMainhand().isEmpty() ? this.target.getHeldItemOffhand() : this.target.getHeldItemMainhand(),
                        this.x.getValue() + 43,
                        this.y.getValue() + 18
                     );
                  armorSize += 30;
               }
               break;
            case 2:
               armorSize = 30;
               mc.getRenderItem().renderItemAndEffectIntoGUI((ItemStack)armorList.get(0), this.x.getValue() + 28, this.y.getValue() + 18);
               mc.getRenderItem().renderItemAndEffectIntoGUI((ItemStack)armorList.get(1), this.x.getValue() + 43, this.y.getValue() + 18);
               if (!this.target.getHeldItemMainhand().isEmpty() && !this.target.getHeldItemOffhand().isEmpty()) {
                  mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemMainhand(), this.x.getValue() + 58, this.y.getValue() + 18);
                  mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemOffhand(), this.x.getValue() + 73, this.y.getValue() + 18);
                  armorSize += 45;
               } else if (!this.target.getHeldItemMainhand().isEmpty() || !this.target.getHeldItemOffhand().isEmpty()) {
                  mc.getRenderItem()
                     .renderItemAndEffectIntoGUI(
                        this.target.getHeldItemMainhand().isEmpty() ? this.target.getHeldItemOffhand() : this.target.getHeldItemMainhand(),
                        this.x.getValue() + 58,
                        this.y.getValue() + 18
                     );
                  armorSize += 30;
               }
               break;
            case 3:
               armorSize = 45;
               mc.getRenderItem().renderItemAndEffectIntoGUI((ItemStack)armorList.get(0), this.x.getValue() + 28, this.y.getValue() + 18);
               mc.getRenderItem().renderItemAndEffectIntoGUI((ItemStack)armorList.get(1), this.x.getValue() + 43, this.y.getValue() + 18);
               mc.getRenderItem().renderItemAndEffectIntoGUI((ItemStack)armorList.get(2), this.x.getValue() + 58, this.y.getValue() + 18);
               if (!this.target.getHeldItemMainhand().isEmpty() && !this.target.getHeldItemOffhand().isEmpty()) {
                  mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemMainhand(), this.x.getValue() + 73, this.y.getValue() + 18);
                  mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemOffhand(), this.x.getValue() + 98, this.y.getValue() + 18);
                  armorSize += 45;
               } else if (!this.target.getHeldItemMainhand().isEmpty() || !this.target.getHeldItemOffhand().isEmpty()) {
                  mc.getRenderItem()
                     .renderItemAndEffectIntoGUI(
                        this.target.getHeldItemMainhand().isEmpty() ? this.target.getHeldItemOffhand() : this.target.getHeldItemMainhand(),
                        this.x.getValue() + 73,
                        this.y.getValue() + 18
                     );
                  armorSize += 30;
               }
               break;
            case 4:
               armorSize = 60;
               mc.getRenderItem().renderItemAndEffectIntoGUI((ItemStack)armorList.get(0), this.x.getValue() + 28, this.y.getValue() + 18);
               mc.getRenderItem().renderItemAndEffectIntoGUI((ItemStack)armorList.get(1), this.x.getValue() + 43, this.y.getValue() + 18);
               mc.getRenderItem().renderItemAndEffectIntoGUI((ItemStack)armorList.get(2), this.x.getValue() + 58, this.y.getValue() + 18);
               mc.getRenderItem().renderItemAndEffectIntoGUI((ItemStack)armorList.get(3), this.x.getValue() + 73, this.y.getValue() + 18);
               if (!this.target.getHeldItemMainhand().isEmpty() && !this.target.getHeldItemOffhand().isEmpty()) {
                  mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemMainhand(), this.x.getValue() + 98, this.y.getValue() + 18);
                  mc.getRenderItem().renderItemAndEffectIntoGUI(this.target.getHeldItemOffhand(), this.x.getValue() + 113, this.y.getValue() + 18);
                  armorSize += 45;
               } else if (!this.target.getHeldItemMainhand().isEmpty() || !this.target.getHeldItemOffhand().isEmpty()) {
                  mc.getRenderItem()
                     .renderItemAndEffectIntoGUI(
                        this.target.getHeldItemMainhand().isEmpty() ? this.target.getHeldItemOffhand() : this.target.getHeldItemMainhand(),
                        this.x.getValue() + 98,
                        this.y.getValue() + 18
                     );
                  armorSize += 30;
               }
         }

         int backgroundStopY = this.y.getValue() + 35;
         int stringWidth = fr.getStringWidth(this.target.getName()) + 30;
         int backgroundStopX;
         if (fr.getStringWidth(this.target.getName()) > armorSize) {
            backgroundStopX = this.x.getValue() + stringWidth;
         } else {
            backgroundStopX = this.x.getValue() + armorSize + 30;
         }

         backgroundStopX += 5;
         backgroundStopY += 5;
         Gui.drawRect(
            this.x.getValue() - 2, this.y.getValue(), backgroundStopX, backgroundStopY, new Color(0, 0, 0, this.backgroundAlpha.getValue()).getRGB()
         );
         int healthBarLength = (int)(this.target.getHealth() / this.target.getMaxHealth() * (float)(backgroundStopX - this.x.getValue()));
         Gui.drawRect(this.x.getValue() - 2, backgroundStopY - 2, this.x.getValue() + healthBarLength, backgroundStopY, color);
         fr.drawString(
            this.target.getName(), (float)(this.x.getValue() + 30), (float)(this.y.getValue() + 8), new Color(255, 255, 255).getRGB(), true
         );
      }
   }
}
