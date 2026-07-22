package github.trollhack.mixins;

import github.trollhack.gui.mainmenu.MainMenu;
import github.trollhack.modules.impl.client.ClickGUI;
import net.minecraft.client.gui.screen.TitleScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static github.trollhack.utils.interfaces.Mc.mc;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        if (ClickGUI.INSTANCE.getKeyBind() == -1) {
            ClickGUI.INSTANCE.setKeyBind(GLFW.GLFW_KEY_RIGHT_SHIFT);
        }
        mc.setScreen(new MainMenu());
    }
}
