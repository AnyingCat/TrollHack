package me.catrix.mod.modules.impl.client;

import me.catrix.mod.modules.Module;
import me.catrix.mod.gui.clickgui.ClickGuiScreen;

public class HudEditor extends Module {
    public static HudEditor INSTANCE;

    public HudEditor() {
        super("HudEditor", "HudEditor", Category.Client);
        setChinese("界面编辑器");
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if (!(mc.currentScreen instanceof ClickGuiScreen)) {
            disable();
        }
    }
}

//    @EventHandler
//    public void onTick(TickEvent event) {
//        if (!(mc.currentScreen instanceof ClickGuiScreen)) {
//            disable();
//        }
//    }
//}
