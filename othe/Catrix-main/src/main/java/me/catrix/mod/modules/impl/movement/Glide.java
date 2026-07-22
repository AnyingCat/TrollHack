package me.catrix.mod.modules.impl.movement;

import me.catrix.api.events.eventbus.EventHandler;
import me.catrix.api.events.eventbus.EventPriority;
import me.catrix.api.events.impl.MoveEvent;
import me.catrix.mod.modules.Module;
import me.catrix.mod.modules.settings.impl.BooleanSetting;
import me.catrix.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.effect.StatusEffects;

import static me.catrix.api.utils.entity.MovementUtil.*;

public class Glide extends Module {
    public static Glide INSTANCE;

    public Glide() {
        super("Glide", Category.Movement);
        setChinese("滑行");
        INSTANCE = this;
    }
    public final BooleanSetting onlyFall = add(new BooleanSetting("OnlyFall", false));
    public final BooleanSetting noCollision = add(new BooleanSetting("NoCollision", false));
    public final SliderSetting yOffset = add(new SliderSetting("YOffset", 0.6, 0.1, 1, 0.01));
    public final SliderSetting speed = add(new SliderSetting("Speed", 0.6, 0.1, 1, 0.01));
    public final SliderSetting downFactor = add(new SliderSetting("FallSpeed", 0, 0.0, 1, 0.000001));
    private MoveEvent event;

    @EventHandler(priority = EventPriority.LOW)
    public void onMove(MoveEvent event) {
        if (nullCheck()) return;
        if (mc.player.input.sneaking) return;
        if (!mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING)) return;
        if (onlyFall.getValue() && mc.player.fallDistance <= 0) return;
        if (mc.player.isOnGround()) return;
        if (mc.player.horizontalCollision && noCollision.getValue()) return;
        if (mc.world.canCollide(mc.player, mc.player.getBoundingBox().offset(0, -yOffset.getValue(), 0))) {
            this.event = event;
            setY(-downFactor.getValue());
            double[] dir = directionSpeed(speed.getValue());
            setX(dir[0]);
            setZ(dir[1]);
        }
    }

    private void setX(double f) {
        event.setX(f);
        setMotionX(f);
    }

    private void setY(double f) {
        event.setY(f);
        setMotionY(f);
    }

    private void setZ(double f) {
        event.setZ(f);
        setMotionZ(f);
    }
}
