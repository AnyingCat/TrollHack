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

public class RectShader2 implements Wrapper {
    private Uniform2f uSize;
    private Uniform2f uLocation;
    private Uniform1f radius;
    private Uniform4f color;
    private Uniform4f color2;
    private Uniform1f colorSplit;

    public static final ManagedCoreShader RECT = ShaderEffectManager.getInstance()
            .manageCoreShader(Identifier.of("minecraft", "rect2"), VertexFormats.POSITION);

    public RectShader2() {
        setup();
    }

    public void setParameters(float x, float y, float width, float height, float r, Color topColor, Color bottomColor, float splitPosition) {
        float i = (float) mc.getWindow().getScaleFactor();
        radius.set(r * i);
        uLocation.set(x * i, -y * i + mc.getWindow().getScaledHeight() * i - height * i);
        uSize.set(width * i, height * i);
        color.set(topColor.getRed() / 255f, topColor.getGreen() / 255f, topColor.getBlue() / 255f, topColor.getAlpha() / 255f);
        color2.set(bottomColor.getRed() / 255f, bottomColor.getGreen() / 255f, bottomColor.getBlue() / 255f, bottomColor.getAlpha() / 255f);
        colorSplit.set(splitPosition);
    }

    public void use() {
        RenderSystem.setShader(RECT::getProgram);
    }

    protected void setup() {
        this.uSize = RECT.findUniform2f("uSize");
        this.uLocation = RECT.findUniform2f("uLocation");
        this.radius = RECT.findUniform1f("radius");
        this.color = RECT.findUniform4f("color");
        this.color2 = RECT.findUniform4f("color2");
        this.colorSplit = RECT.findUniform1f("colorSplit");
    }
}