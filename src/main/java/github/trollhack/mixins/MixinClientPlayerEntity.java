package github.trollhack.mixins;

import com.mojang.authlib.GameProfile;
import github.trollhack.events.Event;
import github.trollhack.events.EventBusHolder;
import github.trollhack.events.impl.MoveEvent;
import github.trollhack.events.impl.MotionEvent;
import github.trollhack.events.impl.TickEvent;
import github.trollhack.modules.impl.movement.NoSlow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        EventBusHolder.INSTANCE.post(TickEvent.INSTANCE);
    }

    @Shadow
    private void sendSprintingPacket() {}

    @Final
    @Shadow
    public ClientPlayNetworkHandler networkHandler;

    @Final
    @Shadow
    protected MinecraftClient client;

    @Shadow
    private double lastX;

    @Shadow
    private double lastBaseY;

    @Shadow
    private double lastZ;

    @Shadow
    private float lastYaw;

    @Shadow
    private float lastPitch;

    @Shadow
    private boolean lastOnGround;

    @Shadow
    private boolean lastSneaking;

    @Shadow
    private int ticksSinceLastPositionPacketSent;

    @Shadow
    private boolean autoJumpEnabled;

    @Shadow
    protected boolean isCamera() {
        return false;
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
    private void sendMovementPacketsHook(CallbackInfo ci) {
        ci.cancel();
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        MotionEvent preEvent = new MotionEvent(
                Event.Stage.Pre,
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYaw(),
                player.getPitch(),
                player.isOnGround()
        );
        EventBusHolder.INSTANCE.post(preEvent);
        if (preEvent.isCancelled()) {
            return;
        }

        this.sendSprintingPacket();

        boolean sneaking = player.isSneaking();
        if (sneaking != this.lastSneaking) {
            this.networkHandler.sendPacket(new ClientCommandC2SPacket(player, sneaking
                    ? ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY
                    : ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            this.lastSneaking = sneaking;
        }

        if (this.isCamera()) {
            double d = preEvent.getX() - this.lastX;
            double e = preEvent.getY() - this.lastBaseY;
            double f = preEvent.getZ() - this.lastZ;
            double g = preEvent.getYaw() - this.lastYaw;
            double h = preEvent.getPitch() - this.lastPitch;
            ++this.ticksSinceLastPositionPacketSent;
            boolean bl2 = MathHelper.squaredMagnitude(d, e, f) > MathHelper.square(2.0E-4)
                    || this.ticksSinceLastPositionPacketSent >= 20;
            boolean bl3 = g != 0.0 || h != 0.0;

            if (player.hasVehicle()) {
                Vec3d vec3d = player.getVelocity();
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                        vec3d.x, -999.0, vec3d.z,
                        preEvent.getYaw(), preEvent.getPitch(), preEvent.isOnGround()));
                bl2 = false;
            } else if (bl2 && bl3) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                        preEvent.getX(), preEvent.getY(), preEvent.getZ(),
                        preEvent.getYaw(), preEvent.getPitch(), preEvent.isOnGround()));
            } else if (bl2) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        preEvent.getX(), preEvent.getY(), preEvent.getZ(), preEvent.isOnGround()));
            } else if (bl3) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                        preEvent.getYaw(), preEvent.getPitch(), preEvent.isOnGround()));
            } else if (this.lastOnGround != preEvent.isOnGround()) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(preEvent.isOnGround()));
            }

            if (bl2) {
                this.lastX = preEvent.getX();
                this.lastBaseY = preEvent.getY();
                this.lastZ = preEvent.getZ();
                this.ticksSinceLastPositionPacketSent = 0;
            }
            if (bl3) {
                this.lastYaw = preEvent.getYaw();
                this.lastPitch = preEvent.getPitch();
            }
            this.lastOnGround = preEvent.isOnGround();
            this.autoJumpEnabled = this.client.options.getAutoJump().getValue();
        }

        EventBusHolder.INSTANCE.post(new MotionEvent(
                Event.Stage.Post,
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYaw(),
                player.getPitch(),
                player.isOnGround()
        ));
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"), cancellable = true)
    private void onMoveHook(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        MoveEvent event = new MoveEvent(movement.x, movement.y, movement.z);
        EventBusHolder.INSTANCE.post(event);
        ci.cancel();
        if (!event.isCancelled()) {
            super.move(movementType, new Vec3d(event.getX(), event.getY(), event.getZ()));
        }
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"), require = 0)
    private boolean onIsUsingItem(ClientPlayerEntity player) {
        if (NoSlow.INSTANCE.canNoSlow()) {
            return false;
        }
        return player.isUsingItem();
    }
}
