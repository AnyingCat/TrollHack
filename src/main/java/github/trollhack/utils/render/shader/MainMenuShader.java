package github.trollhack.utils.render.shader;

import github.trollhack.utils.animation.FrameRateCounter;
import github.trollhack.utils.interfaces.Mc;
import github.trollhack.utils.render.shader.satin.api.managed.ManagedCoreShader;
import github.trollhack.utils.render.shader.satin.api.managed.ShaderEffectManager;
import github.trollhack.utils.render.shader.satin.api.managed.uniform.Uniform1f;
import github.trollhack.utils.render.shader.satin.api.managed.uniform.Uniform2f;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class MainMenuShader implements Mc {
    private Uniform1f Time;
    private Uniform2f uSize;
    public static float time_ = 10000f;

    public static final ManagedCoreShader MAIN_MENU = ShaderEffectManager.getInstance()
            .manageCoreShader(Identifier.of("troll", "menu"), VertexFormats.POSITION);

    public MainMenuShader() {
        setup();
    }

    public void setParameters(float x, float y, float width, float height) {
        float i = (float) mc.getWindow().getScaleFactor();
        this.uSize.set(width * i, height * i);

        time_ += (float) (0.55 * deltaTime());
        this.Time.set(time_);
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
    }
}
