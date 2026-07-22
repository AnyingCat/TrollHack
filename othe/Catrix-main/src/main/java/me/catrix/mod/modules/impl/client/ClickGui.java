package me.catrix.mod.modules.impl.client;

import me.catrix.Catrix;
import me.catrix.core.impl.GuiManager;
import me.catrix.api.utils.math.Easing;
import me.catrix.api.utils.math.FadeUtils;
import me.catrix.mod.gui.clickgui.ClickGuiScreen;
import me.catrix.mod.gui.clickgui.components.Component;
import me.catrix.mod.gui.clickgui.components.impl.ModuleComponent;
import me.catrix.mod.gui.clickgui.tabs.ClickGuiTab;
import me.catrix.mod.modules.Module;
import me.catrix.mod.modules.settings.impl.BooleanSetting;
import me.catrix.mod.modules.settings.impl.ColorSetting;
import me.catrix.mod.modules.settings.impl.EnumSetting;
import me.catrix.mod.modules.settings.impl.SliderSetting;

import java.awt.*;

public class ClickGui extends Module {
    public static ClickGui INSTANCE;
    private final EnumSetting<Pages> page = add(new EnumSetting<>("Page", Pages.General));
    public final BooleanSetting activeBox = add(new BooleanSetting("ActiveBox", true, () -> page.getValue() == Pages.Element));
    public final BooleanSetting center = add(new BooleanSetting("Center", false, () -> page.getValue() == Pages.Element));
    public final ColorSetting bind = add(new ColorSetting("Bind", new Color(255, 255, 255), () -> page.getValue() == Pages.Element).injectBoolean(false));

    public final BooleanSetting gear = add(new BooleanSetting("Gear", false, () -> page.getValue() == Pages.General));
    public final BooleanSetting icon = add(new BooleanSetting("Icon", true, () -> page.getValue() == Pages.General));
    public final BooleanSetting chinese = add(new BooleanSetting("Chinese", false, () -> page.getValue() == Pages.General));
    public final BooleanSetting font = add(new BooleanSetting("Font", true, () -> page.getValue() == Pages.General));
    public final BooleanSetting maxFill = add(new BooleanSetting("MaxFill", false, () -> page.getValue() == Pages.General));
    public final BooleanSetting sound = add(new BooleanSetting("Sound", true, () -> page.getValue() == Pages.General));
    public final SliderSetting height = add(new SliderSetting("Height", 15, 10, 20, 1, () -> page.getValue() == Pages.General));
    public final EnumSetting<Mode> mode = add(new EnumSetting<>("EnableAnim", Mode.Pull, () -> page.getValue() == Pages.General));
    public final SliderSetting animationTime = add(new SliderSetting("AnimationTime", 200, 0, 1000, 1, () -> page.getValue() == Pages.General));
    public final EnumSetting<Easing> ease = add(new EnumSetting<>("Ease", Easing.IOS, () -> page.getValue() == Pages.General));

    public final ColorSetting color = add(new ColorSetting("Main", new Color(0x6EACAFFD, true), () -> page.getValue() == Pages.Color));
    public final ColorSetting mainHover = add(new ColorSetting("Hover", new Color(0x6EBCBFFA, true), () -> page.getValue() == Pages.Color));
    public final ColorSetting bar = add(new ColorSetting("Bar", new Color(0x6EACAFFD, true), () -> page.getValue() == Pages.Color));
    public final ColorSetting disableText = add(new ColorSetting("DisableText", new Color(255, 255, 255), () -> page.getValue() == Pages.Color));
    public final ColorSetting enableText = add(new ColorSetting("EnableText", -1, () -> page.getValue() == Pages.Color));
    public final ColorSetting module = add(new ColorSetting("Module", new Color(0x0000000, true), () -> page.getValue() == Pages.Color));
    public final ColorSetting moduleHover = add(new ColorSetting("ModuleHover", new Color(0x44BCBFFA, true), () -> page.getValue() == Pages.Color));
    public final ColorSetting setting = add(new ColorSetting("Setting", new Color(0x0000000, true), () -> page.getValue() == Pages.Color));
    public final ColorSetting settingHover = add(new ColorSetting("SettingHover", new Color(0x44BCBFFA, true), () -> page.getValue() == Pages.Color));
    public final ColorSetting background = add(new ColorSetting("Background", new Color(0x5A000000, true), () -> page.getValue() == Pages.Color));
    public ClickGui() {
        super("ClickGui", Category.Client);
        setChinese("菜单");
        INSTANCE = this;
    }

    public static final FadeUtils fade = new FadeUtils(300);

    @Override
    public void onUpdate() {
        if (chinese.getValue()) {
            font.setValue(false);
        }
        if (!(mc.currentScreen instanceof ClickGuiScreen)) {
            disable();
        }
    }

    int lastHeight;
    @Override
    public void onEnable() {
        //size = scale.getValue();
        if (lastHeight != height.getValueInt()) {
            for (ClickGuiTab tab : Catrix.GUI.tabs) {
                for (Component component : tab.getChildren()) {
                    if (component instanceof ModuleComponent moduleComponent) {
                        for (Component settingComponent : moduleComponent.getSettingsList()) {
                            settingComponent.setHeight(height.getValueInt());
                            settingComponent.defaultHeight = height.getValueInt();
                        }
                    }
                    component.setHeight(height.getValueInt());
                    component.defaultHeight = height.getValueInt();
                }
            }
            lastHeight = height.getValueInt();
        }
        fade.reset();
        if (nullCheck()) {
            disable();
            return;
        }
        mc.setScreen(GuiManager.clickGui);
    }

    @Override
    public void onDisable() {
        if (mc.currentScreen instanceof ClickGuiScreen) {
            mc.setScreen(null);
        }
    }

    public enum Mode {
        Scale, Pull, None
    }

    private enum Pages {
        General,
        Color,
        Element
    }

    public enum Type {
        Old,
        New
    }
}