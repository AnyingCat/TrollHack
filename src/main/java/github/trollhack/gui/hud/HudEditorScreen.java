package github.trollhack.gui.hud;

import github.trollhack.core.Managers;
import github.trollhack.gui.clickgui.ClickGUIScreen;
import github.trollhack.gui.clickgui.component.SettingComponent;
import github.trollhack.gui.clickgui.component.setting.*;
import github.trollhack.modules.HudModule;
import github.trollhack.modules.impl.client.GuiSetting;
import github.trollhack.modules.impl.client.HudEditor;
import github.trollhack.settings.Setting;
import github.trollhack.settings.impl.*;
import github.trollhack.utils.render.Render2DUtil;
import github.trollhack.utils.render.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static github.trollhack.gui.clickgui.component.Component.mc;

public class HudEditorScreen extends Screen {
    public static HudEditorScreen INSTANCE;

    private final List<HudModule> hudModules;
    private HudModule draggingModule = null;
    private List<SettingComponent> settingComponents = null;
    private HudModule settingHudModule = null;

    private float settingX, settingY;
    private float settingScrollProgress, settingScrollSpeed;
    private long settingLastScrollUpdate, settingLastBoundaryCheck;

    private static final float PANEL_WIDTH = 100.0f;
    private static final float PANEL_HEIGHT = 400.0f;
    private static final float MODULE_HEIGHT = 12.0f;
    private static final float SETTING_WIDTH = 120.0f;
    private static final float SETTING_MAX_HEIGHT = 200.0f;
    private float panelX, panelY;
    private boolean panelDragging;
    private float panelDragOffX, panelDragOffY;
    private float panelScrollProgress, panelScrollSpeed;
    private long panelLastScrollUpdate = System.currentTimeMillis();
    private long panelLastBoundaryCheck = System.currentTimeMillis();

    public HudEditorScreen(List<HudModule> hudModules) {
        super(Text.literal("HudEditor"));
        this.hudModules = hudModules;
    }

    public static HudEditorScreen getInstance() {
        if (INSTANCE == null) INSTANCE = new HudEditorScreen(Managers.HUD.hudModules);
        return INSTANCE;
    }

    @Override
    protected void init() {
        super.init();
        panelX = 2f;
        panelY = 10f;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrices = context.getMatrices();

        for (HudModule hud : hudModules) {
            if (!hud.isEnabled()) continue;
            hud.onHudRender(context);
            float x = hud.getRenderX(), y = hud.getPosY(), w = hud.getWidth(), h = hud.getHeight();
            boolean hovered = hud.isHovered(mouseX, mouseY);
            Color outline = hud == draggingModule ? new Color(255, 140, 180, 200)
                : hovered ? new Color(255, 255, 255, 120) : new Color(255, 255, 255, 50);
            Render2DUtil.drawRectOutline(matrices, x, y, w, h, 0.5f, outline);
            if (FontRenderers.ducksans != null)
                FontRenderers.ducksans.drawText(matrices, hud.getName(), x + 2f, y - 9f, 0.7f, new Color(255, 255, 255, 150));
        }

        {
            long now = System.currentTimeMillis();
            double t = (now - panelLastScrollUpdate) / 100.0;
            panelLastScrollUpdate = now;
            double ln = Math.log(0.25);
            double newSpeed = panelScrollSpeed * Math.pow(0.25, t);
            panelScrollProgress += (float)((newSpeed / ln) - (panelScrollSpeed / ln));
            panelScrollSpeed = (float)newSpeed;
            if (now - panelLastBoundaryCheck >= 100L) {
                panelLastBoundaryCheck = now;
                float max = Math.max(hudModules.size() * (MODULE_HEIGHT + GuiSetting.INSTANCE.yMargin.getValue()) + MODULE_HEIGHT + 4f - PANEL_HEIGHT, 0.01f);
                if (panelScrollProgress < 0f) panelScrollSpeed = panelScrollProgress * -0.4f;
                else if (panelScrollProgress > max) panelScrollSpeed = (panelScrollProgress - max) * -0.4f;
            }
        }
        float xMargin = GuiSetting.INSTANCE.xMargin.getValue();
        float yMargin = GuiSetting.INSTANCE.yMargin.getValue();
        float titleHeight = MODULE_HEIGHT + 4f;

        if (GuiSetting.INSTANCE.backgroundBlur.getValue() > 0)
            Render2DUtil.drawRoundedBlur(matrices, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 0, GuiSetting.INSTANCE.getBackGround(), GuiSetting.INSTANCE.backgroundBlur.getValue() * 15f, 1.0f);
        Render2DUtil.drawRect(matrices, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, GuiSetting.INSTANCE.getBackGround());
        if (GuiSetting.INSTANCE.windowOutline.getValue()) {
            Color p = GuiSetting.INSTANCE.getPrimary();
            Render2DUtil.drawRectOutline(matrices, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 0.5f, new Color(p.getRed(), p.getGreen(), p.getBlue(), 255));
        }
        if (GuiSetting.INSTANCE.titleBar.getValue())
            Render2DUtil.drawRect(matrices, panelX, panelY, PANEL_WIDTH, titleHeight, GuiSetting.INSTANCE.getPrimary());
        if (FontRenderers.ducksans != null)
            FontRenderers.ducksans.drawText(matrices, "HUD", panelX + 3f, panelY + 3f, 1.0f, GuiSetting.INSTANCE.getText());

        context.enableScissor((int)(panelX + xMargin), (int)(panelY + titleHeight), (int)(panelX + PANEL_WIDTH - xMargin), (int)(panelY + PANEL_HEIGHT));
        float moduleY = titleHeight + yMargin - panelScrollProgress;
        for (HudModule hud : hudModules) {
            float adjustedY = panelY + moduleY;
            if (adjustedY + MODULE_HEIGHT < panelY + titleHeight) { moduleY += MODULE_HEIGHT + yMargin; continue; }
            if (adjustedY > panelY + PANEL_HEIGHT) break;
            float mx = panelX + xMargin, mw = PANEL_WIDTH - xMargin * 2;
            boolean modHovered = mouseX >= mx && mouseX <= mx + mw && mouseY >= adjustedY && mouseY <= adjustedY + MODULE_HEIGHT;
            Render2DUtil.drawRect(matrices, mx, adjustedY, mw, MODULE_HEIGHT, new Color(0, 0, 0, 40));
            if (hud.isEnabled()) Render2DUtil.drawRect(matrices, mx, adjustedY, mw, MODULE_HEIGHT, GuiSetting.INSTANCE.getPrimary());
            if (modHovered) Render2DUtil.drawRect(matrices, mx, adjustedY, mw, MODULE_HEIGHT, GuiSetting.INSTANCE.getHover());
            if (FontRenderers.ducksans != null)
                FontRenderers.ducksans.drawText(matrices, hud.getName(), mx + 2f, adjustedY + 2f, 0.9f, GuiSetting.INSTANCE.getText(), panelX + xMargin, panelY + titleHeight, PANEL_WIDTH - xMargin * 2, PANEL_HEIGHT - titleHeight);
            moduleY += MODULE_HEIGHT + yMargin;
        }
        context.disableScissor();

        if (settingHudModule != null) {
            {
                long now = System.currentTimeMillis();
                double t = (now - settingLastScrollUpdate) / 100.0;
                settingLastScrollUpdate = now;
                double ln = Math.log(0.25);
                double newSpeed = settingScrollSpeed * Math.pow(0.25, t);
                settingScrollProgress += (float)((newSpeed / ln) - (settingScrollSpeed / ln));
                settingScrollSpeed = (float)newSpeed;
                if (now - settingLastBoundaryCheck >= 100L) {
                    settingLastBoundaryCheck = now;
                    float draggableH = FontRenderers.ducksans.getStringHeight(1.0f) + 6.0f;
                    float contentH = 0f;
                    SettingComponent lastVisible = null;
                    for (SettingComponent comp : settingComponents) {
                        if (comp.isVisible()) { contentH += comp.getHeight() + yMargin; lastVisible = comp; }
                    }
                    float totalH = Math.min(draggableH + contentH + yMargin, SETTING_MAX_HEIGHT);
                    float max = lastVisible == null ? 0.01f : Math.max(contentH + yMargin - (totalH - draggableH), 0.01f);
                    if (settingScrollProgress < 0f) settingScrollSpeed = settingScrollProgress * -0.4f;
                    else if (settingScrollProgress > max) settingScrollSpeed = (settingScrollProgress - max) * -0.4f;
                }
            }
            float draggableHeight = FontRenderers.ducksans.getStringHeight(1.0f) + 6.0f;
            float contentH = 0f;
            for (SettingComponent comp : settingComponents) { if (comp.isVisible()) contentH += comp.getHeight() + yMargin; }
            float totalHeight = Math.min(draggableHeight + contentH + yMargin, SETTING_MAX_HEIGHT);

            if (GuiSetting.INSTANCE.backgroundBlur.getValue() > 0)
                Render2DUtil.drawRoundedBlur(matrices, settingX, settingY, SETTING_WIDTH, totalHeight, 0, GuiSetting.INSTANCE.getBackGround(), GuiSetting.INSTANCE.backgroundBlur.getValue() * 15f, 1.0f);
            Render2DUtil.drawRect(matrices, settingX, settingY, SETTING_WIDTH, totalHeight, GuiSetting.INSTANCE.getBackGround());
            if (GuiSetting.INSTANCE.windowOutline.getValue()) {
                Color p = GuiSetting.INSTANCE.getPrimary();
                Render2DUtil.drawRectOutline(matrices, settingX, settingY, SETTING_WIDTH, totalHeight, 0.5f, new Color(p.getRed(), p.getGreen(), p.getBlue(), 255));
            }
            if (GuiSetting.INSTANCE.titleBar.getValue())
                Render2DUtil.drawRect(matrices, settingX, settingY, SETTING_WIDTH, draggableHeight, GuiSetting.INSTANCE.getPrimary());
            if (FontRenderers.ducksans != null)
                FontRenderers.ducksans.drawText(matrices, settingHudModule.getName(), settingX + 3.0f, settingY + 3.5f, 1.0f, GuiSetting.INSTANCE.getText());

            context.enableScissor((int)(settingX + xMargin), (int)(settingY + draggableHeight), (int)(settingX + SETTING_WIDTH - xMargin), (int)(settingY + totalHeight));
            float clipX = settingX + xMargin, clipY = settingY + draggableHeight, clipW = SETTING_WIDTH - xMargin * 2, clipH = totalHeight - draggableHeight;
            float sy = draggableHeight + yMargin - settingScrollProgress;
            for (SettingComponent comp : settingComponents) {
                if (!comp.isVisible()) continue;
                if (sy + comp.getHeight() < draggableHeight) { sy += comp.getHeight() + yMargin; continue; }
                if (sy > totalHeight) break;
                comp.setPosition(settingX + xMargin, settingY + sy);
                comp.setWidth(SETTING_WIDTH - xMargin * 2);
                comp.setClip(clipX, clipY, clipW, clipH);
                comp.render(context, mouseX, mouseY, delta);
                sy += comp.getHeight() + yMargin;
            }
            context.disableScissor();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (settingHudModule != null) {
            float draggableH = FontRenderers.ducksans.getStringHeight(1.0f) + 6.0f;
            float yMargin = GuiSetting.INSTANCE.yMargin.getValue();
            float contentH = 0f;
            for (SettingComponent c : settingComponents) { if (c.isVisible()) contentH += c.getHeight() + yMargin; }
            float totalH = Math.min(draggableH + contentH + yMargin, SETTING_MAX_HEIGHT);
            if (mouseX >= settingX && mouseX <= settingX + SETTING_WIDTH && mouseY >= settingY && mouseY <= settingY + totalH) {
                for (SettingComponent comp : settingComponents) { if (comp.isVisible()) comp.mouseClicked(mouseX, mouseY, button); }
                return true;
            }
            settingHudModule = null;
            settingComponents = null;
            return true;
        }

        float titleHeight = MODULE_HEIGHT + 4f;
        float xMargin = GuiSetting.INSTANCE.xMargin.getValue();
        float yMargin = GuiSetting.INSTANCE.yMargin.getValue();

        if (button == 0) {
            if (mouseX >= panelX && mouseX <= panelX + PANEL_WIDTH && mouseY >= panelY && mouseY <= panelY + titleHeight) {
                panelDragging = true;
                panelDragOffX = (float)(mouseX - panelX);
                panelDragOffY = (float)(mouseY - panelY);
                return true;
            }
            if (mouseX >= panelX && mouseX <= panelX + PANEL_WIDTH && mouseY >= panelY && mouseY <= panelY + PANEL_HEIGHT) {
                float moduleY = titleHeight + yMargin - panelScrollProgress;
                for (HudModule hud : hudModules) {
                    float adjustedY = panelY + moduleY;
                    float mx = panelX + xMargin, mw = PANEL_WIDTH - xMargin * 2;
                    if (mouseX >= mx && mouseX <= mx + mw && mouseY >= adjustedY && mouseY <= adjustedY + MODULE_HEIGHT) {
                        hud.toggle();
                        return true;
                    }
                    moduleY += MODULE_HEIGHT + yMargin;
                }
                return true;
            }
            for (HudModule hud : hudModules) {
                if (hud.isEnabled() && hud.isHovered(mouseX, mouseY)) {
                    draggingModule = hud;
                    hud.setDragging(true);
                    hud.setDragOffset((float)mouseX - hud.getPosX(), (float)mouseY - hud.getPosY());
                    return true;
                }
            }
        }

        if (button == 1 && mouseX >= panelX && mouseX <= panelX + PANEL_WIDTH && mouseY >= panelY && mouseY <= panelY + PANEL_HEIGHT) {
            float moduleY = titleHeight + yMargin - panelScrollProgress;
            for (HudModule hud : hudModules) {
                float adjustedY = panelY + moduleY;
                float mx = panelX + xMargin, mw = PANEL_WIDTH - xMargin * 2;
                if (mouseX >= mx && mouseX <= mx + mw && mouseY >= adjustedY && mouseY <= adjustedY + MODULE_HEIGHT) {
                    if (!hud.getSettings().isEmpty()) {
                        settingHudModule = hud;
                        settingX = (float)mouseX;
                        settingY = (float)mouseY;
                        settingScrollProgress = 0f;
                        settingScrollSpeed = 0f;
                        settingLastScrollUpdate = System.currentTimeMillis();
                        settingLastBoundaryCheck = System.currentTimeMillis();
                        settingComponents = new ArrayList<>();
                        for (Setting<?> s : hud.getSettings()) {
                            SettingComponent comp = switch (s) {
                                case BooleanSetting bs -> new BooleanSettingComponent(bs);
                                case FloatSetting fs -> new NumberSettingComponent(fs);
                                case IntegerSetting is -> new NumberSettingComponent(is);
                                case EnumSetting<?> es -> new EnumSettingComponent<>(es);
                                case StringSetting ss -> new StringSettingComponent(ss);
                                case ColorSetting cs -> new ColorSettingComponent(cs);
                                default -> null;
                            };
                            if (comp != null) settingComponents.add(comp);
                        }
                    }
                    return true;
                }
                moduleY += MODULE_HEIGHT + yMargin;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (settingComponents != null) for (SettingComponent c : settingComponents) c.mouseReleased(mouseX, mouseY, button);
        if (draggingModule != null) { draggingModule.setDragging(false); draggingModule = null; }
        panelDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (settingComponents != null) for (SettingComponent c : settingComponents) c.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        if (draggingModule != null) {
            float newX = draggingModule.clampDragX((float)mouseX - draggingModule.getDragOffsetX());
            float newY = Math.max(0, Math.min(mc.getWindow().getScaledHeight() - draggingModule.getHeight(), (float)mouseY - draggingModule.getDragOffsetY()));
            draggingModule.setPosX(newX);
            draggingModule.setPosY(newY);
            return true;
        }
        if (panelDragging) { panelX = (float)(mouseX - panelDragOffX); panelY = (float)(mouseY - panelDragOffY); return true; }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (settingHudModule != null) {
            float draggableH = FontRenderers.ducksans.getStringHeight(1.0f) + 6.0f;
            float yMargin = GuiSetting.INSTANCE.yMargin.getValue();
            float contentH = 0f;
            for (SettingComponent c : settingComponents) { if (c.isVisible()) contentH += c.getHeight() + yMargin; }
            float totalH = Math.min(draggableH + contentH + yMargin, SETTING_MAX_HEIGHT);
            if (mouseX >= settingX && mouseX <= settingX + SETTING_WIDTH && mouseY >= settingY && mouseY <= settingY + totalH) {
                settingScrollSpeed -= (float)verticalAmount * 24.0f;
                return true;
            }
        }
        if (mouseX >= panelX && mouseX <= panelX + PANEL_WIDTH && mouseY >= panelY && mouseY <= panelY + PANEL_HEIGHT) {
            panelScrollSpeed -= (float)verticalAmount * 24.0f;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (settingHudModule != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) { settingHudModule = null; settingComponents = null; return true; }
            for (SettingComponent c : settingComponents) { if (c.keyPressed(keyCode, scanCode, modifiers)) return true; }
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            mc.setScreen(ClickGUIScreen.getInstance());
            HudEditor.INSTANCE.setEnabled(false);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (settingComponents != null) { for (SettingComponent c : settingComponents) { if (c.charTyped(chr, modifiers)) return true; } return false; }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }
}
