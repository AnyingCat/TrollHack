//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.movement;

import java.util.Objects;
import me.rebirthclient.api.events.impl.MoveEvent;
import me.rebirthclient.api.events.impl.UpdateWalkingPlayerEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class Strafe extends Module {
   public static Strafe INSTANCE = new Strafe();
   public final Setting<Strafe.Mode> mode = this.add(new Setting<>("Mode", Strafe.Mode.Normal));
   private final Setting<Boolean> jump = this.add(new Setting<>("Jump", false));
   private final Setting<Double> jumpMotion = this.add(new Setting<>("JumpMotion", 0.40123128, 0.1, 1.0));
   private final Setting<Float> multiplier = this.add(new Setting<>("Factor", 1.67F, 0.0F, 3.0F));
   private final Setting<Float> plier = this.add(new Setting<>("Factor+", 2.149F, 0.0F, 3.0F));
   private final Setting<Float> Dist = this.add(new Setting<>("Dist", 0.6896F, 0.1F, 1.0F));
   private final Setting<Float> multiDist = this.add(new Setting<>("Dist+", 0.795F, 0.1F, 1.0F));
   private final Setting<Float> SPEEDY = this.add(new Setting<>("SpeedY", 730.0F, 500.0F, 800.0F));
   private final Setting<Float> SPEEDH = this.add(new Setting<>("SpeedH", 159.0F, 100.0F, 300.0F));
   private final Setting<Float> StrafeH = this.add(new Setting<>("SpeedH", 0.993F, 0.1F, 1.0F));
   private final Setting<Float> StrafeY = this.add(new Setting<>("SpeedY", 0.99F, 0.1F, 1.2F));
   int stage;
   private double lastDist;
   private double moveSpeed;

   public Strafe() {
      super("Strafe", "Modifies sprinting", Category.MOVEMENT);
      INSTANCE = this;
   }

   @Override
   public String getInfo() {
      return this.mode.getValue().name();
   }

   @SubscribeEvent
   public void onUpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent event) {
      if (!fullNullCheck()) {
         this.lastDist = Math.sqrt(
            (mc.player.posX - mc.player.prevPosX) * (mc.player.posX - mc.player.prevPosX)
               + (mc.player.posZ - mc.player.prevPosZ) * (mc.player.posZ - mc.player.prevPosZ)
         );
      }
   }

   @SubscribeEvent
   public void onStrafe(MoveEvent event) {
      if (!fullNullCheck()) {
         if (!HoleSnap.INSTANCE.isOn()) {
            if (!mc.player.isInWater() && !mc.player.isInLava()) {
               if (mc.player.onGround) {
                  this.stage = 2;
               }

               if (this.stage == 0) {
                  ++this.stage;
                  this.lastDist = 0.0;
               } else if (this.stage == 2) {
                  double motionY = this.jumpMotion.getValue();
                  if (mc.player.onGround && this.jump.getValue() || mc.gameSettings.keyBindJump.isKeyDown()) {
                     if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                        motionY += (double)(
                           (float)(((PotionEffect)Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST))).getAmplifier() + 1) * 0.1F
                        );
                     }

                     mc.player.motionY = motionY;
                     event.setY(mc.player.motionY);
                     this.moveSpeed *= (double)(this.mode.getValue() == Strafe.Mode.Normal ? this.multiplier.getValue() : this.plier.getValue()).floatValue();
                  }
               } else if (this.stage == 3) {
                  this.moveSpeed = this.lastDist
                     - (double)(this.mode.getValue() == Strafe.Mode.Normal ? this.Dist.getValue() : this.multiDist.getValue()).floatValue()
                        * (this.lastDist - this.getBaseMoveSpeed());
               } else {
                  if ((
                        mc.world
                                 .getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, mc.player.motionY, 0.0))
                                 .size()
                              > 0
                           || mc.player.collidedVertically
                     )
                     && this.stage > 0) {
                     this.stage = mc.player.moveForward == 0.0F && mc.player.moveStrafing == 0.0F ? 0 : 1;
                  }

                  this.moveSpeed = this.lastDist
                     - this.lastDist / (double)(this.mode.getValue() == Strafe.Mode.Normal ? this.SPEEDY.getValue() : this.SPEEDH.getValue()).floatValue();
               }

               this.moveSpeed = !mc.gameSettings.keyBindJump.isKeyDown()
                     && (
                        !InventoryMove.INSTANCE.isOn()
                           || !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())
                           || mc.currentScreen instanceof GuiChat
                     )
                     && mc.player.onGround
                  ? this.getBaseMoveSpeed()
                  : Math.max(this.moveSpeed, this.getBaseMoveSpeed());
               double n = (double)mc.player.movementInput.moveForward;
               double n2 = (double)mc.player.movementInput.moveStrafe;
               double n3 = (double)mc.player.rotationYaw;
               if (n == 0.0 && n2 == 0.0) {
                  event.setX(0.0);
                  event.setZ(0.0);
               } else if (n != 0.0 && n2 != 0.0) {
                  n *= Math.sin(Math.PI / 4);
                  n2 *= Math.cos(Math.PI / 4);
               }

               double n4 = this.mode.getValue() == Strafe.Mode.Normal
                  ? (double)this.StrafeH.getValue().floatValue()
                  : (double)this.StrafeY.getValue().floatValue();
               event.setX((n * this.moveSpeed * -Math.sin(Math.toRadians(n3)) + n2 * this.moveSpeed * Math.cos(Math.toRadians(n3))) * n4);
               event.setZ((n * this.moveSpeed * Math.cos(Math.toRadians(n3)) - n2 * this.moveSpeed * -Math.sin(Math.toRadians(n3))) * n4);
               ++this.stage;
               event.setCanceled(false);
            }
         }
      }
   }

   public double getBaseMoveSpeed() {
      double n = 0.2873;
      if (mc.player.isPotionActive(MobEffects.SPEED)) {
         n *= 1.0 + 0.2 * (double)(((PotionEffect)Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SPEED))).getAmplifier() + 1);
      }

      return n;
   }

   public static enum Mode {
      Normal,
      Strict;
   }
}
