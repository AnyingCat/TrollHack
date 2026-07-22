/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.Map;
import net.minecraft.util.Util;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Direction;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.slf4j.Logger;

public class AffineTransformations {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Map<Direction, AffineTransformation> DIRECTION_ROTATIONS = Util.make(Maps.newEnumMap(Direction.class), map -> {
        map.put(Direction.SOUTH, AffineTransformation.identity());
        map.put(Direction.EAST, new AffineTransformation(null, new Quaternionf().rotateY(1.5707964f), null, null));
        map.put(Direction.WEST, new AffineTransformation(null, new Quaternionf().rotateY(-1.5707964f), null, null));
        map.put(Direction.NORTH, new AffineTransformation(null, new Quaternionf().rotateY((float)Math.PI), null, null));
        map.put(Direction.UP, new AffineTransformation(null, new Quaternionf().rotateX(-1.5707964f), null, null));
        map.put(Direction.DOWN, new AffineTransformation(null, new Quaternionf().rotateX(1.5707964f), null, null));
    });
    public static final Map<Direction, AffineTransformation> INVERTED_DIRECTION_ROTATIONS = Util.make(Maps.newEnumMap(Direction.class), map -> {
        for (Direction direction : Direction.values()) {
            map.put(direction, DIRECTION_ROTATIONS.get(direction).invert());
        }
    });

    public static AffineTransformation setupUvLock(AffineTransformation transformation) {
        Matrix4f matrix4f = new Matrix4f().translation(0.5f, 0.5f, 0.5f);
        matrix4f.mul(transformation.getMatrix());
        matrix4f.translate(-0.5f, -0.5f, -0.5f);
        return new AffineTransformation(matrix4f);
    }

    public static AffineTransformation method_35829(AffineTransformation transformation) {
        Matrix4f matrix4f = new Matrix4f().translation(-0.5f, -0.5f, -0.5f);
        matrix4f.mul(transformation.getMatrix());
        matrix4f.translate(0.5f, 0.5f, 0.5f);
        return new AffineTransformation(matrix4f);
    }

    public static AffineTransformation uvLock(AffineTransformation transformation, Direction dir) {
        Direction direction = Direction.transform(transformation.getMatrix(), dir);
        AffineTransformation affineTransformation = transformation.invert();
        if (affineTransformation == null) {
            LOGGER.debug("Failed to invert transformation {}", (Object)transformation);
            return AffineTransformation.identity();
        }
        AffineTransformation affineTransformation2 = INVERTED_DIRECTION_ROTATIONS.get(dir).multiply(affineTransformation).multiply(DIRECTION_ROTATIONS.get(direction));
        return AffineTransformations.setupUvLock(affineTransformation2);
    }
}

