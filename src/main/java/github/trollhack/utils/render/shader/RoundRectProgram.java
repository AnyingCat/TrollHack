package github.trollhack.utils.render.shader;

import github.trollhack.utils.interfaces.Mc;
import github.trollhack.utils.render.shader.satin.api.managed.ManagedCoreShader;
import github.trollhack.utils.render.shader.satin.api.managed.ShaderEffectManager;
import com.mojang.blaze3d.systems.RenderSystem;
import github.trollhack.utils.render.shader.satin.api.managed.uniform.Uniform2f;
import github.trollhack.utils.render.shader.satin.api.managed.uniform.Uniform4f;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import java.awt.*;

public class RoundRectProgram implements Mc {
    private Uniform2f uSize;
    private Uniform2f uLocation;
    private Uniform4f radii;
    private Uniform4f color;
    private Uniform2f inputResolution;

    public static final ManagedCoreShader ROUND_RECT = ShaderEffectManager.getInstance()
            .manageCoreShader(Identifier.of("troll", "roundrect"), VertexFormats.POSITION);

    public RoundRectProgram() {
        setup();
    }

    public void setParameters(float x, float y, float width, float height, float tl, float tr, float br, float bl, Color c) {
        float i = (float) mc.getWindow().getScaleFactor();
        radii.set(bl * i, br * i, tr * i, tl * i);
        uLocation.set(x * i, -y * i + mc.getWindow().getScaledHeight() * i - height * i);
        uSize.set(width * i, height * i);
        color.set(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f);
    }

    public void use() {
        var buffer = MinecraftClient.getInstance().getFramebuffer();
        inputResolution.set((float) buffer.textureWidth, (float) buffer.textureHeight);
        RenderSystem.setShader(ROUND_RECT::getProgram);
    }

    protected void setup() {
        this.inputResolution = ROUND_RECT.findUniform2f("InputResolution");
        this.uSize = ROUND_RECT.findUniform2f("uSize");
        this.uLocation = ROUND_RECT.findUniform2f("uLocation");
        this.radii = ROUND_RECT.findUniform4f("radii");
        this.color = ROUND_RECT.findUniform4f("color");
    }
}
