package me.catrix.api.utils.math;

public class FrameRateCounter {
    public static final FrameRateCounter INSTANCE = new FrameRateCounter();
    int fps = 5;

    public int getFps() {
        return fps;
    }
}
