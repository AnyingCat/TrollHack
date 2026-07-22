/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import net.minecraft.entity.EntityAttachments;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public record EntityDimensions(float width, float height, float eyeHeight, EntityAttachments attachments, boolean fixed) {
    private EntityDimensions(float width, float height, boolean fixed) {
        this(width, height, EntityDimensions.getDefaultEyeHeight(height), EntityAttachments.of(width, height), fixed);
    }

    private static float getDefaultEyeHeight(float height) {
        return height * 0.85f;
    }

    public Box getBoxAt(Vec3d pos) {
        return this.getBoxAt(pos.x, pos.y, pos.z);
    }

    public Box getBoxAt(double x, double y, double z) {
        float f = this.width / 2.0f;
        float g = this.height;
        return new Box(x - (double)f, y, z - (double)f, x + (double)f, y + (double)g, z + (double)f);
    }

    public EntityDimensions scaled(float ratio) {
        return this.scaled(ratio, ratio);
    }

    public EntityDimensions scaled(float widthRatio, float heightRatio) {
        if (this.fixed || widthRatio == 1.0f && heightRatio == 1.0f) {
            return this;
        }
        return new EntityDimensions(this.width * widthRatio, this.height * heightRatio, this.eyeHeight * heightRatio, this.attachments.scale(widthRatio, heightRatio, widthRatio), false);
    }

    public static EntityDimensions changing(float width, float height) {
        return new EntityDimensions(width, height, false);
    }

    public static EntityDimensions fixed(float width, float height) {
        return new EntityDimensions(width, height, true);
    }

    public EntityDimensions withEyeHeight(float eyeHeight) {
        return new EntityDimensions(this.width, this.height, eyeHeight, this.attachments, this.fixed);
    }

    public EntityDimensions withAttachments(EntityAttachments.Builder attachments) {
        return new EntityDimensions(this.width, this.height, this.eyeHeight, attachments.build(this.width, this.height), this.fixed);
    }
}

