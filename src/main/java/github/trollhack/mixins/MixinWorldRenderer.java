package github.trollhack.mixins;

import github.trollhack.core.Managers;
import github.trollhack.core.impl.ShaderManager;
import github.trollhack.modules.impl.render.ESP;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static github.trollhack.utils.interfaces.Mc.mc;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/PostEffectProcessor;render(F)V", ordinal = 0))
    private void replaceShaderHook(PostEffectProcessor instance, float tickDelta) {
        if (ESP.INSTANCE.isEnabled() && mc.world != null && ESP.INSTANCE.mode.getValue() == ESP.Mode.SHADER) {
            if (Managers.SHADER.fullNullCheck()) return;
            Managers.SHADER.setupShader(Managers.SHADER.getShaderOutline(ShaderManager.Shader.Default));
        } else {
            instance.render(tickDelta);
        }
    }

}
