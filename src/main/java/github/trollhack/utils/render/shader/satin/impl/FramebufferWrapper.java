package github.trollhack.utils.render.shader.satin.impl;

import github.trollhack.utils.render.shader.satin.api.managed.ManagedFramebuffer;
import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.util.Window;

public final class FramebufferWrapper implements ManagedFramebuffer {
    private final String name;
    private Framebuffer wrapped;

    FramebufferWrapper(String name) {
        this.name = name;
    }

    void findTarget(PostEffectProcessor shaderEffect) {
        if (shaderEffect == null) {
            this.wrapped = null;
        } else {
            this.wrapped = shaderEffect.getSecondaryTarget(this.name);
            if (this.wrapped == null) {
                LogUtils.getLogger().warn("No target framebuffer found with name {} in shader {}", this.name, shaderEffect.getName());
            }
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public Framebuffer getFramebuffer() {
        return wrapped;
    }

    @Override
    public void beginWrite(boolean updateViewport) {
        if (this.wrapped != null) {
            this.wrapped.beginWrite(updateViewport);
        }
    }

    @Override
    public void draw() {
        Window window = MinecraftClient.getInstance().getWindow();
        this.draw(window.getFramebufferWidth(), window.getFramebufferHeight(), true);
    }

    @Override
    public void draw(int width, int height, boolean disableBlend) {
        if (this.wrapped != null) {
            this.wrapped.draw(width, height, disableBlend);
        }
    }

    @Override
    public void clear() {
        clear(MinecraftClient.IS_SYSTEM_MAC);
    }

    @Override
    public void clear(boolean swallowErrors) {
        if (this.wrapped != null) {
            this.wrapped.clear(swallowErrors);
        }
    }
}
