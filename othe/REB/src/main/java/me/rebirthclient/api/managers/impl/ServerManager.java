//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.managers.impl;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Objects;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.Mod;
import me.rebirthclient.mod.modules.impl.client.HUD;
import net.minecraft.client.network.NetHandlerPlayClient;

public class ServerManager extends Mod {
   private final float[] tpsCounts = new float[10];
   private final DecimalFormat format = new DecimalFormat("##.00#");
   private final Timer timer = new Timer();
   private float TPS = 20.0F;
   private long lastUpdate = -1L;
   private String serverBrand = "";

   public void onPacketReceived() {
      this.timer.reset();
   }

   public boolean isServerNotResponding() {
      return this.timer.passedMs((long)HUD.INSTANCE.lagTime.getValue().intValue());
   }

   public long serverRespondingTime() {
      return this.timer.getPassedTimeMs();
   }

   public void update() {
      long currentTime = System.currentTimeMillis();
      if (this.lastUpdate == -1L) {
         this.lastUpdate = currentTime;
      } else {
         long timeDiff = currentTime - this.lastUpdate;
         float tickTime = (float)timeDiff / 20.0F;
         if (tickTime == 0.0F) {
            tickTime = 50.0F;
         }

         float tps;
         if ((tps = 1000.0F / tickTime) > 20.0F) {
            tps = 20.0F;
         }

         System.arraycopy(this.tpsCounts, 0, this.tpsCounts, 1, this.tpsCounts.length - 1);
         this.tpsCounts[0] = tps;
         double total = 0.0;

         for(float f : this.tpsCounts) {
            total += (double)f;
         }

         if ((total = total / (double)this.tpsCounts.length) > 20.0) {
            total = 20.0;
         }

         this.TPS = Float.parseFloat(this.format.format(total));
         this.lastUpdate = currentTime;
      }
   }

   public void reset() {
      Arrays.fill(this.tpsCounts, 20.0F);
      this.TPS = 20.0F;
   }

   public float getTpsFactor() {
      return 20.0F / this.TPS;
   }

   public float getTPS() {
      return this.TPS;
   }

   public String getServerBrand() {
      return this.serverBrand;
   }

   public void setServerBrand(String brand) {
      this.serverBrand = brand;
   }

   public int getPing() {
      if (fullNullCheck()) {
         return 0;
      } else {
         try {
            return ((NetHandlerPlayClient)Objects.requireNonNull(mc.getConnection()))
               .getPlayerInfo(Wrapper.mc.getConnection().getGameProfile().getId())
               .getResponseTime();
         } catch (Exception var2) {
            return 0;
         }
      }
   }
}
