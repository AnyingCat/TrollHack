/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import java.util.List;
import net.minecraft.util.math.Vec3d;

public enum EntityAttachmentType {
    PASSENGER(Point.AT_HEIGHT),
    VEHICLE(Point.ZERO),
    NAME_TAG(Point.AT_HEIGHT),
    WARDEN_CHEST(Point.WARDEN_CHEST);

    private final Point point;

    private EntityAttachmentType(Point point) {
        this.point = point;
    }

    public List<Vec3d> createPoint(float width, float height) {
        return this.point.create(width, height);
    }

    public static interface Point {
        public static final List<Vec3d> NONE = List.of(Vec3d.ZERO);
        public static final Point ZERO = (width, height) -> NONE;
        public static final Point AT_HEIGHT = (width, height) -> List.of(new Vec3d(0.0, height, 0.0));
        public static final Point WARDEN_CHEST = (width, height) -> List.of(new Vec3d(0.0, (double)height / 2.0, 0.0));

        public List<Vec3d> create(float var1, float var2);
    }
}

