//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.asm.mixins;

import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.commands.Command;
import me.rebirthclient.mod.modules.impl.misc.SilentDisconnect;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({NetHandlerPlayClient.class})
public class MixinNetHandlerPlayClient {
   @Inject(
      method = {"onDisconnect"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onDisconnect(ITextComponent reason, CallbackInfo callbackInfo) {
      if (Wrapper.mc.player != null && Wrapper.mc.world != null && SilentDisconnect.INSTANCE.isOn()) {
         Command.sendMessage("§4[!] You get kicked! reason: §7" + reason.getFormattedText());
         callbackInfo.cancel();
      }
   }
}
