package github.trollhack.mixins;

import github.trollhack.utils.render.shader.satin.impl.SamplerAccess;
import net.minecraft.client.gl.ShaderProgram;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(ShaderProgram.class)
public abstract class MixinShaderProgram implements SamplerAccess {
    @Shadow @Final private Map<String, Object> samplers;

    @Override
    public boolean hasSampler(String name) {
        return this.samplers.containsKey(name);
    }

    @Override
    @Accessor("samplerNames")
    public abstract List<String> getSamplerNames();

    @Override
    @Accessor("loadedSamplerIds")
    public abstract List<Integer> getSamplerShaderLocs();
}
