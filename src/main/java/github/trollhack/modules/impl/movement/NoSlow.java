package github.trollhack.modules.impl.movement;

import github.trollhack.events.impl.PacketEvent;
import github.trollhack.events.impl.TickEvent;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.settings.impl.EnumSetting;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

public class NoSlow extends Module {
    public static final NoSlow INSTANCE = new NoSlow();

    public enum Mode {
        NCP,
        StrictNCP,
        Grim
    }

    public final EnumSetting<Mode> mode = enumSetting("Mode", Mode.NCP);

    public NoSlow() {
        super("NoSlow", Category.MOVEMENT);
    }

    public String getInfo() {
        return mode.getValue().name();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent event) {
        if (nullCheck() || !isItemNoSlowActive()) return;

        if (mode.getValue() == Mode.StrictNCP) {
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
        } else if (mode.getValue() == Mode.Grim) {
            if (mc.player.getActiveHand() == Hand.OFF_HAND) {
                int slot = mc.player.getInventory().selectedSlot;
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot % 8 + 1));
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot % 7 + 2));
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPacketSend(PacketEvent.Send event) {
        if (nullCheck() || !isItemNoSlowActive()) return;

        if (mode.getValue() == Mode.Grim && event.getPacket() instanceof PlayerInteractItemC2SPacket packet) {
            if (packet.getHand() == Hand.MAIN_HAND && mc.player.getActiveHand() == Hand.MAIN_HAND) {
                mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, 0, mc.player.getYaw(), mc.player.getPitch()));
            }
        }
    }

    private boolean isItemNoSlowActive() {
        return mc.player.isUsingItem() && !mc.player.isRiding() && !mc.player.isFallFlying();
    }

    public boolean canNoSlow() {
        return isEnabled();
    }
}
