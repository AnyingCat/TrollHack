//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.api.events.impl.MotionEvent;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AntiAim extends Module {
   private final Setting<AntiAim.Mode> pitchMode = this.register(new Setting<>("PitchMode", AntiAim.Mode.None));
   private final Setting<AntiAim.Mode> yawMode = this.register(new Setting<>("YawMode", AntiAim.Mode.None));
   public Setting<Integer> Speed = this.register(new Setting<>("Speed", 1, 1, 45));
   public Setting<Integer> yawDelta = this.register(new Setting<>("YawDelta", 60, -360, 360));
   public Setting<Integer> pitchDelta = this.register(new Setting<>("PitchDelta", 10, -90, 90));
   public final Setting<Boolean> allowInteract = this.register(new Setting<>("AllowInteract", true));
   private float rotationYaw;
   private float rotationPitch;
   private float pitch_sinus_step;
   private float yaw_sinus_step;

   public AntiAim() {
      super("AntiAim", "fun", Category.PLAYER);
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public void onSync(MotionEvent event) {
      if (!this.allowInteract.getValue() || !mc.gameSettings.keyBindAttack.isKeyDown() && !mc.gameSettings.keyBindUseItem.isKeyDown()) {
         if (this.yawMode.getValue() != AntiAim.Mode.None) {
            event.setYaw(this.rotationYaw);
         }

         if (this.pitchMode.getValue() != AntiAim.Mode.None) {
            event.setPitch(this.rotationPitch);
         }
      }
   }

   @Override
   public void onUpdate() {
      if (this.pitchMode.getValue() == AntiAim.Mode.RandomAngle && mc.player.ticksExisted % this.Speed.getValue() == 0) {
         this.rotationPitch = MathUtil.random(90.0F, -90.0F);
      }

      if (this.yawMode.getValue() == AntiAim.Mode.RandomAngle && mc.player.ticksExisted % this.Speed.getValue() == 0) {
         this.rotationYaw = MathUtil.random(0.0F, 360.0F);
      }

      if (this.yawMode.getValue() == AntiAim.Mode.Spin && mc.player.ticksExisted % this.Speed.getValue() == 0) {
         this.rotationYaw += (float)this.yawDelta.getValue().intValue();
         if (this.rotationYaw > 360.0F) {
            this.rotationYaw = 0.0F;
         }

         if (this.rotationYaw < 0.0F) {
            this.rotationYaw = 360.0F;
         }
      }

      if (this.pitchMode.getValue() == AntiAim.Mode.Spin && mc.player.ticksExisted % this.Speed.getValue() == 0) {
         this.rotationPitch += (float)this.pitchDelta.getValue().intValue();
         if (this.rotationPitch > 90.0F) {
            this.rotationPitch = -90.0F;
         }

         if (this.rotationPitch < -90.0F) {
            this.rotationPitch = 90.0F;
         }
      }

      if (this.pitchMode.getValue() == AntiAim.Mode.Sinus) {
         this.pitch_sinus_step += (float)this.Speed.getValue().intValue() / 10.0F;
         this.rotationPitch = (float)(
            (double)mc.player.rotationPitch + (double)this.pitchDelta.getValue().intValue() * Math.sin((double)this.pitch_sinus_step)
         );
         this.rotationPitch = MathUtil.clamp(this.rotationPitch, -90.0F, 90.0F);
      }

      if (this.yawMode.getValue() == AntiAim.Mode.Sinus) {
         this.yaw_sinus_step += (float)this.Speed.getValue().intValue() / 10.0F;
         this.rotationYaw = (float)(
            (double)mc.player.rotationYaw + (double)this.yawDelta.getValue().intValue() * Math.sin((double)this.yaw_sinus_step)
         );
      }

      if (this.pitchMode.getValue() == AntiAim.Mode.Fixed) {
         this.rotationPitch = (float)this.pitchDelta.getValue().intValue();
      }

      if (this.yawMode.getValue() == AntiAim.Mode.Fixed) {
         this.rotationYaw = (float)this.yawDelta.getValue().intValue();
      }

      if (this.pitchMode.getValue() == AntiAim.Mode.Static) {
         this.rotationPitch = mc.player.rotationPitch + (float)this.pitchDelta.getValue().intValue();
         this.rotationPitch = MathUtil.clamp(this.rotationPitch, -90.0F, 90.0F);
      }

      if (this.yawMode.getValue() == AntiAim.Mode.Static) {
         this.rotationYaw = mc.player.rotationYaw % 360.0F + (float)this.yawDelta.getValue().intValue();
      }
   }

   public static enum Mode {
      None,
      RandomAngle,
      Spin,
      Sinus,
      Fixed,
      Static;
   }
}
