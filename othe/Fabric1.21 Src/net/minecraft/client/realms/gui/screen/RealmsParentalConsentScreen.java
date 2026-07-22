/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.realms.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Urls;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RealmsParentalConsentScreen
extends RealmsScreen {
    private static final Text PRIVACY_INFO_TEXT = Text.translatable("mco.account.privacy.information");
    private static final int field_46850 = 15;
    private final DirectionalLayoutWidget layout = DirectionalLayoutWidget.vertical();
    private final Screen parent;
    @Nullable
    private MultilineTextWidget privacyInfoWidget;

    public RealmsParentalConsentScreen(Screen parent) {
        super(NarratorManager.EMPTY);
        this.parent = parent;
    }

    @Override
    public void init() {
        this.layout.spacing(15).getMainPositioner().alignHorizontalCenter();
        this.privacyInfoWidget = new MultilineTextWidget(PRIVACY_INFO_TEXT, this.textRenderer).setCentered(true);
        this.layout.add(this.privacyInfoWidget);
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.add(DirectionalLayoutWidget.horizontal().spacing(8));
        MutableText text = Text.translatable("mco.account.privacy.info.button");
        directionalLayoutWidget.add(ButtonWidget.builder(text, ConfirmLinkScreen.opening((Screen)this, Urls.GDPR)).build());
        directionalLayoutWidget.add(ButtonWidget.builder(ScreenTexts.BACK, button -> this.close()).build());
        this.layout.forEachChild(child -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(child);
        });
        this.initTabNavigation();
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    protected void initTabNavigation() {
        if (this.privacyInfoWidget != null) {
            this.privacyInfoWidget.setMaxWidth(this.width - 15);
        }
        this.layout.refreshPositions();
        SimplePositioningWidget.setPos(this.layout, this.getNavigationFocus());
    }

    @Override
    public Text getNarratedTitle() {
        return PRIVACY_INFO_TEXT;
    }
}

