package me.catrix.mod.modules.impl.hud;

import me.catrix.Catrix;
import me.catrix.mod.modules.Module;
import net.minecraft.client.gui.DrawContext;

public class InventoryHud extends Module {
    public static InventoryHud INSTANCE;

    public InventoryHud() {
        super("InventoryHud", Category.Hud);
        setChinese("背包物品显示");
        INSTANCE = this;
    }

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        if (InventoryHud.INSTANCE.isOn()) {
            Catrix.GUI.inventoryHud.draw(drawContext, tickDelta, null);
        }
    }
}

