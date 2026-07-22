package github.trollhack.modules.impl.client;

import github.trollhack.gui.hud.HudEditorScreen;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;

public class HudEditor extends Module {
    public static final HudEditor INSTANCE = new HudEditor();

    public HudEditor() {
        super("HudEditor", Category.CLIENT);
    }

    @Override
    public boolean shouldShowBind() {
        return false;
    }

    @Override
    public void onEnable() {
        if (nullCheck()) return;
        mc.setScreen(HudEditorScreen.getInstance());
    }

    @Override
    public void onDisable() {
        if (mc.currentScreen instanceof HudEditorScreen) mc.setScreen(null);
    }
}
