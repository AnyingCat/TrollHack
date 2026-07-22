package github.trollhack.modules.impl.hud;

import github.trollhack.core.Managers;
import github.trollhack.modules.HudModule;
import github.trollhack.modules.Module;
import github.trollhack.modules.impl.client.GuiSetting;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.settings.impl.EnumSetting;
import github.trollhack.settings.impl.FloatSetting;
import github.trollhack.utils.animation.AnimationUtil;
import github.trollhack.utils.render.Render2DUtil;
import github.trollhack.utils.render.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModuleList extends HudModule {
    public static final ModuleList INSTANCE = new ModuleList();

    public enum Mode {
        LEFT_TAG,
        RIGHT_TAG,
        FRAME
    }

    public enum SortingMode {
        LENGTH,
        ALPHABET,
        CATEGORY
    }

    private final EnumSetting<Mode> mode = enumSetting("Mode", Mode.LEFT_TAG);
    private final EnumSetting<SortingMode> sortingMode = enumSetting("Sorting", SortingMode.LENGTH);
    private final FloatSetting scale = floatSetting("Scale", 1.0f, 0.5f, 2.0f, 0.05f);
    private final BooleanSetting showInvisible = booleanSetting("ShowInvisible", false);
    private final BooleanSetting bindOnly = booleanSetting("BindOnly", true, () -> !showInvisible.getValue());
    private final BooleanSetting rainbow = booleanSetting("Rainbow", true);
    private final FloatSetting rainbowLength = floatSetting("RainbowLength", 10.0f, 1.0f, 20.0f, 0.5f, rainbow::getValue);
    private final FloatSetting indexedHue = floatSetting("IndexedHue", 0.5f, 0.0f, 1.0f, 0.05f, rainbow::getValue);
    private final FloatSetting saturation = floatSetting("Saturation", 0.5f, 0.0f, 1.0f, 0.01f, rainbow::getValue);
    private final FloatSetting brightness = floatSetting("Brightness", 1.0f, 0.0f, 1.0f, 0.01f, rainbow::getValue);

    private final Map<Module, ModuleToggleFlag> toggleMap = new LinkedHashMap<>();

    public ModuleList() {
        super("ModuleList", 2, 20, 60, 100);
    }

    @Override
    public boolean isRightAligned() {
        int screenWidth = mc.getWindow().getScaledWidth();
        return getPosX() + getWidth() / 2.0f > screenWidth / 2.0f;
    }

    @Override
    public float getRenderX() {
        return isRightAligned() ? getPosX() - getWidth() : getPosX();
    }

    @Override
    public float clampDragX(float newX) {
        if (isRightAligned()) {
            return Math.max(getWidth(), Math.min(mc.getWindow().getScaledWidth(), newX));
        }
        return super.clampDragX(newX);
    }

    @Override
    public void onHudRender(DrawContext context) {
        if (FontRenderers.ducksans == null) return;

        for (Module module : Managers.MODULE.getModules()) {
            toggleMap.computeIfAbsent(module, ModuleToggleFlag::new).update();
        }

        List<Module> sorted = Managers.MODULE.getModules().stream()
            .filter(m -> {
                ModuleToggleFlag flag = toggleMap.get(m);
                return flag != null && flag.getProgress() > 0.0f;
            })
            .collect(Collectors.toList());

        switch (sortingMode.getValue()) {
            case LENGTH -> sorted.sort(Comparator.comparingDouble(m -> -FontRenderers.ducksans.getStringWidth(m.getName(), scale.getValue())));
            case ALPHABET -> sorted.sort(Comparator.comparing(Module::getName));
            case CATEGORY -> sorted.sort(Comparator.comparing(Module::getCategory));
        }

        MatrixStack matrices = context.getMatrices();
        float s = scale.getValue();
        float fontHeight = FontRenderers.ducksans.getStringHeight(s);
        float y = getPosY();
        float maxWidth = 0;
        int index = 0;

        float timedHue = 0.0f;
        if (rainbow.getValue()) {
            float lengthMs = rainbowLength.getValue() * 1000.0f;
            timedHue = (System.currentTimeMillis() % (long) lengthMs) / lengthMs;
        }

        boolean rightAligned = isRightAligned();

        for (Module module : sorted) {
            ModuleToggleFlag flag = toggleMap.get(module);
            if (flag == null) continue;

            float progress = flag.getProgress();
            if (progress <= 0.0f) continue;

            String name = module.getName();
            float textWidth = FontRenderers.ducksans.getStringWidth(name, s);
            float displayHeight = (fontHeight + 2.0f) * progress;
            float animationXOffset = textWidth * (1.0f - progress);

            Color color = rainbow.getValue()
                ? Color.getHSBColor(timedHue + indexedHue.getValue() * 0.05f * index, saturation.getValue(), brightness.getValue())
                : GuiSetting.INSTANCE.getPrimary();

            float textLeftX = rightAligned ? getPosX() + animationXOffset - textWidth : getPosX() + animationXOffset;

            float bgX = textLeftX - 2.0f;
            float bgW = textWidth + 4.0f;

            Render2DUtil.drawRect(matrices, bgX, y, bgW, displayHeight, GuiSetting.INSTANCE.getBackGround());

            switch (mode.getValue()) {
                case LEFT_TAG -> Render2DUtil.drawRect(matrices, bgX - 2.0f, y, 2.0f, displayHeight, color);
                case RIGHT_TAG -> Render2DUtil.drawRect(matrices, bgX + bgW, y, 2.0f, displayHeight, color);
                case FRAME -> {}
            }

            if (progress > 0.01f) {
                Color textBase = GuiSetting.INSTANCE.getText();
                Color textColor = new Color(
                    textBase.getRed(),
                    textBase.getGreen(),
                    textBase.getBlue(),
                    (int) (textBase.getAlpha() * progress)
                );
                FontRenderers.ducksans.drawText(matrices, name, textLeftX, y + 1.0f, s, textColor);
            }

            maxWidth = Math.max(maxWidth, textWidth + 4.0f);
            y += displayHeight;
            index++;
        }

        setWidth(Math.max(maxWidth, 20f));
        setHeight(Math.max(y - getPosY(), 10f));
    }

    private boolean getModuleDisplayState(Module module) {
        if (!module.isEnabled()) return false;
        if (showInvisible.getValue()) return true;
        if (!module.shouldShowBind()) return false;
        if (bindOnly.getValue() && module.bind.isEmpty()) return false;
        return true;
    }

    private class ModuleToggleFlag {
        private final Module module;
        private boolean lastState;
        private long lastUpdateTime;

        public ModuleToggleFlag(Module module) {
            this.module = module;
            this.lastState = getModuleDisplayState(module);
            this.lastUpdateTime = System.currentTimeMillis();
        }

        public void update() {
            boolean currentState = getModuleDisplayState(module);
            if (currentState != lastState) {
                lastState = currentState;
                lastUpdateTime = System.currentTimeMillis();
            }
        }

        public float getProgress() {
            float delta = Math.min(1.0f, (System.currentTimeMillis() - lastUpdateTime) / 300.0f);
            if (lastState) {
                return AnimationUtil.Easing.EASE_OUT_CUBIC.inc(delta);
            } else {
                return AnimationUtil.Easing.EASE_IN_CUBIC.dec(delta);
            }
        }
    }
}
