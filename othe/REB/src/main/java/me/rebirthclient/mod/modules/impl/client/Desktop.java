//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.client;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.util.List;
import java.util.stream.Collectors;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.Display;

public class Desktop extends Module {
   final Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
   final TrayIcon icon = new TrayIcon(this.image, "Rebirth");
   private final Setting<Boolean> onlyTabbed = this.add(new Setting<>("OnlyTabbed", false));
   private final Setting<Boolean> visualRange = this.add(new Setting<>("VisualRange", true));
   private final Setting<Boolean> selfPop = this.add(new Setting<>("TotemPop", true));
   private final Setting<Boolean> mention = this.add(new Setting<>("Mention", true));
   private final Setting<Boolean> dm = this.add(new Setting<>("DM", true));
   private final List<Entity> knownPlayers = new java.util.ArrayList();
   private List<Entity> players;

   public Desktop() {
      super("Desktop", "Desktop notifications", Category.CLIENT);
   }

   @Override
   public void onDisable() {
      this.knownPlayers.clear();
      this.removeIcon();
   }

   @Override
   public void onEnable() {
      this.addIcon();
   }

   @Override
   public void onLoad() {
      if (this.isOn()) {
         this.addIcon();
      }
   }

   @Override
   public void onUnload() {
      this.onDisable();
   }

   @Override
   public void onUpdate() {
      if (!fullNullCheck() && this.visualRange.getValue()) {
         try {
            if (!Display.isActive() && this.onlyTabbed.getValue()) {
               return;
            }
         } catch (Exception var4) {
         }

         this.players = mc.world.loadedEntityList.stream().filter(entityx -> entityx instanceof EntityPlayer).collect(Collectors.toList());

         try {
            for(Entity entity : this.players) {
               if (entity instanceof EntityPlayer
                  && !entity.getName().equalsIgnoreCase(mc.player.getName())
                  && !this.knownPlayers.contains(entity)
                  && !Managers.FRIENDS.isFriend(entity.getName())) {
                  this.knownPlayers.add(entity);
                  this.icon.displayMessage("Rebirth", entity.getName() + " has entered your visual range!", MessageType.INFO);
               }
            }
         } catch (Exception var5) {
         }

         try {
            this.knownPlayers
               .removeIf(
                  entityx -> entityx instanceof EntityPlayer
                        && !entityx.getName().equalsIgnoreCase(mc.player.getName())
                        && !this.players.contains(entityx)
               );
         } catch (Exception var3) {
         }
      }
   }

   @Override
   public void onTotemPop(EntityPlayer player) {
      if (!fullNullCheck() && player == mc.player && this.selfPop.getValue()) {
         this.icon.displayMessage("Rebirth", "You are popping!", MessageType.WARNING);
      }
   }

   @SubscribeEvent
   public void onClientChatReceived(ClientChatReceivedEvent event) {
      if (!fullNullCheck()) {
         String message = String.valueOf(event.getMessage());
         if (message.contains(mc.player.getName()) && this.mention.getValue()) {
            this.icon.displayMessage("Rebirth", "New chat mention!", MessageType.INFO);
         }

         if (message.contains("whispers:") && this.dm.getValue()) {
            this.icon.displayMessage("Rebirth", "New direct message!", MessageType.INFO);
         }
      }
   }

   private void addIcon() {
      SystemTray tray = SystemTray.getSystemTray();
      this.icon.setImageAutoSize(true);
      this.icon.setToolTip("rebirth alpha");

      try {
         tray.add(this.icon);
      } catch (AWTException var3) {
         var3.printStackTrace();
      }
   }

   private void removeIcon() {
      SystemTray tray = SystemTray.getSystemTray();
      tray.remove(this.icon);
   }
}
