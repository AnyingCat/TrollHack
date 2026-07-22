/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.text.Text;
import net.minecraft.util.ProgressListener;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ProgressScreen
extends Screen
implements ProgressListener {
    @Nullable
    private Text title;
    @Nullable
    private Text task;
    private int progress;
    private boolean done;
    private final boolean closeAfterFinished;

    public ProgressScreen(boolean closeAfterFinished) {
        super(NarratorManager.EMPTY);
        this.closeAfterFinished = closeAfterFinished;
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
    public void setTitle(Text title) {
        this.setTitleAndTask(title);
    }

    @Override
    public void setTitleAndTask(Text title) {
        this.title = title;
        this.setTask(Text.translatable("menu.working"));
    }

    @Override
    public void setTask(Text task) {
        this.task = task;
        this.progressStagePercentage(0);
    }

    @Override
    public void progressStagePercentage(int percentage) {
        this.progress = percentage;
    }

    @Override
    public void setDone() {
        this.done = true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.done) {
            if (this.closeAfterFinished) {
                this.client.setScreen(null);
            }
            return;
        }
        super.render(context, mouseX, mouseY, delta);
        if (this.title != null) {
            context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 70, 0xFFFFFF);
        }
        if (this.task != null && this.progress != 0) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.empty().append(this.task).append(" " + this.progress + "%"), this.width / 2, 90, 0xFFFFFF);
        }
    }
}

