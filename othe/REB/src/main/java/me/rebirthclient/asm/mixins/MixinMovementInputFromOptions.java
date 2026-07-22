//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.asm.mixins;

import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.modules.impl.movement.InventoryMove;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MovementInputFromOptions;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({MovementInputFromOptions.class})
public class MixinMovementInputFromOptions implements Wrapper {
   @Redirect(
      method = {"updatePlayerMoveState"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/settings/KeyBinding;isKeyDown()Z"
)
   )
   public boolean isKeyPressed(KeyBinding keyBinding) {
      int keyCode = keyBinding.getKeyCode();
      if (keyCode <= 0) {
         return keyBinding.isKeyDown();
      } else if (keyCode >= 256) {
         return keyBinding.isKeyDown();
      } else if (!InventoryMove.INSTANCE.isOn()) {
         return keyBinding.isKeyDown();
      } else if (Minecraft.getMinecraft().currentScreen == null) {
         return keyBinding.isKeyDown();
      } else if (Minecraft.getMinecraft().currentScreen instanceof GuiChat) {
         return keyBinding.isKeyDown();
      } else {
         return keyCode == 42 && !InventoryMove.INSTANCE.sneak.getValue() ? keyBinding.isKeyDown() : Keyboard.isKeyDown(keyCode);
      }
   }
}
