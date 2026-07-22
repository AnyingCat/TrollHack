package github.trollhack.utils.animation;

public class AnimationUtil {

    public static double easeOutCubic(double progress) {
        return 1 - Math.pow(1 - progress, 3);
    }

    public static double easeInCubic(double progress) {
        return Math.pow(progress, 3);
    }

    public static double easeOutQuart(double progress) {
        return 1 - Math.pow(1 - progress, 4);
    }

    public static double easeOutQuint(double progress) {
        return 1 - Math.pow(1 - progress, 5);
    }

    public static double easeOutExpo(double progress) {
        return progress == 1 ? 1 : 1 - Math.pow(2, -10 * progress);
    }

    public static double easeOutSine(double progress) {
        return Math.sin((progress * Math.PI) / 2);
    }

    public static double easeInOutCubic(double progress) {
        return progress < 0.5
            ? 4 * Math.pow(progress, 3)
            : 1 - Math.pow(-2 * progress + 2, 3) / 2;
    }

    public static double easeInOutQuart(double progress) {
        return progress < 0.5
            ? 8 * Math.pow(progress, 4)
            : 1 - Math.pow(-2 * progress + 2, 4) / 2;
    }

    public static double easeOutBack(double progress) {
        double c1 = 1.70158;
        double c3 = c1 + 1;
        return 1 + c3 * Math.pow(progress - 1, 3) + c1 * Math.pow(progress - 1, 2);
    }

    public static double animate(double current, double target, double speed) {
        if (current == target) return target;
        double delta = target - current;
        if (Math.abs(delta) < 0.001) return target;
        return current + delta * Math.min(speed, 1.0);
    }

    public static double animateWithEasing(double current, double target, double progress, Easing easing) {
        if (progress >= 1.0) return target;
        if (progress <= 0.0) return current;
        return current + (target - current) * applyEasing(progress, easing);
    }

    private static double applyEasing(double progress, Easing easing) {
        return switch (easing) {
            case LINEAR -> progress;
            case EASE_IN_CUBIC -> easeInCubic(progress);
            case EASE_OUT_CUBIC -> easeOutCubic(progress);
            case EASE_OUT_QUART -> easeOutQuart(progress);
            case EASE_OUT_QUINT -> easeOutQuint(progress);
            case EASE_OUT_EXPO -> easeOutExpo(progress);
            case EASE_OUT_SINE -> easeOutSine(progress);
            case EASE_INOUT_CUBIC -> easeInOutCubic(progress);
            case EASE_INOUT_QUART -> easeInOutQuart(progress);
            case EASE_OUT_BACK -> easeOutBack(progress);
        };
    }

    public enum Easing {
        LINEAR,
        EASE_IN_CUBIC,
        EASE_OUT_CUBIC,
        EASE_OUT_QUART,
        EASE_OUT_QUINT,
        EASE_OUT_EXPO,
        EASE_OUT_SINE,
        EASE_INOUT_CUBIC,
        EASE_INOUT_QUART,
        EASE_OUT_BACK;

        public float inc(float x) {
            if (x <= 0.0f) return 0.0f;
            if (x >= 1.0f) return 1.0f;
            return (float) applyEasing(x, this);
        }

        public float inc(float x, float min, float max) {
            if (max == min) return min;
            if (max < min) {
                float oldMax = max;
                max = min;
                min = oldMax;
            }
            if (x <= 0.0f) return min;
            if (x >= 1.0f) return max;
            return lerp(min, max, inc(x));
        }

        public float incOrDec(float x, float min, float max) {
            return lerp(min, max, inc(x));
        }

        public float dec(float x) {
            if (x <= 0.0f) return 1.0f;
            if (x >= 1.0f) return 0.0f;
            return 1.0f - inc(x);
        }

        private static float lerp(float min, float max, float value) {
            return min + (max - min) * value;
        }
    }

    public static float toDelta(long start, float length) {
        return Math.max(0.0f, Math.min(1.0f, (System.currentTimeMillis() - start) / length));
    }

    public static long toDelta(long start) {
        return System.currentTimeMillis() - start;
    }
}
