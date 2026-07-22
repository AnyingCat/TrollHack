/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.screen;

import java.util.function.BooleanSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DownloadingTerrainScreen
extends Screen {
    private static final Text TEXT = Text.translatable("multiplayer.downloadingTerrain");
    private static final long MIN_LOAD_TIME_MS = 30000L;
    private final long loadStartTime;
    private final BooleanSupplier shouldClose;
    private final WorldEntryReason worldEntryReason;
    @Nullable
    private Sprite backgroundSprite;

    public DownloadingTerrainScreen(BooleanSupplier shouldClose, WorldEntryReason worldEntryReason) {
        super(NarratorManager.EMPTY);
        this.shouldClose = shouldClose;
        this.worldEntryReason = worldEntryReason;
        this.loadStartTime = System.currentTimeMillis();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected boolean hasUsageText() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, TEXT, this.width / 2, this.height / 2 - 50, 0xFFFFFF);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        switch (this.worldEntryReason.ordinal()) {
            case 2: {
                this.renderPanoramaBackground(context, delta);
                this.applyBlur(delta);
                this.renderDarkening(context);
                break;
            }
            case 0: {
                context.drawSprite(0, 0, -90, context.getScaledWindowWidth(), context.getScaledWindowHeight(), this.getBackgroundSprite());
                break;
            }
            case 1: {
                context.fillWithLayer(RenderLayer.getEndPortal(), 0, 0, this.width, this.height, 0);
            }
        }
    }

    private Sprite getBackgroundSprite() {
        if (this.backgroundSprite != null) {
            return this.backgroundSprite;
        }
        this.backgroundSprite = this.client.getBlockRenderManager().getModels().getModelParticleSprite(Blocks.NETHER_PORTAL.getDefaultState());
        return this.backgroundSprite;
    }

    @Override
    public void tick() {
        if (this.shouldClose.getAsBoolean() || System.currentTimeMillis() > this.loadStartTime + 30000L) {
            this.close();
        }
    }

    @Override
    public void close() {
        this.client.getNarratorManager().narrate(Text.translatable("narrator.ready_to_play"));
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum WorldEntryReason {
        NETHER_PORTAL,
        END_PORTAL,
        OTHER;

    }
}

