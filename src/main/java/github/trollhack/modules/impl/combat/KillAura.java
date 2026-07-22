package github.trollhack.modules.impl.combat;

import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.settings.impl.FloatSetting;
import github.trollhack.settings.impl.EnumSetting;

public class KillAura extends Module {
    public static final KillAura INSTANCE = new KillAura();

    public enum Mode {
        SINGLE,
        MULTI,
        RANGE
    }

    public final FloatSetting range = floatSetting("Range", 4.5f, 1.0f, 6.0f, 0.1f);
    public final FloatSetting cps = floatSetting("CPS", 10.0f, 1.0f, 20.0f, 0.1f);
    public final EnumSetting<Mode> mode = enumSetting("Mode", Mode.SINGLE);
    public final BooleanSetting autoBlock = booleanSetting("Auto block", false);
    public final BooleanSetting prioritiseTargets = booleanSetting("Prioritise Targets", true);

    public KillAura() {
        super("KillAura", Category.COMBAT);
    }

    @Override
    public void onUpdate() {
    }
}
