/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.hud;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record ChatHudLine(int creationTick, Text content, @Nullable MessageSignatureData signature, @Nullable MessageIndicator indicator) {
    @Nullable
    public MessageIndicator.Icon getIcon() {
        return this.indicator != null ? this.indicator.icon() : null;
    }

    @Nullable
    public MessageSignatureData signature() {
        return this.signature;
    }

    @Nullable
    public MessageIndicator indicator() {
        return this.indicator;
    }

    @Environment(value=EnvType.CLIENT)
    public record Visible(int addedTime, OrderedText content, @Nullable MessageIndicator indicator, boolean endOfEntry) {
        @Nullable
        public MessageIndicator indicator() {
            return this.indicator;
        }
    }
}

