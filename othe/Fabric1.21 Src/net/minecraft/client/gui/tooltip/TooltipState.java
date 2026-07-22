/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.tooltip;

import java.time.Duration;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.FocusedTooltipPositioner;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.gui.tooltip.WidgetTooltipPositioner;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TooltipState {
    @Nullable
    private Tooltip tooltip;
    private Duration delay = Duration.ZERO;
    private long renderCheckTime;
    private boolean prevShouldRender;

    public void setDelay(Duration delay) {
        this.delay = delay;
    }

    public void setTooltip(@Nullable Tooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Nullable
    public Tooltip getTooltip() {
        return this.tooltip;
    }

    public void render(boolean hovered, boolean focused, ScreenRect focus) {
        Screen screen;
        boolean bl;
        if (this.tooltip == null) {
            this.prevShouldRender = false;
            return;
        }
        boolean bl2 = bl = hovered || focused && MinecraftClient.getInstance().getNavigationType().isKeyboard();
        if (bl != this.prevShouldRender) {
            if (bl) {
                this.renderCheckTime = Util.getMeasuringTimeMs();
            }
            this.prevShouldRender = bl;
        }
        if (bl && Util.getMeasuringTimeMs() - this.renderCheckTime > this.delay.toMillis() && (screen = MinecraftClient.getInstance().currentScreen) != null) {
            screen.setTooltip(this.tooltip, this.createPositioner(focus, hovered, focused), focused);
        }
    }

    private TooltipPositioner createPositioner(ScreenRect focus, boolean hovered, boolean focused) {
        if (!hovered && focused && MinecraftClient.getInstance().getNavigationType().isKeyboard()) {
            return new FocusedTooltipPositioner(focus);
        }
        return new WidgetTooltipPositioner(focus);
    }

    public void appendNarrations(NarrationMessageBuilder builder) {
        if (this.tooltip != null) {
            this.tooltip.appendNarrations(builder);
        }
    }
}

