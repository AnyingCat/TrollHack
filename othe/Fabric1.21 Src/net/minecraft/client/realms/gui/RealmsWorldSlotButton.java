/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.realms.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.RealmsWorldOptions;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.realms.util.RealmsTextureManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RealmsWorldSlotButton
extends ButtonWidget {
    private static final Identifier SLOT_FRAME = Identifier.ofVanilla("widget/slot_frame");
    private static final Identifier CHECKMARK = Identifier.ofVanilla("icon/checkmark");
    public static final Identifier EMPTY_FRAME = Identifier.ofVanilla("textures/gui/realms/empty_frame.png");
    public static final Identifier PANORAMA_0 = Identifier.ofVanilla("textures/gui/title/background/panorama_0.png");
    public static final Identifier PANORAMA_2 = Identifier.ofVanilla("textures/gui/title/background/panorama_2.png");
    public static final Identifier PANORAMA_3 = Identifier.ofVanilla("textures/gui/title/background/panorama_3.png");
    private static final Text ACTIVE_TOOLTIP = Text.translatable("mco.configure.world.slot.tooltip.active");
    private static final Text MINIGAME_TOOLTIP = Text.translatable("mco.configure.world.slot.tooltip.minigame");
    private static final Text TOOLTIP = Text.translatable("mco.configure.world.slot.tooltip");
    static final Text MINIGAME_SLOT_NAME = Text.translatable("mco.worldSlot.minigame");
    private final int slotIndex;
    @Nullable
    private State state;

    public RealmsWorldSlotButton(int x, int y, int width, int height, int slotIndex, ButtonWidget.PressAction pressAction) {
        super(x, y, width, height, ScreenTexts.EMPTY, pressAction, DEFAULT_NARRATION_SUPPLIER);
        this.slotIndex = slotIndex;
    }

    @Nullable
    public State getState() {
        return this.state;
    }

    public void setServer(RealmsServer server) {
        this.state = new State(server, this.slotIndex);
        this.updateTooltip(this.state, server.minigameName);
    }

    private void updateTooltip(State state, @Nullable String minigameName) {
        Text text;
        switch (state.action.ordinal()) {
            case 2: {
                Text text2 = ACTIVE_TOOLTIP;
                break;
            }
            case 1: {
                Text text2;
                if (state.minigame) {
                    text2 = MINIGAME_TOOLTIP;
                    break;
                }
                text2 = TOOLTIP;
                break;
            }
            default: {
                Text text2 = text = null;
            }
        }
        if (text != null) {
            this.setTooltip(Tooltip.of(text));
        }
        MutableText mutableText = Text.literal(state.slotName);
        if (state.minigame && minigameName != null) {
            mutableText = mutableText.append(ScreenTexts.SPACE).append(minigameName);
        }
        this.setMessage(mutableText);
    }

    static Action getAction(RealmsServer server, boolean active, boolean minigame) {
        if (active && !server.expired && server.state != RealmsServer.State.UNINITIALIZED) {
            return Action.JOIN;
        }
        if (!(active || minigame && server.expired)) {
            return Action.SWITCH_SLOT;
        }
        return Action.NOTHING;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean bl2;
        if (this.state == null) {
            return;
        }
        int i = this.getX();
        int j = this.getY();
        boolean bl = this.isSelected();
        Identifier identifier = this.state.minigame ? RealmsTextureManager.getTextureId(String.valueOf(this.state.imageId), this.state.image) : (this.state.empty ? EMPTY_FRAME : (this.state.image != null && this.state.imageId != -1L ? RealmsTextureManager.getTextureId(String.valueOf(this.state.imageId), this.state.image) : (this.slotIndex == 1 ? PANORAMA_0 : (this.slotIndex == 2 ? PANORAMA_2 : (this.slotIndex == 3 ? PANORAMA_3 : EMPTY_FRAME)))));
        if (this.state.isCurrentlyActiveSlot) {
            context.setShaderColor(0.56f, 0.56f, 0.56f, 1.0f);
        }
        context.drawTexture(identifier, i + 3, j + 3, 0.0f, 0.0f, 74, 74, 74, 74);
        boolean bl3 = bl2 = bl && this.state.action != Action.NOTHING;
        if (bl2) {
            context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        } else if (this.state.isCurrentlyActiveSlot) {
            context.setShaderColor(0.8f, 0.8f, 0.8f, 1.0f);
        } else {
            context.setShaderColor(0.56f, 0.56f, 0.56f, 1.0f);
        }
        context.drawGuiTexture(SLOT_FRAME, i, j, 80, 80);
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        if (this.state.isCurrentlyActiveSlot) {
            RenderSystem.enableBlend();
            context.drawGuiTexture(CHECKMARK, i + 67, j + 4, 9, 8);
            RenderSystem.disableBlend();
        }
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        context.drawCenteredTextWithShadow(textRenderer, this.state.slotName, i + 40, j + 66, Colors.WHITE);
        context.drawCenteredTextWithShadow(textRenderer, RealmsMainScreen.getVersionText(this.state.version, this.state.compatibility.isCompatible()), i + 40, j + 80 + 2, Colors.WHITE);
    }

    @Environment(value=EnvType.CLIENT)
    public static class State {
        final boolean isCurrentlyActiveSlot;
        final String slotName;
        final String version;
        final RealmsServer.Compatibility compatibility;
        final long imageId;
        @Nullable
        final String image;
        public final boolean empty;
        public final boolean minigame;
        public final Action action;

        public State(RealmsServer server, int slot) {
            boolean bl = this.minigame = slot == 4;
            if (this.minigame) {
                this.isCurrentlyActiveSlot = server.isMinigame();
                this.slotName = MINIGAME_SLOT_NAME.getString();
                this.imageId = server.minigameId;
                this.image = server.minigameImage;
                this.empty = server.minigameId == -1;
                this.version = "";
                this.compatibility = RealmsServer.Compatibility.UNVERIFIABLE;
            } else {
                RealmsWorldOptions realmsWorldOptions = server.slots.get(slot);
                this.isCurrentlyActiveSlot = server.activeSlot == slot && !server.isMinigame();
                this.slotName = realmsWorldOptions.getSlotName(slot);
                this.imageId = realmsWorldOptions.templateId;
                this.image = realmsWorldOptions.templateImage;
                this.empty = realmsWorldOptions.empty;
                this.version = realmsWorldOptions.version;
                this.compatibility = realmsWorldOptions.compatibility;
            }
            this.action = RealmsWorldSlotButton.getAction(server, this.isCurrentlyActiveSlot, this.minigame);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Action {
        NOTHING,
        SWITCH_SLOT,
        JOIN;

    }
}

