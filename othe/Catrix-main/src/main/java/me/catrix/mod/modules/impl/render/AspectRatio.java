package me.catrix.mod.modules.impl.render;

import me.catrix.mod.modules.settings.impl.SliderSetting;
import me.catrix.mod.modules.Module;

public class AspectRatio extends Module {
    public static AspectRatio INSTANCE;

    public final SliderSetting ratio =
            add(new SliderSetting("Ratio", 1.78, 0.0, 5.0, 0.01));
    public AspectRatio() {
        super("AspectRatio", Category.Render);
        setChinese("分辨率");
        INSTANCE = this;
    }
}
