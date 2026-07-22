package me.catrix.mod.modules.impl.hud;

import me.catrix.Catrix;
import me.catrix.mod.modules.Module;
import net.minecraft.client.gui.DrawContext;

public class ItemsCountHud extends Module {
    public static ItemsCountHud INSTANCE;

    public ItemsCountHud() {
        super("ItemsCountHud", Category.Hud);
        setChinese("物品显示");
        INSTANCE = this;
    }

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        if (ItemsCountHud.INSTANCE.isOn()) {
            Catrix.GUI.itemsCountHud.draw(drawContext, tickDelta, null);
        }
    }
}
