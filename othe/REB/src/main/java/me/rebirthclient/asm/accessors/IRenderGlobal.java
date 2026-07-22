package me.rebirthclient.asm.accessors;

import java.util.Map;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.shader.ShaderGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({RenderGlobal.class})
public interface IRenderGlobal {
   @Accessor("entityOutlineShader")
   ShaderGroup getEntityOutlineShader();

   @Accessor("damagedBlocks")
   Map<Integer, DestroyBlockProgress> getDamagedBlocks();
}
