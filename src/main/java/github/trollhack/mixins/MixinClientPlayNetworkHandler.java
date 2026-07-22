package github.trollhack.mixins;

import github.trollhack.events.EventBusHolder;
import github.trollhack.events.impl.TotemPopEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Inject(method = "onEntityStatus", at = @At("TAIL"))
    private void onEntityStatus(EntityStatusS2CPacket packet, CallbackInfo ci) {
        if (packet.getStatus() == EntityStatuses.USE_TOTEM_OF_UNDYING) {
            if (packet.getEntity(MinecraftClient.getInstance().world) instanceof PlayerEntity player) {
                EventBusHolder.INSTANCE.post(new TotemPopEvent.Detect(player));
            }
        }
    }
}
