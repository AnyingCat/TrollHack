package me.catrix.mod.modules.impl.misc;

import me.catrix.api.events.eventbus.EventHandler;
import me.catrix.api.events.impl.PacketEvent;
import me.catrix.core.impl.CommandManager;
import me.catrix.api.utils.entity.EntityUtil;
import me.catrix.api.utils.math.Timer;
import me.catrix.mod.modules.Module;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class Debug extends Module {

    public Debug() {
        super("Debug", Category.Misc);
    }

    Timer timer = new Timer();
    @EventHandler
    public void onReceivePacket(PacketEvent.Send event) {
        if (nullCheck()) return;
        if (event.getPacket() instanceof PlayerActionC2SPacket packet) {
            if (packet.getAction() == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
                timer.reset();
            } else if (packet.getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK) {
                CommandManager.sendChatMessage(timer.getPassedTimeMs() + "ms");
            }
            CommandManager.sendChatMessage(packet.getAction().name());
        }
    }

    @Override
    public void onEnable() {
        if (nullCheck()) return;
        BlockPos pos = EntityUtil.getPlayerPos(true).down();
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, Direction.DOWN));
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.DOWN));
    }
}