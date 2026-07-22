package me.catrix.mod.modules.impl.hud;

import me.catrix.Catrix;
import me.catrix.mod.modules.Module;
import net.minecraft.client.gui.DrawContext;

public class KeyDisplayHud extends Module {
    public static KeyDisplayHud INSTANCE;
    public KeyDisplayHud() {
        super("KeyDisplayHud",Category.Hud);
        INSTANCE = this;
    }

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        if (KeyDisplayHud.INSTANCE.isOn()) {
            Catrix.GUI.keyDisplayHud.draw(drawContext, tickDelta, null);
        }
    }
}