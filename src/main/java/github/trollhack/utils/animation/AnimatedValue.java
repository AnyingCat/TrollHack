package github.trollhack.utils.animation;

public class AnimatedValue {
    private double currentValue;
    private double targetValue;
    private long animationStartTime;
    private long animationDuration;
    private AnimationUtil.Easing easing;
    private boolean animationActive;

    public AnimatedValue(double initialValue) {
        this.currentValue = initialValue;
        this.targetValue = initialValue;
        this.animationDuration = 300;
        this.easing = AnimationUtil.Easing.EASE_OUT_CUBIC;
        this.animationActive = false;
    }

    public AnimatedValue(double initialValue, long duration, AnimationUtil.Easing easing) {
        this.currentValue = initialValue;
        this.targetValue = initialValue;
        this.animationDuration = duration;
        this.easing = easing;
        this.animationActive = false;
    }

    public void setTarget(double target) {
        if (this.targetValue != target) {
            this.targetValue = target;
            this.animationStartTime = System.currentTimeMillis();
            this.animationActive = true;
        }
    }

    public void update() {
        if (!animationActive) return;
        double progress = Math.min(1.0, (double) (System.currentTimeMillis() - animationStartTime) / animationDuration);
        if (progress >= 1.0) {
            currentValue = targetValue;
            animationActive = false;
            return;
        }
        currentValue = AnimationUtil.animateWithEasing(currentValue, targetValue, progress, easing);
    }

    public double getValue() { return currentValue; }
    public float getValueFloat() { return (float) currentValue; }
    public double getTarget() { return targetValue; }
    public boolean isAnimationActive() { return animationActive; }
    public void setDuration(long duration) { this.animationDuration = duration; }
    public void setEasing(AnimationUtil.Easing easing) { this.easing = easing; }

    public void forceFinish() {
        currentValue = targetValue;
        animationActive = false;
    }

    public void reset(double value) {
        currentValue = value;
        targetValue = value;
        animationActive = false;
    }

    public void setTo(double value) {
        currentValue = value;
        targetValue = value;
        animationActive = false;
    }
}
