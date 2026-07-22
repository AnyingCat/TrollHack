package github.trollhack.gui.clickgui;

import github.trollhack.gui.clickgui.component.SettingComponent;
import github.trollhack.gui.clickgui.component.setting.BooleanSettingComponent;
import github.trollhack.gui.clickgui.component.setting.BindSettingComponent;
import github.trollhack.gui.clickgui.component.setting.ColorSettingComponent;
import github.trollhack.gui.clickgui.component.setting.EnumSettingComponent;
import github.trollhack.gui.clickgui.component.setting.NumberSettingComponent;
import github.trollhack.gui.clickgui.component.setting.StringSettingComponent;
import github.trollhack.modules.Module;
import github.trollhack.modules.impl.client.GuiSetting;
import github.trollhack.settings.Setting;
import github.trollhack.settings.impl.BindSetting;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.settings.impl.ColorSetting;
import github.trollhack.settings.impl.EnumSetting;
import github.trollhack.settings.impl.FloatSetting;
import github.trollhack.settings.impl.IntegerSetting;
import github.trollhack.settings.impl.StringSetting;
import github.trollhack.utils.render.Render2DUtil;
import github.trollhack.utils.render.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class SettingPanel {
    private static final float WINDOW_WIDTH = 120.0f;
    private static final float MAX_WINDOW_HEIGHT = 200.0f;

    private final Module module;
    private final List<SettingComponent> settingComponents = new ArrayList<>();

    private float x;
    private float y;
    private float width = WINDOW_WIDTH;

    private float scrollProgress = 0f;
    private float scrollSpeed = 0f;
    private long lastScrollUpdate = System.currentTimeMillis();
    private long lastBoundaryCheck = System.currentTimeMillis();

    public SettingPanel(Module module, float x, float y) {
        this.module = module;
        this.x = x;
        this.y = y;
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof BindSetting && !module.shouldShowBind()) continue;
            SettingComponent component = switch (setting) {
                case BooleanSetting s -> new BooleanSettingComponent(s);
                case FloatSetting s -> new NumberSettingComponent(s);
                case IntegerSetting s -> new NumberSettingComponent(s);
                case EnumSetting<?> s -> new EnumSettingComponent<>(s);
                case StringSetting s -> new StringSettingComponent(s);
                case BindSetting s -> new BindSettingComponent(s);
                case ColorSetting s -> new ColorSettingComponent(s);
                default -> null;
            };
            if (component != null) {
                settingComponents.add(component);
            }
        }
    }

    private float getDraggableHeight() {
        return FontRenderers.ducksans.getStringHeight(1.0f) + 6.0f;
    }

    private float getTotalHeight() {
        float draggableHeight = getDraggableHeight();
        float yMargin = GuiSetting.INSTANCE.yMargin.getValue();
        float contentHeight = 0f;
        for (SettingComponent comp : settingComponents) {
            if (comp.isVisible()) {
                contentHeight += comp.getHeight() + yMargin;
            }
        }
        return Math.min(draggableHeight + contentHeight + yMargin, MAX_WINDOW_HEIGHT);
    }

    private void updateScrollProgress() {
        if (settingComponents.isEmpty()) return;

        long currentTime = System.currentTimeMillis();
        double x = (currentTime - lastScrollUpdate) / 100.0;
        lastScrollUpdate = currentTime;

        double lnHalf = Math.log(0.25);
        double newSpeed = scrollSpeed * Math.pow(0.25, x);
        scrollProgress += (float) ((newSpeed / lnHalf) - (scrollSpeed / lnHalf));
        scrollSpeed = (float) newSpeed;

        if (currentTime - lastBoundaryCheck >= 100L) {
            lastBoundaryCheck = currentTime;
            float draggableHeight = getDraggableHeight();
            float yMargin = GuiSetting.INSTANCE.yMargin.getValue();
            float totalHeight = getTotalHeight();
            SettingComponent lastVisible = null;
            for (int i = settingComponents.size() - 1; i >= 0; i--) {
                if (settingComponents.get(i).isVisible()) {
                    lastVisible = settingComponents.get(i);
                    break;
                }
            }
            float maxScroll;
            if (lastVisible == null) {
                maxScroll = draggableHeight;
            } else {
                float lastY = draggableHeight + yMargin;
                for (SettingComponent comp : settingComponents) {
                    if (!comp.isVisible()) continue;
                    if (comp == lastVisible) break;
                    lastY += comp.getHeight() + yMargin;
                }
                maxScroll = Math.max(lastY + lastVisible.getHeight() + yMargin - totalHeight, 0.01f);
            }
            if (scrollProgress < 0.0f) {
                scrollSpeed = scrollProgress * -0.4f;
            } else if (scrollProgress > maxScroll) {
                scrollSpeed = (scrollProgress - maxScroll) * -0.4f;
            }
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        updateScrollProgress();
        width = WINDOW_WIDTH;

        MatrixStack matrices = context.getMatrices();
        float draggableHeight = getDraggableHeight();
        float totalHeight = getTotalHeight();
        float xMargin = GuiSetting.INSTANCE.xMargin.getValue();
        float yMargin = GuiSetting.INSTANCE.yMargin.getValue();

        if (GuiSetting.INSTANCE.backgroundBlur.getValue() > 0) {
            float blurStrength = GuiSetting.INSTANCE.backgroundBlur.getValue() * 15f;
            Render2DUtil.drawRoundedBlur(matrices, x, y, width, totalHeight,
                0, GuiSetting.INSTANCE.getBackGround(), blurStrength, 1.0f);
        }

        if (GuiSetting.INSTANCE.titleBar.getValue()) {
            Render2DUtil.drawRect(matrices, x, y + draggableHeight, width, totalHeight - draggableHeight, GuiSetting.INSTANCE.getBackGround());
        } else {
            Render2DUtil.drawRect(matrices, x, y, width, totalHeight, GuiSetting.INSTANCE.getBackGround());
        }

        if (GuiSetting.INSTANCE.windowOutline.getValue()) {
            Color primary = GuiSetting.INSTANCE.getPrimary();
            Color outlineColor = new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), 255);
            Render2DUtil.drawRectOutline(matrices, x, y, width, totalHeight, 0.5f, outlineColor);
        }

        if (GuiSetting.INSTANCE.titleBar.getValue()) {
            Render2DUtil.drawRect(matrices, x, y, width, draggableHeight, GuiSetting.INSTANCE.getPrimary());
        }

        if (FontRenderers.ducksans != null) {
            String name = module.getName();
            FontRenderers.ducksans.drawText(matrices, name, x + 3.0f, y + 3.5f, 1.0f, GuiSetting.INSTANCE.getText());
        }

        renderSettings(context, mouseX, mouseY, delta, draggableHeight, totalHeight, xMargin, yMargin);
    }

    private void renderSettings(DrawContext context, int mouseX, int mouseY, float delta,
                                 float draggableHeight, float totalHeight, float xMargin, float yMargin) {
        context.enableScissor((int)(x + xMargin), (int)(y + draggableHeight),
                              (int)(x + width - xMargin), (int)(y + totalHeight));

        float clipX = x + xMargin;
        float clipY = y + draggableHeight;
        float clipW = width - xMargin * 2;
        float clipH = totalHeight - draggableHeight;

        float settingY = draggableHeight + yMargin - scrollProgress;

        for (SettingComponent comp : settingComponents) {
            if (!comp.isVisible()) continue;

            float adjustedY = settingY;
            if (adjustedY + comp.getHeight() < draggableHeight) {
                settingY += comp.getHeight() + yMargin;
                continue;
            }
            if (adjustedY > totalHeight) {
                break;
            }

            comp.setPosition(x + xMargin, y + adjustedY);
            comp.setWidth(width - xMargin * 2);
            comp.setClip(clipX, clipY, clipW, clipH);
            comp.render(context, mouseX, mouseY, delta);

            settingY += comp.getHeight() + yMargin;
        }

        context.disableScissor();
    }

    public boolean isHovered(double mouseX, double mouseY) {
        float totalHeight = getTotalHeight();
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + totalHeight;
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        for (SettingComponent comp : settingComponents) {
            if (!comp.isVisible()) continue;
            comp.mouseClicked(mouseX, mouseY, button);
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        for (SettingComponent comp : settingComponents) {
            comp.mouseReleased(mouseX, mouseY, button);
        }
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (SettingComponent comp : settingComponents) {
            comp.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (SettingComponent comp : settingComponents) {
            if (comp.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    public boolean charTyped(char chr, int modifiers) {
        for (SettingComponent comp : settingComponents) {
            if (comp.charTyped(chr, modifiers)) {
                return true;
            }
        }
        return false;
    }

    public void handleScroll(double verticalAmount) {
        scrollSpeed -= (float) verticalAmount * 24.0f;
    }

    public Module getModule() {
        return module;
    }
}
