package github.trollhack.gui.clickgui;

import github.trollhack.core.impl.ModuleManager;
import github.trollhack.gui.clickgui.component.ModuleComponent;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.modules.impl.client.GuiSetting;
import github.trollhack.utils.interfaces.Mc;
import github.trollhack.utils.render.Render2DUtil;
import github.trollhack.utils.render.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class CategoryPanel implements Mc {
    private static final float MIN_WINDOW_WIDTH = 80.0f;
    private static final float MAX_WINDOW_WIDTH = 200.0f;
    private static final float WINDOW_HEIGHT = 400.0f;

    private final Category category;
    private final ModuleManager moduleManager;
    private final List<ModuleComponent> moduleComponents = new ArrayList<>();

    private float x;
    private float y;
    private float width = MIN_WINDOW_WIDTH;

    private boolean dragging = false;
    private double dragOffsetX;
    private double dragOffsetY;

    private float scrollProgress = 0f;
    private float scrollSpeed = 0f;
    private long lastScrollUpdate = System.currentTimeMillis();
    private long lastBoundaryCheck = System.currentTimeMillis();

    public CategoryPanel(Category category, ModuleManager moduleManager, float x, float y) {
        this.category = category;
        this.moduleManager = moduleManager;
        this.x = x;
        this.y = y;
        moduleComponents.clear();
        for (Module module : moduleManager.getModulesByCategory(category)) {
            moduleComponents.add(new ModuleComponent(module, x, 0, width));
        }
        width = calculateOptimalWidth();
    }

    private float getDraggableHeight() {
        return FontRenderers.ducksans.getStringHeight(1.0f) + 6.0f;
    }

    private float calculateOptimalWidth() {
        float maxWidth = MIN_WINDOW_WIDTH;
        float xMargin = GuiSetting.INSTANCE.xMargin.getValue();

        if (FontRenderers.ducksans != null) {
            float titleWidth = FontRenderers.ducksans.getStringWidth(category.getName(), 1.0f) + 20.0f;
            maxWidth = Math.max(maxWidth, titleWidth);
        }

        for (ModuleComponent comp : moduleComponents) {
            if (comp.isVisible()) {
                float childMinWidth = comp.getMinWidth();
                maxWidth = Math.max(maxWidth, childMinWidth + xMargin * 2.0f);
            }
        }

        return Math.min(maxWidth, MAX_WINDOW_WIDTH);
    }

    private float getTotalHeight() {
        return WINDOW_HEIGHT;
    }

    private void updateScrollProgress() {
        if (moduleComponents.isEmpty()) return;

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
            ModuleComponent lastVisible = null;
            for (int i = moduleComponents.size() - 1; i >= 0; i--) {
                if (moduleComponents.get(i).isVisible()) {
                    lastVisible = moduleComponents.get(i);
                    break;
                }
            }
            float maxScroll;
            if (lastVisible == null) {
                maxScroll = draggableHeight;
            } else {
                float lastY = draggableHeight + yMargin;
                for (ModuleComponent comp : moduleComponents) {
                    if (!comp.isVisible()) continue;
                    if (comp == lastVisible) break;
                    lastY += comp.getHeight() + yMargin;
                }
                maxScroll = Math.max(lastY + lastVisible.getHeight() + yMargin - WINDOW_HEIGHT, 0.01f);
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
        width = calculateOptimalWidth();

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
            String name = category.getName();
            FontRenderers.ducksans.drawText(matrices, name, x + 3.0f, y + 3.5f, 1.0f, GuiSetting.INSTANCE.getText());
        }

        renderModules(context, mouseX, mouseY, delta, draggableHeight, totalHeight, xMargin, yMargin);
    }

    private void renderModules(DrawContext context, int mouseX, int mouseY, float delta,
                                float draggableHeight, float totalHeight, float xMargin, float yMargin) {
        context.enableScissor((int)(x + xMargin), (int)(y + draggableHeight),
                              (int)(x + width - xMargin), (int)(y + totalHeight));

        float clipX = x + xMargin;
        float clipY = y + draggableHeight;
        float clipW = width - xMargin * 2;
        float clipH = totalHeight - draggableHeight;

        float moduleY = draggableHeight + yMargin - scrollProgress;

        for (ModuleComponent comp : moduleComponents) {
            if (!comp.isVisible()) continue;

            float adjustedY = moduleY;
            if (adjustedY + comp.getHeight() < draggableHeight) {
                moduleY += comp.getHeight() + yMargin;
                continue;
            }
            if (adjustedY > totalHeight) {
                break;
            }

            comp.setPosition(x + xMargin, y + adjustedY);
            comp.setWidth(width - xMargin * 2);
            comp.setClip(clipX, clipY, clipW, clipH);
            comp.render(context, mouseX, mouseY, delta);

            moduleY += comp.getHeight() + yMargin;
        }

        context.disableScissor();
    }

    public boolean isHovered(double mouseX, double mouseY) {
        float totalHeight = getTotalHeight();
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + totalHeight;
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        float totalHeight = getTotalHeight();
        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + totalHeight) {
            return;
        }

        if (button == 0 && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + getDraggableHeight()) {
            dragging = true;
            dragOffsetX = mouseX - x;
            dragOffsetY = mouseY - y;
            return;
        }

        for (ModuleComponent comp : moduleComponents) {
            if (!comp.isVisible()) continue;
            comp.mouseClicked(mouseX, mouseY, button);
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        for (ModuleComponent comp : moduleComponents) {
            comp.mouseReleased(mouseX, mouseY, button);
        }
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            this.x = (float) (mouseX - dragOffsetX);
            this.y = (float) (mouseY - dragOffsetY);
            return;
        }
        for (ModuleComponent comp : moduleComponents) {
            comp.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        for (ModuleComponent comp : moduleComponents) {
            comp.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public boolean charTyped(char chr, int modifiers) {
        for (ModuleComponent comp : moduleComponents) {
            if (comp.charTyped(chr, modifiers)) {
                return true;
            }
        }
        return false;
    }

    public void handleScroll(double verticalAmount) {
        scrollSpeed -= (float) verticalAmount * 24.0f;
    }

    public boolean isDragging() {
        return dragging;
    }

    public Category getCategory() {
        return category;
    }

    public List<ModuleComponent> getModuleComponents() {
        return moduleComponents;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }
}
