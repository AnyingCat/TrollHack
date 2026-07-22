package me.catrix.mod.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import me.catrix.Catrix;
import me.catrix.api.utils.math.Timer;
import me.catrix.api.utils.render.Render2DUtil;
import me.catrix.api.utils.render.RenderShadersUtil;
import me.catrix.core.impl.GuiManager;
import me.catrix.mod.gui.clickgui.ClickGuiScreen;
import me.catrix.mod.gui.clickgui.tabs.Tab;
import me.catrix.mod.gui.font.FontRenderers;
import me.catrix.mod.modules.impl.client.ClickGui;
import me.catrix.mod.modules.impl.client.HudEditor;
import me.catrix.mod.modules.impl.hud.SelfHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL40C;

import java.awt.*;

public class SelfHUD extends Tab {
    //    public static SelfHUD INSTANCE;
//    public int kills = 0;
    private final Timer sessionTimer = new Timer();

    public SelfHUD() {
        this.width = 137;
        this.height = 55;
        this.x = (int) Catrix.CONFIG.getFloat("self_hud_x", 1);
        this.y = (int) Catrix.CONFIG.getFloat("self_hud_y", 57);
    }

    @Override
    public void update(double mouseX, double mouseY) {
        if (GuiManager.currentGrabbed == null && SelfHud.INSTANCE.isOn() && HudEditor.INSTANCE.isOn()) {
            if (mouseX >= x && mouseX <= x + width) {
                if (mouseY >= y && mouseY <= y + height) {
                    if (ClickGuiScreen.clicked) {
                        GuiManager.currentGrabbed = this;
                    }
                }
            }
        }
    }

    @Override
    public void draw(DrawContext drawContext, float partialTicks, Color color) {
        if (ClickGui.INSTANCE.isOn() && !HudEditor.INSTANCE.isOn()) {
            return;
        }
        if (SelfHud.INSTANCE.isOn() && mc.player != null) {
            renderSelfHud(drawContext, mc.player);
        }
    }

//    @EventHandler
//    private void death(PacketEvent.Receive event) {
//        if(event.getPacket() instanceof EntityStatusS2CPacket pac && pac.getStatus() == 3){
//            if(!(pac.getEntity(mc.world) instanceof PlayerEntity)) return;
//           if(KillAura.target == pac.getEntity(mc.world) || AutoCrystal.INSTANCE.displayTarget == pac.getEntity(mc.world)){
//                kills++;
//            }
//        }
//    }

    private void renderSelfHud(DrawContext drawContext, AbstractClientPlayerEntity player) {
        RenderShadersUtil.drawRoundedBlur(drawContext.getMatrices(), x, y, width, height, 9f, new Color(0x35000000, true), 15.0f, 0.55f);
        RenderShadersUtil.drawRect2(drawContext.getMatrices(), x, y, width, height, 9f, new Color(0x4F000000, true),new Color(0x6E3A3A3A, true),0.73f);
//        if (HudEditor.INSTANCE.isOff() && ClickGui.INSTANCE.isOff()) {
//            RenderShadersUtil.drawBlurredShadow(drawContext.getMatrices(), x, y, width, height, 10, new Color(0x54000000, true));
//        }
        FontRenderers.icon.drawString(drawContext.getMatrices(), "5",
                x + 2.5f, y + 3,
                Color.WHITE.getRGB());
        FontRenderers.ui2.drawString(drawContext.getMatrices(), "Session Info:",
                x + 16, y + 6,
                Color.WHITE.getRGB());
        MatrixStack matrixStack = drawContext.getMatrices();
        matrixStack.push();
        RenderSystem.setShaderTexture(0, player.getSkinTextures().texture());
        RenderSystem.enableBlend();
        RenderSystem.colorMask(false, false, false, true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT, false);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        Render2DUtil.renderRoundedQuadInternal(matrixStack.peek().getPositionMatrix(), 1f, 1f, 1f, 1f, x + 3f, y + 17, x + 3f + 33f, y + 17 + 33f, 7, 20);
        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderShadersUtil.renderTexture(drawContext.getMatrices(), (int) (x + 3.5f), (int) (y + 17.5F), 35, 35, 8, 8, 8, 8, 64, 64);
        RenderShadersUtil.renderTexture(drawContext.getMatrices(), (int) (x + 3.5f), (int) (y + 17.5F), 35, 35, 40, 8, 8, 8, 64, 64);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.defaultBlendFunc();
        matrixStack.pop();
        FontRenderers.ui2.drawString(drawContext.getMatrices(), player.getName().getString(),
                x + 45, y + 20, Color.WHITE.getRGB());
//        FontRenderers.ui2.drawString(drawContext.getMatrices(), "kill: " + Formatting.WHITE + kills,
//                x + 45, y + 32,
//                Color.WHITE.getRGB());
        FontRenderers.ui2.drawString(drawContext.getMatrices(), "Pops: " + Catrix.POP.getPop(player.getName().getString()),
                x + 45, y + 32,
                Color.WHITE.getRGB());
        FontRenderers.ui2.drawString(drawContext.getMatrices(), "Played: " + formatTime(sessionTimer.getPassedTimeMs()),
                x + 45, y + 44,
                Color.GRAY.getRGB());
    }

    private String formatTime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        if (hours > 0) {
            return String.format("%dh %02dm %02ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %02ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
}