package github.trollhack.modules.impl.movement;

import github.trollhack.events.impl.TickEvent;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.settings.impl.EnumSetting;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.effect.StatusEffects;

public class Sprint extends Module {
    public static Sprint INSTANCE = new Sprint();

    public enum Mode {
        Legit,
        Rage
    }

    public final EnumSetting<Mode> mode = enumSetting("Mode", Mode.Legit);
    public final BooleanSetting ignoreBlindness = booleanSetting("IgnoreBlindness", false);
    public final BooleanSetting ignoreHunger = booleanSetting("IgnoreHunger", false);
    public final BooleanSetting ignoreCollision = booleanSetting("IgnoreCollision", false);

    public Sprint() {
        super("Sprint", Category.MOVEMENT);
    }

    public String getInfo() {
        return mode.getValue().name();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent event) {
        if (nullCheck()) return;
        if (mc.player.input.movementForward == 0 && mc.player.input.movementSideways == 0) return;

        if (shouldSprint() && !mc.player.isSprinting()) {
            mc.player.setSprinting(true);
        }
    }

    private boolean shouldSprint() {
        if (!ignoreHunger.getValue() && mc.player.getHungerManager().getFoodLevel() <= 6 && !mc.player.isCreative()) return false;
        if (!ignoreBlindness.getValue() && mc.player.hasStatusEffect(StatusEffects.BLINDNESS)) return false;
        if (!ignoreCollision.getValue() && mc.player.horizontalCollision) return false;
        if (mc.player.isSneaking() || mc.player.isRiding() || mc.player.isInFluid()) return false;
        return mode.getValue() != Mode.Legit || mc.player.input.movementForward > 0;
    }
}
