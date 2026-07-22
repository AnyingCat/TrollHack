package me.catrix.mod.modules.impl.hud;

import me.catrix.Catrix;
import me.catrix.mod.gui.elements.TargetHUD;
import me.catrix.mod.modules.Module;
import me.catrix.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.client.gui.DrawContext;

public class TargetHud extends Module {
    public static TargetHud INSTANCE;

    public final BooleanSetting follow = add(new BooleanSetting("Follow",false));

    public TargetHud() {
        super("TargetHud", Category.Hud);
        setChinese("目标玩家显示");
        INSTANCE = this;
    }

    @Override
    public void onUpdate(){
        TargetHUD.INSTANCE.healthAnimation.update();
    }

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        if (TargetHud.INSTANCE.isOn()) {
            Catrix.GUI.targetHud.draw(drawContext, tickDelta, null);
        }
    }
}