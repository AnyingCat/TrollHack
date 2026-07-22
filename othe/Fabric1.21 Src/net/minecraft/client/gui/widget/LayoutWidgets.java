/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.widget;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;

@Environment(value=EnvType.CLIENT)
public class LayoutWidgets {
    private static final int SPACING = 4;

    private LayoutWidgets() {
    }

    public static LayoutWidget createLabeledWidget(TextRenderer textRenderer, Widget widget, Text label) {
        return LayoutWidgets.createLabeledWidget(textRenderer, widget, label, positioner -> {});
    }

    public static LayoutWidget createLabeledWidget(TextRenderer textRenderer, Widget widget, Text label, Consumer<Positioner> callback) {
        DirectionalLayoutWidget directionalLayoutWidget = DirectionalLayoutWidget.vertical().spacing(4);
        directionalLayoutWidget.add(new TextWidget(label, textRenderer));
        directionalLayoutWidget.add(widget, callback);
        return directionalLayoutWidget;
    }
}

