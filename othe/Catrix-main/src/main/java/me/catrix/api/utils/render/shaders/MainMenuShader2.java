package me.catrix.api.utils.render.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.satin.api.managed.ManagedCoreShader;
import ladysnake.satin.api.managed.ShaderEffectManager;
import ladysnake.satin.api.managed.uniform.Uniform1f;
import ladysnake.satin.api.managed.uniform.Uniform2f;
import ladysnake.satin.api.managed.uniform.Uniform4f;
import me.catrix.api.utils.Wrapper;
import me.catrix.api.utils.math.FrameRateCounter;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class MainMenuShader2 implements Wrapper {
    private Uniform1f Time;
    private Uniform2f uSize;
    private Uniform2f resolution;
    private Uniform2f mouse;
    private Uniform4f color;
    public static float time_ = 10000f;

    public static final ManagedCoreShader MAIN_MENU = ShaderEffectManager.getInstance()
            .manageCoreShader(Identifier.of("minecraft", "menu2"), VertexFormats.POSITION);

    public MainMenuShader2() {
        setup();
    }

    public void setParameters(float x, float y, float width, float height) {
        float i = (float) mc.getWindow().getScaleFactor();
        this.uSize.set(width * i, height * i);
        this.resolution.set(width * i, height * i);

        if (mc.mouse != null) {
            double mouseX = mc.mouse.getX();
            double mouseY = mc.mouse.getY();
            this.mouse.set((float) mouseX, (float) mouseY);
        } else {
            this.mouse.set(width / 2f, height / 2f);
        }

        time_ += (float) (0.55 * deltaTime());
        this.Time.set(time_);
        // this.color.set(HudEditor.getColor(0).getRed() / 255f, HudEditor.getColor(0).getGreen() / 255f, HudEditor.getColor(0).getBlue() / 255f, HudEditor.getColor(0).getAlpha() / 255f);
    }

    public static float deltaTime() {
        return FrameRateCounter.INSTANCE.getFps() > 5 ? (1f / FrameRateCounter.INSTANCE.getFps()) : 0.016f;
    }

    public void use() {
        RenderSystem.setShader(MAIN_MENU::getProgram);
    }

    protected void setup() {
        uSize = MAIN_MENU.findUniform2f("uSize");
        Time = MAIN_MENU.findUniform1f("Time");
        resolution = MAIN_MENU.findUniform2f("resolution");
        mouse = MAIN_MENU.findUniform2f("mouse");
        color = MAIN_MENU.findUniform4f("color");
    }
}