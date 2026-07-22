//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.HashMap;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.commands.Command;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.hud.Notifications;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketChatMessage;

public class PopCounter extends Module {
   public static final HashMap<String, Integer> TotemPopContainer = new HashMap<>();
   public static PopCounter INSTANCE = new PopCounter();

   public PopCounter() {
      super("PopCounter", "Counts other players totem pops", Category.MISC);
      INSTANCE = this;
   }

   @Override
   public void onDeath(EntityPlayer player) {
      if (TotemPopContainer.containsKey(player.getName())) {
         int l_Count = TotemPopContainer.get(player.getName());
         TotemPopContainer.remove(player.getName());
         if (l_Count == 1) {
            if (mc.player.equals(player)) {
               if (this.isOn()) {
                  this.sendMessageWithID("§fYou§r died after popping " + ChatFormatting.WHITE + l_Count + "§r totem.", player.getEntityId());
               }

               if (AutoEZ.INSTANCE.isOn() && AutoEZ.INSTANCE.whenSelf.getValue()) {
                  mc.player.connection.sendPacket(new CPacketChatMessage(AutoEZ.INSTANCE.SelfString.getValue()));
               }
            } else {
               if (this.isOn()) {
                  this.sendMessageWithID(
                     ChatFormatting.WHITE + player.getName() + "§r died after popping " + ChatFormatting.WHITE + l_Count + "§r totem.",
                     player.getEntityId()
                  );
               }

               if (AutoEZ.INSTANCE.isOn() && (!Managers.FRIENDS.isFriend(player.getName()) || AutoEZ.INSTANCE.whenFriend.getValue())) {
                  if (AutoEZ.INSTANCE.popCounter.getValue()) {
                     mc.player
                        .connection
                        .sendPacket(
                           new CPacketChatMessage(
                              (String)AutoEZ.INSTANCE.EzString.getValue() + " " + player.getName() + " popping " + l_Count + " totem."
                           )
                        );
                  } else {
                     mc.player
                        .connection
                        .sendPacket(new CPacketChatMessage((String)AutoEZ.INSTANCE.EzString.getValue() + " " + player.getName()));
                  }
               }
            }
         } else if (mc.player.equals(player)) {
            if (this.isOn()) {
               this.sendMessageWithID("§fYou§r died after popping " + ChatFormatting.WHITE + l_Count + "§r totems.", player.getEntityId());
            }

            if (AutoEZ.INSTANCE.isOn() && AutoEZ.INSTANCE.whenSelf.getValue()) {
               mc.player.connection.sendPacket(new CPacketChatMessage(AutoEZ.INSTANCE.SelfString.getValue()));
            }
         } else {
            if (this.isOn()) {
               this.sendMessageWithID(
                  ChatFormatting.WHITE + player.getName() + "§r died after popping " + ChatFormatting.WHITE + l_Count + "§r totems.",
                  player.getEntityId()
               );
            }

            if (AutoEZ.INSTANCE.isOn() && (!Managers.FRIENDS.isFriend(player.getName()) || AutoEZ.INSTANCE.whenFriend.getValue())) {
               if (AutoEZ.INSTANCE.popCounter.getValue()) {
                  mc.player
                     .connection
                     .sendPacket(
                        new CPacketChatMessage((String)AutoEZ.INSTANCE.EzString.getValue() + " " + player.getName() + " popping " + l_Count + " totems.")
                     );
               } else {
                  mc.player
                     .connection
                     .sendPacket(new CPacketChatMessage((String)AutoEZ.INSTANCE.EzString.getValue() + " " + player.getName()));
               }
            }
         }
      } else if (AutoEZ.INSTANCE.isOn() && (!Managers.FRIENDS.isFriend(player.getName()) || AutoEZ.INSTANCE.whenFriend.getValue())) {
         mc.player.connection.sendPacket(new CPacketChatMessage((String)AutoEZ.INSTANCE.EzString.getValue() + " " + player.getName()));
      }
   }

   @Override
   public void onTotemPop(EntityPlayer player) {
      int l_Count = 1;
      if (TotemPopContainer.containsKey(player.getName())) {
         l_Count = TotemPopContainer.get(player.getName());
         TotemPopContainer.put(player.getName(), ++l_Count);
      } else {
         TotemPopContainer.put(player.getName(), l_Count);
      }

      if (l_Count == 1) {
         if (mc.player.equals(player)) {
            if (this.isOn()) {
               this.sendMessageWithID("§fYou§r popped " + ChatFormatting.WHITE + l_Count + "§r totem.", player.getEntityId());
            }
         } else if (this.isOn()) {
            this.sendMessageWithID(
               ChatFormatting.WHITE + player.getName() + " §ropped " + ChatFormatting.GREEN + l_Count + "§r totems.", player.getEntityId()
            );
         }
      } else if (mc.player.equals(player)) {
         if (this.isOn()) {
            this.sendMessageWithID("§fYou§r popped " + ChatFormatting.WHITE + l_Count + "§r totem.", player.getEntityId());
         }
      } else if (this.isOn()) {
         this.sendMessageWithID(
            ChatFormatting.WHITE + player.getName() + " §rhas popped " + ChatFormatting.GREEN + l_Count + "§r totems.", player.getEntityId()
         );
      }
   }

   @Override
   public void sendMessageWithID(String message, int id) {
      Notifications.notifyList.add(new Notifications.Notifys(message));
      if (!nullCheck()) {
         mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new Command.ChatMessage(Managers.TEXT.getPrefix() + "§6[!] " + message), id);
      }
   }
}
