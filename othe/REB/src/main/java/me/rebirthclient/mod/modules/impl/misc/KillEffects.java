//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.misc;

import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class KillEffects extends Module {
   private final Setting<KillEffects.Lightning> lightning = this.add(new Setting<>("Lightning", KillEffects.Lightning.NORMAL));
   private final Setting<KillEffects.KillSound> killSound = this.add(new Setting<>("KillSound", KillEffects.KillSound.OFF));
   private final Setting<Boolean> oneself = this.add(new Setting<>("Oneself", true));
   private final Timer timer = new Timer();

   public KillEffects() {
      super("KillEffects", "jajaja hypixel mode", Category.MISC);
   }

   @Override
   public void onDeath(EntityPlayer player) {
      if (player != null
         && (player != mc.player || this.oneself.getValue())
         && !(player.getHealth() > 0.0F)
         && !mc.player.isDead
         && !nullCheck()
         && !fullNullCheck()) {
         if (this.timer.passedMs(1500L)) {
            if (this.lightning.getValue() != KillEffects.Lightning.OFF) {
               mc.world
                  .spawnEntity(new EntityLightningBolt(mc.world, player.posX, player.posY, player.posZ, true));
               if (this.lightning.getValue() == KillEffects.Lightning.NORMAL) {
                  mc.player.playSound(SoundEvents.ENTITY_LIGHTNING_THUNDER, 0.5F, 1.0F);
               }
            }

            if (this.killSound.getValue() != KillEffects.KillSound.OFF) {
               SoundEvent sound = this.getSound();
               if (sound != null) {
                  mc.player.playSound(sound, 1.0F, 1.0F);
               }
            }

            this.timer.reset();
         }
      }
   }

   private SoundEvent getSound() {
      switch((KillEffects.KillSound)this.killSound.getValue()) {
         case CS:
            return new SoundEvent(new ResourceLocation("rebirth", "kill_sound_cs"));
         case NEVERLOSE:
            return new SoundEvent(new ResourceLocation("rebirth", "kill_sound_nl"));
         case HYPIXEL:
            return SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
         default:
            return null;
      }
   }

   private static enum KillSound {
      CS,
      NEVERLOSE,
      HYPIXEL,
      OFF;
   }

   private static enum Lightning {
      NORMAL,
      SILENT,
      OFF;
   }
}
