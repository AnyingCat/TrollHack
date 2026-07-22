/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.ShaderStage;

/**
 * A superinterface of {@link ShaderProgram} that exposes methods necessary
 * for linking or deleting this shader program.
 */
@Environment(value=EnvType.CLIENT)
public interface ShaderProgramSetupView {
    public int getGlRef();

    public void markUniformsDirty();

    public ShaderStage getVertexShader();

    public ShaderStage getFragmentShader();

    public void attachReferencedShaders();
}

