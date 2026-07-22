package github.trollhack.gui.clickgui.component;

import github.trollhack.modules.impl.client.GuiSetting;
import github.trollhack.utils.animation.AnimationUtil;
import github.trollhack.utils.render.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

public abstract class Component {
    public enum MouseState { NONE, HOVER, CLICK, DRAG }

    public static final MinecraftClient mc = MinecraftClient.getInstance();

    protected float x;
    protected float y;
    protected float width;
    protected float height;
    protected boolean visible = true;

    protected MouseState prevState = MouseState.NONE;
    protected MouseState mouseState = MouseState.NONE;
    protected long lastStateUpdateTime = 0;
    protected boolean clicking = false;
    protected boolean dragging = false;

    public Component(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void render(DrawContext context, int mouseX, int mouseY, float delta);

    public abstract void mouseClicked(double mouseX, double mouseY, int button);

    public abstract void mouseReleased(double mouseX, double mouseY, int button);

    public abstract void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY);

    public abstract boolean keyPressed(int keyCode, int scanCode, int modifiers);

    public boolean charTyped(char chr, int modifiers) {
        return false;
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    protected void setMouseState(MouseState newState) {
        if (newState != mouseState) {
            prevState = mouseState;
            mouseState = newState;
            lastStateUpdateTime = System.currentTimeMillis();
        }
    }

    protected void updateMouseState(double mouseX, double mouseY) {
        boolean hovered = isHovered(mouseX, mouseY);
        MouseState newState;
        if (dragging) {
            newState = MouseState.DRAG;
        } else if (clicking) {
            newState = MouseState.CLICK;
        } else if (hovered) {
            newState = MouseState.HOVER;
        } else {
            newState = MouseState.NONE;
        }
        setMouseState(newState);
    }

    protected Color getStateColor(MouseState state) {
        return switch (state) {
            case NONE -> GuiSetting.INSTANCE.getIdle();
            case HOVER -> GuiSetting.INSTANCE.getHover();
            case CLICK, DRAG -> GuiSetting.INSTANCE.getClick();
        };
    }

    protected float getTransitionProgress() {
        return AnimationUtil.Easing.EASE_OUT_EXPO.inc(AnimationUtil.toDelta(lastStateUpdateTime, 300.0f));
    }

    protected float getHoverScale() {
        float prev = prevState == MouseState.NONE ? 0.0f : 1.0f;
        float curr = mouseState == MouseState.NONE ? 0.0f : 1.0f;
        return AnimationUtil.Easing.EASE_OUT_BACK.incOrDec(
            AnimationUtil.toDelta(lastStateUpdateTime, 300.0f),
            prev, curr
        );
    }

    protected float getClickedScale() {
        float prev = (prevState == MouseState.CLICK || prevState == MouseState.DRAG) ? 1.0f : 0.0f;
        float curr = (mouseState == MouseState.CLICK || mouseState == MouseState.DRAG) ? 1.0f : 0.0f;
        return AnimationUtil.Easing.EASE_OUT_CUBIC.incOrDec(
            AnimationUtil.toDelta(lastStateUpdateTime, 300.0f),
            prev, curr
        );
    }

    protected Color getOverlayColor() {
        return ColorUtil.mix(
            getStateColor(prevState),
            getStateColor(mouseState),
            getTransitionProgress()
        );
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public boolean isVisible() { return visible; }

    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setPosition(float x, float y) { this.x = x; this.y = y; }
    public void setWidth(float width) { this.width = width; }
    public void setHeight(float height) { this.height = height; }
    public void setVisible(boolean visible) { this.visible = visible; }
}
