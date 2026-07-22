//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.managers.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import me.rebirthclient.api.util.ProfileUtil;
import me.rebirthclient.mod.Mod;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.player.EntityPlayer;

public class FriendManager extends Mod {
   private List<FriendManager.Friend> friends = new ArrayList<>();

   public FriendManager() {
      super("Friends");
   }

   public boolean isCool(String name) {
      List<String> coolList = Arrays.asList("iMadCat");
      return coolList.contains(name);
   }

   public boolean isFriend(String name) {
      if (name.equals("§aYou")) {
         return true;
      } else {
         this.cleanFriends();
         return this.friends.stream().anyMatch(friend -> friend.username.equalsIgnoreCase(name));
      }
   }

   public boolean isFriend(EntityPlayer player) {
      return this.isFriend(player.getName());
   }

   public void addFriend(String name) {
      FriendManager.Friend friend = this.getFriendByName(name);
      if (friend != null) {
         this.friends.add(friend);
      }

      this.cleanFriends();
   }

   public void removeFriend(String name) {
      this.cleanFriends();

      for(FriendManager.Friend friend : this.friends) {
         if (friend.getUsername().equalsIgnoreCase(name)) {
            this.friends.remove(friend);
            break;
         }
      }
   }

   public void onLoad() {
      this.friends = new ArrayList<>();
      this.resetSettings();
   }

   public void saveFriends() {
      this.resetSettings();
      this.cleanFriends();

      for(FriendManager.Friend friend : this.friends) {
         this.add(new Setting<>(friend.getUuid().toString(), friend.getUsername()));
      }
   }

   public void cleanFriends() {
      this.friends.stream().filter(Objects::nonNull).filter(friend -> friend.getUsername() != null);
   }

   public List<FriendManager.Friend> getFriends() {
      this.cleanFriends();
      return this.friends;
   }

   public FriendManager.Friend getFriendByName(String input) {
      UUID uuid = ProfileUtil.getUUIDFromName(input);
      return uuid != null ? new FriendManager.Friend(input, uuid) : null;
   }

   public void addFriend(FriendManager.Friend friend) {
      this.friends.add(friend);
   }

   public static class Friend {
      private final String username;
      private final UUID uuid;

      public Friend(String username, UUID uuid) {
         this.username = username;
         this.uuid = uuid;
      }

      public String getUsername() {
         return this.username;
      }

      public UUID getUuid() {
         return this.uuid;
      }
   }
}
