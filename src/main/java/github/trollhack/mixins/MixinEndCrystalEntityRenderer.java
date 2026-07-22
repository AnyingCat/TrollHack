package github.trollhack.mixins;

import github.trollhack.modules.impl.render.CrystalChams;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndCrystalEntityRenderer.class)
public abstract class MixinEndCrystalEntityRenderer {
    @Shadow
    @Final
    private ModelPart core;

    @Shadow
    @Final
    private ModelPart frame;

    @Shadow
    @Final
    private ModelPart bottom;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRenderHead(EndCrystalEntity endCrystalEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (CrystalChams.INSTANCE.isEnabled() && CrystalChams.INSTANCE.shouldRender(endCrystalEntity)) {
            CrystalChams.renderCrystal(endCrystalEntity, g, matrixStack, vertexConsumerProvider, i, core, frame, bottom);
            ci.cancel();
        }
    }
}
