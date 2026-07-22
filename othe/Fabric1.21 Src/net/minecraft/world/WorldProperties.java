/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import java.util.Locale;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.HeightLimitView;

public interface WorldProperties {
    public BlockPos getSpawnPos();

    public float getSpawnAngle();

    public long getTime();

    public long getTimeOfDay();

    public boolean isThundering();

    public boolean isRaining();

    public void setRaining(boolean var1);

    public boolean isHardcore();

    public GameRules getGameRules();

    public Difficulty getDifficulty();

    public boolean isDifficultyLocked();

    default public void populateCrashReport(CrashReportSection reportSection, HeightLimitView world) {
        reportSection.add("Level spawn location", () -> CrashReportSection.createPositionString(world, this.getSpawnPos()));
        reportSection.add("Level time", () -> String.format(Locale.ROOT, "%d game time, %d day time", this.getTime(), this.getTimeOfDay()));
    }
}

