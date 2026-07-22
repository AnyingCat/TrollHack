/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.font;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface MultilineText {
    public static final MultilineText EMPTY = new MultilineText(){

        @Override
        public void drawCenterWithShadow(DrawContext context, int x, int y) {
        }

        @Override
        public void drawCenterWithShadow(DrawContext context, int x, int y, int lineHeight, int color) {
        }

        @Override
        public void drawWithShadow(DrawContext context, int x, int y, int lineHeight, int color) {
        }

        @Override
        public int draw(DrawContext context, int x, int y, int lineHeight, int color) {
            return y;
        }

        @Override
        public int count() {
            return 0;
        }

        @Override
        public int getMaxWidth() {
            return 0;
        }
    };

    public static MultilineText create(TextRenderer renderer, Text ... texts) {
        return MultilineText.create(renderer, Integer.MAX_VALUE, Integer.MAX_VALUE, texts);
    }

    public static MultilineText create(TextRenderer renderer, int maxWidth, Text ... texts) {
        return MultilineText.create(renderer, maxWidth, Integer.MAX_VALUE, texts);
    }

    public static MultilineText create(TextRenderer renderer, Text text, int maxWidth) {
        return MultilineText.create(renderer, maxWidth, Integer.MAX_VALUE, text);
    }

    public static MultilineText create(final TextRenderer renderer, final int maxWidth, final int maxLines, final Text ... texts) {
        if (texts.length == 0) {
            return EMPTY;
        }
        return new MultilineText(){
            @Nullable
            private List<Line> lines;
            @Nullable
            private Language language;

            @Override
            public void drawCenterWithShadow(DrawContext context, int x, int y) {
                this.drawCenterWithShadow(context, x, y, renderer.fontHeight, -1);
            }

            @Override
            public void drawCenterWithShadow(DrawContext context, int x, int y, int lineHeight, int color) {
                int i = y;
                for (Line line : this.getLines()) {
                    context.drawCenteredTextWithShadow(renderer, line.text, x, i, color);
                    i += lineHeight;
                }
            }

            @Override
            public void drawWithShadow(DrawContext context, int x, int y, int lineHeight, int color) {
                int i = y;
                for (Line line : this.getLines()) {
                    context.drawTextWithShadow(renderer, line.text, x, i, color);
                    i += lineHeight;
                }
            }

            @Override
            public int draw(DrawContext context, int x, int y, int lineHeight, int color) {
                int i = y;
                for (Line line : this.getLines()) {
                    context.drawText(renderer, line.text, x, i, color, false);
                    i += lineHeight;
                }
                return i;
            }

            private List<Line> getLines() {
                Language language = Language.getInstance();
                if (this.lines != null && language == this.language) {
                    return this.lines;
                }
                this.language = language;
                ArrayList<OrderedText> list = new ArrayList<OrderedText>();
                for (Text text : texts) {
                    list.addAll(renderer.wrapLines(text, maxWidth));
                }
                this.lines = new ArrayList<Line>();
                for (OrderedText orderedText : list.subList(0, Math.min(list.size(), maxLines))) {
                    this.lines.add(new Line(orderedText, renderer.getWidth(orderedText)));
                }
                return this.lines;
            }

            @Override
            public int count() {
                return this.getLines().size();
            }

            @Override
            public int getMaxWidth() {
                return Math.min(maxWidth, this.getLines().stream().mapToInt(Line::width).max().orElse(0));
            }
        };
    }

    public void drawCenterWithShadow(DrawContext var1, int var2, int var3);

    public void drawCenterWithShadow(DrawContext var1, int var2, int var3, int var4, int var5);

    public void drawWithShadow(DrawContext var1, int var2, int var3, int var4, int var5);

    public int draw(DrawContext var1, int var2, int var3, int var4, int var5);

    public int count();

    public int getMaxWidth();

    @Environment(value=EnvType.CLIENT)
    public record Line(OrderedText text, int width) {
    }
}

