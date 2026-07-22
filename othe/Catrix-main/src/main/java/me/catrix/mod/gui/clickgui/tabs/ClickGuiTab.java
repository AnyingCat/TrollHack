package me.catrix.mod.gui.clickgui.tabs;

import me.catrix.Catrix;
import me.catrix.api.utils.math.Animation;
import me.catrix.mod.gui.clickgui.components.Component;
import me.catrix.mod.gui.clickgui.components.impl.ModuleComponent;
import me.catrix.mod.gui.font.FontRenderers;
import me.catrix.mod.modules.Module;
import me.catrix.mod.modules.impl.client.ClickGui;
import me.catrix.core.impl.GuiManager;
import me.catrix.api.utils.render.Render2DUtil;
import me.catrix.api.utils.render.TextUtil;
import me.catrix.mod.gui.clickgui.ClickGuiScreen;
import me.catrix.mod.modules.impl.client.HudEditor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.ArrayList;


public class ClickGuiTab extends Tab {
    protected String title;
    protected final boolean drawBorder = true;
    private Module.Category category = null;
    protected final ArrayList<ModuleComponent> children = new ArrayList<>();

    public ClickGuiTab(String title, int x, int y) {
        this.title = title;
        this.x = Catrix.CONFIG.getInt(title + "_x", x);
        this.y = Catrix.CONFIG.getInt(title + "_y", y);
        this.width = 98;
        this.mc = MinecraftClient.getInstance();
    }

    public ClickGuiTab(Module.Category category, int x, int y) {
        this(category.name(), x, y);
        this.category = category;
    }

    public ArrayList<ModuleComponent> getChildren() {
        return children;
    }

    public final String getTitle() {
        return title;
    }

    public final void setTitle(String title) {
        this.title = title;
    }

    public final int getX() {
        return x;
    }

    public final void setX(int x) {
        this.x = x;
    }

    public final int getY() {
        return y;
    }

    public final void setY(int y) {
        this.y = y;
    }

    public final int getWidth() {
        return width;
    }

    public final void setWidth(int width) {
        this.width = width;
    }

    public final int getHeight() {
        return height;
    }

    public final void setHeight(int height) {
        this.height = height;
    }

    public final boolean isGrabbed() {
        return (GuiManager.currentGrabbed == this);
    }

    public final void addChild(ModuleComponent component) {
        this.children.add(component);
    }

    boolean popped = true;

    private boolean isHudEditorOpen() {
        boolean isHudCategory = (category == Module.Category.Hud);
        return (isHudCategory && !HudEditor.INSTANCE.isOn()) || (!isHudCategory && HudEditor.INSTANCE.isOn());
    }

    @Override
    public void update(double mouseX, double mouseY) {
        if (isHudEditorOpen()) return;
        onMouseClick(mouseX, mouseY);
        if (popped) {
            int tempHeight = 1;
            for (ModuleComponent child : children) {
                tempHeight += (child.getHeight());
            }
            this.height = tempHeight;
            int i = defaultHeight;
            for (ModuleComponent child : this.children) {
                child.update(i, mouseX, mouseY);
                i += child.getHeight();
            }
        }
    }

    public void onMouseClick(double mouseX, double mouseY) {
        if (GuiManager.currentGrabbed == null) {
            if (mouseX >= (x) && mouseX <= (x + width)) {
                if (mouseY >= (y + 1) && mouseY <= (y + 14)) {
                    if (ClickGuiScreen.clicked) {
                        GuiManager.currentGrabbed = this;
                    } else if (ClickGuiScreen.rightClicked) {
                        popped = !popped;
                        ClickGuiScreen.rightClicked = false;
                        Component.sound();
                    }
                }
            }
        }
    }

    public double currentHeight = 0;
    Animation animation = new Animation();

    @Override


    public void draw(DrawContext drawContext, float partialTicks, Color color) {
        if (isHudEditorOpen()) return;
        int tempHeight = 1;
        for (ModuleComponent child : children) {
            tempHeight += (child.getHeight());
        }
        this.height = tempHeight;
        MatrixStack matrixStack = drawContext.getMatrices();
        currentHeight = animation.get(height);
        if (drawBorder) {
            Render2DUtil.drawRect(matrixStack, x, y - 1, width, 15, ClickGui.INSTANCE.bar.getValue());
            if (popped)
                Render2DUtil.drawRect(matrixStack, x, y + 14, width, (int) currentHeight, ClickGui.INSTANCE.background.getValue());
        }
        if (popped) {
            float borderTop = y + 14;
            Render2DUtil.drawRect(matrixStack,
                    x, borderTop,
                    1.0f, borderTop + (int) currentHeight - borderTop,
                    ClickGui.INSTANCE.bar.getValue().getRGB());
            Render2DUtil.drawRect(matrixStack,
                    x + width - 1, borderTop,
                    1.0f, borderTop + (int) currentHeight - borderTop,
                    ClickGui.INSTANCE.bar.getValue().getRGB());
            Render2DUtil.drawRect(matrixStack,
                    x, borderTop + (int) currentHeight,
                    width, 1.0f,
                    ClickGui.INSTANCE.bar.getValue().getRGB());
        }
        if (popped) {
            int i = defaultHeight;
            for (Component child : children) {
                child.draw(i, drawContext, partialTicks, color, false);
                i += child.getHeight();
            }
        }
        //TextUtil.drawString(drawContext, this.title, x + width / 2d - TextUtil.getWidth(title) / 2, y + 3, new Color(255, 255, 255));
        if (ClickGui.INSTANCE.icon.getValue()) {
            TextUtil.drawString(drawContext, title, x + 20, y + 3, new Color(255, 255, 255));
            if (title.equals("Combat")) {
                FontRenderers.icon.drawString(drawContext.getMatrices(), "b", x + 3, y + 1, new Color(255, 255, 255).getRGB());
            }
            if (title.equals("Misc")) {
                FontRenderers.icon.drawString(drawContext.getMatrices(), "[", x + 3, y + 1, new Color(255, 255, 255).getRGB());
            }
            if (title.equals("Render")) {
                FontRenderers.icon.drawString(drawContext.getMatrices(), "a", x + 3, y + 1, new Color(255, 255, 255).getRGB());
            }
            if (title.equals("Movement")) {
                FontRenderers.icon.drawString(drawContext.getMatrices(), "8", x + 3, y + 1, new Color(255, 255, 255).getRGB());
            }
            if (title.equals("Player")) {
                FontRenderers.icon.drawString(drawContext.getMatrices(), "5", x + 3, y + 1, new Color(255, 255, 255).getRGB());
            }
            if (title.equals("Exploit")) {
                FontRenderers.icon.drawString(drawContext.getMatrices(), "6", x + 3, y + 1, new Color(255, 255, 255).getRGB());
            }
            if (title.equals("Client")) {
                FontRenderers.icon.drawString(drawContext.getMatrices(), "7", x + 3, y + 1, new Color(255, 255, 255).getRGB());
            }
            if (title.equals("Hud")) {
                FontRenderers.icon.drawString(drawContext.getMatrices(), "7", x + 3, y + 1, new Color(255, 255, 255).getRGB());
            }
        } else {
            TextUtil.drawString(drawContext, title, x + 4, y + 3, new Color(255, 255, 255));
        }
    }
}