package github.trollhack.utils.render;

import java.awt.Color;

public class ColorUtil {

    public static Color mix(Color a, Color b, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        return new Color(
            Math.max(0, Math.min(255, (int) (a.getRed() + (b.getRed() - a.getRed()) * t))),
            Math.max(0, Math.min(255, (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t))),
            Math.max(0, Math.min(255, (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t))),
            Math.max(0, Math.min(255, (int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t)))
        );
    }
}
