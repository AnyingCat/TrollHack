/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * A button with an icon and an optional text.
 * 
 * @see ButtonWidget
 */
@Environment(value=EnvType.CLIENT)
public abstract class TextIconButtonWidget
extends ButtonWidget {
    protected final Identifier texture;
    protected final int textureWidth;
    protected final int textureHeight;

    TextIconButtonWidget(int width, int height, Text message, int textureWidth, int textureHeight, Identifier texture, ButtonWidget.PressAction onPress, @Nullable ButtonWidget.NarrationSupplier narrationSupplier) {
        super(0, 0, width, height, message, onPress, narrationSupplier == null ? DEFAULT_NARRATION_SUPPLIER : narrationSupplier);
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.texture = texture;
    }

    public static Builder builder(Text text, ButtonWidget.PressAction onPress, boolean hideLabel) {
        return new Builder(text, onPress, hideLabel);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final Text text;
        private final ButtonWidget.PressAction onPress;
        private final boolean hideText;
        private int width = 150;
        private int height = 20;
        @Nullable
        private Identifier texture;
        private int textureWidth;
        private int textureHeight;
        @Nullable
        ButtonWidget.NarrationSupplier narrationSupplier;

        public Builder(Text text, ButtonWidget.PressAction onPress, boolean hideText) {
            this.text = text;
            this.onPress = onPress;
            this.hideText = hideText;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder dimension(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder texture(Identifier texture, int width, int height) {
            this.texture = texture;
            this.textureWidth = width;
            this.textureHeight = height;
            return this;
        }

        public Builder narration(ButtonWidget.NarrationSupplier narrationSupplier) {
            this.narrationSupplier = narrationSupplier;
            return this;
        }

        public TextIconButtonWidget build() {
            if (this.texture == null) {
                throw new IllegalStateException("Sprite not set");
            }
            if (this.hideText) {
                return new IconOnly(this.width, this.height, this.text, this.textureWidth, this.textureHeight, this.texture, this.onPress, this.narrationSupplier);
            }
            return new WithText(this.width, this.height, this.text, this.textureWidth, this.textureHeight, this.texture, this.onPress, this.narrationSupplier);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class WithText
    extends TextIconButtonWidget {
        protected WithText(int i, int j, Text text, int k, int l, Identifier identifier, ButtonWidget.PressAction pressAction, @Nullable ButtonWidget.NarrationSupplier narrationSupplier) {
            super(i, j, text, k, l, identifier, pressAction, narrationSupplier);
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);
            int i = this.getX() + this.getWidth() - this.textureWidth - 2;
            int j = this.getY() + this.getHeight() / 2 - this.textureHeight / 2;
            context.drawGuiTexture(this.texture, i, j, this.textureWidth, this.textureHeight);
        }

        @Override
        public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
            int i = this.getX() + 2;
            int j = this.getX() + this.getWidth() - this.textureWidth - 4;
            int k = this.getX() + this.getWidth() / 2;
            WithText.drawScrollableText(context, textRenderer, this.getMessage(), k, i, this.getY(), j, this.getY() + this.getHeight(), color);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class IconOnly
    extends TextIconButtonWidget {
        protected IconOnly(int i, int j, Text text, int k, int l, Identifier identifier, ButtonWidget.PressAction pressAction, @Nullable ButtonWidget.NarrationSupplier narrationSupplier) {
            super(i, j, text, k, l, identifier, pressAction, narrationSupplier);
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);
            int i = this.getX() + this.getWidth() / 2 - this.textureWidth / 2;
            int j = this.getY() + this.getHeight() / 2 - this.textureHeight / 2;
            context.drawGuiTexture(this.texture, i, j, this.textureWidth, this.textureHeight);
        }

        @Override
        public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
        }
    }
}

