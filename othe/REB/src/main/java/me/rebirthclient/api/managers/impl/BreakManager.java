package me.rebirthclient.api.managers.impl;

import java.util.HashMap;
import me.rebirthclient.api.util.Wrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class BreakManager implements Wrapper {
   public static final HashMap<EntityPlayer, BlockPos> MineMap = new HashMap();

   public static boolean isMine(BlockPos pos) {
      for(EntityPlayer i : MineMap.keySet()) {
         if (i != null) {
            BlockPos pos2 = (BlockPos)MineMap.get(i);
            if (pos2 != null && new BlockPos(pos2).equals(new BlockPos(pos))) {
               return true;
            }
         }
      }

      return false;
   }
}
