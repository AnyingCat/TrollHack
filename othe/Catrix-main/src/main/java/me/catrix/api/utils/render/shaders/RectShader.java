package me.catrix.api.utils.render.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.satin.api.managed.ManagedCoreShader;
import ladysnake.satin.api.managed.ShaderEffectManager;
import ladysnake.satin.api.managed.uniform.Uniform1f;
import ladysnake.satin.api.managed.uniform.Uniform2f;
import ladysnake.satin.api.managed.uniform.Uniform4f;
import me.catrix.api.utils.Wrapper;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

import java.awt.*;

public class RectShader implements Wrapper {
    private Uniform2f uSize;
    private Uniform2f uLocation;
    private Uniform1f radius;
    private Uniform4f color;

    public static final ManagedCoreShader RECT = ShaderEffectManager.getInstance()
            .manageCoreShader(Identifier.of("minecraft", "rect"), VertexFormats.POSITION);

    public RectShader() {
        setup();
    }

    public void setParameters(float x, float y, float width, float height, float r, Color color1) {
        float i = (float) mc.getWindow().getScaleFactor();
        radius.set(r * i);
        uLocation.set(x * i, -y * i + mc.getWindow().getScaledHeight() * i - height * i);
        uSize.set(width * i, height * i);
        color.set(color1.getRed() / 255f, color1.getGreen() / 255f, color1.getBlue() / 255f, color1.getAlpha() / 255f);
    }

    public void use() {
        RenderSystem.setShader(RECT::getProgram);
    }

    protected void setup() {
        this.uSize = RECT.findUniform2f("uSize");
        this.uLocation = RECT.findUniform2f("uLocation");
        this.radius = RECT.findUniform1f("radius");
        this.color = RECT.findUniform4f("color");
    }
}