package me.catrix.mod.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import me.catrix.Catrix;
import me.catrix.api.utils.math.EaseOutCirc;
import me.catrix.api.utils.render.Render2DUtil;
import me.catrix.api.utils.render.RenderShadersUtil;
import me.catrix.api.utils.render.TextUtil;
import me.catrix.core.impl.GuiManager;
import me.catrix.mod.gui.clickgui.ClickGuiScreen;
import me.catrix.mod.gui.clickgui.tabs.Tab;
import me.catrix.mod.gui.font.FontRenderers;
import me.catrix.mod.modules.impl.client.ClickGui;
import me.catrix.mod.modules.impl.client.HudEditor;
import me.catrix.mod.modules.impl.combat.AutoAnchor;
import me.catrix.mod.modules.impl.combat.AutoCrystal;
import me.catrix.mod.modules.impl.combat.KillAura;
import me.catrix.mod.modules.impl.hud.TargetHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL40C;

import java.awt.*;

public class TargetHUD extends Tab {
    private PlayerEntity lastTarget = null;
    public final EaseOutCirc healthAnimation = new EaseOutCirc();
    public static TargetHUD INSTANCE;
    public TargetHUD() {
        this.width = 137;
        this.height = 48;
        this.x = (int) Catrix.CONFIG.getFloat("target_x", 0);
        this.y = (int) Catrix.CONFIG.getFloat("target_y", 250);
        INSTANCE = this;
    }

    @Override
    public void update(double mouseX, double mouseY) {
        if (GuiManager.currentGrabbed == null && TargetHud.INSTANCE.isOn() && HudEditor.INSTANCE.isOn()) {
            if (mouseX >= x && mouseX <= x + width) {
                if (mouseY >= y && mouseY <= y + height) {
                    if (ClickGuiScreen.clicked) {
                        GuiManager.currentGrabbed = this;
                    }
                }
            }
        }
        PlayerEntity target = getTarget();
        if (target != lastTarget) {
            lastTarget = target;
        }
    }

    @Override
    public void draw(DrawContext drawContext, float partialTicks, Color color) {
        if (TargetHud.INSTANCE.isOn()) {
//            if (HudEditor.INSTANCE.isOn()) {
//                Render2DUtil.drawRect(matrixStack, x, y, width, height, new Color(0, 0, 0, 70));
//            }
            PlayerEntity target = getTarget();
            if (target != null) {
                if (TargetHud.INSTANCE.follow.getValue()) {
                    updateFollowTarget(target);
                }
                renderTargetHud(drawContext, target);
            }
        }
    }

    private void updateFollowTarget(PlayerEntity target) {
        double x = target.prevX + (target.getX() - target.prevX) * mc.getTickDelta();
        double y = target.prevY + (target.getY() - target.prevY) * mc.getTickDelta() + target.getStandingEyeHeight();
        double z = target.prevZ + (target.getZ() - target.prevZ) * mc.getTickDelta();
        Vec3d worldPos = new Vec3d(x, y, z);
        Vec3d screenPos = TextUtil.worldSpaceToScreenSpace(worldPos);
        if (screenPos.z > 0 && screenPos.z < 1) {
            this.x = (int) screenPos.x + 15;
            this.y = (int) screenPos.y - this.height / 2;
        }
    }

    private PlayerEntity getTarget() {
        PlayerEntity target = null;
        if (AutoCrystal.INSTANCE.isOn() && AutoCrystal.INSTANCE.displayTarget != null &&
                !AutoCrystal.INSTANCE.displayTarget.isDead() && AutoCrystal.INSTANCE.displayTarget.getWorld() != null &&
                !AutoCrystal.INSTANCE.displayTarget.isRemoved()) {
            target = AutoCrystal.INSTANCE.displayTarget;
        } else if (KillAura.INSTANCE.isOn() && KillAura.target instanceof PlayerEntity killAuraTarget) {
            if (!killAuraTarget.isDead() && killAuraTarget.getWorld() != null && !killAuraTarget.isRemoved()) {
                target = killAuraTarget;
            }
        } else if (mc.currentScreen instanceof ChatScreen || HudEditor.INSTANCE.isOn()) {
            target = mc.player;
        }
        if (target == null && AutoAnchor.INSTANCE.isOn() && AutoAnchor.INSTANCE.displayTarget != null &&
                !AutoAnchor.INSTANCE.displayTarget.isDead() && AutoAnchor.INSTANCE.displayTarget.getWorld() != null &&
                !AutoAnchor.INSTANCE.displayTarget.isRemoved()) {
            target = AutoAnchor.INSTANCE.displayTarget;
        }
        if (target != null && mc.player != null) {
            double distance = mc.player.distanceTo(target);
            if (distance > 12.0) {
                target = null;
            }
        }
        return target;
    }

    private void renderTargetHud(DrawContext drawContext, PlayerEntity target) {
        RenderShadersUtil.drawRoundedBlur(drawContext.getMatrices(), x, y, width, height, 9f, new Color(0x35000000,true),15.0f,0.55f);
        if (HudEditor.INSTANCE.isOff() && ClickGui.INSTANCE.isOff())  {
            RenderShadersUtil.drawBlurredShadow(drawContext.getMatrices(), x, y, width, height, 10, new Color(0x54000000, true));
        }
        float hurtPercent = (interpolateFloat(Math.max(target.hurtTime == 0 ? 0 : target.hurtTime + 1, 0), target.hurtTime, mc.getTickDelta())) / 8f;
        healthAnimation.setValue(target.getHealth() + target.getAbsorptionAmount());
        if (target instanceof AbstractClientPlayerEntity) {
            MatrixStack matrixStack = drawContext.getMatrices();
            matrixStack.push();
            matrixStack.translate(x + 3.5f + 20, y + 3.5f + 20, 0);
            matrixStack.scale(1 - hurtPercent / 15f, 1 - hurtPercent / 15f, 1f);
            matrixStack.translate(-(x + 3.5f + 20), -(y + 3.5f + 20), 0);
            RenderSystem.setShaderTexture(0, ((AbstractClientPlayerEntity) target).getSkinTextures().texture());
            RenderSystem.enableBlend();
            RenderSystem.colorMask(false, false, false, true);
            RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
            RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT, false);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            Render2DUtil.renderRoundedQuadInternal(matrixStack.peek().getPositionMatrix(), 1f,1f,1f,1f, x + 3.0f, y + 3.0f, x + 3.0f + 39, y + 3.5f + 39, 7, 10);
            RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderColor(1f, 1f - hurtPercent / 2, 1f - hurtPercent / 2, 1f);
            RenderShadersUtil.renderTexture(drawContext.getMatrices(), (int) (x + 3.5f), (int) (y + 3.5f), 40, 40, 8, 8, 8, 8, 64, 64);
            RenderShadersUtil.renderTexture(drawContext.getMatrices(), (int) (x + 3.5f), (int) (y + 3.5f), 40, 40, 40, 8, 8, 8, 64, 64);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.defaultBlendFunc();
            matrixStack.pop();
        }
        RenderShadersUtil.drawRect(drawContext.getMatrices(),
                x + 48, y + 32,
                85f, 11f, 4f,
                new Color(0x4D000000, true));
        RenderShadersUtil.drawBlurredShadow(drawContext.getMatrices(),
                x + 48 - 2, y + 32 - 2,
                85f + 4, 11f + 4,
                10,
                new Color(0x4F000000, true));
        float healthRatio = Math.max(0f, Math.min((float) healthAnimation.getAnimationD() / target.getMaxHealth(), 1f));
        RenderShadersUtil.drawRect(drawContext.getMatrices(),
                x + 48, y + 32,
                Math.max(8f, 85f * healthRatio), 11f, 4f,
                new Color(0xAD840EFF, true));
        FontRenderers.ui.drawString(drawContext.getMatrices(), target.getName().getString(),
                x + 48, y + 7,
                Color.WHITE.getRGB());
//        FontRenderers.ui.drawString(drawContext.getMatrices(), "Pops: " + Catrix.POP.getPop(target.getName().getString()),
//                x + 50, y + 20.5f,
//                Color.WHITE.getRGB());
        FontRenderers.ui.drawCenteredString(drawContext.getMatrices(), String.format("%.1f", (float)healthAnimation.getAnimationD()),
                x + 48 + 85f / 2, y + 32.5f,
                Color.WHITE.getRGB());
    }

    private float interpolateFloat(float current, float last, float partialTicks) {
        return last + (current - last) * partialTicks;
    }
}