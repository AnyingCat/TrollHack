/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.scoreboard;

import java.util.Objects;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.text.MutableText;
import org.jetbrains.annotations.Nullable;

public interface ReadableScoreboardScore {
    public int getScore();

    public boolean isLocked();

    @Nullable
    public NumberFormat getNumberFormat();

    default public MutableText getFormattedScore(NumberFormat fallbackFormat) {
        return Objects.requireNonNullElse(this.getNumberFormat(), fallbackFormat).format(this.getScore());
    }

    public static MutableText getFormattedScore(@Nullable ReadableScoreboardScore score, NumberFormat fallbackFormat) {
        return score != null ? score.getFormattedScore(fallbackFormat) : fallbackFormat.format(0);
    }
}

