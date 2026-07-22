package github.trollhack.modules.impl.render;

import github.trollhack.events.impl.PacketEvent;
import github.trollhack.events.impl.TickEvent;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.settings.impl.EnumSetting;
import github.trollhack.settings.impl.IntegerSetting;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public class Ambience extends Module {
    public static final Ambience INSTANCE = new Ambience();

    public enum TimeMode {
        Custom,
        Dawn,
        Day,
        Noon,
        Dusk,
        Night,
        Midnight
    }

    public final BooleanSetting customTime = booleanSetting("CustomTime", false);
    public final EnumSetting<TimeMode> timeMode = enumSetting("TimeMode", TimeMode.Night, () -> customTime.getValue());
    public final IntegerSetting customTimeValue = integerSetting("Time", 18000, 0, 24000, 100,
            () -> customTime.getValue() && timeMode.getValue() == TimeMode.Custom);

    public final BooleanSetting fullBright = booleanSetting("FullBright", false);

    private long oldTime;

    public Ambience() {
        super("Ambience", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (nullCheck()) return;
        oldTime = mc.world.getTimeOfDay();
    }

    @Override
    public void onDisable() {
        if (nullCheck()) return;
        mc.world.setTimeOfDay(oldTime);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent event) {
        if (nullCheck()) return;

        if (customTime.getValue()) {
            mc.world.setTimeOfDay(switch (timeMode.getValue()) {
                case Custom -> customTimeValue.getValue();
                case Dawn -> 23041L;
                case Day -> 1000L;
                case Noon -> 6000L;
                case Dusk -> 12610L;
                case Night -> 13000L;
                case Midnight -> 18000L;
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPacketReceive(PacketEvent.Receive event) {
        if (nullCheck()) return;

        if (customTime.getValue() && event.getPacket() instanceof WorldTimeUpdateS2CPacket packet) {
            oldTime = packet.getTime();
            event.setCancelled(true);
        }
    }
}
