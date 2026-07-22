//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.asm.mixins;

import me.rebirthclient.api.events.impl.FreecamEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.modules.impl.render.ESP2D;
import me.rebirthclient.mod.modules.impl.render.Models;
import me.rebirthclient.mod.modules.impl.render.NameTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({RenderPlayer.class})
public class MixinRenderPlayer {
   private final ResourceLocation amongUs = new ResourceLocation("textures/rebirth/models/amongus.png");
   private final ResourceLocation rabbit = new ResourceLocation("textures/rebirth/models/rabbit.png");
   private final ResourceLocation freddy = new ResourceLocation("textures/rebirth/models/freddy.png");

   @Inject(
      method = {"renderEntityName*"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void renderEntityNameHook(AbstractClientPlayer entityIn, double x, double y, double z, String name, double distanceSq, CallbackInfo info) {
      if (NameTags.INSTANCE.isOn() || ESP2D.INSTANCE.isOn()) {
         info.cancel();
      }
   }

   @Redirect(
      method = {"doRender"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/entity/AbstractClientPlayer;isUser()Z"
)
   )
   private boolean isUserRedirect(AbstractClientPlayer abstractClientPlayer) {
      Minecraft mc = Minecraft.getMinecraft();
      FreecamEvent event = new FreecamEvent();
      MinecraftForge.EVENT_BUS.post(event);
      if (!event.isCanceled()) {
         return abstractClientPlayer.isUser();
      } else {
         return abstractClientPlayer.isUser() && abstractClientPlayer == mc.getRenderViewEntity();
      }
   }

   @Inject(
      method = {"getEntityTexture"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void getEntityTexture(AbstractClientPlayer entity, CallbackInfoReturnable<ResourceLocation> ci) {
      if (Models.INSTANCE.isOn()
         && (
            !Models.INSTANCE.onlySelf.getValue()
               || entity == Minecraft.getMinecraft().player
               || Managers.FRIENDS.isFriend(entity.getName()) && Models.INSTANCE.friends.getValue()
         )) {
         if (Models.INSTANCE.Mode.getValue() == Models.mode.AmongUs) {
            ci.setReturnValue(this.amongUs);
         }

         if (Models.INSTANCE.Mode.getValue() == Models.mode.Rabbit) {
            ci.setReturnValue(this.rabbit);
         }

         if (Models.INSTANCE.Mode.getValue() == Models.mode.Freddy) {
            ci.setReturnValue(this.freddy);
         }
      } else {
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         ci.setReturnValue(entity.getLocationSkin());
      }
   }
}
