package me.catrix.mod.modules.impl.hud;

import me.catrix.Catrix;
import me.catrix.mod.modules.Module;
import net.minecraft.client.gui.DrawContext;

public class SelfHud extends Module {
    public static SelfHud INSTANCE;
    public SelfHud() {
        super("SelfHud",Category.Hud);
        setChinese("自身显示");
        INSTANCE = this;
    }

//    @Override
//    public void onDisable() {
//        SelfHUD.INSTANCE.kills = 0;
//    }

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        if (SelfHud.INSTANCE.isOn()) {
            Catrix.GUI.selfHud.draw(drawContext, tickDelta, null);
        }
    }
}
