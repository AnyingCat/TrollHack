package me.catrix.asm.mixins;

import me.catrix.Catrix;
import me.catrix.mod.modules.impl.client.ClientSetting;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.TimeoutException;
import me.catrix.api.events.impl.PacketEvent;
import me.catrix.core.impl.CommandManager;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.handler.PacketEncoderException;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientConnection.class)
public class MixinClientConnection {

	@Inject(at = { @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;handlePacket(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;)V", ordinal = 0) }, method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", cancellable = true)
	protected void onChannelRead(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
		PacketEvent event = new PacketEvent.Receive(packet);
		Catrix.EVENT_BUS.post(event);
		if (event.isCancelled()) {
			ci.cancel();
		}
	}

	@Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"),cancellable = true)
	private void onSendPacketPre(Packet<?> packet, CallbackInfo info) {
		PacketEvent.Send event = new PacketEvent.Send(packet);
		Catrix.EVENT_BUS.post(event);
		if (event.isCancelled()) {
			info.cancel();
		}
	}

	@Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("TAIL"),cancellable = true)
	private void onSendPacketPost(Packet<?> packet, CallbackInfo info) {
		PacketEvent.SendPost event = new PacketEvent.SendPost(packet);
		Catrix.EVENT_BUS.post(event);
		if (event.isCancelled()) {
			info.cancel();
		}
	}

	@Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
	private void exceptionCaught(ChannelHandlerContext context, Throwable throwable, CallbackInfo ci) {
		if (!(throwable instanceof TimeoutException) && !(throwable instanceof PacketEncoderException) && ClientSetting.INSTANCE.caughtException.getValue()) {
			if (ClientSetting.INSTANCE.log.getValue()) CommandManager.sendChatMessage("§4Caught exception: §7" + throwable.getMessage());
			ci.cancel();
		}
	}
}
