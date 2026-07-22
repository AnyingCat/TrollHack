//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.api.events.impl.StepEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.managers.impl.SneakManager;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.MovementUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.exploit.BlockClip;
import me.rebirthclient.mod.modules.impl.exploit.Clip;
import me.rebirthclient.mod.modules.impl.player.Freecam;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.entity.passive.EntityMule;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NewStep extends Module {
   public static NewStep INSTANCE;
   private final Timer stepTimer = new Timer();
   public Setting<Float> height = this.register(new Setting<>("Height", 2.0F, 1.0F, 2.5F));
   public Setting<Boolean> entityStep = this.add(new Setting<>("EntityStep", false));
   public Setting<Boolean> useTimer = this.add(new Setting<>("Timer", true));
   public Setting<Boolean> strict = this.add(new Setting<>("Strict", false));
   private final Setting<Boolean> pauseBurrow = this.add(new Setting<>("PauseBurrow", true));
   private final Setting<Boolean> pauseSneak = this.add(new Setting<>("PauseSneak", true));
   private final Setting<Boolean> pauseWeb = this.add(new Setting<>("PauseWeb", true));
   private final Setting<Boolean> onlyMoving = this.add(new Setting<>("OnlyMoving", true));
   public Setting<Integer> stepDelay = this.register(new Setting<>("StepDelay", 200, 0, 1000));
   private final Setting<NewStep.Mode> mode = this.add(new Setting<>("Mode", NewStep.Mode.NORMAL));
   public static boolean timer;
   private Entity entityRiding;

   public NewStep() {
      super("NewStep", "褏芯写懈褌褜 锌芯 斜谢芯泻邪屑 1 懈谢�? 2 斜谢芯泻�?", Category.MOVEMENT);
      INSTANCE = this;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      mc.player.stepHeight = 0.6F;
      if (this.entityRiding != null) {
         if (!(this.entityRiding instanceof EntityHorse)
            && !(this.entityRiding instanceof EntityLlama)
            && !(this.entityRiding instanceof EntityMule)
            && (!(this.entityRiding instanceof EntityPig) || !this.entityRiding.isBeingRidden() || !((EntityPig)this.entityRiding).canBeSteered())) {
            this.entityRiding.stepHeight = 0.5F;
         } else {
            this.entityRiding.stepHeight = 1.0F;
         }
      }
   }

   @Override
   public void onUpdate() {
      if ((
            mc.world.getBlockState(EntityUtil.getPlayerPos()).getBlock() == Blocks.PISTON_HEAD
               || mc.world.getBlockState(EntityUtil.getPlayerPos()).getBlock() == Blocks.OBSIDIAN
               || mc.world.getBlockState(EntityUtil.getPlayerPos()).getBlock() == Blocks.ENDER_CHEST
               || mc.world.getBlockState(EntityUtil.getPlayerPos()).getBlock() == Blocks.BEDROCK
         )
         && this.pauseBurrow.getValue()) {
         mc.player.stepHeight = 0.6F;
      } else if ((
            mc.world.getBlockState(EntityUtil.getPlayerPos().up()).getBlock() == Blocks.PISTON_HEAD
               || mc.world.getBlockState(EntityUtil.getPlayerPos().up()).getBlock() == Blocks.OBSIDIAN
               || mc.world.getBlockState(EntityUtil.getPlayerPos().up()).getBlock() == Blocks.ENDER_CHEST
               || mc.world.getBlockState(EntityUtil.getPlayerPos().up()).getBlock() == Blocks.BEDROCK
         )
         && this.pauseBurrow.getValue()) {
         mc.player.stepHeight = 0.6F;
      } else if (this.pauseWeb.getValue() && mc.player.isInWeb) {
         mc.player.stepHeight = 0.6F;
      } else if (SneakManager.isSneaking && this.pauseSneak.getValue()) {
         mc.player.stepHeight = 0.6F;
      } else if (this.onlyMoving.getValue() && !MovementUtil.isMoving() && HoleSnap.INSTANCE.isOff()) {
         mc.player.stepHeight = 0.6F;
      } else if (Clip.INSTANCE.isOn() || BlockClip.INSTANCE.isOn()) {
         mc.player.stepHeight = 0.6F;
      } else if (mc.player.capabilities.isFlying || Freecam.INSTANCE.isOn()) {
         mc.player.stepHeight = 0.6F;
      } else if (EntityUtil.isInLiquid()) {
         mc.player.stepHeight = 0.6F;
      } else {
         if (timer && mc.player.onGround) {
            Managers.TIMER.set(1.0F);
            timer = false;
         }

         if (mc.player.onGround && this.stepTimer.passedMs((long)this.stepDelay.getValue().intValue())) {
            if (mc.player.isRiding() && mc.player.getRidingEntity() != null) {
               this.entityRiding = mc.player.getRidingEntity();
               if (this.entityStep.getValue()) {
                  mc.player.getRidingEntity().stepHeight = this.height.getValue();
               }
            } else {
               mc.player.stepHeight = this.height.getValue();
            }
         } else if (mc.player.isRiding() && mc.player.getRidingEntity() != null) {
            this.entityRiding = mc.player.getRidingEntity();
            if (this.entityRiding != null) {
               if (!(this.entityRiding instanceof EntityHorse)
                  && !(this.entityRiding instanceof EntityLlama)
                  && !(this.entityRiding instanceof EntityMule)
                  && (!(this.entityRiding instanceof EntityPig) || !this.entityRiding.isBeingRidden() || !((EntityPig)this.entityRiding).canBeSteered())) {
                  this.entityRiding.stepHeight = 0.5F;
               } else {
                  this.entityRiding.stepHeight = 1.0F;
               }
            }
         } else {
            mc.player.stepHeight = 0.6F;
         }
      }
   }

   @SubscribeEvent
   public void onStep(StepEvent event) {
      if (this.mode.getValue().equals(NewStep.Mode.NORMAL)) {
         double stepHeight = event.getAxisAlignedBB().minY - mc.player.posY;
         if (stepHeight <= 0.0 || stepHeight > (double)this.height.getValue().floatValue()) {
            return;
         }

         double[] offsets = this.getOffset(stepHeight);
         if (offsets != null && offsets.length > 1) {
            if (this.useTimer.getValue()) {
               Managers.TIMER.set(1.0F / (float)offsets.length);
               timer = true;
            }

            for(double offset : offsets) {
               mc.player
                  .connection
                  .sendPacket(new Position(mc.player.posX, mc.player.posY + offset, mc.player.posZ, false));
            }
         }

         this.stepTimer.reset();
      }
   }

   public double[] getOffset(double height) {
      if (height == 0.75) {
         return this.strict.getValue() ? new double[]{0.42, 0.753, 0.75} : new double[]{0.42, 0.753};
      } else if (height == 0.8125) {
         return this.strict.getValue() ? new double[]{0.39, 0.7, 0.8125} : new double[]{0.39, 0.7};
      } else if (height == 0.875) {
         return this.strict.getValue() ? new double[]{0.39, 0.7, 0.875} : new double[]{0.39, 0.7};
      } else if (height == 1.0) {
         return this.strict.getValue() ? new double[]{0.42, 0.753, 1.0} : new double[]{0.42, 0.753};
      } else if (height == 1.5) {
         return new double[]{0.42, 0.75, 1.0, 1.16, 1.23, 1.2};
      } else if (height == 2.0) {
         return new double[]{0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43};
      } else {
         return height == 2.5 ? new double[]{0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907} : null;
      }
   }

   public static enum Mode {
      NORMAL,
      VANILLA;
   }
}
