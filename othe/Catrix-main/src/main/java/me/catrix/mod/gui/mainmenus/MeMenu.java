package me.catrix.mod.gui.mainmenus;

import com.mojang.blaze3d.systems.RenderSystem;
import me.catrix.api.utils.Wrapper;
import me.catrix.api.utils.render.RenderShadersUtil;
import me.catrix.mod.gui.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MeMenu extends Screen implements Wrapper {
    public MeMenu() {
        super(Text.translatable("narrator.screen.title"));
        loadAnimationFrames();
    }

    private final List<Identifier> animationFrames = new ArrayList<>();
    private int currentFrame = 0;
    private long lastFrameTime = 0;

    private void loadAnimationFrames() {
        animationFrames.clear();
        for (int i = 1; i <= 50; i++) {
            Identifier frame = new Identifier("textures/bg/ezgif-frame-" + String.format("%03d", i) + ".png");
            animationFrames.add(frame);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (System.currentTimeMillis() - lastFrameTime >= 100) {
            currentFrame = (currentFrame + 1) % 50;
            lastFrameTime = System.currentTimeMillis();
        }
        if (!animationFrames.isEmpty()) {
            Identifier currentFrameTexture = animationFrames.get(currentFrame);
            context.drawTexture(currentFrameTexture, 0, 0, 0, 0, this.width, this.height, this.width, this.height);
        }
        context.drawTextWithShadow(this.textRenderer, "免费社区版，倒卖死全家，官方群:1072262390", 2, height - 20, new Color(0xFFFFFF).getRGB());
        RenderShadersUtil.drawRect(context.getMatrices(), (float) (this.width / 2) - 190.0f / 2, Math.max((float) (this.height / 2) - 20.0f, Math.min(height / 4, height / 2 - 80) + 110), 190.0f, 20.0f, 3f, new Color(0x5F050505, true));
        RenderShadersUtil.drawBlurredShadow(context.getMatrices(), (float) (this.width / 2) - 190.0f / 2, Math.max((float) (this.height / 2) - 20.0f, Math.min(height / 4, height / 2 - 80) + 110), 190.0f, 20.0f, 10, new Color(0x47000000, true));
        FontRenderers.ui.drawString(context.getMatrices(), "Singleplayer",
                (float) (this.width / 2) - 190.0f / 2 + (190.0f - FontRenderers.ui.getWidth("Singleplayer")) / 2f,
                Math.max((float) (this.height / 2) - 20.0f, Math.min(height / 4, height / 2 - 80) + 110) + (25.0f - FontRenderers.ui.getFontHeight()) / 2f,
                new Color(0xFFFFFF).getRGB());

        RenderShadersUtil.drawRect(context.getMatrices(), (float) (this.width / 2) - 190.0f / 2, Math.max((float) (this.height / 2) - 20.0f, Math.min(height / 4, height / 2 - 80) + 110) + 25.0f, 190.0f, 20.0f, 3f, new Color(0x5F050505, true));
        RenderShadersUtil.drawBlurredShadow(context.getMatrices(), (float) (this.width / 2) - 190.0f / 2, Math.max((float) (this.height / 2) - 20.0f, Math.min(height / 4, height / 2 - 80) + 110) + 25.0f, 190.0f, 20.0f, 10, new Color(0x47000000, true));
        FontRenderers.ui.drawString(context.getMatrices(), "Multiplayer",
                (float) (this.width / 2) - 190.0f / 2 + (190.0f - FontRenderers.ui.getWidth("Multiplayer")) / 2f,
                Math.max((float) (this.height / 2) - 20.0f, Math.min(height / 4, height / 2 - 80) + 110) + 25.0f + (25.0f - FontRenderers.ui.getFontHeight()) / 2f,
                new Color(0xFFFFFF).getRGB());

        RenderShadersUtil.drawRect(context.getMatrices(), (float) (this.width / 2) - 190.0f / 2, Math.max((float) (this.height / 2) - 20.0f, Math.min(height / 4, height / 2 - 80) + 110) + 50.0f, 93.0f, 20.0f, 3f, new Color(0x5F050505, true));
        RenderShadersUtil.drawBlurredShadow(context.getMatrices(), (float) (this.width / 2) - 190.0f / 2, Math.max((float) (this.height / 2) - 20.0f, Math.min(height / 4, height / 2 - 80) + 110) + 50.0f, 93.0f, 20.0f, 10, new Color(0x47000000, true));
        FontRenderers.ui.drawString(context.getMatrices(), "Options",
                (float) (this.width / 2) - 190.0f / 2 + (93.0f - FontRenderers.ui.getWidth("Options")) / 2f,
                Math.max((float) (this.height / 2) - 20.0f, Math.min(height / 4, height / 2 - 80) + 110) + 50.0f + (25.0f - FontRenderers.ui.getFontHeight()) / 2f,
                new Color(0xFFFFFF).getRGB());

        RenderShadersUtil.drawRect(context.getMatrices(), (float) (this.width / 2) - 190.0f / 2 + 93.0f + 4.0f, Math.max((float) (this.height / 2) - 20.0f, Math.min(height / 4, height / 2 - 80) + 110) + 50.0f, 93.0f, 20.0f, 3f, new Color(0x5F050505, true));
        RenderShadersUtil.drawBlurredShadow(context.getMatrices(), (float) (this.width / 2) - 190.0f / 2 + 93.0f + 4.0f, Math.max((float) (this.height / 2) - 20.0f, Math.min(height / 4, height / 2 - 80) + 110) + 50.0f, 93.0f, 20.0f, 10, new Color(0x47000000, true));
        FontRenderers.ui.drawString(context.getMatrices(), "Exit",
                (float) (this.width / 2) - 190.0f / 2 + 93.0f + 4.0f + (93.0f - FontRenderers.ui.getWidth("Exit")) / 2f,
                Math.max((float) (this.height / 2) - 20.0f, Math.min(height / 4, height / 2 - 80) + 110) + 50.0f + (25.0f - FontRenderers.ui.getFontHeight()) / 2f,
                new Color(0xFFFFFF).getRGB());

        RenderSystem.disableBlend();
    }

    public boolean isMouseHoveringRect(double x, double y, double w, double h, double mouseX, double mouseY) {
        return mouseX >= x && mouseY >= y && mouseX <= x + w && mouseY <= y + h;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

        if (isMouseHoveringRect((float) (this.width / 2) - 190.0f / 2, Math.max((float) (this.height / 2) - 20.0f, Math.min(height / 4, height / 2 - 80) + 110), 190.0f, 20.0f, mouseX, mouseY)) {
            if (client != null) {
                client.setScreen(new SelectWorldScreen(this));
            }
            return true;
        }
        if (isMouseHoveringRect((float) (this.width / 2) - 190.0f / 2, Math.max((float) (this.height / 2) - 20.0f, Math.min(height / 4, height / 2 - 80) + 110) + 25.0f, 190.0f, 20.0f, mouseX, mouseY)) {
            if (!mc.options.skipMultiplayerWarning) {
                mc.options.skipMultiplayerWarning = true;
                mc.options.write();
            }
            if (client != null) {
                client.setScreen(new MultiplayerScreen(this));
            }
            return true;
        }
        if (isMouseHoveringRect((float) (this.width / 2) - 190.0f / 2, Math.max((float) (this.height / 2) - 20.0f, Math.min(height / 4, height / 2 - 80) + 110) + 50.0f, 93.0f, 20.0f, mouseX, mouseY)) {
            if (client != null) {
                client.setScreen(new OptionsScreen(this, mc.options));
            }
            return true;
        }
        if (isMouseHoveringRect((float) (this.width / 2) - 190.0f / 2 + 93.0f + 4.0f, Math.max((float) (this.height / 2) - 20.0f, Math.min(height / 4, height / 2 - 80) + 110) + 50.0f, 93.0f, 20.0f, mouseX, mouseY)) {
            mc.stop();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}