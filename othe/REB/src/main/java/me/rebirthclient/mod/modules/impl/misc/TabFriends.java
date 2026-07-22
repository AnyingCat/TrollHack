//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.misc;

import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;

public class TabFriends extends Module {
   public static String color = "";
   public static TabFriends INSTANCE;
   public static Setting<Boolean> prefix;
   public final Setting<TabFriends.FriendColor> mode = this.add(new Setting<>("Color", TabFriends.FriendColor.Green));

   public TabFriends() {
      super("TabFriends", "Renders your friends differently in the tablist", Category.MISC);
      prefix = this.add(new Setting<>("Prefix", true));
      INSTANCE = this;
   }

   public static String getPlayerName(NetworkPlayerInfo networkPlayerInfoIn) {
      String name = networkPlayerInfoIn.getDisplayName() != null
         ? networkPlayerInfoIn.getDisplayName().getFormattedText()
         : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
      if (Managers.FRIENDS.isFriend(name)) {
         return prefix.getValue() ? "§7[" + color + "F§7] " + color + name : color + name;
      } else {
         return name;
      }
   }

   @Override
   public void onUpdate() {
      switch((TabFriends.FriendColor)this.mode.getValue()) {
         case White:
            color = "§f";
            break;
         case DarkRed:
            color = "§4";
            break;
         case Red:
            color = "§c";
            break;
         case Gold:
            color = "§6";
            break;
         case Yellow:
            color = "§e";
            break;
         case DarkGreen:
            color = "§2";
            break;
         case Green:
            color = "§a";
            break;
         case Aqua:
            color = "§b";
            break;
         case DarkAqua:
            color = "§3";
            break;
         case DarkBlue:
            color = "§1";
            break;
         case Blue:
            color = "§9";
            break;
         case LightPurple:
            color = "§d";
            break;
         case DarkPurple:
            color = "§5";
            break;
         case Gray:
            color = "§7";
            break;
         case DarkGray:
            color = "§8";
            break;
         case Black:
            color = "§0";
            break;
         case None:
            color = "";
      }
   }

   public static enum FriendColor {
      DarkRed,
      Red,
      Gold,
      Yellow,
      DarkGreen,
      Green,
      Aqua,
      DarkAqua,
      DarkBlue,
      Blue,
      LightPurple,
      DarkPurple,
      Gray,
      DarkGray,
      Black,
      White,
      None;
   }
}
