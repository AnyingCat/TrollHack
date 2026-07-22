//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.asm.mixins;

import com.mojang.authlib.GameProfile;
import me.rebirthclient.api.events.impl.JumpEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.modules.impl.player.TpsSync;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({EntityPlayer.class})
public abstract class MixinEntityPlayer extends EntityLivingBase {
   public MixinEntityPlayer(World worldIn, GameProfile gameProfileIn) {
      super(worldIn);
   }

   @Inject(
      method = {"getCooldownPeriod"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void getCooldownPeriodHook(CallbackInfoReturnable<Float> callbackInfoReturnable) {
      TpsSync tpsSync = TpsSync.INSTANCE;
      if (tpsSync.isOn() && tpsSync.attack.getValue()) {
         callbackInfoReturnable.setReturnValue(
            (float)(
               1.0
                  / ((EntityPlayer)EntityPlayer.class.cast(this)).getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue()
                  * 20.0
                  * (double)Managers.SERVER.getTpsFactor()
            )
         );
      }
   }

   @Inject(
      method = {"jump"},
      at = {@At("HEAD")}
   )
   public void onJump(CallbackInfo ci) {
      if (Minecraft.getMinecraft().player.getName().equals(this.getName())) {
         MinecraftForge.EVENT_BUS.post(new JumpEvent(this.motionX, this.motionY));
      }
   }
}
