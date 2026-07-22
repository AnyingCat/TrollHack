//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.misc;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Bind;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;

public class GhostBlock extends Module {
   public final Setting<Bind> bind = this.add(new Setting<>("Bind", new Bind(-1)));
   private final Setting<Boolean> hold = this.add(new Setting<>("Hold", false));
   private boolean clicked = false;

   public GhostBlock() {
      super("GhostBlock", "shady addons", Category.MISC);
   }

   @Override
   public void onTick() {
      if (mc.currentScreen == null && this.bind.getValue().isDown()) {
         if (!this.clicked || this.hold.getValue()) {
            RayTraceResult ray = mc.objectMouseOver;
            if (ray != null && ray.typeOfHit == Type.BLOCK) {
               BlockPos blockpos = ray.getBlockPos();
               mc.world.setBlockToAir(blockpos);
            }
         }

         this.clicked = true;
      } else {
         this.clicked = false;
      }
   }
}
