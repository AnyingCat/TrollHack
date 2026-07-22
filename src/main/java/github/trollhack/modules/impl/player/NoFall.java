package github.trollhack.modules.impl.player;

import github.trollhack.events.impl.MotionEvent;
import github.trollhack.events.impl.PacketEvent;
import github.trollhack.mixins.accessors.IPlayerMoveC2SPacket;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.settings.impl.EnumSetting;
import github.trollhack.settings.impl.FloatSetting;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall extends Module {
    public static final NoFall INSTANCE = new NoFall();

    public enum Mode {
        Packet,
        SpoofGround
    }

    public final EnumSetting<Mode> mode = enumSetting("Mode", Mode.SpoofGround);
    public final FloatSetting minFallDistance = floatSetting("MinFallDistance", 3.0f, 0.0f, 20.0f, 0.5f);

    public NoFall() {
        super("NoFall", Category.PLAYER);
    }

    public String getInfo() {
        return mode.getValue().name();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMotion(MotionEvent event) {
        if (nullCheck() || !event.isPre()) return;
        if (mc.player.isCreative() || mc.player.isSpectator()) return;
        if (mc.player.getAbilities().invulnerable || mc.player.getAbilities().flying) return;
        if (mc.player.isFallFlying()) return;

        if (!shouldTrigger()) return;

        if (mode.getValue() == Mode.SpoofGround) {
            event.setOnGround(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPacketSend(PacketEvent.Send event) {
        if (nullCheck()) return;
        if (mc.player.isCreative() || mc.player.isSpectator()) return;
        if (mc.player.getAbilities().invulnerable || mc.player.getAbilities().flying) return;

        if (mode.getValue() == Mode.Packet) {
            if (event.getPacket() instanceof PlayerMoveC2SPacket packet) {
                if (shouldTrigger()) {
                    IPlayerMoveC2SPacket accessor = (IPlayerMoveC2SPacket) packet;
                    accessor.setOnGround(true);
                    mc.player.fallDistance = 0;
                }
            }
        }
    }

    private boolean shouldTrigger() {
        if (mc.player == null) return false;
        if (mc.player.isFallFlying()) return false;
        return mc.player.fallDistance >= minFallDistance.getValue();
    }
}