/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

/**
 * Contains string-related helper methods.
 */
public class StringHelper {
    private static final Pattern FORMATTING_CODE = Pattern.compile("(?i)\\u00A7[0-9A-FK-OR]");
    private static final Pattern LINE_BREAK = Pattern.compile("\\r\\n|\\v");
    private static final Pattern ENDS_WITH_LINE_BREAK = Pattern.compile("(?:\\r\\n|\\v)$");

    /**
     * {@return the length of the {@code tick} in the MM:SS format, where
     * the MM is the minutes and SS is the seconds (optionally zero-padded)}
     */
    public static String formatTicks(int ticks, float tickRate) {
        int i = MathHelper.floor((float)ticks / tickRate);
        int j = i / 60;
        i %= 60;
        int k = j / 60;
        j %= 60;
        if (k > 0) {
            return String.format(Locale.ROOT, "%02d:%02d:%02d", k, j, i);
        }
        return String.format(Locale.ROOT, "%02d:%02d", j, i);
    }

    /**
     * {@return the {@code text} with all formatting codes removed}
     * 
     * <p>A formatting code is the character {@code \u00a7} followed by
     * a numeric character or a letter A to F, K to O, or R.
     * 
     * @see Formatting#strip
     */
    public static String stripTextFormat(String text) {
        return FORMATTING_CODE.matcher(text).replaceAll("");
    }

    /**
     * {@return true if {@code text} is {@code null} or empty, false otherwise}
     */
    public static boolean isEmpty(@Nullable String text) {
        return StringUtils.isEmpty(text);
    }

    /**
     * {@return {@code text} truncated to at most {@code maxLength} characters,
     * optionally with ellipsis}
     */
    public static String truncate(String text, int maxLength, boolean addEllipsis) {
        if (text.length() <= maxLength) {
            return text;
        }
        if (addEllipsis && maxLength > 3) {
            return text.substring(0, maxLength - 3) + "...";
        }
        return text.substring(0, maxLength);
    }

    /**
     * {@return the number of linebreaks in {@code text}}
     * 
     * <p>A linebreak is either a CRLF sequence or a vertical tab (U+000B).
     */
    public static int countLines(String text) {
        if (text.isEmpty()) {
            return 0;
        }
        Matcher matcher = LINE_BREAK.matcher(text);
        int i = 1;
        while (matcher.find()) {
            ++i;
        }
        return i;
    }

    /**
     * {@return true if {@code text} ends with a linebreak, false otherwise}
     * 
     * <p>A linebreak is either a CRLF sequence or a vertical tab (U+000B).
     */
    public static boolean endsWithLineBreak(String text) {
        return ENDS_WITH_LINE_BREAK.matcher(text).find();
    }

    /**
     * {@return {@code text} truncated to at most 256 characters without ellipsis}
     * 
     * @apiNote This is used when sending chat messages.
     */
    public static String truncateChat(String text) {
        return StringHelper.truncate(text, 256, false);
    }

    /**
     * {@return whether {@code c} is a valid character}
     * 
     * <p>Characters are valid if they are not an ASCII control code or {@code \u00a7}.
     */
    public static boolean isValidChar(char c) {
        return c != '\u00a7' && c >= ' ' && c != '\u007f';
    }

    public static boolean isValidPlayerName(String name) {
        if (name.length() > 16) {
            return false;
        }
        return name.chars().filter(c -> c <= 32 || c >= 127).findAny().isEmpty();
    }

    /**
     * {@return {@code string} with all {@linkplain #isValidChar invalid characters},
     * including linebreak ({@code \n}), removed}
     */
    public static String stripInvalidChars(String string) {
        return StringHelper.stripInvalidChars(string, false);
    }

    /**
     * {@return {@code string} with all {@linkplain #isValidChar invalid characters}
     * removed}
     */
    public static String stripInvalidChars(String string, boolean allowLinebreak) {
        StringBuilder stringBuilder = new StringBuilder();
        for (char c : string.toCharArray()) {
            if (StringHelper.isValidChar(c)) {
                stringBuilder.append(c);
                continue;
            }
            if (!allowLinebreak || c != '\n') continue;
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    public static boolean isWhitespace(int c) {
        return Character.isWhitespace(c) || Character.isSpaceChar(c);
    }

    public static boolean isBlank(@Nullable String string) {
        if (string == null || string.length() == 0) {
            return true;
        }
        return string.chars().allMatch(StringHelper::isWhitespace);
    }
}

