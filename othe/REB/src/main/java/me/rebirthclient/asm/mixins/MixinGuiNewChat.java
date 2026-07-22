//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.asm.mixins;

import java.util.List;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.mod.modules.impl.client.Chat;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GuiNewChat.class})
public abstract class MixinGuiNewChat extends Gui {
   @Shadow
   public boolean isScrolled;
   private float percentComplete;
   private long prevMillis = System.currentTimeMillis();
   private float animationPercent;

   @Redirect(
      method = {"drawChat"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/gui/GuiNewChat;drawRect(IIIII)V"
)
   )
   private void drawRectHook(int left, int top, int right, int bottom, int color) {
      Chat mod = Chat.INSTANCE;
      ClickGui gui = ClickGui.INSTANCE;
      int rectColor = mod.colorRect.getValue()
         ? (
            gui.rainbow.getValue()
               ? ColorUtil.toARGB(
                  ColorUtil.rainbow(gui.rainbowDelay.getValue()).getRed(),
                  ColorUtil.rainbow(gui.rainbowDelay.getValue()).getGreen(),
                  ColorUtil.rainbow(gui.rainbowDelay.getValue()).getBlue(),
                  45
               )
               : ColorUtil.toARGB(gui.color.getValue().getRed(), gui.color.getValue().getGreen(), gui.color.getValue().getBlue(), 45)
         )
         : color;
      if (mod.isOn()) {
         if (mod.rect.getValue()) {
            Gui.drawRect(left, top, right, bottom, rectColor);
         } else {
            Gui.drawRect(left, top, right, bottom, 0);
         }
      } else {
         Gui.drawRect(left, top, right, bottom, color);
      }
   }

   @Redirect(
      method = {"drawChat"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"
)
   )
   private int drawStringWithShadow(FontRenderer fontRenderer, String text, float x, float y, int color) {
      if (text.contains("§(§)")) {
         Wrapper.mc.fontRenderer.drawStringWithShadow(text, x, y, ColorUtil.injectAlpha(Chat.INSTANCE.color.getValue(), color >> 24 & 0xFF).getRGB());
      } else {
         Wrapper.mc.fontRenderer.drawStringWithShadow(text, x, y, color);
      }

      return 0;
   }

   @Redirect(
      method = {"setChatLine"},
      at = @At(
   value = "INVOKE",
   target = "Ljava/util/List;size()I",
   ordinal = 0,
   remap = false
)
   )
   public int drawnChatLinesSize(List<ChatLine> list) {
      return Chat.INSTANCE.isOn() && Chat.INSTANCE.infinite.getValue() ? -2147483647 : list.size();
   }

   @Redirect(
      method = {"setChatLine"},
      at = @At(
   value = "INVOKE",
   target = "Ljava/util/List;size()I",
   ordinal = 2,
   remap = false
)
   )
   public int chatLinesSize(List<ChatLine> list) {
      return Chat.INSTANCE.isOn() && Chat.INSTANCE.infinite.getValue() ? -2147483647 : list.size();
   }

   @Shadow
   public float getChatScale() {
      return Wrapper.mc.gameSettings.chatScale;
   }

   private void updatePercentage(long diff) {
      if (this.percentComplete < 1.0F) {
         this.percentComplete += 0.004F * (float)diff;
      }

      this.percentComplete = MathUtil.clamp(this.percentComplete, 0.0F, 1.0F);
   }

   @Inject(
      method = {"drawChat"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void modifyChatRendering(CallbackInfo ci) {
      long current = System.currentTimeMillis();
      long diff = current - this.prevMillis;
      this.prevMillis = current;
      this.updatePercentage(diff);
      float t = this.percentComplete;
      this.animationPercent = MathUtil.clamp(1.0F - --t * t * t * t, 0.0F, 1.0F);
   }

   @Inject(
      method = {"drawChat"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/renderer/GlStateManager;pushMatrix()V",
   ordinal = 0,
   shift = At.Shift.AFTER
)}
   )
   private void translate(CallbackInfo ci) {
      float y = 1.0F;
      if (!this.isScrolled && Chat.INSTANCE.isOn() && Chat.INSTANCE.animation.getValue()) {
         y += (9.0F - 9.0F * this.animationPercent) * this.getChatScale();
      }

      GlStateManager.translate(0.0F, y, 0.0F);
   }

   @ModifyArg(
      method = {"drawChat"},
      at = @At(
   value = "INVOKE",
   target = "Ljava/util/List;get(I)Ljava/lang/Object;",
   ordinal = 0,
   remap = false
),
      index = 0
   )
   private int getLineBeingDrawn(int line) {
      return line;
   }

   @Inject(
      method = {"printChatMessageWithOptionalDeletion"},
      at = {@At("HEAD")}
   )
   private void resetPercentage(CallbackInfo ci) {
      this.percentComplete = 0.0F;
   }

   @ModifyVariable(
      method = {"setChatLine"},
      at = @At("STORE"),
      ordinal = 0
   )
   private List<ITextComponent> setNewLines(List<ITextComponent> original) {
      return original;
   }
}
