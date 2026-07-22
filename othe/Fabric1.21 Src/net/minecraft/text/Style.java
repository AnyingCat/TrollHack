/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.text;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * The style of a {@link Text}, representing cosmetic attributes. It includes
 * font, formatting, click/hover events (actions), color, etc.
 * 
 * <p>A style is immutable.
 * 
 * @see Text
 */
public class Style {
    /**
     * An empty style.
     */
    public static final Style EMPTY = new Style(null, null, null, null, null, null, null, null, null, null);
    /**
     * The identifier for the default font of a style.
     */
    public static final Identifier DEFAULT_FONT_ID = Identifier.ofVanilla("default");
    @Nullable
    final TextColor color;
    @Nullable
    final Boolean bold;
    @Nullable
    final Boolean italic;
    @Nullable
    final Boolean underlined;
    @Nullable
    final Boolean strikethrough;
    @Nullable
    final Boolean obfuscated;
    @Nullable
    final ClickEvent clickEvent;
    @Nullable
    final HoverEvent hoverEvent;
    @Nullable
    final String insertion;
    @Nullable
    final Identifier font;

    private static Style of(Optional<TextColor> color, Optional<Boolean> bold, Optional<Boolean> italic, Optional<Boolean> underlined, Optional<Boolean> strikethrough, Optional<Boolean> obfuscated, Optional<ClickEvent> optional, Optional<HoverEvent> optional2, Optional<String> optional3, Optional<Identifier> optional4) {
        Style style = new Style(color.orElse(null), bold.orElse(null), italic.orElse(null), underlined.orElse(null), strikethrough.orElse(null), obfuscated.orElse(null), optional.orElse(null), optional2.orElse(null), optional3.orElse(null), optional4.orElse(null));
        if (style.equals(EMPTY)) {
            return EMPTY;
        }
        return style;
    }

    private Style(@Nullable TextColor color, @Nullable Boolean bold, @Nullable Boolean italic, @Nullable Boolean underlined, @Nullable Boolean strikethrough, @Nullable Boolean obfuscated, @Nullable ClickEvent clickEvent, @Nullable HoverEvent hoverEvent, @Nullable String insertion, @Nullable Identifier font) {
        this.color = color;
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.strikethrough = strikethrough;
        this.obfuscated = obfuscated;
        this.clickEvent = clickEvent;
        this.hoverEvent = hoverEvent;
        this.insertion = insertion;
        this.font = font;
    }

    /**
     * Returns the color of this style.
     */
    @Nullable
    public TextColor getColor() {
        return this.color;
    }

    /**
     * Returns whether the style has bold formatting.
     * 
     * @see Formatting#BOLD
     */
    public boolean isBold() {
        return this.bold == Boolean.TRUE;
    }

    /**
     * Returns whether the style has italic formatting.
     * 
     * @see Formatting#ITALIC
     */
    public boolean isItalic() {
        return this.italic == Boolean.TRUE;
    }

    /**
     * Returns whether the style has strikethrough formatting.
     * 
     * @see Formatting#STRIKETHROUGH
     */
    public boolean isStrikethrough() {
        return this.strikethrough == Boolean.TRUE;
    }

    /**
     * Returns whether the style has underline formatting.
     * 
     * @see Formatting#UNDERLINE
     */
    public boolean isUnderlined() {
        return this.underlined == Boolean.TRUE;
    }

    /**
     * Returns whether the style has obfuscated formatting.
     * 
     * @see Formatting#OBFUSCATED
     */
    public boolean isObfuscated() {
        return this.obfuscated == Boolean.TRUE;
    }

    /**
     * Returns if this is the empty style.
     * 
     * @see #EMPTY
     */
    public boolean isEmpty() {
        return this == EMPTY;
    }

    /**
     * Returns the click event of this style.
     */
    @Nullable
    public ClickEvent getClickEvent() {
        return this.clickEvent;
    }

    /**
     * Returns the hover event of this style.
     */
    @Nullable
    public HoverEvent getHoverEvent() {
        return this.hoverEvent;
    }

    /**
     * Returns the insertion text of the style.
     * 
     * <p>An insertion is inserted when a piece of text clicked while shift key
     * is down in the chat HUD.
     */
    @Nullable
    public String getInsertion() {
        return this.insertion;
    }

    /**
     * Returns the font of this style.
     */
    public Identifier getFont() {
        return this.font != null ? this.font : DEFAULT_FONT_ID;
    }

    private static <T> Style with(Style newStyle, @Nullable T oldAttribute, @Nullable T newAttribute) {
        if (oldAttribute != null && newAttribute == null && newStyle.equals(EMPTY)) {
            return EMPTY;
        }
        return newStyle;
    }

    /**
     * Returns a new style with the color provided and all other attributes of
     * this style.
     * 
     * @param color the new color
     */
    public Style withColor(@Nullable TextColor color) {
        if (Objects.equals(this.color, color)) {
            return this;
        }
        return Style.with(new Style(color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.color, color);
    }

    /**
     * Returns a new style with the color provided and all other attributes of
     * this style.
     * 
     * @param color the new color
     */
    public Style withColor(@Nullable Formatting color) {
        return this.withColor(color != null ? TextColor.fromFormatting(color) : null);
    }

    public Style withColor(int rgbColor) {
        return this.withColor(TextColor.fromRgb(rgbColor));
    }

    /**
     * Returns a new style with the bold attribute provided and all other
     * attributes of this style.
     * 
     * @param bold the new bold property
     */
    public Style withBold(@Nullable Boolean bold) {
        if (Objects.equals(this.bold, bold)) {
            return this;
        }
        return Style.with(new Style(this.color, bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.bold, bold);
    }

    /**
     * Returns a new style with the italic attribute provided and all other
     * attributes of this style.
     * 
     * @param italic the new italic property
     */
    public Style withItalic(@Nullable Boolean italic) {
        if (Objects.equals(this.italic, italic)) {
            return this;
        }
        return Style.with(new Style(this.color, this.bold, italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.italic, italic);
    }

    /**
     * Returns a new style with the underline attribute provided and all other
     * attributes of this style.
     */
    public Style withUnderline(@Nullable Boolean underline) {
        if (Objects.equals(this.underlined, underline)) {
            return this;
        }
        return Style.with(new Style(this.color, this.bold, this.italic, underline, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.underlined, underline);
    }

    public Style withStrikethrough(@Nullable Boolean strikethrough) {
        if (Objects.equals(this.strikethrough, strikethrough)) {
            return this;
        }
        return Style.with(new Style(this.color, this.bold, this.italic, this.underlined, strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.strikethrough, strikethrough);
    }

    public Style withObfuscated(@Nullable Boolean obfuscated) {
        if (Objects.equals(this.obfuscated, obfuscated)) {
            return this;
        }
        return Style.with(new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.obfuscated, obfuscated);
    }

    /**
     * Returns a new style with the click event provided and all other
     * attributes of this style.
     * 
     * @param clickEvent the new click event
     */
    public Style withClickEvent(@Nullable ClickEvent clickEvent) {
        if (Objects.equals(this.clickEvent, clickEvent)) {
            return this;
        }
        return Style.with(new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, clickEvent, this.hoverEvent, this.insertion, this.font), this.clickEvent, clickEvent);
    }

    /**
     * Returns a new style with the hover event provided and all other
     * attributes of this style.
     * 
     * @param hoverEvent the new hover event
     */
    public Style withHoverEvent(@Nullable HoverEvent hoverEvent) {
        if (Objects.equals(this.hoverEvent, hoverEvent)) {
            return this;
        }
        return Style.with(new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, hoverEvent, this.insertion, this.font), this.hoverEvent, hoverEvent);
    }

    /**
     * Returns a new style with the insertion provided and all other
     * attributes of this style.
     * 
     * @param insertion the new insertion string
     */
    public Style withInsertion(@Nullable String insertion) {
        if (Objects.equals(this.insertion, insertion)) {
            return this;
        }
        return Style.with(new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, insertion, this.font), this.insertion, insertion);
    }

    /**
     * Returns a new style with the font provided and all other
     * attributes of this style.
     * 
     * @param font the new font
     */
    public Style withFont(@Nullable Identifier font) {
        if (Objects.equals(this.font, font)) {
            return this;
        }
        return Style.with(new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, font), this.font, font);
    }

    /**
     * Returns a new style with the formatting provided and all other
     * attributes of this style.
     * 
     * @param formatting the new formatting
     */
    public Style withFormatting(Formatting formatting) {
        TextColor textColor = this.color;
        Boolean boolean_ = this.bold;
        Boolean boolean2 = this.italic;
        Boolean boolean3 = this.strikethrough;
        Boolean boolean4 = this.underlined;
        Boolean boolean5 = this.obfuscated;
        switch (formatting) {
            case OBFUSCATED: {
                boolean5 = true;
                break;
            }
            case BOLD: {
                boolean_ = true;
                break;
            }
            case STRIKETHROUGH: {
                boolean3 = true;
                break;
            }
            case UNDERLINE: {
                boolean4 = true;
                break;
            }
            case ITALIC: {
                boolean2 = true;
                break;
            }
            case RESET: {
                return EMPTY;
            }
            default: {
                textColor = TextColor.fromFormatting(formatting);
            }
        }
        return new Style(textColor, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    /**
     * Returns a new style with the formatting provided and some applicable
     * attributes of this style.
     * 
     * <p>When a color formatting is passed for {@code formatting}, the other
     * formattings, including bold, italic, strikethrough, underlined, and
     * obfuscated, are all removed.
     * 
     * @param formatting the new formatting
     */
    public Style withExclusiveFormatting(Formatting formatting) {
        TextColor textColor = this.color;
        Boolean boolean_ = this.bold;
        Boolean boolean2 = this.italic;
        Boolean boolean3 = this.strikethrough;
        Boolean boolean4 = this.underlined;
        Boolean boolean5 = this.obfuscated;
        switch (formatting) {
            case OBFUSCATED: {
                boolean5 = true;
                break;
            }
            case BOLD: {
                boolean_ = true;
                break;
            }
            case STRIKETHROUGH: {
                boolean3 = true;
                break;
            }
            case UNDERLINE: {
                boolean4 = true;
                break;
            }
            case ITALIC: {
                boolean2 = true;
                break;
            }
            case RESET: {
                return EMPTY;
            }
            default: {
                boolean5 = false;
                boolean_ = false;
                boolean3 = false;
                boolean4 = false;
                boolean2 = false;
                textColor = TextColor.fromFormatting(formatting);
            }
        }
        return new Style(textColor, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    /**
     * Returns a new style with the formattings provided and all other
     * attributes of this style.
     * 
     * @param formattings an array of new formattings
     */
    public Style withFormatting(Formatting ... formattings) {
        TextColor textColor = this.color;
        Boolean boolean_ = this.bold;
        Boolean boolean2 = this.italic;
        Boolean boolean3 = this.strikethrough;
        Boolean boolean4 = this.underlined;
        Boolean boolean5 = this.obfuscated;
        block8: for (Formatting formatting : formattings) {
            switch (formatting) {
                case OBFUSCATED: {
                    boolean5 = true;
                    continue block8;
                }
                case BOLD: {
                    boolean_ = true;
                    continue block8;
                }
                case STRIKETHROUGH: {
                    boolean3 = true;
                    continue block8;
                }
                case UNDERLINE: {
                    boolean4 = true;
                    continue block8;
                }
                case ITALIC: {
                    boolean2 = true;
                    continue block8;
                }
                case RESET: {
                    return EMPTY;
                }
                default: {
                    textColor = TextColor.fromFormatting(formatting);
                }
            }
        }
        return new Style(textColor, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    /**
     * Returns a new style with the undefined attributes of this style filled
     * by the {@code parent} style.
     * 
     * @param parent the parent style
     */
    public Style withParent(Style parent) {
        if (this == EMPTY) {
            return parent;
        }
        if (parent == EMPTY) {
            return this;
        }
        return new Style(this.color != null ? this.color : parent.color, this.bold != null ? this.bold : parent.bold, this.italic != null ? this.italic : parent.italic, this.underlined != null ? this.underlined : parent.underlined, this.strikethrough != null ? this.strikethrough : parent.strikethrough, this.obfuscated != null ? this.obfuscated : parent.obfuscated, this.clickEvent != null ? this.clickEvent : parent.clickEvent, this.hoverEvent != null ? this.hoverEvent : parent.hoverEvent, this.insertion != null ? this.insertion : parent.insertion, this.font != null ? this.font : parent.font);
    }

    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder("{");
        class Writer {
            private boolean shouldAppendComma;

            Writer() {
            }

            private void appendComma() {
                if (this.shouldAppendComma) {
                    stringBuilder.append(',');
                }
                this.shouldAppendComma = true;
            }

            void append(String key, @Nullable Boolean value) {
                if (value != null) {
                    this.appendComma();
                    if (!value.booleanValue()) {
                        stringBuilder.append('!');
                    }
                    stringBuilder.append(key);
                }
            }

            void append(String key, @Nullable Object value) {
                if (value != null) {
                    this.appendComma();
                    stringBuilder.append(key);
                    stringBuilder.append('=');
                    stringBuilder.append(value);
                }
            }
        }
        Writer writer = new Writer();
        writer.append("color", this.color);
        writer.append("bold", this.bold);
        writer.append("italic", this.italic);
        writer.append("underlined", this.underlined);
        writer.append("strikethrough", this.strikethrough);
        writer.append("obfuscated", this.obfuscated);
        writer.append("clickEvent", this.clickEvent);
        writer.append("hoverEvent", this.hoverEvent);
        writer.append("insertion", this.insertion);
        writer.append("font", this.font);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Style) {
            Style style = (Style)o;
            return this.bold == style.bold && Objects.equals(this.getColor(), style.getColor()) && this.italic == style.italic && this.obfuscated == style.obfuscated && this.strikethrough == style.strikethrough && this.underlined == style.underlined && Objects.equals(this.clickEvent, style.clickEvent) && Objects.equals(this.hoverEvent, style.hoverEvent) && Objects.equals(this.insertion, style.insertion) && Objects.equals(this.font, style.font);
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion);
    }

    public static class Codecs {
        public static final MapCodec<Style> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(TextColor.CODEC.optionalFieldOf("color").forGetter(style -> Optional.ofNullable(style.color)), Codec.BOOL.optionalFieldOf("bold").forGetter(style -> Optional.ofNullable(style.bold)), Codec.BOOL.optionalFieldOf("italic").forGetter(style -> Optional.ofNullable(style.italic)), Codec.BOOL.optionalFieldOf("underlined").forGetter(style -> Optional.ofNullable(style.underlined)), Codec.BOOL.optionalFieldOf("strikethrough").forGetter(style -> Optional.ofNullable(style.strikethrough)), Codec.BOOL.optionalFieldOf("obfuscated").forGetter(style -> Optional.ofNullable(style.obfuscated)), ClickEvent.CODEC.optionalFieldOf("clickEvent").forGetter(style -> Optional.ofNullable(style.clickEvent)), HoverEvent.CODEC.optionalFieldOf("hoverEvent").forGetter(style -> Optional.ofNullable(style.hoverEvent)), Codec.STRING.optionalFieldOf("insertion").forGetter(style -> Optional.ofNullable(style.insertion)), Identifier.CODEC.optionalFieldOf("font").forGetter(style -> Optional.ofNullable(style.font))).apply((Applicative<Style, ?>)instance, Style::of));
        public static final Codec<Style> CODEC = MAP_CODEC.codec();
        public static final PacketCodec<RegistryByteBuf, Style> PACKET_CODEC = PacketCodecs.unlimitedRegistryCodec(CODEC);
    }
}

