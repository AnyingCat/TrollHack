//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.api.events.impl.TurnEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Bind;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FreeLook extends Module {
   public final Setting<Bind> bind = this.add(new Setting<>("Bind", new Bind(-1)));
   boolean enabled = false;
   private float dYaw;
   private float dPitch;

   public FreeLook() {
      super("FreeLook", "Rotate your camera and not your player in 3rd person", Category.PLAYER);
   }

   @Override
   public void onTick() {
      if (mc.currentScreen == null && this.bind.getValue().isDown()) {
         if (!this.enabled) {
            this.dYaw = 0.0F;
            this.dPitch = 0.0F;
            mc.gameSettings.thirdPersonView = 1;
         }

         this.enabled = true;
      } else {
         if (this.enabled) {
            mc.gameSettings.thirdPersonView = 0;
         }

         this.enabled = false;
      }

      if (mc.gameSettings.thirdPersonView != 1 && this.enabled) {
         this.enabled = false;
         mc.gameSettings.thirdPersonView = 0;
      }
   }

   @Override
   public void onDisable() {
      this.enabled = false;
      mc.gameSettings.thirdPersonView = 0;
   }

   @SubscribeEvent
   public void onCameraSetup(CameraSetup event) {
      if (mc.gameSettings.thirdPersonView > 0 && this.enabled) {
         event.setYaw(event.getYaw() + this.dYaw);
         event.setPitch(event.getPitch() + this.dPitch);
      }
   }

   @SubscribeEvent
   public void onTurn(TurnEvent event) {
      if (mc.gameSettings.thirdPersonView > 0 && this.enabled) {
         this.dYaw = (float)((double)this.dYaw + (double)event.getYaw() * 0.15);
         this.dPitch = (float)((double)this.dPitch - (double)event.getPitch() * 0.15);
         this.dPitch = MathHelper.clamp(this.dPitch, -90.0F, 90.0F);
         event.setCanceled(true);
      }
   }
}
