package github.trollhack.mixins;

import github.trollhack.events.EventBusHolder;
import github.trollhack.events.impl.JumpEvent;
import github.trollhack.events.impl.TravelEvent;
import github.trollhack.modules.impl.client.Rotations;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity {

    @Inject(method = "travel", at = @At("HEAD"))
    private void onTravelPre(Vec3d movementInput, CallbackInfo ci) {
        EventBusHolder.INSTANCE.post(new TravelEvent(true));
        Rotations.INSTANCE.applyMoveFixPre();
    }

    @Inject(method = "travel", at = @At("RETURN"))
    private void onTravelPost(Vec3d movementInput, CallbackInfo ci) {
        Rotations.INSTANCE.applyMoveFixPost();
        EventBusHolder.INSTANCE.post(new TravelEvent(false));
    }

    @Inject(method = "jump", at = @At("HEAD"))
    private void onJumpPre(CallbackInfo ci) {
        Rotations rotations = Rotations.INSTANCE;
        if (rotations.isEnabled() && rotations.moveFix.getValue() != Rotations.MoveFixMode.Off && !Float.isNaN(rotations.fixRotation)) {
            rotations.applyMoveFixPre();
        }
        EventBusHolder.INSTANCE.post(new JumpEvent(true));
    }

    @Inject(method = "jump", at = @At("RETURN"))
    private void onJumpPost(CallbackInfo ci) {
        Rotations rotations = Rotations.INSTANCE;
        if (rotations.isEnabled() && rotations.moveFix.getValue() != Rotations.MoveFixMode.Off && !Float.isNaN(rotations.fixRotation)) {
            rotations.applyMoveFixPost();
        }
        EventBusHolder.INSTANCE.post(new JumpEvent(false));
    }
}
