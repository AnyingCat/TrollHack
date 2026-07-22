//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.RenderFogColorEvent;
import me.rebirthclient.api.events.impl.RenderSkyEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Ambience extends Module {
   public static Ambience INSTANCE;
   public final Setting<Boolean> customTime = this.add(new Setting<>("CustomTime", false).setParent());
   private final Setting<Integer> time = this.add(new Setting<>("Time", 0, 0, 24000, v -> this.customTime.isOpen()));
   public final Setting<Boolean> noFog = this.add(new Setting<>("NoFog", false));
   public final Setting<Color> lightMap = this.add(new Setting<>("LightMap", new Color(-557395713, true)).injectBoolean(false).hideAlpha());
   public final Setting<Color> sky = this.add(new Setting<>("OverWorldSky", new Color(8224213)).injectBoolean(true).hideAlpha());
   public final Setting<Color> skyNether = this.add(new Setting<>("NetherSky", new Color(8224213)).injectBoolean(true).hideAlpha());
   public final Setting<Color> fog = this.add(new Setting<>("OverWorldFog", new Color(13401557)).injectBoolean(false).hideAlpha());
   public final Setting<Color> fogNether = this.add(new Setting<>("NetherFog", new Color(13401557)).injectBoolean(false).hideAlpha());

   public Ambience() {
      super("Ambience", "Custom ambience", Category.RENDER);
      INSTANCE = this;
   }

   @SubscribeEvent
   public void init(WorldEvent event) {
      if (this.customTime.getValue()) {
         event.getWorld().setWorldTime((long)this.time.getValue().intValue());
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public void onReceivePacket(PacketEvent.Receive event) {
      if (!event.isCanceled()) {
         if (event.getPacket() instanceof SPacketTimeUpdate && this.customTime.getValue()) {
            event.setCanceled(true);
         }
      }
   }

   @SubscribeEvent
   public void setFogColor(RenderFogColorEvent event) {
      if (this.fog.booleanValue && mc.player.dimension == 0) {
         event.setColor(this.fog.getValue());
         event.setCanceled(true);
      } else if (this.fogNether.booleanValue && mc.player.dimension == -1) {
         event.setColor(this.fogNether.getValue());
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public void setSkyColor(RenderSkyEvent event) {
      if (this.sky.booleanValue && mc.player.dimension == 0) {
         event.setColor(this.sky.getValue());
         event.setCanceled(true);
      } else if (this.skyNether.booleanValue && mc.player.dimension == -1) {
         event.setColor(this.skyNether.getValue());
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public void setFogDensity(FogDensity event) {
      if (this.noFog.getValue()) {
         event.setDensity(0.0F);
         event.setCanceled(true);
      }
   }

   public Color getColor() {
      return new Color(
         this.lightMap.getValue().getRed(), this.lightMap.getValue().getGreen(), this.lightMap.getValue().getBlue(), this.lightMap.getValue().getAlpha()
      );
   }
}
