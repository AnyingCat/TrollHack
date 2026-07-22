package me.catrix.mod.modules.impl.hud;

import me.catrix.Catrix;
import me.catrix.mod.modules.Module;
import net.minecraft.client.gui.DrawContext;

public class PotionHud extends Module {
    public static PotionHud INSTANCE;
    public PotionHud() {
        super("PotionHud",Category.Hud);
        setChinese("药水显示");
        INSTANCE = this;
    }

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        if (PotionHud.INSTANCE.isOn()) {
            Catrix.GUI.potionHud.draw(drawContext, tickDelta, null);
        }
    }
}
