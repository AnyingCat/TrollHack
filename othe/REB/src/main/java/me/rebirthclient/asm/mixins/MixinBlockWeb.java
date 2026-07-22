//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.asm.mixins;

import javax.annotation.Nullable;
import me.rebirthclient.mod.modules.impl.movement.AntiWeb;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin({BlockWeb.class})
public class MixinBlockWeb extends Block {
   protected MixinBlockWeb() {
      super(Material.WEB);
   }

   /**
    * @author makecat
    * @reason makecat
    */
   @Nullable
   @Overwrite
   public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
      return AntiWeb.INSTANCE.isOn() && AntiWeb.INSTANCE.antiModeSetting.getValue() == AntiWeb.AntiMode.Block ? FULL_BLOCK_AABB : NULL_AABB;
   }
}
