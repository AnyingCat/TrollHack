/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.spectator.SpectatorMenu;
import net.minecraft.client.gui.hud.spectator.SpectatorMenuCloseCallback;
import net.minecraft.client.gui.hud.spectator.SpectatorMenuCommand;
import net.minecraft.client.gui.hud.spectator.SpectatorMenuState;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SpectatorHud
implements SpectatorMenuCloseCallback {
    private static final Identifier HOTBAR_TEXTURE = Identifier.ofVanilla("hud/hotbar");
    private static final Identifier HOTBAR_SELECTION_TEXTURE = Identifier.ofVanilla("hud/hotbar_selection");
    private static final long FADE_OUT_DELAY = 5000L;
    private static final long FADE_OUT_DURATION = 2000L;
    private final MinecraftClient client;
    private long lastInteractionTime;
    @Nullable
    private SpectatorMenu spectatorMenu;

    public SpectatorHud(MinecraftClient client) {
        this.client = client;
    }

    public void selectSlot(int slot) {
        this.lastInteractionTime = Util.getMeasuringTimeMs();
        if (this.spectatorMenu != null) {
            this.spectatorMenu.useCommand(slot);
        } else {
            this.spectatorMenu = new SpectatorMenu(this);
        }
    }

    private float getSpectatorMenuHeight() {
        long l = this.lastInteractionTime - Util.getMeasuringTimeMs() + 5000L;
        return MathHelper.clamp((float)l / 2000.0f, 0.0f, 1.0f);
    }

    public void renderSpectatorMenu(DrawContext context) {
        if (this.spectatorMenu == null) {
            return;
        }
        float f = this.getSpectatorMenuHeight();
        if (f <= 0.0f) {
            this.spectatorMenu.close();
            return;
        }
        int i = context.getScaledWindowWidth() / 2;
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, -90.0f);
        int j = MathHelper.floor((float)context.getScaledWindowHeight() - 22.0f * f);
        SpectatorMenuState spectatorMenuState = this.spectatorMenu.getCurrentState();
        this.renderSpectatorMenu(context, f, i, j, spectatorMenuState);
        context.getMatrices().pop();
    }

    protected void renderSpectatorMenu(DrawContext context, float height, int x, int y, SpectatorMenuState state) {
        RenderSystem.enableBlend();
        context.setShaderColor(1.0f, 1.0f, 1.0f, height);
        context.drawGuiTexture(HOTBAR_TEXTURE, x - 91, y, 182, 22);
        if (state.getSelectedSlot() >= 0) {
            context.drawGuiTexture(HOTBAR_SELECTION_TEXTURE, x - 91 - 1 + state.getSelectedSlot() * 20, y - 1, 24, 23);
        }
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        for (int i = 0; i < 9; ++i) {
            this.renderSpectatorCommand(context, i, context.getScaledWindowWidth() / 2 - 90 + i * 20 + 2, y + 3, height, state.getCommand(i));
        }
        RenderSystem.disableBlend();
    }

    private void renderSpectatorCommand(DrawContext context, int slot, int x, float y, float height, SpectatorMenuCommand command) {
        if (command != SpectatorMenu.BLANK_COMMAND) {
            int i = (int)(height * 255.0f);
            context.getMatrices().push();
            context.getMatrices().translate(x, y, 0.0f);
            float f = command.isEnabled() ? 1.0f : 0.25f;
            context.setShaderColor(f, f, f, height);
            command.renderIcon(context, f, i);
            context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            context.getMatrices().pop();
            if (i > 3 && command.isEnabled()) {
                Text text = this.client.options.hotbarKeys[slot].getBoundKeyLocalizedText();
                context.drawTextWithShadow(this.client.textRenderer, text, x + 19 - 2 - this.client.textRenderer.getWidth(text), (int)y + 6 + 3, 0xFFFFFF + (i << 24));
            }
        }
    }

    public void render(DrawContext context) {
        int i = (int)(this.getSpectatorMenuHeight() * 255.0f);
        if (i > 3 && this.spectatorMenu != null) {
            Text text;
            SpectatorMenuCommand spectatorMenuCommand = this.spectatorMenu.getSelectedCommand();
            Text text2 = text = spectatorMenuCommand == SpectatorMenu.BLANK_COMMAND ? this.spectatorMenu.getCurrentGroup().getPrompt() : spectatorMenuCommand.getName();
            if (text != null) {
                int j = this.client.textRenderer.getWidth(text);
                int k = (context.getScaledWindowWidth() - j) / 2;
                int l = context.getScaledWindowHeight() - 35;
                context.drawTextWithBackground(this.client.textRenderer, text, k, l, j, ColorHelper.Argb.withAlpha(i, -1));
            }
        }
    }

    @Override
    public void close(SpectatorMenu menu) {
        this.spectatorMenu = null;
        this.lastInteractionTime = 0L;
    }

    public boolean isOpen() {
        return this.spectatorMenu != null;
    }

    public void cycleSlot(int offset) {
        int i;
        for (i = this.spectatorMenu.getSelectedSlot() + offset; !(i < 0 || i > 8 || this.spectatorMenu.getCommand(i) != SpectatorMenu.BLANK_COMMAND && this.spectatorMenu.getCommand(i).isEnabled()); i += offset) {
        }
        if (i >= 0 && i <= 8) {
            this.spectatorMenu.useCommand(i);
            this.lastInteractionTime = Util.getMeasuringTimeMs();
        }
    }

    public void useSelectedCommand() {
        this.lastInteractionTime = Util.getMeasuringTimeMs();
        if (this.isOpen()) {
            int i = this.spectatorMenu.getSelectedSlot();
            if (i != -1) {
                this.spectatorMenu.useCommand(i);
            }
        } else {
            this.spectatorMenu = new SpectatorMenu(this);
        }
    }
}

