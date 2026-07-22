package github.trollhack.modules.impl.client;

import github.trollhack.gui.clickgui.ClickGUIScreen;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.settings.impl.BooleanSetting;

public class ClickGUI extends Module {

    public static final ClickGUI INSTANCE = new ClickGUI();

    public final BooleanSetting blur = booleanSetting("Blur", false);
    public final BooleanSetting background = booleanSetting("BackGround", false);

    public ClickGUI() {
        super("ClickGUI", Category.CLIENT);
    }

    @Override
    public void onEnable() {
        if (mc.player != null && mc.world != null) {
            mc.setScreen(ClickGUIScreen.getInstance());
        }
    }

    @Override
    public void onDisable() {
        if (mc.currentScreen instanceof ClickGUIScreen) {
            mc.setScreen(null);
        }
    }
}
