//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import org.lwjgl.input.Mouse;

public class MCF extends Module {
   private boolean didClick;

   public MCF() {
      super("MCF", "Middle click your fellow friends", Category.MISC);
   }

   @Override
   public void onUpdate() {
      if (Mouse.isButtonDown(2)) {
         if (!this.didClick && mc.currentScreen == null) {
            this.onClick();
         }

         this.didClick = true;
      } else {
         this.didClick = false;
      }
   }

   private void onClick() {
      RayTraceResult result = mc.objectMouseOver;
      if (result != null && result.typeOfHit == Type.ENTITY) {
         Entity entity = result.entityHit;
         if (result.entityHit instanceof EntityPlayer) {
            if (Managers.FRIENDS.isFriend(entity.getName())) {
               Managers.FRIENDS.removeFriend(entity.getName());
               this.sendMessage(ChatFormatting.RED + entity.getName() + ChatFormatting.RED + " has been unfriended.");
            } else {
               Managers.FRIENDS.addFriend(entity.getName());
               this.sendMessage(ChatFormatting.AQUA + entity.getName() + ChatFormatting.GREEN + " has been friended.");
            }
         }
      }

      this.didClick = true;
   }
}
