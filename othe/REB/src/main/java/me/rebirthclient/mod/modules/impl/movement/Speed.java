//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.movement;

import java.util.List;
import java.util.Objects;
import me.rebirthclient.api.events.impl.MotionEvent;
import me.rebirthclient.api.events.impl.MoveEvent;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.PushEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.MovementUtil;
import me.rebirthclient.api.util.PositionUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.exploit.PacketFly;
import me.rebirthclient.mod.modules.impl.player.Freecam;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Speed extends Module {
   protected final Setting<Speed.SpeedMode> mode = this.register(new Setting<>("Mode", Speed.SpeedMode.Instant));
   protected final Setting<Boolean> jump = this.register(new Setting<>("Jump", true, v -> this.mode.getValue() == Speed.SpeedMode.Strafe));
   protected final Setting<Boolean> inWater = this.register(new Setting<>("InWater", false));
   protected final Setting<Double> strafeSpeed = this.register(
      new Setting<>("StrafeSpeed", 287.3, 100.0, 1000.0, v -> this.mode.getValue() == Speed.SpeedMode.Strafe)
   );
   protected final Setting<Double> speedSet = this.register(new Setting<>("Speed", 4.0, 0.1, 10.0, v -> this.mode.getValue() == Speed.SpeedMode.Vanilla));
   protected final Setting<Integer> constTicks = this.register(
      new Setting<>("ConstTicks", 10, 1, 40, v -> this.mode.getValue() == Speed.SpeedMode.Constantiam)
   );
   protected final Setting<Integer> constOff = this.register(new Setting<>("ConstOff", 3, 1, 10, v -> this.mode.getValue() == Speed.SpeedMode.Constantiam));
   protected final Setting<Double> constFactor = this.register(
      new Setting<>("ConstFactor", 2.149, 1.0, 5.0, v -> this.mode.getValue() == Speed.SpeedMode.Constantiam)
   );
   protected final Setting<Boolean> useTimer = this.register(
      new Setting<>("UseTimer", false, v -> this.mode.getValue() == Speed.SpeedMode.Strafe || this.mode.getValue() == Speed.SpeedMode.LowHop)
   );
   protected final Setting<Boolean> noWaterInstant = this.register(
      new Setting<>("NoLiquidInstant", false, v -> this.mode.getValue() == Speed.SpeedMode.Instant)
   );
   protected final Setting<Boolean> explosions = this.register(new Setting<>("Explosions", false));
   protected final Setting<Boolean> velocity = this.register(new Setting<>("Velocity", false));
   protected final Setting<Float> multiplier = this.register(new Setting<>("H-Factor", 1.0F, 0.0F, 5.0F));
   protected final Setting<Float> vertical = this.register(new Setting<>("V-Factor", 1.0F, 0.0F, 5.0F));
   protected final Setting<Integer> coolDown = this.register(new Setting<>("CoolDown", 1000, 0, 5000));
   protected final Setting<Boolean> directional = this.register(new Setting<>("Directional", false));
   protected final Setting<Boolean> lagOut = this.register(new Setting<>("LagOutBlocks", false));
   protected final Setting<Integer> lagTime = this.register(new Setting<>("LagTime", 500, 0, 1000, v -> this.mode.getValue() == Speed.SpeedMode.Strafe));
   protected final Setting<Double> cap = this.register(new Setting<>("Cap", 10.0, 0.0, 10.0));
   protected final Setting<Boolean> scaleCap = this.register(new Setting<>("ScaleCap", false));
   protected final Setting<Boolean> slow = this.register(new Setting<>("Slowness", false));
   protected final Setting<Boolean> modify = this.register(new Setting<>("Modify", false).setParent());
   protected final Setting<Double> xzFactor = this.register(new Setting<>("XZ-Factor", 1.0, 0.0, 5.0, v -> this.modify.isOpen()));
   protected final Setting<Double> yFactor = this.register(new Setting<>("Y-Factor", 1.0, 0.0, 5.0, v -> this.modify.isOpen()));
   protected final Timer expTimer = new Timer();
   protected final Timer lagTimer = new Timer();
   protected boolean stop;
   protected int vanillaStage;
   protected int onGroundStage;
   protected int oldGroundStage;
   protected double speed;
   protected double distance;
   protected int gayStage;
   protected int stage;
   protected int ncpStage;
   protected int bhopStage;
   protected int vStage;
   protected int lowStage;
   protected int constStage;
   protected double lastExp;
   protected double lastDist;
   protected boolean boost;

   public Speed() {
      super("Speed", "3ar", Category.MOVEMENT);
   }

   @Override
   public String getInfo() {
      return this.mode.getValue().toString();
   }

   @Override
   public void onEnable() {
      if (mc.player != null) {
         this.speed = MovementUtil.getSpeed();
         this.distance = MovementUtil.getDistance2D();
      }

      this.vanillaStage = 0;
      this.onGroundStage = 2;
      this.oldGroundStage = 2;
      this.ncpStage = 0;
      this.gayStage = 1;
      this.vStage = 1;
      this.bhopStage = 4;
      this.stage = 4;
      this.lowStage = 4;
      this.lastDist = 0.0;
      this.constStage = 0;
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public void invoke(PacketEvent.Receive event) {
      if (event.getPacket() instanceof SPacketEntityVelocity) {
         SPacketEntityVelocity packet = event.getPacket();
         EntityPlayerSP player = mc.player;
         if (player != null && packet.getEntityID() == player.getEntityId() && !this.directional.getValue() && this.velocity.getValue()) {
            double speed = Math.sqrt((double)(packet.getMotionX() * packet.getMotionX() + packet.getMotionZ() * packet.getMotionZ())) / 8000.0;
            this.lastExp = this.expTimer.passed((long)this.coolDown.getValue().intValue()) ? speed : speed - this.lastExp;
            if (this.lastExp > 0.0) {
               this.expTimer.reset();
               mc.addScheduledTask(() -> {
                  this.speed += this.lastExp * (double)this.multiplier.getValue().floatValue();
                  this.distance += this.lastExp * (double)this.multiplier.getValue().floatValue();
                  if (mc.player.motionY > 0.0 && this.vertical.getValue() != 0.0F) {
                     mc.player.motionY *= (double)this.vertical.getValue().floatValue();
                  }
               });
            }
         }
      } else if (event.getPacket() instanceof SPacketPlayerPosLook) {
         this.lagTimer.reset();
         if (mc.player != null) {
            this.distance = 0.0;
         }

         this.speed = 0.0;
         this.vanillaStage = 0;
         this.onGroundStage = 2;
         this.ncpStage = 1;
         this.gayStage = 1;
         this.vStage = 1;
         this.bhopStage = 4;
         this.stage = 4;
         this.lowStage = 4;
         this.constStage = 0;
      } else if (event.getPacket() instanceof SPacketExplosion && this.explosions.getValue() && MovementUtil.isMoving()) {
         SPacketExplosion packet = event.getPacket();
         BlockPos pos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
         if (mc.player.getDistanceSq(pos) < 100.0
            && (!this.directional.getValue() || !MovementUtil.isInMovementDirection(packet.getX(), packet.getY(), packet.getZ()))) {
            double speed = Math.sqrt((double)(packet.getMotionX() * packet.getMotionX() + packet.getMotionZ() * packet.getMotionZ()));
            this.lastExp = this.expTimer.passed((long)this.coolDown.getValue().intValue()) ? speed : speed - this.lastExp;
            if (this.lastExp > 0.0) {
               this.expTimer.reset();
               mc.addScheduledTask(() -> {
                  this.speed += this.lastExp * (double)this.multiplier.getValue().floatValue();
                  this.distance += this.lastExp * (double)this.multiplier.getValue().floatValue();
                  if (mc.player.motionY > 0.0) {
                     mc.player.motionY *= (double)this.vertical.getValue().floatValue();
                  }
               });
            }
         }
      }
   }

   @SubscribeEvent
   public void invoke(MotionEvent event) {
      if (!PacketFly.INSTANCE.isOn() && !Freecam.INSTANCE.isOn()) {
         if (MovementUtil.noMovementKeys()) {
            mc.player.motionX = 0.0;
            mc.player.motionZ = 0.0;
         }

         if (this.mode.getValue() == Speed.SpeedMode.OldGround) {
            switch(event.getStage()) {
               case 0:
                  if (this.notColliding()) {
                     ++this.oldGroundStage;
                  } else {
                     this.oldGroundStage = 0;
                  }

                  if (this.oldGroundStage == 4) {
                     event.setY(event.getY() + (PositionUtil.isBoxColliding() ? 0.2 : 0.4) + MovementUtil.getJumpSpeed());
                  }
                  break;
               case 1:
                  if (this.oldGroundStage == 3) {
                     mc.player.motionX *= 3.25;
                     mc.player.motionZ *= 3.25;
                  } else if (this.oldGroundStage == 4) {
                     mc.player.motionX /= 1.4;
                     mc.player.motionZ /= 1.4;
                     this.oldGroundStage = 2;
                  }
            }
         }

         this.distance = MovementUtil.getDistance2D();
         if (this.mode.getValue() == Speed.SpeedMode.OnGround && this.onGroundStage == 3) {
            event.setY(event.getY() + (PositionUtil.isBoxColliding() ? 0.2 : 0.4) + MovementUtil.getJumpSpeed());
         }
      }
   }

   @SubscribeEvent
   public void invoke(MoveEvent event) {
      if (!PacketFly.INSTANCE.isOn() && !Freecam.INSTANCE.isOn()) {
         if ((this.inWater.getValue() || !PositionUtil.inLiquid() && !PositionUtil.inLiquid(true))
            && !mc.player.isOnLadder()
            && !mc.player.isEntityInsideOpaqueBlock()) {
            if (this.stop) {
               this.stop = false;
            } else {
               this.move(this.mode.getValue(), event);
               if (this.modify.getValue()) {
                  event.setX(event.getX() * this.xzFactor.getValue());
                  event.setY(event.getY() * this.yFactor.getValue());
                  event.setZ(event.getZ() * this.xzFactor.getValue());
               }
            }
         } else {
            this.stop = true;
         }
      }
   }

   @Override
   public void onDisable() {
      Managers.TIMER.reset();
   }

   private void move(Speed.SpeedMode mode, MoveEvent event) {
      switch(mode) {
         case Instant:
            if (mc.player.isElytraFlying()) {
               return;
            }

            if (LongJump.INSTANCE.isOn()) {
               return;
            }

            if (!this.noWaterInstant.getValue() || !mc.player.isInWater() && !mc.player.isInLava()) {
               MovementUtil.strafe(event, MovementUtil.getSpeed(this.slow.getValue()));
            }
         case OldGround:
         case None:
         default:
            break;
         case OnGround:
            if (mc.player.onGround || this.onGroundStage == 3) {
               if (!mc.player.collidedHorizontally && mc.player.moveForward != 0.0F || mc.player.moveStrafing != 0.0F) {
                  if (this.onGroundStage == 2) {
                     this.speed *= 2.149;
                     this.onGroundStage = 3;
                  } else if (this.onGroundStage == 3) {
                     this.onGroundStage = 2;
                     this.speed = this.distance - 0.66 * (this.distance - MovementUtil.getSpeed(this.slow.getValue()));
                  } else if (PositionUtil.isBoxColliding() || mc.player.collidedVertically) {
                     this.onGroundStage = 1;
                  }
               }

               this.speed = Math.min(this.speed, this.getCap());
               this.speed = Math.max(this.speed, MovementUtil.getSpeed(this.slow.getValue()));
               MovementUtil.strafe(event, this.speed);
            }
            break;
         case Vanilla:
            MovementUtil.strafe(event, this.speedSet.getValue() / 10.0);
            break;
         case NCP:
            if (mc.player.isElytraFlying()) {
               return;
            }

            if (LongJump.INSTANCE.isOn()) {
               return;
            }

            switch(this.ncpStage) {
               case 0:
                  ++this.ncpStage;
                  this.lastDist = 0.0;
                  break;
               case 1:
               default:
                  if ((
                        mc.world.getCollisionBoxes(null, mc.player.getEntityBoundingBox().offset(0.0, mc.player.motionY, 0.0)).size()
                              > 0
                           || mc.player.collidedVertically
                     )
                     && this.ncpStage > 0) {
                     this.ncpStage = mc.player.moveForward == 0.0F && mc.player.moveStrafing == 0.0F ? 0 : 1;
                  }

                  this.speed = this.lastDist - this.lastDist / 159.0;
                  break;
               case 2:
                  if ((mc.player.moveForward != 0.0F || mc.player.moveStrafing != 0.0F) && mc.player.onGround) {
                     event.setY(mc.player.motionY = (PositionUtil.isBoxColliding() ? 0.2 : 0.3999) + MovementUtil.getJumpSpeed());
                     this.speed *= 2.149;
                  }
                  break;
               case 3:
                  this.speed = this.lastDist - 0.7095 * (this.lastDist - MovementUtil.getSpeed(this.slow.getValue()));
            }

            this.speed = Math.min(this.speed, this.getCap());
            this.speed = Math.max(this.speed, MovementUtil.getSpeed(this.slow.getValue()));
            MovementUtil.strafe(event, this.speed);
            ++this.ncpStage;
            break;
         case Strafe:
            if (!MovementUtil.isMoving()) {
               return;
            }

            if (mc.player.isElytraFlying()) {
               return;
            }

            if (LongJump.INSTANCE.isOn()) {
               return;
            }

            if (!this.lagTimer.passedMs((long)this.lagTime.getValue().intValue())) {
               return;
            }

            if (this.useTimer.getValue() && this.lagTimer.passedMs(250L)) {
               if (Managers.TIMER.get() == 1.0F) {
                  Managers.TIMER.set(1.0888F);
               }
            } else if (Managers.TIMER.get() == 1.0888F) {
               Managers.TIMER.reset();
            }

            if (this.stage == 1 && MovementUtil.isMoving()) {
               this.speed = 1.35 * MovementUtil.getSpeed(this.slow.getValue(), this.strafeSpeed.getValue() / 1000.0) - 0.01;
            } else if (this.stage != 2 || !MovementUtil.isMoving() || !MovementUtil.isJumping() && !this.jump.getValue()) {
               if (this.stage == 3) {
                  this.speed = this.distance - 0.66 * (this.distance - MovementUtil.getSpeed(this.slow.getValue(), this.strafeSpeed.getValue() / 1000.0));
                  this.boost = !this.boost;
               } else {
                  if ((
                        mc.world.getCollisionBoxes(null, mc.player.getEntityBoundingBox().offset(0.0, mc.player.motionY, 0.0)).size()
                              > 0
                           || mc.player.collidedVertically
                     )
                     && this.stage > 0) {
                     this.stage = MovementUtil.isMoving() ? 1 : 0;
                  }

                  this.speed = this.distance - this.distance / 159.0;
               }
            } else {
               double yMotion = 0.3999 + MovementUtil.getJumpSpeed();
               mc.player.motionY = yMotion;
               event.setY(yMotion);
               this.speed *= this.boost ? 1.6835 : 1.395;
            }

            this.speed = Math.min(this.speed, this.getCap());
            this.speed = Math.max(this.speed, MovementUtil.getSpeed(this.slow.getValue(), this.strafeSpeed.getValue() / 1000.0));
            MovementUtil.strafe(event, this.speed);
            if (MovementUtil.isMoving()) {
               ++this.stage;
            }
            break;
         case GayHop:
            if (!this.lagTimer.passedMs(100L)) {
               this.gayStage = 1;
               return;
            }

            if (!MovementUtil.isMoving()) {
               this.speed = MovementUtil.getSpeed(this.slow.getValue());
            }

            if (this.gayStage == 1 && mc.player.collidedVertically && MovementUtil.isMoving()) {
               this.speed = 0.25 + MovementUtil.getSpeed(this.slow.getValue()) - 0.01;
            } else if (this.gayStage == 2 && mc.player.collidedVertically && MovementUtil.isMoving()) {
               double yMotion = (PositionUtil.isBoxColliding() ? 0.2 : 0.4) + MovementUtil.getJumpSpeed();
               mc.player.motionY = yMotion;
               event.setY(yMotion);
               this.speed *= 2.149;
            } else if (this.gayStage == 3) {
               this.speed = this.distance - 0.66 * (this.distance - MovementUtil.getSpeed(this.slow.getValue()));
            } else {
               if (mc.player.onGround && this.gayStage > 0) {
                  if (1.35 * MovementUtil.getSpeed(this.slow.getValue()) - 0.01 > this.speed) {
                     this.gayStage = 0;
                  } else {
                     this.gayStage = MovementUtil.isMoving() ? 1 : 0;
                  }
               }

               this.speed = this.distance - this.distance / 159.0;
            }

            this.speed = Math.min(this.speed, this.getCap());
            this.speed = Math.max(this.speed, MovementUtil.getSpeed(this.slow.getValue()));
            if (this.gayStage > 0) {
               MovementUtil.strafe(event, this.speed);
            }

            if (MovementUtil.isMoving()) {
               ++this.gayStage;
            }
            break;
         case Bhop:
            if (!this.lagTimer.passedMs(100L)) {
               this.bhopStage = 4;
               return;
            }

            if (MathUtil.round(mc.player.posY - (double)((int)mc.player.posY), 3) == MathUtil.round(0.138, 3)) {
               mc.player.motionY -= 0.08 + MovementUtil.getJumpSpeed();
               event.setY(event.getY() - (0.0931 + MovementUtil.getJumpSpeed()));
               mc.player.posY -= 0.0931 + MovementUtil.getJumpSpeed();
            }

            if ((double)this.bhopStage == 2.0 && MovementUtil.isMoving()) {
               double yMotion = (PositionUtil.isBoxColliding() ? 0.2 : 0.4) + MovementUtil.getJumpSpeed();
               mc.player.motionY = yMotion;
               event.setY(yMotion);
               this.speed *= 2.149;
            } else if ((double)this.bhopStage == 3.0) {
               this.speed = this.distance - 0.66 * (this.distance - MovementUtil.getSpeed(this.slow.getValue()));
            } else {
               if (mc.player.onGround) {
                  this.bhopStage = 1;
               }

               this.speed = this.distance - this.distance / 159.0;
            }

            this.speed = Math.min(this.speed, this.getCap());
            this.speed = Math.max(this.speed, MovementUtil.getSpeed(this.slow.getValue()));
            MovementUtil.strafe(event, this.speed);
            ++this.bhopStage;
            break;
         case VHop:
            if (!this.lagTimer.passedMs(100L)) {
               this.vStage = 1;
               return;
            }

            if (!MovementUtil.isMoving()) {
               this.speed = MovementUtil.getSpeed(this.slow.getValue());
            }

            if (MathUtil.round(mc.player.posY - (double)((int)mc.player.posY), 3) == MathUtil.round(0.4, 3)) {
               event.setY(mc.player.motionY = 0.31 + MovementUtil.getJumpSpeed());
            } else if (MathUtil.round(mc.player.posY - (double)((int)mc.player.posY), 3) == MathUtil.round(0.71, 3)) {
               event.setY(mc.player.motionY = 0.04 + MovementUtil.getJumpSpeed());
            } else if (MathUtil.round(mc.player.posY - (double)((int)mc.player.posY), 3) == MathUtil.round(0.75, 3)) {
               event.setY(mc.player.motionY = -0.2 + MovementUtil.getJumpSpeed());
            }

            if (mc.world.getCollisionBoxes(null, mc.player.getEntityBoundingBox().offset(0.0, -0.56, 0.0)).size() > 0
               && MathUtil.round(mc.player.posY - (double)((int)mc.player.posY), 3) == MathUtil.round(0.55, 3)) {
               event.setY(mc.player.motionY = -0.14 + MovementUtil.getJumpSpeed());
            }

            if (this.vStage == 1 && mc.player.collidedVertically && MovementUtil.isMoving()) {
               this.speed = 2.0 * MovementUtil.getSpeed(this.slow.getValue()) - 0.01;
            } else if (this.vStage == 2 && mc.player.collidedVertically && MovementUtil.isMoving()) {
               event.setY(mc.player.motionY = (PositionUtil.isBoxColliding() ? 0.2 : 0.4) + MovementUtil.getJumpSpeed());
               this.speed *= 2.149;
            } else if (this.vStage == 3) {
               this.speed = this.distance - 0.66 * (this.distance - MovementUtil.getSpeed(this.slow.getValue()));
            } else {
               if (mc.player.onGround && this.vStage > 0) {
                  if (1.35 * MovementUtil.getSpeed(this.slow.getValue()) - 0.01 > this.speed) {
                     this.vStage = 0;
                  } else {
                     this.vStage = MovementUtil.isMoving() ? 1 : 0;
                  }
               }

               this.speed = this.distance - this.distance / 159.0;
            }

            if (this.vStage > 8) {
               this.speed = MovementUtil.getSpeed(this.slow.getValue());
            }

            this.speed = Math.min(this.speed, this.getCap());
            this.speed = Math.max(this.speed, MovementUtil.getSpeed(this.slow.getValue()));
            if (this.vStage > 0) {
               MovementUtil.strafe(event, this.speed);
            }

            if (MovementUtil.isMoving()) {
               ++this.vStage;
            }
            break;
         case LowHop:
            if (!this.lagTimer.passedMs(100L)) {
               return;
            }

            if (this.useTimer.getValue() && this.lagTimer.passedMs(250L)) {
               if (Managers.TIMER.get() == 1.0F) {
                  Managers.TIMER.set(1.0888F);
               }
            } else if (Managers.TIMER.get() == 1.0888F) {
               Managers.TIMER.reset();
            }

            if (!mc.player.collidedHorizontally) {
               if (MathUtil.round(mc.player.posY - (double)((int)mc.player.posY), 3) == MathUtil.round(0.4, 3)) {
                  event.setY(mc.player.motionY = 0.31 + MovementUtil.getJumpSpeed());
               } else if (MathUtil.round(mc.player.posY - (double)((int)mc.player.posY), 3) == MathUtil.round(0.71, 3)) {
                  event.setY(mc.player.motionY = 0.04 + MovementUtil.getJumpSpeed());
               } else if (MathUtil.round(mc.player.posY - (double)((int)mc.player.posY), 3) == MathUtil.round(0.75, 3)) {
                  event.setY(mc.player.motionY = -0.2 - MovementUtil.getJumpSpeed());
               } else if (MathUtil.round(mc.player.posY - (double)((int)mc.player.posY), 3) == MathUtil.round(0.55, 3)) {
                  event.setY(mc.player.motionY = -0.14 + MovementUtil.getJumpSpeed());
               } else if (MathUtil.round(mc.player.posY - (double)((int)mc.player.posY), 3) == MathUtil.round(0.41, 3)) {
                  event.setY(mc.player.motionY = -0.2 + MovementUtil.getJumpSpeed());
               }
            }

            if (this.lowStage == 1 && MovementUtil.isMoving()) {
               this.speed = 1.35 * MovementUtil.getSpeed(this.slow.getValue()) - 0.01;
            } else if (this.lowStage == 2 && MovementUtil.isMoving()) {
               event.setY(mc.player.motionY = (PositionUtil.isBoxColliding() ? 0.2 : 0.3999) + MovementUtil.getJumpSpeed());
               this.speed *= this.boost ? 1.5685 : 1.3445;
            } else if (this.lowStage == 3) {
               this.speed = this.distance - 0.66 * (this.distance - MovementUtil.getSpeed(this.slow.getValue()));
               this.boost = !this.boost;
            } else {
               if (mc.player.onGround && this.lowStage > 0) {
                  this.lowStage = MovementUtil.isMoving() ? 1 : 0;
               }

               this.speed = this.distance - this.distance / 159.0;
            }

            this.speed = Math.min(this.speed, this.getCap());
            this.speed = Math.max(this.speed, MovementUtil.getSpeed(this.slow.getValue()));
            MovementUtil.strafe(event, this.speed);
            if (MovementUtil.isMoving()) {
               ++this.lowStage;
            }
            break;
         case Constantiam:
            if (!this.lagTimer.passedMs(100L)) {
               this.constStage = 0;
               return;
            }

            if (!MovementUtil.isMoving()) {
               this.speed = MovementUtil.getSpeed(this.slow.getValue());
            }

            if (this.constStage == 0 && MovementUtil.isMoving() && mc.player.onGround) {
               this.speed = 0.08;
            } else if (this.constStage == 1 && mc.player.collidedVertically && MovementUtil.isMoving()) {
               this.speed = 0.25 + MovementUtil.getSpeed(this.slow.getValue()) - 0.01;
            } else if (this.constStage == 2 && mc.player.collidedVertically && MovementUtil.isMoving()) {
               double yMotion = (PositionUtil.isBoxColliding() ? 0.2 : 0.4) + MovementUtil.getJumpSpeed();
               mc.player.motionY = yMotion;
               event.setY(yMotion);
               this.speed *= this.constFactor.getValue();
            } else if (this.constStage == 3) {
               this.speed = this.distance - 0.66 * (this.distance - MovementUtil.getSpeed(this.slow.getValue()));
            } else {
               if (mc.player.onGround && this.constStage > 0) {
                  this.constStage = 0;
               }

               if (!mc.player.onGround && this.constStage > this.constOff.getValue() && this.constStage < this.constTicks.getValue()) {
                  if (mc.player.ticksExisted % 2 == 0) {
                     event.setY(0.00118212);
                  } else {
                     event.setY(-0.00118212);
                  }
               }

               this.speed = this.distance - this.distance / 159.0;
            }

            this.speed = Math.min(this.speed, this.getCap());
            if (this.constStage != 0) {
               this.speed = Math.max(this.speed, MovementUtil.getSpeed(this.slow.getValue()));
            }

            MovementUtil.strafe(event, this.speed);
            if (MovementUtil.isMoving()) {
               ++this.constStage;
            }
      }
   }

   @SubscribeEvent
   public void invoke(PushEvent event) {
      if (this.lagOut.getValue()) {
         event.setCanceled(false);
      }
   }

   public boolean notColliding() {
      boolean stepping = false;
      List<AxisAlignedBB> collisions = mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().grow(0.1, 0.0, 0.1));
      if ((Step.INSTANCE.isOn() || NewStep.INSTANCE.isOn()) && !collisions.isEmpty()) {
         stepping = true;
      }

      return mc.player.onGround && !stepping && !PositionUtil.inLiquid() && !PositionUtil.inLiquid(true);
   }

   public double getCap() {
      double ret = this.cap.getValue();
      if (!this.scaleCap.getValue()) {
         return ret;
      } else {
         if (mc.player.isPotionActive(MobEffects.SPEED)) {
            int amplifier = ((PotionEffect)Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SPEED))).getAmplifier();
            ret *= 1.0 + 0.2 * (double)(amplifier + 1);
         }

         if (this.slow.getValue() && mc.player.isPotionActive(MobEffects.SLOWNESS)) {
            int amplifier = ((PotionEffect)Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SLOWNESS))).getAmplifier();
            ret /= 1.0 + 0.2 * (double)(amplifier + 1);
         }

         return ret;
      }
   }

   public static enum SpeedMode {
      Instant,
      OldGround,
      OnGround,
      Vanilla,
      NCP,
      Strafe,
      GayHop,
      Bhop,
      VHop,
      LowHop,
      Constantiam,
      None;
   }
}
