package github.trollhack.events.impl;

import github.trollhack.events.Event;

public class RotateEvent extends Event {
    private float yaw;
    private float pitch;
    private boolean modified;

    public RotateEvent(float yaw, float pitch) {
        super(Stage.Pre);
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        modified = true;
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        modified = true;
        this.pitch = pitch;
    }

    public boolean isModified() {
        return modified;
    }

    public void setRotation(float yaw, float pitch) {
        setYaw(yaw);
        setPitch(pitch);
    }

    public void setYawNoModify(float yaw) {
        this.yaw = yaw;
    }

    public void setPitchNoModify(float pitch) {
        this.pitch = pitch;
    }
}
