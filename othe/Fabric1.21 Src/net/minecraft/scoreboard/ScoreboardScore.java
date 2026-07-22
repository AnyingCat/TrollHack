/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.scoreboard;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.NumberFormatTypes;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class ScoreboardScore
implements ReadableScoreboardScore {
    private static final String SCORE_NBT_KEY = "Score";
    private static final String LOCKED_NBT_KEY = "Locked";
    private static final String DISPLAY_NBT_KEY = "display";
    private static final String FORMAT_NBT_KEY = "format";
    private int score;
    private boolean locked = true;
    @Nullable
    private Text displayText;
    @Nullable
    private NumberFormat numberFormat;

    @Override
    public int getScore() {
        return this.score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Nullable
    public Text getDisplayText() {
        return this.displayText;
    }

    public void setDisplayText(@Nullable Text text) {
        this.displayText = text;
    }

    @Override
    @Nullable
    public NumberFormat getNumberFormat() {
        return this.numberFormat;
    }

    public void setNumberFormat(@Nullable NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
    }

    public NbtCompound toNbt(RegistryWrapper.WrapperLookup registries) {
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putInt(SCORE_NBT_KEY, this.score);
        nbtCompound.putBoolean(LOCKED_NBT_KEY, this.locked);
        if (this.displayText != null) {
            nbtCompound.putString(DISPLAY_NBT_KEY, Text.Serialization.toJsonString(this.displayText, registries));
        }
        if (this.numberFormat != null) {
            NumberFormatTypes.CODEC.encodeStart(registries.getOps(NbtOps.INSTANCE), this.numberFormat).ifSuccess(formatElement -> nbtCompound.put(FORMAT_NBT_KEY, (NbtElement)formatElement));
        }
        return nbtCompound;
    }

    public static ScoreboardScore fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        ScoreboardScore scoreboardScore = new ScoreboardScore();
        scoreboardScore.score = nbt.getInt(SCORE_NBT_KEY);
        scoreboardScore.locked = nbt.getBoolean(LOCKED_NBT_KEY);
        if (nbt.contains(DISPLAY_NBT_KEY, NbtElement.STRING_TYPE)) {
            scoreboardScore.displayText = Text.Serialization.fromJson(nbt.getString(DISPLAY_NBT_KEY), registries);
        }
        if (nbt.contains(FORMAT_NBT_KEY, NbtElement.COMPOUND_TYPE)) {
            NumberFormatTypes.CODEC.parse(registries.getOps(NbtOps.INSTANCE), nbt.get(FORMAT_NBT_KEY)).ifSuccess(format -> {
                scoreboardScore.numberFormat = format;
            });
        }
        return scoreboardScore;
    }
}

