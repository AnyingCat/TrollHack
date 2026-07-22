/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.scoreboard;

import java.util.function.IntFunction;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import org.jetbrains.annotations.Nullable;

public enum ScoreboardDisplaySlot implements StringIdentifiable
{
    LIST(0, "list"),
    SIDEBAR(1, "sidebar"),
    BELOW_NAME(2, "below_name"),
    TEAM_BLACK(3, "sidebar.team.black"),
    TEAM_DARK_BLUE(4, "sidebar.team.dark_blue"),
    TEAM_DARK_GREEN(5, "sidebar.team.dark_green"),
    TEAM_DARK_AQUA(6, "sidebar.team.dark_aqua"),
    TEAM_DARK_RED(7, "sidebar.team.dark_red"),
    TEAM_DARK_PURPLE(8, "sidebar.team.dark_purple"),
    TEAM_GOLD(9, "sidebar.team.gold"),
    TEAM_GRAY(10, "sidebar.team.gray"),
    TEAM_DARK_GRAY(11, "sidebar.team.dark_gray"),
    TEAM_BLUE(12, "sidebar.team.blue"),
    TEAM_GREEN(13, "sidebar.team.green"),
    TEAM_AQUA(14, "sidebar.team.aqua"),
    TEAM_RED(15, "sidebar.team.red"),
    TEAM_LIGHT_PURPLE(16, "sidebar.team.light_purple"),
    TEAM_YELLOW(17, "sidebar.team.yellow"),
    TEAM_WHITE(18, "sidebar.team.white");

    public static final StringIdentifiable.EnumCodec<ScoreboardDisplaySlot> CODEC;
    public static final IntFunction<ScoreboardDisplaySlot> FROM_ID;
    private final int id;
    private final String name;

    private ScoreboardDisplaySlot(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    @Override
    public String asString() {
        return this.name;
    }

    @Nullable
    public static ScoreboardDisplaySlot fromFormatting(Formatting formatting) {
        return switch (formatting) {
            default -> throw new MatchException(null, null);
            case Formatting.BLACK -> TEAM_BLACK;
            case Formatting.DARK_BLUE -> TEAM_DARK_BLUE;
            case Formatting.DARK_GREEN -> TEAM_DARK_GREEN;
            case Formatting.DARK_AQUA -> TEAM_DARK_AQUA;
            case Formatting.DARK_RED -> TEAM_DARK_RED;
            case Formatting.DARK_PURPLE -> TEAM_DARK_PURPLE;
            case Formatting.GOLD -> TEAM_GOLD;
            case Formatting.GRAY -> TEAM_GRAY;
            case Formatting.DARK_GRAY -> TEAM_DARK_GRAY;
            case Formatting.BLUE -> TEAM_BLUE;
            case Formatting.GREEN -> TEAM_GREEN;
            case Formatting.AQUA -> TEAM_AQUA;
            case Formatting.RED -> TEAM_RED;
            case Formatting.LIGHT_PURPLE -> TEAM_LIGHT_PURPLE;
            case Formatting.YELLOW -> TEAM_YELLOW;
            case Formatting.WHITE -> TEAM_WHITE;
            case Formatting.BOLD, Formatting.ITALIC, Formatting.UNDERLINE, Formatting.RESET, Formatting.OBFUSCATED, Formatting.STRIKETHROUGH -> null;
        };
    }

    static {
        CODEC = StringIdentifiable.createCodec(ScoreboardDisplaySlot::values);
        FROM_ID = ValueLists.createIdToValueFunction(ScoreboardDisplaySlot::getId, ScoreboardDisplaySlot.values(), ValueLists.OutOfBoundsHandling.ZERO);
    }
}

