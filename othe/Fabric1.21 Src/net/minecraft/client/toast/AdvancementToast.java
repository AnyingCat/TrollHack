/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.toast;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class AdvancementToast
implements Toast {
    private static final Identifier TEXTURE = Identifier.ofVanilla("toast/advancement");
    public static final int DEFAULT_DURATION_MS = 5000;
    private final AdvancementEntry advancement;
    private boolean soundPlayed;

    public AdvancementToast(AdvancementEntry advancement) {
        this.advancement = advancement;
    }

    @Override
    public Toast.Visibility draw(DrawContext context, ToastManager manager, long startTime) {
        AdvancementDisplay advancementDisplay = this.advancement.value().display().orElse(null);
        context.drawGuiTexture(TEXTURE, 0, 0, this.getWidth(), this.getHeight());
        if (advancementDisplay != null) {
            int i;
            List<OrderedText> list = manager.getClient().textRenderer.wrapLines(advancementDisplay.getTitle(), 125);
            int n = i = advancementDisplay.getFrame() == AdvancementFrame.CHALLENGE ? 0xFF88FF : 0xFFFF00;
            if (list.size() == 1) {
                context.drawText(manager.getClient().textRenderer, advancementDisplay.getFrame().getToastText(), 30, 7, i | Colors.BLACK, false);
                context.drawText(manager.getClient().textRenderer, list.get(0), 30, 18, -1, false);
            } else {
                int j = 1500;
                float f = 300.0f;
                if (startTime < 1500L) {
                    int k = MathHelper.floor(MathHelper.clamp((float)(1500L - startTime) / 300.0f, 0.0f, 1.0f) * 255.0f) << 24 | 0x4000000;
                    context.drawText(manager.getClient().textRenderer, advancementDisplay.getFrame().getToastText(), 30, 11, i | k, false);
                } else {
                    int k = MathHelper.floor(MathHelper.clamp((float)(startTime - 1500L) / 300.0f, 0.0f, 1.0f) * 252.0f) << 24 | 0x4000000;
                    int l = this.getHeight() / 2 - list.size() * manager.getClient().textRenderer.fontHeight / 2;
                    for (OrderedText orderedText : list) {
                        context.drawText(manager.getClient().textRenderer, orderedText, 30, l, 0xFFFFFF | k, false);
                        l += manager.getClient().textRenderer.fontHeight;
                    }
                }
            }
            if (!this.soundPlayed && startTime > 0L) {
                this.soundPlayed = true;
                if (advancementDisplay.getFrame() == AdvancementFrame.CHALLENGE) {
                    manager.getClient().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f));
                }
            }
            context.drawItemWithoutEntity(advancementDisplay.getIcon(), 8, 8);
            return (double)startTime >= 5000.0 * manager.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
        }
        return Toast.Visibility.HIDE;
    }
}

