//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Color;
import java.util.HashMap;
import me.rebirthclient.api.events.impl.DamageBlockEvent;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.util.FadeUtils;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.misc.TabFriends;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BreakESP extends Module {
   static final HashMap<EntityPlayer, BreakESP.MinePosition> MineMap = new HashMap<>();
   public static BreakESP INSTANCE = new BreakESP();
   private final Setting<Boolean> renderAir = this.add(new Setting<>("RenderAir", true));
   private final Setting<Boolean> renderSelf = this.add(new Setting<>("OneSelf", true));
   private final Setting<Boolean> renderUnknown = this.add(new Setting<>("RenderUnknown", true));
   private final Setting<Double> range = this.add(new Setting<>("Range", 15.0, 0.0, 50.0));
   private final Setting<Boolean> renderName = this.add(new Setting<>("RenderName", true).setParent());
   private final Setting<TabFriends.FriendColor> nameColor = this.add(new Setting<>("Color", TabFriends.FriendColor.Gray, v -> this.renderName.isOpen()));
   private final Setting<Boolean> renderProgress = this.add(new Setting<>("Progress", true, v -> this.renderName.isOpen()).setParent());
   private final Setting<BreakESP.Mode> animationMode = this.add(new Setting<>("AnimationMode", BreakESP.Mode.Up));
   private final Setting<Integer> animationTime = this.add(new Setting<>("AnimationTime", 1000, 0, 5000));
   private final Setting<Boolean> box = this.add(new Setting<>("Box", true).setParent());
   private final Setting<Integer> boxAlpha = this.add(new Setting<>("BoxAlpha", 30, 0, 255, v -> this.box.isOpen()));
   private final Setting<Boolean> outline = this.add(new Setting<>("Outline", true).setParent());
   private final Setting<Integer> outlineAlpha = this.add(new Setting<>("OutlineAlpha", 100, 0, 255, v -> this.outline.isOpen()));
   private final Setting<Color> color = this.add(new Setting<>("Color", new Color(255, 255, 255, 100)).hideAlpha());
   private final Setting<Color> doubleRender = this.add(new Setting<>("Double", new Color(255, 255, 255, 100)).injectBoolean(true).hideAlpha());

   public BreakESP() {
      super("BreakESP", "Show mine postion", Category.RENDER);
      INSTANCE = this;
   }

   @Override
   public void onDisable() {
      MineMap.clear();
   }

   @SubscribeEvent
   public void BlockBreak(DamageBlockEvent event) {
      if (event.getPosition().getY() != -1) {
         EntityPlayer breaker = (EntityPlayer)mc.world.getEntityByID(event.getBreakerId());
         if (breaker != null
            && !(
               breaker.getDistance(
                     (double)event.getPosition().getX() + 0.5,
                     (double)event.getPosition().getY(),
                     (double)event.getPosition().getZ() + 0.5
                  )
                  > 7.0
            )) {
            if (!MineMap.containsKey(breaker)) {
               MineMap.put(breaker, new BreakESP.MinePosition(breaker));
            }

            MineMap.get(breaker).update(event.getPosition());
         }
      }
   }

   @Override
   public void onRender3D(Render3DEvent event) {
      EntityPlayer[] array = MineMap.keySet().toArray(new EntityPlayer[0]);

      for(EntityPlayer entityPlayer : array) {
         if (entityPlayer != null && !entityPlayer.isEntityAlive()) {
            MineMap.remove(entityPlayer);
         }
      }

      MineMap.values()
         .forEach(
            miner -> {
               if (!this.renderAir.getValue() && mc.world.isAirBlock(miner.first)) {
                  miner.finishFirst();
               }
      
               if ((!miner.firstFinished || this.renderAir.getValue())
                  && (miner.miner != mc.player || this.renderSelf.getValue())
                  && miner.first.getDistance((int)mc.player.posX, (int)mc.player.posY, (int)mc.player.posZ)
                     < this.range.getValue()
                  && (miner.miner != null || this.renderUnknown.getValue())) {
                  this.draw(miner.first, miner.firstFade.easeOutQuad(), this.color.getValue());
                  if (this.renderName.getValue()) {
                     double x = (double)miner.first.getX() - mc.getRenderManager().renderPosX + 0.5;
                     double y = (double)miner.first.getY() - mc.getRenderManager().renderPosY - 1.0;
                     double z = (double)miner.first.getZ() - mc.getRenderManager().renderPosZ + 0.5;
                     RenderUtil.drawText(
                        ChatFormatting.GRAY + (miner.miner == null ? this.getColor() + "Unknown" : this.getColor() + miner.miner.getName()),
                        x,
                        this.renderProgress.getValue() ? y + 0.15 : y,
                        z,
                        new Color(255, 255, 255, 255)
                     );
                     if (this.renderProgress.getValue()) {
                        if (mc.world.isAirBlock(miner.first)) {
                           RenderUtil.drawText(ChatFormatting.GREEN + "Broke", x, y - 0.15, z, new Color(255, 255, 255, 255));
                        } else {
                           RenderUtil.drawText(ChatFormatting.GREEN + "Breaking", x, y - 0.15, z, new Color(255, 255, 255, 255));
                        }
                     }
                  }
               }
      
               if ((miner.miner != mc.player || this.renderSelf.getValue()) && !miner.secondFinished && miner.second != null) {
                  if (mc.world.isAirBlock(miner.second)) {
                     miner.finishSecond();
                  } else if (!miner.second.equals(miner.first)
                     && miner.second
                           .getDistance((int)mc.player.posX, (int)mc.player.posY, (int)mc.player.posZ)
                        < this.range.getValue()
                     && (miner.miner != null || this.renderUnknown.getValue())
                     && this.doubleRender.booleanValue) {
                     this.draw(miner.second, miner.secondFade.easeOutQuad(), this.doubleRender.getValue());
                     if (this.renderName.getValue()) {
                        double x = (double)miner.second.getX() - mc.getRenderManager().renderPosX + 0.5;
                        double y = (double)miner.second.getY() - mc.getRenderManager().renderPosY - 1.0;
                        double z = (double)miner.second.getZ() - mc.getRenderManager().renderPosZ + 0.5;
                        RenderUtil.drawText(
                           ChatFormatting.GRAY + (miner.miner == null ? this.getColor() + "Unknown" : this.getColor() + miner.miner.getName()),
                           x,
                           y + 0.15,
                           z,
                           new Color(255, 255, 255, 255)
                        );
                        RenderUtil.drawText(ChatFormatting.GOLD + "Double", x, y - 0.15, z, new Color(255, 255, 255, 255));
                     }
                  }
               }
            }
         );
   }

   public String getColor() {
      switch((TabFriends.FriendColor)this.nameColor.getValue()) {
         case White:
            return "§f";
         case DarkRed:
            return "§4";
         case Red:
            return "§c";
         case Gold:
            return "§6";
         case Yellow:
            return "§e";
         case DarkGreen:
            return "§2";
         case Green:
            return "§a";
         case Aqua:
            return "§b";
         case DarkAqua:
            return "§3";
         case DarkBlue:
            return "§1";
         case Blue:
            return "§9";
         case LightPurple:
            return "§d";
         case DarkPurple:
            return "§5";
         case Gray:
            return "§7";
         case DarkGray:
            return "§8";
         case Black:
            return "§0";
         default:
            return "";
      }
   }

   public void draw(BlockPos pos, double size, Color color) {
      if (this.animationMode.getValue() != BreakESP.Mode.Both) {
         AxisAlignedBB axisAlignedBB;
         if (this.animationMode.getValue() == BreakESP.Mode.InToOut) {
            axisAlignedBB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos).grow(size / 2.0 - 0.5);
         } else if (this.animationMode.getValue() == BreakESP.Mode.Up) {
            AxisAlignedBB bb = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
            axisAlignedBB = new AxisAlignedBB(
               bb.minX,
               bb.minY,
               bb.minZ,
               bb.maxX,
               bb.minY + (bb.maxY - bb.minY) * size,
               bb.maxZ
            );
         } else if (this.animationMode.getValue() == BreakESP.Mode.Down) {
            AxisAlignedBB bb = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
            axisAlignedBB = new AxisAlignedBB(
               bb.minX,
               bb.maxY - (bb.maxY - bb.minY) * size,
               bb.minZ,
               bb.maxX,
               bb.maxY,
               bb.maxZ
            );
         } else if (this.animationMode.getValue() == BreakESP.Mode.None) {
            axisAlignedBB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
         } else {
            AxisAlignedBB bb = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos).grow(size / 2.0 - 0.5);
            AxisAlignedBB bb2 = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
            if (this.animationMode.getValue() == BreakESP.Mode.Horizontal) {
               axisAlignedBB = new AxisAlignedBB(
                  bb2.minX, bb.minY, bb2.minZ, bb2.maxX, bb.maxY, bb2.maxZ
               );
            } else {
               axisAlignedBB = new AxisAlignedBB(bb.minX, bb2.minY, bb.minZ, bb.maxX, bb2.maxY, bb.maxZ);
            }
         }

         if (this.outline.getValue()) {
            RenderUtil.drawBBBox(axisAlignedBB, color, this.outlineAlpha.getValue());
         }

         if (this.box.getValue()) {
            RenderUtil.drawBBFill(axisAlignedBB, color, this.boxAlpha.getValue());
         }
      } else {
         AxisAlignedBB axisAlignedBB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos).grow(size / 2.0 - 0.5);
         if (this.outline.getValue()) {
            RenderUtil.drawBBBox(axisAlignedBB, color, this.outlineAlpha.getValue());
         }

         if (this.box.getValue()) {
            RenderUtil.drawBBFill(axisAlignedBB, color, this.boxAlpha.getValue());
         }

         axisAlignedBB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos).grow(-Math.abs(size / 2.0 - 1.0));
         if (this.outline.getValue()) {
            RenderUtil.drawBBBox(axisAlignedBB, color, this.outlineAlpha.getValue());
         }

         if (this.box.getValue()) {
            RenderUtil.drawBBFill(axisAlignedBB, color, this.boxAlpha.getValue());
         }
      }
   }

   private static class MinePosition {
      public final EntityPlayer miner;
      public FadeUtils firstFade = new FadeUtils((long)BreakESP.INSTANCE.animationTime.getValue().intValue());
      public FadeUtils secondFade = new FadeUtils((long)BreakESP.INSTANCE.animationTime.getValue().intValue());
      public BlockPos first;
      public BlockPos second;
      public boolean secondFinished;
      public boolean firstFinished;

      public MinePosition(EntityPlayer player) {
         this.miner = player;
         this.secondFinished = true;
      }

      public void finishSecond() {
         this.secondFinished = true;
      }

      public void finishFirst() {
         this.firstFinished = true;
      }

      public void update(BlockPos pos) {
         if (this.first == null || !this.first.equals(pos) || !BreakESP.INSTANCE.renderAir.getValue()) {
            if (this.secondFinished || this.second == null) {
               this.second = pos;
               this.secondFinished = false;
               this.secondFade = new FadeUtils((long)BreakESP.INSTANCE.animationTime.getValue().intValue());
            }

            if (this.first == null || !this.first.equals(pos) || this.firstFinished) {
               this.firstFade = new FadeUtils((long)BreakESP.INSTANCE.animationTime.getValue().intValue());
            }

            this.firstFinished = false;
            this.first = pos;
         }
      }
   }

   public static enum Mode {
      Down,
      Up,
      InToOut,
      Both,
      Vertical,
      Horizontal,
      None;
   }
}
