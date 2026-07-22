package github.trollhack.mixins;

import github.trollhack.events.EventBusHolder;
import github.trollhack.events.impl.PacketEvent;
import github.trollhack.utils.network.PacketSilentManager;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientConnection {

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (PacketSilentManager.removeSilent(packet)) return;

        PacketEvent.Send event = new PacketEvent.Send(packet);
        EventBusHolder.INSTANCE.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        } else if (event.getPacket() != packet) {
            ci.cancel();
            PacketSilentManager.addSilent(event.getPacket());
            ((ClientConnection) (Object) this).send(event.getPacket());
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("TAIL"))
    private void onSendPacketPost(Packet<?> packet, CallbackInfo ci) {
        if (PacketSilentManager.isSilent(packet)) return;
        EventBusHolder.INSTANCE.post(new PacketEvent.SendPost(packet));
    }

    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static void onHandlePacket(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        PacketEvent.Receive event = new PacketEvent.Receive(packet);
        EventBusHolder.INSTANCE.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
