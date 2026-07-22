package github.trollhack.mixins;

import github.trollhack.core.Managers;
import github.trollhack.core.impl.RotationManager;
import github.trollhack.modules.impl.client.Rotations;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> {
    @Unique
    private LivingEntity lastEntity;

    @Unique
    private float originalYaw;
    @Unique
    private float originalHeadYaw;
    @Unique
    private float originalBodyYaw;
    @Unique
    private float originalPitch;
    @Unique
    private float originalPrevYaw;
    @Unique
    private float originalPrevHeadYaw;
    @Unique
    private float originalPrevBodyYaw;
    @Unique
    private float originalPrevPitch;
    @Unique
    private boolean rotated;

    @Inject(method = "render", at = @At("HEAD"))
    public void onRenderPre(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        rotated = false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && livingEntity == mc.player && Rotations.INSTANCE.isEnabled() && !Rotations.INSTANCE.serverRotate.getValue()) {
            RotationManager rm = Managers.ROTATION;
            originalYaw = livingEntity.getYaw();
            originalHeadYaw = livingEntity.headYaw;
            originalBodyYaw = livingEntity.bodyYaw;
            originalPitch = livingEntity.getPitch();
            originalPrevYaw = livingEntity.prevYaw;
            originalPrevHeadYaw = livingEntity.prevHeadYaw;
            originalPrevBodyYaw = livingEntity.prevBodyYaw;
            originalPrevPitch = livingEntity.prevPitch;

            livingEntity.setYaw(rm.getRenderYawOffset());
            livingEntity.headYaw = rm.getRotationYawHead();
            livingEntity.bodyYaw = rm.getRenderYawOffset();
            livingEntity.setPitch(rm.getRenderPitch());
            livingEntity.prevYaw = rm.getPrevRenderYawOffset();
            livingEntity.prevHeadYaw = rm.getPrevRotationYawHead();
            livingEntity.prevBodyYaw = rm.getPrevRenderYawOffset();
            livingEntity.prevPitch = rm.getPrevRenderPitch();
            rotated = true;
        }
        lastEntity = livingEntity;
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void onRenderPost(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (!rotated) return;
        livingEntity.setYaw(originalYaw);
        livingEntity.headYaw = originalHeadYaw;
        livingEntity.bodyYaw = originalBodyYaw;
        livingEntity.setPitch(originalPitch);
        livingEntity.prevYaw = originalPrevYaw;
        livingEntity.prevHeadYaw = originalPrevHeadYaw;
        livingEntity.prevBodyYaw = originalPrevBodyYaw;
        livingEntity.prevPitch = originalPrevPitch;
        rotated = false;
    }
}
