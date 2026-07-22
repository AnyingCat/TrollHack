package me.rebirthclient.asm.accessors;

import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({EntityRenderer.class})
public interface IEntityRenderer {
   @Invoker("setupCameraTransform")
   void invokeSetupCameraTransform(float var1, int var2);

   @Invoker("renderHand")
   void invokeRenderHand(float var1, int var2);
}
