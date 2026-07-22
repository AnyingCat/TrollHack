package me.rebirthclient.asm.accessors;

import java.util.List;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ShaderGroup.class})
public interface IShaderGroup {
   @Accessor("listShaders")
   List<Shader> getListShaders();

   @Accessor("mainFramebuffer")
   Framebuffer getMainFramebuffer();
}
