package github.trollhack.utils.render.shader.satin.impl;

import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.ShaderProgram;

import java.util.List;

public abstract class ManagedUniformBase {
    protected final String name;

    public ManagedUniformBase(String name) {
        this.name = name;
    }

    public abstract boolean findUniformTargets(List<PostEffectPass> shaders);

    public abstract boolean findUniformTarget(ShaderProgram shader);

    public String getName() {
        return name;
    }
}
