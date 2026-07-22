/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public enum StructureLiquidSettings implements StringIdentifiable
{
    IGNORE_WATERLOGGING("ignore_waterlogging"),
    APPLY_WATERLOGGING("apply_waterlogging");

    public static Codec<StructureLiquidSettings> codec;
    private final String id;

    private StructureLiquidSettings(String id) {
        this.id = id;
    }

    @Override
    public String asString() {
        return this.id;
    }

    static {
        codec = StringIdentifiable.createBasicCodec(StructureLiquidSettings::values);
    }
}

