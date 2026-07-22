package me.catrix.core.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.catrix.Catrix;
import me.catrix.api.utils.Wrapper;
import me.catrix.api.utils.math.FadeUtils;
import me.catrix.api.utils.render.Snow;
import me.catrix.mod.gui.clickgui.ClickGuiScreen;
import me.catrix.mod.gui.clickgui.components.impl.ModuleComponent;
import me.catrix.mod.gui.clickgui.tabs.ClickGuiTab;
import me.catrix.mod.gui.clickgui.tabs.Tab;
import me.catrix.mod.gui.elements.*;
import me.catrix.mod.modules.Module;
import me.catrix.mod.modules.impl.client.ClickGui;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class GuiManager implements Wrapper {
    public final ArrayList<ClickGuiTab> tabs = new ArrayList<>();
    public static final ClickGuiScreen clickGui = new ClickGuiScreen();
    public final ArmorHUD armorHud;
    public final InventoryHUD inventoryHud;
    public final ItemsCountHUD itemsCountHud;
    public final KeyDisplayHUD keyDisplayHud;
    public final PotionHUD potionHud;
    public final TargetHUD targetHud;
    public final SelfHUD selfHud;
    public static Tab currentGrabbed = null;
    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private int mouseX;
    private int mouseY;

    public GuiManager() {

        armorHud = new ArmorHUD();
        inventoryHud = new InventoryHUD();
        itemsCountHud = new ItemsCountHUD();
        keyDisplayHud = new KeyDisplayHUD();
        targetHud = new TargetHUD();
        selfHud = new SelfHUD();
        potionHud = new PotionHUD();

        int xOffset = 30;
        for (Module.Category category : Module.Category.values()) {
            ClickGuiTab tab = new ClickGuiTab(category, xOffset, 50);
            for (Module module : Catrix.MODULE.modules) {
                if (module.getCategory() == category) {
                    ModuleComponent button = new ModuleComponent(tab, module);
                    tab.addChild(button);
                }
            }
            tabs.add(tab);
            xOffset += tab.getWidth() + 5;
        }
    }

    public Color getColor() {
        return ClickGui.INSTANCE.color.getValue();
    }

    public void onUpdate() {
        if (isClickGuiOpen()) {
            for (ClickGuiTab tab : tabs) {
                tab.update(mouseX, mouseY);
            }
            armorHud.update(mouseX, mouseY);
            inventoryHud.update(mouseX, mouseY);
            itemsCountHud.update(mouseX, mouseY);
            keyDisplayHud.update(mouseX, mouseY);
            targetHud.update(mouseX, mouseY);
            selfHud.update(mouseX, mouseY);
            potionHud.update(mouseX, mouseY);
        }
    }

    public void draw(int x, int y, DrawContext drawContext, float tickDelta) {
        MatrixStack matrixStack = drawContext.getMatrices();
        boolean mouseClicked = ClickGuiScreen.clicked;
        mouseX = x;
        mouseY = y;
        if (!mouseClicked) {
            currentGrabbed = null;
        }
        if (currentGrabbed != null) {
            currentGrabbed.moveWindow((lastMouseX - mouseX), (lastMouseY - mouseY));
        }
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        RenderSystem.enableCull();
        matrixStack.push();
        //matrixStack.scale((float) ClickGui.size, (float) ClickGui.size, 1);
        armorHud.draw(drawContext, tickDelta, getColor());
        inventoryHud.draw(drawContext, tickDelta, getColor());
        itemsCountHud.draw(drawContext, tickDelta, getColor());
        keyDisplayHud.draw(drawContext, tickDelta, getColor());
        targetHud.draw(drawContext, tickDelta, getColor());
        selfHud.draw(drawContext, tickDelta, getColor());
        potionHud.draw(drawContext, tickDelta, getColor());
        double quad = ClickGui.fade.ease(FadeUtils.Ease.In2);
        if (quad < 1) {
            switch (ClickGui.INSTANCE.mode.getValue()) {
                case Pull -> {
                    quad = 1 - quad;
                    matrixStack.translate(0, -100 * quad, 0);
                }
                case Scale -> matrixStack.scale((float) quad, (float) quad, 1);
            }
        }
        for (ClickGuiTab tab : tabs) {
            tab.draw(drawContext, tickDelta, getColor());
        }
        matrixStack.pop();
    }

    public boolean isClickGuiOpen() {
        return mc.currentScreen instanceof ClickGuiScreen;
    }

    public static final ArrayList<Snow> snows = new ArrayList<>(){
        {
            Random random = new Random();
            for (int i = 0; i < 100; ++i) {
                for (int y = 0; y < 3; ++y) {
                    add(new Snow(25 * i, y * -50, random.nextInt(3) + 1, random.nextInt(2) + 1));
                }
            }
        }
    };
}
