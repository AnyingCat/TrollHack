/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.text;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.TextContent;
import net.minecraft.text.Texts;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SelectorTextContent
implements TextContent {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<SelectorTextContent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("selector")).forGetter(SelectorTextContent::getPattern), TextCodecs.CODEC.optionalFieldOf("separator").forGetter(SelectorTextContent::getSeparator)).apply((Applicative<SelectorTextContent, ?>)instance, SelectorTextContent::new));
    public static final TextContent.Type<SelectorTextContent> TYPE = new TextContent.Type<SelectorTextContent>(CODEC, "selector");
    private final String pattern;
    @Nullable
    private final EntitySelector selector;
    protected final Optional<Text> separator;

    public SelectorTextContent(String pattern, Optional<Text> separator) {
        this.pattern = pattern;
        this.separator = separator;
        this.selector = SelectorTextContent.readSelector(pattern);
    }

    @Nullable
    private static EntitySelector readSelector(String pattern) {
        EntitySelector entitySelector = null;
        try {
            EntitySelectorReader entitySelectorReader = new EntitySelectorReader(new StringReader(pattern));
            entitySelector = entitySelectorReader.read();
        } catch (CommandSyntaxException commandSyntaxException) {
            LOGGER.warn("Invalid selector component: {}: {}", (Object)pattern, (Object)commandSyntaxException.getMessage());
        }
        return entitySelector;
    }

    @Override
    public TextContent.Type<?> getType() {
        return TYPE;
    }

    public String getPattern() {
        return this.pattern;
    }

    @Nullable
    public EntitySelector getSelector() {
        return this.selector;
    }

    public Optional<Text> getSeparator() {
        return this.separator;
    }

    @Override
    public MutableText parse(@Nullable ServerCommandSource source, @Nullable Entity sender, int depth) throws CommandSyntaxException {
        if (source == null || this.selector == null) {
            return Text.empty();
        }
        Optional<MutableText> optional = Texts.parse(source, this.separator, sender, depth);
        return Texts.join(this.selector.getEntities(source), optional, Entity::getDisplayName);
    }

    @Override
    public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> visitor, Style style) {
        return visitor.accept(style, this.pattern);
    }

    @Override
    public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
        return visitor.accept(this.pattern);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SelectorTextContent)) return false;
        SelectorTextContent selectorTextContent = (SelectorTextContent)o;
        if (!this.pattern.equals(selectorTextContent.pattern)) return false;
        if (!this.separator.equals(selectorTextContent.separator)) return false;
        return true;
    }

    public int hashCode() {
        int i = this.pattern.hashCode();
        i = 31 * i + this.separator.hashCode();
        return i;
    }

    public String toString() {
        return "pattern{" + this.pattern + "}";
    }
}

