/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.vault;

import net.minecraft.util.math.MathHelper;

public class VaultClientData {
    public static final float DISPLAY_ROTATION_SPEED = 10.0f;
    private float displayRotation;
    private float prevDisplayRotation;

    VaultClientData() {
    }

    public float getDisplayRotation() {
        return this.displayRotation;
    }

    public float getPreviousDisplayRotation() {
        return this.prevDisplayRotation;
    }

    void rotateDisplay() {
        this.prevDisplayRotation = this.displayRotation;
        this.displayRotation = MathHelper.wrapDegrees(this.displayRotation + 10.0f);
    }
}

