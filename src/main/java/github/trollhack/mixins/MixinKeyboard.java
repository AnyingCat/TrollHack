package github.trollhack.mixins;

import github.trollhack.core.Managers;
import github.trollhack.modules.impl.render.Fov;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKeyboard {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (client.player == null || client.world == null) return;
        if (client.currentScreen != null) return;

        if (action == 1) {
            Managers.MODULE.handleKeyPress(key);
            Fov.INSTANCE.onZoomKeyPress(key);
        } else if (action == 0) {
            Managers.MODULE.handleKeyRelease(key);
            Fov.INSTANCE.onZoomKeyRelease(key);
        }
    }
}
