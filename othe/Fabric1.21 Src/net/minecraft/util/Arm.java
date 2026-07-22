/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.TranslatableOption;
import net.minecraft.util.function.ValueLists;

/**
 * An enum representing an entity's arm.
 * 
 * @see Hand
 */
public enum Arm implements TranslatableOption,
StringIdentifiable
{
    LEFT(0, "left", "options.mainHand.left"),
    RIGHT(1, "right", "options.mainHand.right");

    public static final Codec<Arm> CODEC;
    public static final IntFunction<Arm> BY_ID;
    private final int id;
    private final String name;
    private final String translationKey;

    private Arm(int id, String name, String translationKey) {
        this.id = id;
        this.name = name;
        this.translationKey = translationKey;
    }

    /**
     * {@return the arm on the opposite side}
     */
    public Arm getOpposite() {
        if (this == LEFT) {
            return RIGHT;
        }
        return LEFT;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getTranslationKey() {
        return this.translationKey;
    }

    @Override
    public String asString() {
        return this.name;
    }

    static {
        CODEC = StringIdentifiable.createCodec(Arm::values);
        BY_ID = ValueLists.createIdToValueFunction(Arm::getId, Arm.values(), ValueLists.OutOfBoundsHandling.ZERO);
    }
}

