/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client;

import com.mojang.logging.LogUtils;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.navigation.GuiNavigationType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.GlfwUtil;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Smoother;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWDropCallback;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class Mouse {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final MinecraftClient client;
    private boolean leftButtonClicked;
    private boolean middleButtonClicked;
    private boolean rightButtonClicked;
    private double x;
    private double y;
    private int controlLeftClicks;
    private int activeButton = -1;
    private boolean hasResolutionChanged = true;
    private int field_1796;
    private double glfwTime;
    private final Smoother cursorXSmoother = new Smoother();
    private final Smoother cursorYSmoother = new Smoother();
    private double cursorDeltaX;
    private double cursorDeltaY;
    private double eventDeltaHorizontalWheel;
    private double eventDeltaVerticalWheel;
    private double lastTickTime = Double.MIN_VALUE;
    private boolean cursorLocked;

    public Mouse(MinecraftClient client) {
        this.client = client;
    }

    private void onMouseButton(long window, int button, int action, int mods) {
        boolean bl;
        if (window != this.client.getWindow().getHandle()) {
            return;
        }
        if (this.client.currentScreen != null) {
            this.client.setNavigationType(GuiNavigationType.MOUSE);
        }
        boolean bl2 = bl = action == 1;
        if (MinecraftClient.IS_SYSTEM_MAC && button == 0) {
            if (bl) {
                if ((mods & 2) == 2) {
                    button = 1;
                    ++this.controlLeftClicks;
                }
            } else if (this.controlLeftClicks > 0) {
                button = 1;
                --this.controlLeftClicks;
            }
        }
        int i = button;
        if (bl) {
            if (this.client.options.getTouchscreen().getValue().booleanValue() && this.field_1796++ > 0) {
                return;
            }
            this.activeButton = i;
            this.glfwTime = GlfwUtil.getTime();
        } else if (this.activeButton != -1) {
            if (this.client.options.getTouchscreen().getValue().booleanValue() && --this.field_1796 > 0) {
                return;
            }
            this.activeButton = -1;
        }
        boolean[] bls = new boolean[]{false};
        if (this.client.getOverlay() == null) {
            if (this.client.currentScreen == null) {
                if (!this.cursorLocked && bl) {
                    this.lockCursor();
                }
            } else {
                double d = this.x * (double)this.client.getWindow().getScaledWidth() / (double)this.client.getWindow().getWidth();
                double e = this.y * (double)this.client.getWindow().getScaledHeight() / (double)this.client.getWindow().getHeight();
                Screen screen = this.client.currentScreen;
                if (bl) {
                    screen.applyMousePressScrollNarratorDelay();
                    Screen.wrapScreenError(() -> {
                        bls[0] = screen.mouseClicked(d, e, i);
                    }, "mouseClicked event handler", screen.getClass().getCanonicalName());
                } else {
                    Screen.wrapScreenError(() -> {
                        bls[0] = screen.mouseReleased(d, e, i);
                    }, "mouseReleased event handler", screen.getClass().getCanonicalName());
                }
            }
        }
        if (!bls[0] && this.client.currentScreen == null && this.client.getOverlay() == null) {
            if (i == 0) {
                this.leftButtonClicked = bl;
            } else if (i == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                this.middleButtonClicked = bl;
            } else if (i == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                this.rightButtonClicked = bl;
            }
            KeyBinding.setKeyPressed(InputUtil.Type.MOUSE.createFromCode(i), bl);
            if (bl) {
                if (this.client.player.isSpectator() && i == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                    this.client.inGameHud.getSpectatorHud().useSelectedCommand();
                } else {
                    KeyBinding.onKeyPressed(InputUtil.Type.MOUSE.createFromCode(i));
                }
            }
        }
    }

    /**
     * Called when a mouse is used to scroll.
     * 
     * @param vertical the vertical scroll distance
     * @param horizontal the horizontal scroll distance
     * @param window the window handle
     */
    private void onMouseScroll(long window, double horizontal, double vertical) {
        if (window == MinecraftClient.getInstance().getWindow().getHandle()) {
            boolean bl = this.client.options.getDiscreteMouseScroll().getValue();
            double d = this.client.options.getMouseWheelSensitivity().getValue();
            double e = (bl ? Math.signum(horizontal) : horizontal) * d;
            double f = (bl ? Math.signum(vertical) : vertical) * d;
            if (this.client.getOverlay() == null) {
                if (this.client.currentScreen != null) {
                    double g = this.x * (double)this.client.getWindow().getScaledWidth() / (double)this.client.getWindow().getWidth();
                    double h = this.y * (double)this.client.getWindow().getScaledHeight() / (double)this.client.getWindow().getHeight();
                    this.client.currentScreen.mouseScrolled(g, h, e, f);
                    this.client.currentScreen.applyMousePressScrollNarratorDelay();
                } else if (this.client.player != null) {
                    int k;
                    if (this.eventDeltaHorizontalWheel != 0.0 && Math.signum(e) != Math.signum(this.eventDeltaHorizontalWheel)) {
                        this.eventDeltaHorizontalWheel = 0.0;
                    }
                    if (this.eventDeltaVerticalWheel != 0.0 && Math.signum(f) != Math.signum(this.eventDeltaVerticalWheel)) {
                        this.eventDeltaVerticalWheel = 0.0;
                    }
                    this.eventDeltaHorizontalWheel += e;
                    this.eventDeltaVerticalWheel += f;
                    int i = (int)this.eventDeltaHorizontalWheel;
                    int j = (int)this.eventDeltaVerticalWheel;
                    if (i == 0 && j == 0) {
                        return;
                    }
                    this.eventDeltaHorizontalWheel -= (double)i;
                    this.eventDeltaVerticalWheel -= (double)j;
                    int n = k = j == 0 ? -i : j;
                    if (this.client.player.isSpectator()) {
                        if (this.client.inGameHud.getSpectatorHud().isOpen()) {
                            this.client.inGameHud.getSpectatorHud().cycleSlot(-k);
                        } else {
                            float l = MathHelper.clamp(this.client.player.getAbilities().getFlySpeed() + (float)j * 0.005f, 0.0f, 0.2f);
                            this.client.player.getAbilities().setFlySpeed(l);
                        }
                    } else {
                        this.client.player.getInventory().scrollInHotbar(k);
                    }
                }
            }
        }
    }

    private void onFilesDropped(long window, List<Path> paths, int invalidFilesCount) {
        if (this.client.currentScreen != null) {
            this.client.currentScreen.filesDragged(paths);
        }
        if (invalidFilesCount > 0) {
            SystemToast.addFileDropFailure(this.client, invalidFilesCount);
        }
    }

    public void setup(long window2) {
        InputUtil.setMouseCallbacks(window2, (window, x, y) -> this.client.execute(() -> this.onCursorPos(window, x, y)), (window, button, action, modifiers) -> this.client.execute(() -> this.onMouseButton(window, button, action, modifiers)), (window, offsetX, offsetY) -> this.client.execute(() -> this.onMouseScroll(window, offsetX, offsetY)), (window, count, names) -> {
            int j;
            ArrayList<Path> list = new ArrayList<Path>(count);
            int i = 0;
            for (j = 0; j < count; ++j) {
                String string = GLFWDropCallback.getName(names, j);
                try {
                    list.add(Paths.get(string, new String[0]));
                    continue;
                } catch (InvalidPathException invalidPathException) {
                    ++i;
                    LOGGER.error("Failed to parse path '{}'", (Object)string, (Object)invalidPathException);
                }
            }
            if (!list.isEmpty()) {
                j = i;
                this.client.execute(() -> this.onFilesDropped(window, list, j));
            }
        });
    }

    private void onCursorPos(long window, double x, double y) {
        if (window != MinecraftClient.getInstance().getWindow().getHandle()) {
            return;
        }
        if (this.hasResolutionChanged) {
            this.x = x;
            this.y = y;
            this.hasResolutionChanged = false;
            return;
        }
        if (this.client.isWindowFocused()) {
            this.cursorDeltaX += x - this.x;
            this.cursorDeltaY += y - this.y;
        }
        this.x = x;
        this.y = y;
    }

    public void tick() {
        double d = GlfwUtil.getTime();
        double e = d - this.lastTickTime;
        this.lastTickTime = d;
        if (this.client.isWindowFocused()) {
            Screen screen = this.client.currentScreen;
            if (screen != null && this.client.getOverlay() == null && (this.cursorDeltaX != 0.0 || this.cursorDeltaY != 0.0)) {
                double f = this.x * (double)this.client.getWindow().getScaledWidth() / (double)this.client.getWindow().getWidth();
                double g = this.y * (double)this.client.getWindow().getScaledHeight() / (double)this.client.getWindow().getHeight();
                Screen.wrapScreenError(() -> screen.mouseMoved(f, g), "mouseMoved event handler", screen.getClass().getCanonicalName());
                if (this.activeButton != -1 && this.glfwTime > 0.0) {
                    double h = this.cursorDeltaX * (double)this.client.getWindow().getScaledWidth() / (double)this.client.getWindow().getWidth();
                    double i = this.cursorDeltaY * (double)this.client.getWindow().getScaledHeight() / (double)this.client.getWindow().getHeight();
                    Screen.wrapScreenError(() -> screen.mouseDragged(f, g, this.activeButton, h, i), "mouseDragged event handler", screen.getClass().getCanonicalName());
                }
                screen.applyMouseMoveNarratorDelay();
            }
            if (this.isCursorLocked() && this.client.player != null) {
                this.updateMouse(e);
            }
        }
        this.cursorDeltaX = 0.0;
        this.cursorDeltaY = 0.0;
    }

    private void updateMouse(double timeDelta) {
        double j;
        double i;
        double d = this.client.options.getMouseSensitivity().getValue() * (double)0.6f + (double)0.2f;
        double e = d * d * d;
        double f = e * 8.0;
        if (this.client.options.smoothCameraEnabled) {
            double g = this.cursorXSmoother.smooth(this.cursorDeltaX * f, timeDelta * f);
            double h = this.cursorYSmoother.smooth(this.cursorDeltaY * f, timeDelta * f);
            i = g;
            j = h;
        } else if (this.client.options.getPerspective().isFirstPerson() && this.client.player.isUsingSpyglass()) {
            this.cursorXSmoother.clear();
            this.cursorYSmoother.clear();
            i = this.cursorDeltaX * e;
            j = this.cursorDeltaY * e;
        } else {
            this.cursorXSmoother.clear();
            this.cursorYSmoother.clear();
            i = this.cursorDeltaX * f;
            j = this.cursorDeltaY * f;
        }
        int k = 1;
        if (this.client.options.getInvertYMouse().getValue().booleanValue()) {
            k = -1;
        }
        this.client.getTutorialManager().onUpdateMouse(i, j);
        if (this.client.player != null) {
            this.client.player.changeLookDirection(i, j * (double)k);
        }
    }

    public boolean wasLeftButtonClicked() {
        return this.leftButtonClicked;
    }

    public boolean wasMiddleButtonClicked() {
        return this.middleButtonClicked;
    }

    public boolean wasRightButtonClicked() {
        return this.rightButtonClicked;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public void onResolutionChanged() {
        this.hasResolutionChanged = true;
    }

    public boolean isCursorLocked() {
        return this.cursorLocked;
    }

    public void lockCursor() {
        if (!this.client.isWindowFocused()) {
            return;
        }
        if (this.cursorLocked) {
            return;
        }
        if (!MinecraftClient.IS_SYSTEM_MAC) {
            KeyBinding.updatePressedStates();
        }
        this.cursorLocked = true;
        this.x = this.client.getWindow().getWidth() / 2;
        this.y = this.client.getWindow().getHeight() / 2;
        InputUtil.setCursorParameters(this.client.getWindow().getHandle(), InputUtil.GLFW_CURSOR_DISABLED, this.x, this.y);
        this.client.setScreen(null);
        this.client.attackCooldown = 10000;
        this.hasResolutionChanged = true;
    }

    public void unlockCursor() {
        if (!this.cursorLocked) {
            return;
        }
        this.cursorLocked = false;
        this.x = this.client.getWindow().getWidth() / 2;
        this.y = this.client.getWindow().getHeight() / 2;
        InputUtil.setCursorParameters(this.client.getWindow().getHandle(), InputUtil.GLFW_CURSOR_NORMAL, this.x, this.y);
    }

    public void setResolutionChanged() {
        this.hasResolutionChanged = true;
    }
}

