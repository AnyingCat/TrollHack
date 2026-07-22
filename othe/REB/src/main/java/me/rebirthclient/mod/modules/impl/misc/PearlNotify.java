//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;

public class PearlNotify extends Module {
   private boolean flag;

   public PearlNotify() {
      super("PearlNotify", "Notifies pearl throws", Category.MISC);
   }

   @Override
   public void onEnable() {
      this.flag = true;
   }

   @Override
   public void onUpdate() {
      Entity enderPearl = null;

      for(Entity e : mc.world.loadedEntityList) {
         if (e instanceof EntityEnderPearl) {
            enderPearl = e;
            break;
         }
      }

      if (enderPearl == null) {
         this.flag = true;
      } else {
         EntityPlayer closestPlayer = null;

         for(EntityPlayer entity : mc.world.playerEntities) {
            if (closestPlayer == null) {
               closestPlayer = entity;
            } else if (!(closestPlayer.getDistance(enderPearl) <= entity.getDistance(enderPearl))) {
               closestPlayer = entity;
            }
         }

         if (closestPlayer == mc.player) {
            this.flag = false;
         }

         if (closestPlayer != null && this.flag) {
            String faceing = enderPearl.getHorizontalFacing().toString();
            if (faceing.equals("West")) {
               faceing = "East";
            } else if (faceing.equals("East")) {
               faceing = "West";
            }

            this.sendMessageWithID(
               Managers.FRIENDS.isFriend(closestPlayer.getName())
                  ? ChatFormatting.AQUA
                     + closestPlayer.getName()
                     + ChatFormatting.GRAY
                     + " has just thrown a pearl heading "
                     + ChatFormatting.AQUA
                     + faceing
                     + "!"
                  : ChatFormatting.RED
                     + closestPlayer.getName()
                     + ChatFormatting.GRAY
                     + " has just thrown a pearl heading "
                     + ChatFormatting.RED
                     + faceing
                     + "!",
               closestPlayer.getEntityId()
            );
            this.flag = false;
         }
      }
   }
}
