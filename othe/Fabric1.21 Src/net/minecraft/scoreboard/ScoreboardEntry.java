/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.scoreboard;

import java.util.Objects;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public record ScoreboardEntry(String owner, int value, @Nullable Text display, @Nullable NumberFormat numberFormatOverride) {
    public boolean hidden() {
        return this.owner.startsWith("#");
    }

    public Text name() {
        if (this.display != null) {
            return this.display;
        }
        return Text.literal(this.owner());
    }

    public MutableText formatted(NumberFormat format) {
        return Objects.requireNonNullElse(this.numberFormatOverride, format).format(this.value);
    }

    @Nullable
    public Text display() {
        return this.display;
    }

    @Nullable
    public NumberFormat numberFormatOverride() {
        return this.numberFormatOverride;
    }
}

