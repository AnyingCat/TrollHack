/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math;

import net.minecraft.util.math.MathHelper;

/**
 * Contains color-related helper methods.
 */
public class ColorHelper {
    public static int channelFromFloat(float value) {
        return MathHelper.floor(value * 255.0f);
    }

    public static class Abgr {
        public static int getAlpha(int abgr) {
            return abgr >>> 24;
        }

        public static int getRed(int abgr) {
            return abgr & 0xFF;
        }

        public static int getGreen(int abgr) {
            return abgr >> 8 & 0xFF;
        }

        public static int getBlue(int abgr) {
            return abgr >> 16 & 0xFF;
        }

        public static int getBgr(int abgr) {
            return abgr & 0xFFFFFF;
        }

        public static int toOpaque(int abgr) {
            return abgr | 0xFF000000;
        }

        public static int getAbgr(int a, int b, int g, int r) {
            return a << 24 | b << 16 | g << 8 | r;
        }

        public static int withAlpha(int alpha, int bgr) {
            return alpha << 24 | bgr & 0xFFFFFF;
        }

        public static int toAbgr(int argb) {
            return argb & 0xFF00FF00 | (argb & 0xFF0000) >> 16 | (argb & 0xFF) << 16;
        }
    }

    public static class Argb {
        public static int getAlpha(int argb) {
            return argb >>> 24;
        }

        public static int getRed(int argb) {
            return argb >> 16 & 0xFF;
        }

        public static int getGreen(int argb) {
            return argb >> 8 & 0xFF;
        }

        public static int getBlue(int argb) {
            return argb & 0xFF;
        }

        public static int getArgb(int alpha, int red, int green, int blue) {
            return alpha << 24 | red << 16 | green << 8 | blue;
        }

        public static int getArgb(int red, int green, int blue) {
            return Argb.getArgb(255, red, green, blue);
        }

        public static int mixColor(int first, int second) {
            return Argb.getArgb(Argb.getAlpha(first) * Argb.getAlpha(second) / 255, Argb.getRed(first) * Argb.getRed(second) / 255, Argb.getGreen(first) * Argb.getGreen(second) / 255, Argb.getBlue(first) * Argb.getBlue(second) / 255);
        }

        public static int lerp(float delta, int start, int end) {
            int i = MathHelper.lerp(delta, Argb.getAlpha(start), Argb.getAlpha(end));
            int j = MathHelper.lerp(delta, Argb.getRed(start), Argb.getRed(end));
            int k = MathHelper.lerp(delta, Argb.getGreen(start), Argb.getGreen(end));
            int l = MathHelper.lerp(delta, Argb.getBlue(start), Argb.getBlue(end));
            return Argb.getArgb(i, j, k, l);
        }

        public static int fullAlpha(int argb) {
            return argb | 0xFF000000;
        }

        public static int withAlpha(int alpha, int rgb) {
            return alpha << 24 | rgb & 0xFFFFFF;
        }

        public static int fromFloats(float a, float r, float g, float b) {
            return Argb.getArgb(ColorHelper.channelFromFloat(a), ColorHelper.channelFromFloat(r), ColorHelper.channelFromFloat(g), ColorHelper.channelFromFloat(b));
        }

        public static int averageArgb(int a, int b) {
            return Argb.getArgb((Argb.getAlpha(a) + Argb.getAlpha(b)) / 2, (Argb.getRed(a) + Argb.getRed(b)) / 2, (Argb.getGreen(a) + Argb.getGreen(b)) / 2, (Argb.getBlue(a) + Argb.getBlue(b)) / 2);
        }
    }
}

