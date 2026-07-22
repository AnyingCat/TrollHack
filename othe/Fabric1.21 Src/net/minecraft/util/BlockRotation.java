/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.DirectionTransformation;
import net.minecraft.util.math.random.Random;

public enum BlockRotation implements StringIdentifiable
{
    NONE("none", DirectionTransformation.IDENTITY),
    CLOCKWISE_90("clockwise_90", DirectionTransformation.ROT_90_Y_NEG),
    CLOCKWISE_180("180", DirectionTransformation.ROT_180_FACE_XZ),
    COUNTERCLOCKWISE_90("counterclockwise_90", DirectionTransformation.ROT_90_Y_POS);

    public static final Codec<BlockRotation> CODEC;
    private final String id;
    private final DirectionTransformation directionTransformation;

    private BlockRotation(String id, DirectionTransformation directionTransformation) {
        this.id = id;
        this.directionTransformation = directionTransformation;
    }

    public BlockRotation rotate(BlockRotation rotation) {
        switch (rotation.ordinal()) {
            case 2: {
                switch (this.ordinal()) {
                    case 0: {
                        return CLOCKWISE_180;
                    }
                    case 1: {
                        return COUNTERCLOCKWISE_90;
                    }
                    case 2: {
                        return NONE;
                    }
                    case 3: {
                        return CLOCKWISE_90;
                    }
                }
            }
            case 3: {
                switch (this.ordinal()) {
                    case 0: {
                        return COUNTERCLOCKWISE_90;
                    }
                    case 1: {
                        return NONE;
                    }
                    case 2: {
                        return CLOCKWISE_90;
                    }
                    case 3: {
                        return CLOCKWISE_180;
                    }
                }
            }
            case 1: {
                switch (this.ordinal()) {
                    case 0: {
                        return CLOCKWISE_90;
                    }
                    case 1: {
                        return CLOCKWISE_180;
                    }
                    case 2: {
                        return COUNTERCLOCKWISE_90;
                    }
                    case 3: {
                        return NONE;
                    }
                }
            }
        }
        return this;
    }

    public DirectionTransformation getDirectionTransformation() {
        return this.directionTransformation;
    }

    public Direction rotate(Direction direction) {
        if (direction.getAxis() == Direction.Axis.Y) {
            return direction;
        }
        switch (this.ordinal()) {
            case 2: {
                return direction.getOpposite();
            }
            case 3: {
                return direction.rotateYCounterclockwise();
            }
            case 1: {
                return direction.rotateYClockwise();
            }
        }
        return direction;
    }

    public int rotate(int rotation, int fullTurn) {
        switch (this.ordinal()) {
            case 2: {
                return (rotation + fullTurn / 2) % fullTurn;
            }
            case 3: {
                return (rotation + fullTurn * 3 / 4) % fullTurn;
            }
            case 1: {
                return (rotation + fullTurn / 4) % fullTurn;
            }
        }
        return rotation;
    }

    public static BlockRotation random(Random random) {
        return Util.getRandom(BlockRotation.values(), random);
    }

    public static List<BlockRotation> randomRotationOrder(Random random) {
        return Util.copyShuffled(BlockRotation.values(), random);
    }

    @Override
    public String asString() {
        return this.id;
    }

    static {
        CODEC = StringIdentifiable.createCodec(BlockRotation::values);
    }
}

