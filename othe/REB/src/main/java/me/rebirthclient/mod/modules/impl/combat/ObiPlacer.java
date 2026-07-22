//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat;

import java.awt.Color;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.DamageUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.FadeUtils;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class ObiPlacer extends Module {
   public final Setting<Boolean> render = this.add(new Setting<>("Render", true).setParent());
   public final Setting<Boolean> outline = this.add(new Setting<>("Outline", true, v -> this.render.isOpen()).setParent());
   public final Setting<Integer> outlineAlpha = this.add(new Setting<>("OutlineAlpha", 150, 0, 255, v -> this.render.isOpen() && this.outline.isOpen()));
   public final Setting<Boolean> box = this.add(new Setting<>("Box", true, v -> this.render.isOpen()).setParent());
   public final Setting<Integer> boxAlpha = this.add(new Setting<>("BoxAlpha", 70, 0, 255, v -> this.render.isOpen() && this.box.isOpen()));
   private final Setting<Color> color = this.add(new Setting<>("Color", new Color(255, 255, 255), v -> this.render.isOpen()).hideAlpha());
   private final Setting<Integer> renderTime = this.add(new Setting<>("RenderTime", 3000, 0, 5000, v -> this.render.isOpen()));
   private final Setting<Integer> shrinkTime = this.add(new Setting<>("ShrinkTime", 600, 0, 5000, v -> this.render.isOpen()));
   private FadeUtils shrinkTimer = new FadeUtils((long)this.shrinkTime.getValue().intValue());
   private final Timer delayTimer = new Timer();
   private final Timer renderTimer = new Timer();
   private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
   private final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
   private final Setting<Float> range = this.add(new Setting<>("Range", 6.0F, 0.0F, 10.0F));
   private final Setting<Float> wallRange = this.add(new Setting<>("WallsRange", 3.5F, 0.0F, 10.0F));
   private final Setting<Integer> placeDelay = this.add(new Setting<>("PlaceDelay", 100, 0, 2000));
   private final Setting<Float> minDmg = this.add(new Setting<>("MinDmg", 6.0F, 0.0F, 10.0F));
   private final Setting<Float> placeRange = this.add(new Setting<>("PlaceRange", 4.0F, 1.0F, 6.0F));
   BlockPos placePos;

   public ObiPlacer() {
      super("ObiPlacer", "auto place obi of crystal", Category.COMBAT);
   }

   @Override
   public void onUpdate() {
      EntityPlayer target = CombatUtil.getTarget((double)this.range.getValue().floatValue());
      if (this.delayTimer.passedMs((long)this.placeDelay.getValue().intValue()) && target != null) {
         this.placePos = this.getPlaceTarget(target);
         if (this.placePos != null && BlockUtil.canPlace(this.placePos)) {
            this.shrinkTimer = new FadeUtils((long)this.shrinkTime.getValue().intValue());
            this.delayTimer.reset();
            this.renderTimer.reset();
            this.placeBlock(this.placePos);
         }
      }
   }

   @Override
   public void onRender3D(Render3DEvent event) {
      if (this.placePos != null
         && !this.renderTimer.passedMs((long)this.renderTime.getValue().intValue())
         && !this.renderTimer.passedMs((long)this.renderTime.getValue().intValue())
         && this.render.getValue()) {
         if (mc.world.getBlockState(this.placePos).getBlock() != Blocks.AIR) {
            if (mc.world.getBlockState(this.placePos).getBlock() != Blocks.FIRE) {
               AxisAlignedBB axisAlignedBB = mc.world
                  .getBlockState(this.placePos)
                  .getSelectedBoundingBox(mc.world, this.placePos)
                  .grow(this.shrinkTimer.easeInQuad() / 2.0 - 1.0);
               if (this.outline.getValue()) {
                  RenderUtil.drawBBBox(axisAlignedBB, this.color.getValue(), this.outlineAlpha.getValue());
               }

               if (this.box.getValue()) {
                  RenderUtil.drawBBFill(axisAlignedBB, this.color.getValue(), this.boxAlpha.getValue());
               }
            }
         }
      }
   }

   private void placeBlock(BlockPos pos) {
      if (BlockUtil.canPlace(pos)) {
         int old = mc.player.inventory.currentItem;
         if (InventoryUtil.findHotbarClass(BlockObsidian.class) != -1) {
            InventoryUtil.doSwap(InventoryUtil.findHotbarClass(BlockObsidian.class));
            BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
            InventoryUtil.doSwap(old);
         }
      }
   }

   @Override
   public String getInfo() {
      if (fullNullCheck()) {
         return null;
      } else {
         EntityPlayer target = CombatUtil.getTarget((double)this.range.getValue().floatValue());
         return target != null ? target.getName() : null;
      }
   }

   private boolean canPlaceCrystal(BlockPos pos) {
      BlockPos obsPos = pos.down();
      BlockPos boost = obsPos.up();
      BlockPos boost2 = obsPos.up(2);
      return (this.getBlock(obsPos) == Blocks.BEDROCK || this.getBlock(obsPos) == Blocks.OBSIDIAN)
         && (this.getBlock(boost) == Blocks.AIR || this.getBlock(boost) == Blocks.FIRE && CombatSetting.INSTANCE.placeInFire.getValue())
         && this.getBlock(boost2) == Blocks.AIR
         && !this.checkEntity(boost2)
         && !this.checkEntity(boost);
   }

   private Block getBlock(BlockPos pos) {
      return mc.world.getBlockState(pos).getBlock();
   }

   private boolean checkEntity(BlockPos pos) {
      for(Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
         if (!entity.isDead && !(entity instanceof EntityEnderCrystal)) {
            return true;
         }
      }

      return false;
   }

   private BlockPos getPlaceTarget(Entity target) {
      for(BlockPos pos : BlockUtil.getBox(5.0F, EntityUtil.getEntityPos(target).down())) {
         if (this.canPlaceCrystal(pos)
            && !CrystalAura.behindWall(pos.up(), (double)this.wallRange.getValue().floatValue())
            && !(
               mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5)
                  > (double)this.placeRange.getValue().floatValue()
            )) {
            float damage = DamageUtil.calculateDamage(pos.down(), target);
            if (damage > this.minDmg.getValue()) {
               return null;
            }
         }
      }

      BlockPos bestPos = null;
      float bestDamage = 0.0F;

      for(BlockPos pos : BlockUtil.getBox(this.range.getValue())) {
         if (mc.world.isAirBlock(pos)
            && mc.world.isAirBlock(pos.up())
            && mc.world.isAirBlock(pos.up(2))
            && !(
               mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5)
                  > (double)this.placeRange.getValue().floatValue()
            )
            && !this.checkEntity(pos.up())
            && !this.checkEntity(pos.up(2))
            && BlockUtil.canPlace(pos)
            && pos.getY() < new BlockPos(target.posX, target.posY + 0.5, target.posZ).getY()) {
            float damage = DamageUtil.calculateDamage(pos, target);
            if (!(damage < this.minDmg.getValue()) && !CrystalAura.behindWall(pos.up(), (double)this.wallRange.getValue().floatValue())) {
               if (bestPos == null) {
                  bestDamage = damage;
                  bestPos = pos;
               } else if (!(damage < bestDamage)) {
                  bestDamage = damage;
                  bestPos = pos;
               }
            }
         }
      }

      return bestPos;
   }
}
