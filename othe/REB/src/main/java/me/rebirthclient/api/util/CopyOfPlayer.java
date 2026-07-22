//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.util;

import me.rebirthclient.api.util.render.entity.StaticModelPlayer;
import net.minecraft.entity.player.EntityPlayer;

public class CopyOfPlayer {
   private final EntityPlayer player;
   private final StaticModelPlayer model;
   private final long time;
   private final double x;
   private final double y;
   private final double z;

   public CopyOfPlayer(EntityPlayer player, long time, double x, double y, double z, boolean slim) {
      this.player = player;
      this.time = time;
      this.x = x;
      this.y = y - (player.isSneaking() ? 0.125 : 0.0);
      this.z = z;
      this.model = new StaticModelPlayer(player, slim, 0.0F);
      this.model.disableArmorLayers();
   }

   public EntityPlayer getPlayer() {
      return this.player;
   }

   public long getTime() {
      return this.time;
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }

   public StaticModelPlayer getModel() {
      return this.model;
   }
}
