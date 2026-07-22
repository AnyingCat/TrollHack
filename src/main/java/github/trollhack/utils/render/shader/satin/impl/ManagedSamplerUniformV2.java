package github.trollhack.utils.render.shader.satin.impl;

import github.trollhack.utils.render.shader.satin.api.managed.uniform.SamplerUniformV2;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.JsonEffectShaderProgram;
import net.minecraft.client.texture.AbstractTexture;

import java.util.function.IntSupplier;

public final class ManagedSamplerUniformV2 extends ManagedSamplerUniformBase implements SamplerUniformV2 {
    public ManagedSamplerUniformV2(String name) {
        super(name);
    }

    @Override
    public void set(AbstractTexture texture) {
        set(texture::getGlId);
    }

    @Override
    public void set(Framebuffer textureFbo) {
        set(textureFbo::getColorAttachment);
    }

    @Override
    public void set(int textureName) {
        set(() -> textureName);
    }

    @Override
    protected void set(Object value) {
        this.set((IntSupplier) value);
    }

    @Override
    public void set(IntSupplier value) {
        SamplerAccess[] targets = this.targets;
        if (targets.length > 0 && this.cachedValue != value) {
            for (SamplerAccess target : targets) {
                ((JsonEffectShaderProgram) target).bindSampler(this.name, value);
            }
            this.cachedValue = value;
        }
    }
}
