/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class AccessibilityOnboardingButtons {
    public static TextIconButtonWidget createLanguageButton(int width, ButtonWidget.PressAction onPress, boolean hideText) {
        return TextIconButtonWidget.builder(Text.translatable("options.language"), onPress, hideText).width(width).texture(Identifier.ofVanilla("icon/language"), 15, 15).build();
    }

    public static TextIconButtonWidget createAccessibilityButton(int width, ButtonWidget.PressAction onPress, boolean hideText) {
        MutableText text = hideText ? Text.translatable("options.accessibility") : Text.translatable("accessibility.onboarding.accessibility.button");
        return TextIconButtonWidget.builder(text, onPress, hideText).width(width).texture(Identifier.ofVanilla("icon/accessibility"), 15, 15).build();
    }
}

