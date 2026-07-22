package github.trollhack.mixins;

import github.trollhack.modules.impl.render.Fov;
import net.minecraft.client.Mouse;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mouse.class)
public class MixinMouse {

    @Redirect(
        method = "updateMouse",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;", ordinal = 0)
    )
    private Object redirectMouseSensitivity(SimpleOption<?> instance) {
        Double original = (Double) instance.getValue();
        if (Fov.INSTANCE.isEnabled() && Fov.INSTANCE.isZooming()) {
            return original * Fov.INSTANCE.getSensitivityMultiplier();
        }
        return original;
    }
}
