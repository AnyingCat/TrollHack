package github.trollhack.mixins;

import github.trollhack.core.Managers;
import github.trollhack.events.EventBusHolder;
import github.trollhack.events.impl.Render3DEvent;
import github.trollhack.modules.Module;
import github.trollhack.modules.impl.render.Fov;
import github.trollhack.modules.impl.render.NoRender;
import github.trollhack.utils.render.ProjectionUtil;
import github.trollhack.utils.render.shader.satin.impl.ReloadableShaderEffectManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static github.trollhack.modules.Module.mc;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Inject(method = "renderWorld", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z",
            opcode = Opcodes.GETFIELD, ordinal = 0))
    private void onRender3D(RenderTickCounter tickCounter, CallbackInfo ci) {
        if (mc.player == null || mc.world == null) return;

        ProjectionUtil.lastProjMat.set(RenderSystem.getProjectionMatrix());
        ProjectionUtil.lastModMat.set(RenderSystem.getModelViewMatrix());

        Camera camera = mc.gameRenderer.getCamera();
        MatrixStack matrices = new MatrixStack();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));

        ProjectionUtil.lastWorldSpaceMatrix.set(matrices.peek().getPositionMatrix());

        EventBusHolder.INSTANCE.post(new Render3DEvent(matrices, tickCounter.getTickDelta(false)));
    }

    @Inject(method = "loadPrograms", at = @At(value = "RETURN"))
    private void loadSatinPrograms(ResourceFactory factory, CallbackInfo ci) {
        ReloadableShaderEffectManager.INSTANCE.reload(factory);
    }

    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderHand(Lnet/minecraft/client/render/Camera;FLorg/joml/Matrix4f;)V", shift = At.Shift.AFTER))
    public void postRender3dHook(RenderTickCounter tickCounter, CallbackInfo ci) {
        if (Module.nullCheck()) return;
        Managers.SHADER.renderShaders();
    }

    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    private void onShowFloatingItem(ItemStack floatingItem, CallbackInfo ci) {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.totemAnimation.getValue()) {
            if (floatingItem.getItem() == Items.TOTEM_OF_UNDYING) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void onTiltViewWhenHurt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.hurtCam.getValue()) {
            ci.cancel();
        }
    }

    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private float onNauseaLerp(float delta, float first, float second) {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.nauseaEffect.getValue()) {
            return 0f;
        }
        return MathHelper.lerp(delta, first, second);
    }

    @ModifyVariable(method = "getFov", at = @At(value = "STORE", ordinal = 1), ordinal = 0)
    private double onGetFovDynamic(double value) {
        return Fov.INSTANCE.getFOVModifierDynamic(value);
    }

    @Inject(method = "getFov", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    private void onGetFovReturn(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        if (changingFov) {
            Fov.INSTANCE.getFOVModifierNoDynamic(cir);
        }
    }
}
