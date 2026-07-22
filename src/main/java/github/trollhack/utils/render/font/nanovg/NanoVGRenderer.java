package github.trollhack.utils.render.font.nanovg;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

import static org.lwjgl.nanovg.NanoVG.*;

public enum NanoVGRenderer {
    INSTANCE;

    private static final int NVG_FLAGS = NanoVGGL3.NVG_ANTIALIAS | NanoVGGL3.NVG_STENCIL_STROKES;

    private long context = 0L;
    private boolean ready = false;
    private boolean frameActive = false;

    public synchronized void initNanoVG() {
        if (ready) return;
        context = NanoVGGL3.nvgCreate(NVG_FLAGS);
        if (context == 0L) {
            throw new RuntimeException("NanoVG initialization failed");
        }
        ready = true;
    }

    public long getContext() {
        if (!ready) initNanoVG();
        return context;
    }

    public void draw(Consumer<Long> renderTask) {
        if (!ready) initNanoVG();
        if (frameActive) {
            renderTask.accept(context);
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        GLState.INSTANCE.push();
        frameActive = true;

        float scale = (float) mc.getWindow().getScaleFactor();
        int fbWidth = mc.getWindow().getFramebufferWidth();
        int fbHeight = mc.getWindow().getFramebufferHeight();

        nvgBeginFrame(context, fbWidth / scale, fbHeight / scale, scale);
        renderTask.accept(context);
        nvgEndFrame(context);

        GLState.INSTANCE.pop();
        GL11.glViewport(0, 0, fbWidth, fbHeight);

        frameActive = false;
    }

    public boolean isReady() { return ready; }
    public boolean isFrameActive() { return frameActive; }

    public synchronized void destroy() {
        if (ready && context != 0L) {
            NanoVGGL3.nvgDelete(context);
            context = 0L;
            ready = false;
        }
    }
}
