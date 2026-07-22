//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.asm.mixins;

import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.modules.impl.movement.NoJumpDelay;
import me.rebirthclient.mod.modules.impl.render.ItemModel;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({EntityLivingBase.class})
public class MixinEntityLivingBase implements Wrapper {
   @Shadow
   private int jumpTicks;

   @Inject(
      method = {"onLivingUpdate"},
      at = {@At("HEAD")}
   )
   private void headLiving(CallbackInfo callbackInfo) {
      if (NoJumpDelay.INSTANCE.isOn()) {
         this.jumpTicks = 0;
      }
   }

   @Inject(
      method = {"getArmSwingAnimationEnd"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void getArmSwingAnimationEnd(CallbackInfoReturnable<Integer> info) {
      ItemModel mod = ItemModel.INSTANCE;
      if (mod.isOn() && mod.customSwing.getValue() && mod.swing.getValue() == ItemModel.Swing.SERVER) {
         info.setReturnValue(-1);
      } else if (mod.isOn() && mod.slowSwing.getValue()) {
         info.setReturnValue(mod.swingSpeed.getValue());
      }
   }

}
