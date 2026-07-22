package github.trollhack.mixins;

import github.trollhack.modules.impl.render.ShulkerPreview;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen {

    @Shadow
    protected ScreenHandler handler;

    @Shadow
    protected Slot focusedSlot;

    @Inject(method = "drawMouseoverTooltip", at = @At("HEAD"), cancellable = true)
    private void onDrawMouseoverTooltip(DrawContext context, int x, int y, CallbackInfo ci) {
        if (ShulkerPreview.INSTANCE.isEnabled()
            && this.handler.getCursorStack().isEmpty()
            && this.focusedSlot != null
            && this.focusedSlot.hasStack()) {
            ItemStack stack = this.focusedSlot.getStack();
            ContainerComponent container = ShulkerPreview.getShulkerData(stack);
            if (container != null) {
                ci.cancel();
                ShulkerPreview.renderShulkerAndItems(context, stack, x, y, container);
            }
        }
    }
}
