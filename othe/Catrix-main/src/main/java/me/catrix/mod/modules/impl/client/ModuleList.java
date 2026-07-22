package me.catrix.mod.modules.impl.client;

import me.catrix.Catrix;
import me.catrix.api.utils.math.AnimateUtil;
import me.catrix.api.utils.render.ColorUtil;
import me.catrix.api.utils.render.RenderShadersUtil;
import me.catrix.mod.gui.font.FontRenderers;
import me.catrix.mod.modules.Module;
import me.catrix.mod.modules.settings.impl.BooleanSetting;
import me.catrix.mod.modules.settings.impl.EnumSetting;
import me.catrix.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleList extends Module {
    public ModuleList() {
        super("ModuleList", Category.Client);
        setChinese("功能列表");
        INSTANCE = this;
    }

    public static ModuleList INSTANCE;
    private final SliderSetting xOffset = add(new SliderSetting("XOffset", 27, 0, 500));
    private final SliderSetting yOffset = add(new SliderSetting("YOffset", 29, 0, 300));
    public final EnumSetting<AnimateUtil.AnimMode> animMode = add(new EnumSetting<>("AnimMode", AnimateUtil.AnimMode.Mio));
    public final BooleanSetting lowerCase = add(new BooleanSetting("LowerCase", false));
    private final BooleanSetting space = add(new BooleanSetting("Space", true));
    private final BooleanSetting down = add(new BooleanSetting("Down", false));
    private final BooleanSetting reverse = add(new BooleanSetting("Reverse", false));
    private final BooleanSetting onlyBind = add(new BooleanSetting("OnlyBind", false));
    private List<Modules> modulesList = new ArrayList<>();

    boolean update;

    @Override
    public void onEnable() {
        modulesList.clear();
        for (Module module : Catrix.MODULE.modules) {
            modulesList.add(new Modules(module));
        }
    }

    private boolean lastSpaceToggled;
    private boolean lastLowercaseToggled;
    public int counter = 20;

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        if (space.getValue() != lastSpaceToggled || lowerCase.getValue() != lastLowercaseToggled) {
            for (Modules modules : modulesList) {
                modules.updateName();
            }
            lastLowercaseToggled = lowerCase.getValue();
            lastSpaceToggled = space.getValue();
        }
        for (Modules modules : modulesList) {
            modules.update();
        }
        if (update) {
            modulesList = modulesList.stream().sorted(Comparator.comparing(module -> -getStringWidth(module.name))).collect(Collectors.toList());
            update = false;
        }
        int lastY = down.getValue() ? mc.getWindow().getScaledHeight() - yOffset.getValueInt() - getFontHeight() : yOffset.getValueInt();
        for (Modules modules : modulesList) {
            if (modules.module.isOn() && modules.module.drawnSetting.getValue() && !(onlyBind.getValue() && modules.module.getBind().getKey() == -1)) {
                modules.enable();
            } else {
                modules.disable();
            }

            if (modules.isEnabled) {
                modules.fade = animate(modules.fade, 1, 0.05);
                if (reverse.getValue()) {
                    modules.x = animate(modules.x, 0, 0.24);
                } else {
                    modules.x = animate(modules.x, getStringWidth(modules.name), 0.24);
                }
            } else {
                modules.fade = animate(modules.fade, 0.08, 0.05);
                if (reverse.getValue()) {
                    modules.x = animate(modules.x, -getStringWidth(modules.name), 0.17);
                } else {
                    modules.x = animate(modules.x, -1, 0.17);
                }
                if ((reverse.getValue() ? modules.x <= -getStringWidth(modules.name) + 1 : modules.x <= 0) || modules.fade <= 0.084) {
                    modules.hide = true;
                    continue;
                }
            }
            if (modules.hide) {
                modules.updateName();
                if (reverse.getValue()) {
                    modules.x = -getStringWidth(modules.name);
                } else {
                    modules.x = 0;
                }
                modules.y = lastY;
                modules.nameUpdated = false;
                modules.hide = false;
            }
            if (modules.nameUpdated) {
                modules.nameUpdated = false;
                modules.y = lastY;
            } else {
                modules.y = animate(modules.y, lastY, 0.33);
            }
            counter += 1;

            if (reverse.getValue()) {
                int textX = (int) (xOffset.getValue() + modules.x);

                RenderShadersUtil.drawBlurredShadow(
                        drawContext.getMatrices(),
                        textX - 4 - 5,
                        (int) modules.y - 2 - 5,
                        (getFontHeight() + 8) + 5,
                        (getFontHeight() + 8) + 10,
                        15,
                        new Color(32, 32, 32, (int) (89 * modules.fade)));

                RenderShadersUtil.drawRoundedBlur(
                        drawContext.getMatrices(),
                        textX - 4,
                        (float) modules.y - 2,
                        getFontHeight() + 8,
                        getFontHeight() + 8,
                        5.0f,
                        new Color(0x35000000, true),
                        15.0f,
                        0.55f);

                RenderShadersUtil.drawBlurredShadow(
                        drawContext.getMatrices(),
                        textX - 4 + getFontHeight() + 8 + 2 - 5,
                        (int) modules.y - 2 - 5,
                        getStringWidth(modules.name) + 8 + 10,
                        (getFontHeight() + 8) + 10,
                        15,
                        new Color(32, 32, 32, (int) (89 * modules.fade)));

                RenderShadersUtil.drawRoundedBlur(
                        drawContext.getMatrices(),
                        textX - 4 + getFontHeight() + 8 + 2,
                        (float) modules.y - 2,
                        getStringWidth(modules.name) + 8,
                        getFontHeight() + 8,
                        5.0f,
                        new Color(0x35000000, true),
                        15.0f,
                        0.55f);

                String iconChar = switch (modules.module.getCategory()) {
                    case Combat -> "b";
                    case Misc -> "[";
                    case Render -> "a";
                    case Movement -> "8";
                    case Player -> "5";
                    case Exploit -> "6";
                    case Client -> "7";
                    default -> "";
                };

                FontRenderers.ui.drawString(
                        drawContext.getMatrices(),
                        modules.name,
                        textX - 4 + getFontHeight() + 8 + 2 + (float) ((getStringWidth(modules.name) + 8) - getStringWidth(modules.name)) / 2,
                        (int) (modules.y - 2 + ((getFontHeight() + 8) - getFontHeight()) / 2f + 2),
                        ColorUtil.injectAlpha(Colors.INSTANCE.getColor(counter), (int) (255 * modules.fade)));

                FontRenderers.icon.drawString(
                        drawContext.getMatrices(),
                        iconChar,
                        textX - 4 + ((getFontHeight() + 8) - FontRenderers.icon.getWidth(iconChar)) / 2f,
                        (float) modules.y - 2 + ((getFontHeight() + 8) - FontRenderers.icon.getFontHeight()) / 2f + 2,
                        ColorUtil.injectAlpha(new Color(Color.WHITE.getRGB()).getRGB(), (int) (255 * modules.fade)));

            } else {
                int textX = (int) (mc.getWindow().getScaledWidth() - modules.x - xOffset.getValue());

                RenderShadersUtil.drawBlurredShadow(
                        drawContext.getMatrices(),
                        textX - 4 - 5,
                        (int) modules.y - 2 - 5,
                        getStringWidth(modules.name) + 8 + 10,
                        (getFontHeight() + 8) + 10,
                        15,
                        new Color(32, 32, 32, (int) (89 * modules.fade)));

                RenderShadersUtil.drawRoundedBlur(
                        drawContext.getMatrices(),
                        textX - 4,
                        (float) modules.y - 2,
                        getStringWidth(modules.name) + 8,
                        getFontHeight() + 8,
                        5.0f,
                        new Color(0x35000000, true),
                        15.0f,
                        0.55f);

                RenderShadersUtil.drawBlurredShadow(
                        drawContext.getMatrices(),
                        textX - 4 + getStringWidth(modules.name) + 8 + 2 - 5,
                        (int) modules.y - 2 - 5,
                        (getFontHeight() + 8) + 5,
                        (getFontHeight() + 8) + 10,
                        15,
                        new Color(32, 32, 32, (int) (89 * modules.fade)));

                RenderShadersUtil.drawRoundedBlur(
                        drawContext.getMatrices(),
                        textX - 4 + getStringWidth(modules.name) + 8 + 2,
                        (float) modules.y - 2,
                        getFontHeight() + 8,
                        getFontHeight() + 8,
                        5.0f,
                        new Color(0x35000000, true),
                        15.0f,
                        0.55f);

                String iconChar = switch (modules.module.getCategory()) {
                    case Combat -> "b";
                    case Misc -> "[";
                    case Render -> "a";
                    case Movement -> "8";
                    case Player -> "5";
                    case Exploit -> "6";
                    case Client -> "7";
                    default -> "";
                };

                FontRenderers.ui.drawString(
                        drawContext.getMatrices(),
                        modules.name,
                        textX,
                        (int) (modules.y - 2 + ((getFontHeight() + 8) - getFontHeight()) / 2f + 2),
                        ColorUtil.injectAlpha(Colors.INSTANCE.getColor(counter), (int) (255 * modules.fade)));

                FontRenderers.icon.drawString(
                        drawContext.getMatrices(),
                        iconChar,
                        textX - 4 + getStringWidth(modules.name) + 8 + 2 + ((getFontHeight() + 8) - FontRenderers.icon.getWidth(iconChar)) / 2f,
                        (float) modules.y - 2 + ((getFontHeight() + 8) - FontRenderers.icon.getFontHeight()) / 2f + 2,
                        ColorUtil.injectAlpha(Colors.INSTANCE.getColor(counter), (int) (255 * modules.fade)));
            }

            if (modules.isEnabled) {
                if (down.getValue()) {
                    lastY -=  ((getFontHeight() + 8) + 6);
                } else {
                    lastY +=  ((getFontHeight() + 8) + 6);
                }
            }
        }
    }

    public double animate(double current, double endPoint, double speed) {
        if (speed >= 1) return endPoint;
        if (speed == 0) return current;
        return AnimateUtil.animate(current, endPoint, speed, animMode.getValue());
    }

    private int getStringWidth(String text) {
        return (int) FontRenderers.ui.getWidth(text);
    }

    private int getFontHeight() {
        return (int) FontRenderers.ui.getFontHeight();
    }

    public class Modules {
        public boolean isEnabled = false;
        public final Module module;
        public double x = 0;
        public double y = 0;
        public double fade = 0;
        public boolean hide = true;

        public Modules(Module module) {
            this.module = module;
        }

        public void enable() {
            if (isEnabled) return;
            isEnabled = true;
        }

        public void disable() {
            if (!isEnabled) return;
            isEnabled = false;
        }

        public String lastName = "";
        public String name = "";
        public boolean nameUpdated = false;

        public void updateName() {
            String name = module.getArrayName();

            this.lastName = name;
            if (space.getValue()) {
                name = module.getDisplayName().replaceAll("([a-z])([A-Z])", "$1 $2");
                if (name.startsWith(" ")) {
                    name = name.replaceFirst(" ", "");
                }
                name = name + module.getArrayInfo();
            }
            if (lowerCase.getValue()) {
                name = name.toLowerCase();
            }
            this.name = name;
            update = true;
        }

        public void update() {
            String name = module.getArrayName();

            if (!this.lastName.equals(name)) {
                this.lastName = name;
                if (space.getValue()) {
                    name = module.getDisplayName().replaceAll("([a-z])([A-Z])", "$1 $2");
                    if (name.startsWith(" ")) {
                        name = name.replaceFirst(" ", "");
                    }
                    name = name + module.getArrayInfo();
                }
                if (lowerCase.getValue()) {
                    name = name.toLowerCase();
                }
                this.name = name;
                update = true;
                nameUpdated = true;
            }
        }
    }
}