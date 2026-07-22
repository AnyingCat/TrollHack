package me.rebirthclient.mod.commands.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.managers.impl.FriendManager;
import me.rebirthclient.mod.commands.Command;

public class FriendCommand extends Command {
   public FriendCommand() {
      super("friend", new String[]{"<add/del/name/clear>", "<name>"});
   }

   @Override
   public void execute(String[] commands) {
      if (commands.length != 1) {
         if (commands.length == 2) {
            if ("reset".equals(commands[0])) {
               Managers.FRIENDS.onLoad();
               sendMessage("Friends got reset.");
            } else {
               sendMessage(commands[0] + (Managers.FRIENDS.isFriend(commands[0]) ? " is friended." : " isn't friended."));
            }
         } else {
            if (commands.length >= 2) {
               String var7 = commands[0];
               switch(var7) {
                  case "add":
                     Managers.FRIENDS.addFriend(commands[1]);
                     sendMessage(ChatFormatting.GREEN + commands[1] + " has been friended");
                     return;
                  case "del":
                     Managers.FRIENDS.removeFriend(commands[1]);
                     sendMessage(ChatFormatting.RED + commands[1] + " has been unfriended");
                     return;
                  default:
                     sendMessage("Unknown Command, try friend add/del (name)");
               }
            }
         }
      } else {
         if (Managers.FRIENDS.getFriends().isEmpty()) {
            sendMessage("Friend list empty D:.");
         } else {
            String f = "Friends: ";

            for(FriendManager.Friend friend : Managers.FRIENDS.getFriends()) {
               try {
                  f = f + friend.getUsername() + ", ";
               } catch (Exception var6) {
               }
            }

            sendMessage(f);
         }
      }
   }
}
