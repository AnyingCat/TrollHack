//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map.Entry;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.math.BlockPos;

public class Search extends Module {
   private final Setting<Float> range = this.add(new Setting<>("Range", 50.0F, 1.0F, 300.0F));
   private final Setting<Boolean> portal = this.add(new Setting<>("Portal", true));
   private final Setting<Boolean> chest = this.add(new Setting<>("Chest", true));
   private final Setting<Boolean> dispenser = this.add(new Setting<>("Dispenser", false));
   private final Setting<Boolean> shulker = this.add(new Setting<>("Shulker", true));
   private final Setting<Boolean> echest = this.add(new Setting<>("Ender Chest", false));
   private final Setting<Boolean> hopper = this.add(new Setting<>("Hopper", false));
   private final Setting<Boolean> cart = this.add(new Setting<>("Minecart", false));
   private final Setting<Boolean> frame = this.add(new Setting<>("Item Frame", false));
   private final Setting<Boolean> box = this.add(new Setting<>("Box", false));
   private final Setting<Integer> boxAlpha = this.add(new Setting<>("BoxAlpha", 125, 0, 255, v -> this.box.getValue()));
   private final Setting<Boolean> outline = this.add(new Setting<>("Outline", true));
   private final Setting<Float> lineWidth = this.add(new Setting<>("LineWidth", 1.0F, 0.1F, 5.0F, v -> this.outline.getValue()));

   public Search() {
      super("Search", "Highlights Containers", Category.RENDER);
   }

   @Override
   public void onRender3D(Render3DEvent event) {
      HashMap<BlockPos, Integer> positions = new HashMap<>();

      int color;
      BlockPos pos;
      for(TileEntity tileEntity : mc.world.loadedTileEntityList) {
         if ((
               tileEntity instanceof TileEntityEndPortal && this.portal.getValue()
                  || tileEntity instanceof TileEntityChest && this.chest.getValue()
                  || tileEntity instanceof TileEntityDispenser && this.dispenser.getValue()
                  || tileEntity instanceof TileEntityShulkerBox && this.shulker.getValue()
                  || tileEntity instanceof TileEntityEnderChest && this.echest.getValue()
                  || tileEntity instanceof TileEntityHopper && this.hopper.getValue()
            )
            && mc.player.getDistanceSq(pos = tileEntity.getPos()) <= MathUtil.square((double)this.range.getValue().floatValue())
            && (color = this.getTileEntityColor(tileEntity)) != -1) {
            positions.put(pos, color);
         }
      }

      for(Entity entity : mc.world.loadedEntityList) {
         if ((entity instanceof EntityItemFrame && this.frame.getValue() || entity instanceof EntityMinecartChest && this.cart.getValue())
            && mc.player.getDistanceSq(pos = entity.getPosition()) <= MathUtil.square((double)this.range.getValue().floatValue())
            && (color = this.getEntityColor(entity)) != -1) {
            positions.put(pos, color);
         }
      }

      for(Entry<BlockPos, Integer> entry : positions.entrySet()) {
         BlockPos blockPos = entry.getKey();
         color = entry.getValue();
         RenderUtil.drawBoxESP(
            blockPos,
            new Color(color),
            false,
            new Color(color),
            this.lineWidth.getValue(),
            this.outline.getValue(),
            this.box.getValue(),
            this.boxAlpha.getValue(),
            false
         );
      }
   }

   private int getTileEntityColor(TileEntity tileEntity) {
      if (tileEntity instanceof TileEntityChest) {
         return ColorUtil.Colors.ORANGE;
      } else if (tileEntity instanceof TileEntityShulkerBox) {
         return ColorUtil.Colors.WHITE;
      } else if (tileEntity instanceof TileEntityEndPortal) {
         return ColorUtil.Colors.GRAY;
      } else if (tileEntity instanceof TileEntityEnderChest) {
         return ColorUtil.Colors.PURPLE;
      } else if (tileEntity instanceof TileEntityHopper) {
         return ColorUtil.Colors.DARK_RED;
      } else {
         return tileEntity instanceof TileEntityDispenser ? ColorUtil.Colors.ORANGE : -1;
      }
   }

   private int getEntityColor(Entity entity) {
      if (entity instanceof EntityMinecartChest) {
         return ColorUtil.Colors.ORANGE;
      } else {
         return entity instanceof EntityItemFrame && ((EntityItemFrame)entity).getDisplayedItem().getItem() instanceof ItemShulkerBox
            ? ColorUtil.Colors.WHITE
            : -1;
      }
   }
}
