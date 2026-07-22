package me.rebirthclient.asm.accessors;

import java.util.Map;
import java.util.UUID;
import net.minecraft.client.gui.BossInfoClient;
import net.minecraft.client.gui.GuiBossOverlay;
import net.minecraft.world.BossInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({GuiBossOverlay.class})
public interface IGuiBossOverlay {
   @Accessor("mapBossInfos")
   Map<UUID, BossInfoClient> getMapBossInfos();

   @Invoker("render")
   void invokeRender(int var1, int var2, BossInfo var3);
}
