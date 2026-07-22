package github.trollhack.utils.render.font;

import github.trollhack.utils.render.font.nanovg.NVGFont;

public final class FontRenderers {
    public static NVGFont ducksans;
    public static NVGFont icon;
    public static NVGFont juraLight;

    private FontRenderers() {}

    public static void init() {
        ducksans = new NVGFont("LexendDeca-Regular.ttf", 9f);
        icon = new NVGFont("icon.ttf", 16f);
        juraLight = new NVGFont("Jura-Light.ttf", 16f);
    }
}
