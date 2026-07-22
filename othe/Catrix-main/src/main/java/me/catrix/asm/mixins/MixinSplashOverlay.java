package me.catrix.asm.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import me.catrix.api.utils.render.Render2DUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(SplashOverlay.class)
public abstract class MixinSplashOverlay {
    @Unique
    private static final Identifier bg = new Identifier("textures/bg.png");
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    @Final
    @Shadow
    private boolean reloading;

    @Shadow
    private float progress;

    @Shadow
    private long reloadCompleteTime = -1L;

    @Shadow
    private long reloadStartTime = -1L;

    @Final
    @Shadow
    private ResourceReload reload;

    @Final
    @Shadow
    private Consumer<Optional<Throwable>> exceptionHandler;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ci.cancel();
        renderCustom(context);
    }

    @Unique
    private void renderCustom(DrawContext context) {
        if (reloading && reloadStartTime == -1L) {
            reloadStartTime = Util.getMeasuringTimeMs();
        }
        float timeSinceComplete = reloadCompleteTime > -1L ? (float) (Util.getMeasuringTimeMs() - reloadCompleteTime) / 1000.0F : -1.0F;
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        context.drawTexture(bg, 0, 0, 0, 0, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight(), mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
        progress = MathHelper.clamp(progress * 0.95F + reload.getProgress() * 0.050000012F, 0.0F, 1.0F);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.3F);
        Render2DUtil.drawRect(context.getMatrices(), 0, mc.getWindow().getScaledHeight() - 5, mc.getWindow().getScaledWidth(), 5, new Color(0x86000000, true));
        int filledWidth = (int) (mc.getWindow().getScaledWidth() * progress);
        if (filledWidth > 0) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Render2DUtil.drawRect(context.getMatrices(), 0, mc.getWindow().getScaledHeight() - 5, filledWidth, 7, new Color(0xA0FFFFFF, true));
        }
        RenderSystem.disableBlend();
        if (timeSinceComplete >= 2.0F) {
            mc.setOverlay(null);
        }
        if (reloadCompleteTime == -1L && reload.isComplete() && (!reloading || (reloadStartTime > -1L && (float) (Util.getMeasuringTimeMs() - reloadStartTime) / 500.0F >= 2.0F))) {
            try {
                reload.throwException();
                exceptionHandler.accept(Optional.empty());
            } catch (Throwable throwable) {
                exceptionHandler.accept(Optional.of(throwable));
            }
            reloadCompleteTime = Util.getMeasuringTimeMs();
            if (mc.currentScreen != null) {
                mc.currentScreen.init(mc, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
            }
        }
    }
}