//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;

public class TNTTime extends Module {
   public TNTTime() {
      super("TNTTime", "show tnt fuse", Category.MISC);
   }

   @Override
   public void onUpdate() {
      for(Entity entity : mc.world.loadedEntityList) {
         if (entity instanceof EntityTNTPrimed) {
            String color = ChatFormatting.GREEN + "";
            if ((double)((EntityTNTPrimed)entity).getFuse() / 20.0 > 0.0) {
               color = ChatFormatting.DARK_RED + "";
            }

            if ((double)((EntityTNTPrimed)entity).getFuse() / 20.0 > 1.0) {
               color = ChatFormatting.RED + "";
            }

            if ((double)((EntityTNTPrimed)entity).getFuse() / 20.0 > 2.0) {
               color = ChatFormatting.YELLOW + "";
            }

            if ((double)((EntityTNTPrimed)entity).getFuse() / 20.0 > 3.0) {
               color = ChatFormatting.GREEN + "";
            }

            entity.setCustomNameTag(color + String.valueOf((double)((EntityTNTPrimed)entity).getFuse() / 20.0).substring(0, 3) + "s");
            entity.setAlwaysRenderNameTag(true);
         }
      }
   }
}
