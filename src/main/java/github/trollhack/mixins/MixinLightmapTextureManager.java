package github.trollhack.mixins;

import github.trollhack.modules.impl.render.Ambience;
import github.trollhack.modules.impl.render.NoRender;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {

    @Shadow
    @Final
    private NativeImageBackedTexture texture;

    @Shadow
    @Final
    private NativeImage image;

    @Shadow
    private boolean dirty;

    @Inject(method = "getDarknessFactor", at = @At("HEAD"), cancellable = true)
    private void onGetDarknessFactor(float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.darknessEffect.getValue()) {
            cir.setReturnValue(0.0f);
        }
    }

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void onUpdate(float tickProgress, CallbackInfo ci) {
        if (Ambience.INSTANCE.isEnabled() && Ambience.INSTANCE.fullBright.getValue()) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 16; j++) {
                    this.image.setColor(j, i, 0xFFFFFFFF);
                }
            }
            this.texture.upload();
            this.dirty = false;
            ci.cancel();
        }
    }
}
