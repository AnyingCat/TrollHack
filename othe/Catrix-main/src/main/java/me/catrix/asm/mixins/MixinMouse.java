package me.catrix.asm.mixins;

import me.catrix.Catrix;
import me.catrix.api.events.impl.MouseUpdateEvent;
import me.catrix.mod.gui.clickgui.ClickGuiScreen;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.catrix.api.utils.Wrapper.mc;
@Mixin(Mouse.class)
public class MixinMouse {
    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouse(long window, int button, int action, int mods, CallbackInfo ci) {
        int key = -(button + 2);
        if (mc.currentScreen instanceof ClickGuiScreen && action == 1 && Catrix.MODULE.setBind(key)) {
            return;
        }
        if (action == 1) {
            Catrix.MODULE.onKeyPressed(key);
        }
        if (action == 0) {
            Catrix.MODULE.onKeyReleased(key);
        }
    }

    @Inject(method = "updateMouse", at = @At("RETURN"))
    private void updateHook(CallbackInfo ci) {
        Catrix.EVENT_BUS.post(new MouseUpdateEvent());
    }
}
