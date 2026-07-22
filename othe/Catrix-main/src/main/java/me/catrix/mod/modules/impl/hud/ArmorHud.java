package me.catrix.mod.modules.impl.hud;

import me.catrix.Catrix;
import me.catrix.mod.modules.Module;
import net.minecraft.client.gui.DrawContext;

public class ArmorHud extends Module {
    public static ArmorHud INSTANCE;

    public ArmorHud() {
        super("ArmorHud", Category.Hud);
        setChinese("装备显示");
        INSTANCE = this;
    }

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        if (ArmorHud.INSTANCE.isOn()) {
            Catrix.GUI.armorHud.draw(drawContext, tickDelta, null);
        }
    }
}
