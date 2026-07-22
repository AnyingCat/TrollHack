package github.trollhack.mixins;

import github.trollhack.modules.impl.client.Rotations;
import github.trollhack.modules.impl.movement.NoSlow;
import github.trollhack.modules.impl.render.ESP;
import github.trollhack.modules.impl.render.NoRender;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    public void isGlowingHook(CallbackInfoReturnable<Boolean> cir) {
        if (ESP.INSTANCE.isGlowing((Entity) (Object) this)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getTeamColorValue", at = @At("HEAD"), cancellable = true)
    private void onGetTeamColorValue(CallbackInfoReturnable<Integer> cir) {
        Integer espColor = ESP.INSTANCE.getEspColor((Entity) (Object) this);
        if (espColor != null) {
            cir.setReturnValue(espColor);
        }
    }

    @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
    private void onIsInvisibleTo(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.invisiblePlayers.getValue()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "slowMovement", at = @At("HEAD"), cancellable = true)
    private void onSlowMovement(BlockState state, Vec3d multiplier, CallbackInfo ci) {
        if ((Object) this == MinecraftClient.getInstance().player && NoSlow.INSTANCE.isEnabled()) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "changeLookDirection", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private double changeLookDirectionYaw(double value) {
        return value;
    }

    @ModifyVariable(method = "changeLookDirection", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private double changeLookDirectionPitch(double value) {
        return value;
    }

    @Inject(method = "updateVelocity", at = @At("HEAD"), cancellable = true)
    private void onUpdateVelocity(float speed, Vec3d movementInput, CallbackInfo ci) {
        if ((Object) this != MinecraftClient.getInstance().player) return;
        Rotations rotations = Rotations.INSTANCE;
        if (!rotations.isEnabled() || rotations.moveFix.getValue() != Rotations.MoveFixMode.Free || Float.isNaN(rotations.fixRotation)) return;

        ci.cancel();
        double d = movementInput.lengthSquared();
        if (d < 1.0E-7) {
            ((Entity) (Object) this).setVelocity(((Entity) (Object) this).getVelocity());
            return;
        }
        Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply(speed);
        float yaw = rotations.fixRotation;
        float f = MathHelper.sin(yaw * MathHelper.RADIANS_PER_DEGREE);
        float g = MathHelper.cos(yaw * MathHelper.RADIANS_PER_DEGREE);
        Vec3d velocity = new Vec3d(
                vec3d.x * g - vec3d.z * f,
                vec3d.y,
                vec3d.z * g + vec3d.x * f
        );
        ((Entity) (Object) this).setVelocity(((Entity) (Object) this).getVelocity().add(velocity));
    }
}
