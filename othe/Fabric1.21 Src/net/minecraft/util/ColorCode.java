/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;

public record ColorCode(int rgba) {
    private static final String HASH = "#";
    public static final Codec<ColorCode> CODEC = Codec.STRING.comapFlatMap(code -> {
        if (!code.startsWith(HASH)) {
            return DataResult.error(() -> "Not a color code: " + code);
        }
        try {
            int i = (int)Long.parseLong(code.substring(1), 16);
            return DataResult.success(new ColorCode(i));
        } catch (NumberFormatException numberFormatException) {
            return DataResult.error(() -> "Exception parsing color code: " + numberFormatException.getMessage());
        }
    }, ColorCode::asString);

    private String asString() {
        return String.format(Locale.ROOT, "#%08X", this.rgba);
    }

    @Override
    public String toString() {
        return this.asString();
    }
}

