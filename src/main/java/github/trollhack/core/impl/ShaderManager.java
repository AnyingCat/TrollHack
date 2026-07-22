package github.trollhack.core.impl;

import github.trollhack.modules.impl.render.ESP;
import github.trollhack.utils.interfaces.IShaderEffect;
import github.trollhack.utils.interfaces.Mc;
import github.trollhack.utils.render.Render3DUtil;
import github.trollhack.utils.render.shader.satin.api.managed.ManagedShaderEffect;
import github.trollhack.utils.render.shader.satin.api.managed.ShaderEffectManager;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL30C;

import java.util.ArrayList;
import java.util.List;

public class ShaderManager implements Mc {
    private static final List<RenderTask> tasks = new ArrayList<>();
    private trollFramebuffer shaderBuffer;

    public float time = 0;

    public static ManagedShaderEffect DEFAULT_OUTLINE;
    public static ManagedShaderEffect DEFAULT;

    public void renderShader(Runnable runnable, Shader mode) {
        tasks.add(new RenderTask(runnable, mode));
    }

    public void renderShaders() {
        ensureInitialized();
        if (shaderBuffer == null) return;

        tasks.forEach(t -> applyShader(t.task(), t.shader()));
        tasks.clear();
    }

    public void applyShader(Runnable runnable, Shader mode) {
        Framebuffer mcBuffer = MinecraftClient.getInstance().getFramebuffer();
        RenderSystem.assertOnRenderThreadOrInit();
        if (shaderBuffer.textureWidth != mcBuffer.textureWidth || shaderBuffer.textureHeight != mcBuffer.textureHeight)
            shaderBuffer.resize(mcBuffer.textureWidth, mcBuffer.textureHeight, false);
        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, shaderBuffer.fbo);
        shaderBuffer.beginWrite(true);
        runnable.run();
        shaderBuffer.endWrite();
        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, mcBuffer.fbo);
        mcBuffer.beginWrite(false);
        ManagedShaderEffect shader = getShader(mode);
        PostEffectProcessor effect = shader.getShaderEffect();

        if (effect != null)
            ((IShaderEffect) effect).addFakeTargetHook("bufIn", shaderBuffer);

        Framebuffer outBuffer = shader.getShaderEffect().getSecondaryTarget("bufOut");
        setupShader(shader);
        shaderBuffer.clear(false);
        mcBuffer.beginWrite(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
        RenderSystem.backupProjectionMatrix();
        outBuffer.draw(outBuffer.textureWidth, outBuffer.textureHeight, false);
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void ensureInitialized() {
        if (DEFAULT != null) return;
        shaderBuffer = new trollFramebuffer(mc.getFramebuffer().textureWidth, mc.getFramebuffer().textureHeight);
        reloadShaders();
    }

    public ManagedShaderEffect getShader(@NotNull Shader mode) {
        return DEFAULT;
    }

    public ManagedShaderEffect getShaderOutline(@NotNull Shader mode) {
        return DEFAULT_OUTLINE;
    }

    public void setupShader(ManagedShaderEffect effect) {
        ESP esp = ESP.INSTANCE;
        effect.setUniformValue("RenderMode", esp.renderMode.getValue().getValue());
        effect.setUniformValue("FillOpacity", esp.fillAlpha.getValue() / 255f);
        effect.setUniformValue("color", esp.outlineColor.getValue().getRed() / 255f, esp.outlineColor.getValue().getGreen() / 255f, esp.outlineColor.getValue().getBlue() / 255f);
        effect.setUniformValue("BlurRadius", esp.blurRadius.getValue());
        effect.setUniformValue("Thickness", esp.thickness.getValue());
        effect.setUniformValue("resolution", (float) mc.getFramebuffer().textureWidth, (float) mc.getFramebuffer().textureHeight);
        effect.render(Render3DUtil.getTickDelta());
    }

    public void reloadShaders() {
        DEFAULT = ShaderEffectManager.getInstance().manage(Identifier.of("troll", "shaders/post/outline.json"));

        DEFAULT_OUTLINE = ShaderEffectManager.getInstance().manage(Identifier.of("troll", "shaders/post/outline.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) return;
            ((IShaderEffect) effect).addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect) effect).addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
        });
    }

    public static class trollFramebuffer extends Framebuffer {
        public trollFramebuffer(int width, int height) {
            super(false);
            RenderSystem.assertOnRenderThreadOrInit();
            resize(width, height, true);
            setClearColor(0f, 0f, 0f, 0f);
        }
    }

    public boolean fullNullCheck() {
        if (DEFAULT != null) return false;
        ensureInitialized();
        return true;
    }

    public record RenderTask(Runnable task, Shader shader) {
    }

    public enum Shader {
        Default
    }
}
