//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.movement;

import java.util.List;
import me.rebirthclient.api.events.impl.MoveEvent;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.UpdateWalkingPlayerEvent;
import me.rebirthclient.api.util.MovementUtil;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LongJump extends Module {
   public static LongJump INSTANCE = new LongJump();
   public final Setting<Double> boost = this.add(new Setting<>("Boost", 4.5, 0.1, 20.0));
   public final Setting<Boolean> noKick = this.add(new Setting<>("AntiKick", true));
   public final Setting<Boolean> autoDisable = this.add(new Setting<>("AutoDisable", true));
   public int stage;
   public int airTicks;
   public int groundTicks;
   public double speed;
   public double distance;
   boolean inAir = false;

   public LongJump() {
      super("LongJump", "ear", Category.MOVEMENT);
      INSTANCE = this;
   }

   @Override
   public void onUpdate() {
      if (!mc.player.onGround) {
         this.inAir = true;
      }

      if (mc.player.onGround && this.inAir && this.autoDisable.getValue()) {
         this.disable();
      }
   }

   @Override
   public void onDisable() {
      this.inAir = false;
   }

   @SubscribeEvent
   public void onReceivePacket(PacketEvent.Receive event) {
      if (event.getPacket() instanceof SPacketPlayerPosLook) {
         if (this.noKick.getValue()) {
            this.disable();
         }

         this.speed = 0.0;
         this.stage = 0;
         this.airTicks = 0;
         this.groundTicks = 0;
      }
   }

   @Override
   public void onEnable() {
      this.inAir = false;
      if (mc.player != null) {
         this.distance = MovementUtil.getDistance2D();
         this.speed = MovementUtil.getSpeed();
      }

      this.stage = 0;
      this.airTicks = 0;
      this.groundTicks = 0;
   }

   @SubscribeEvent
   public void onUpdate(UpdateWalkingPlayerEvent event) {
      this.distance = MovementUtil.getDistance2D();
   }

   @SubscribeEvent
   public void MoveEvent(MoveEvent event) {
      if (mc.player.moveStrafing <= 0.0F && mc.player.moveForward <= 0.0F) {
         this.stage = 1;
      }

      if (MathUtil.round(mc.player.posY - (double)((int)mc.player.posY), 3) == MathUtil.round(0.943, 3)) {
         mc.player.motionY -= 0.03;
         event.setY(event.getY() - 0.03);
      }

      if (this.stage == 1 && MovementUtil.isMoving()) {
         this.stage = 2;
         this.speed = this.boost.getValue() * MovementUtil.getSpeed() - 0.01;
      } else if (this.stage == 2) {
         this.stage = 3;
         mc.player.motionY = 0.424;
         event.setY(0.424);
         this.speed *= 2.149802;
      } else if (this.stage == 3) {
         this.stage = 4;
         double difference = 0.66 * (this.distance - MovementUtil.getSpeed());
         this.speed = this.distance - difference;
      } else {
         if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, mc.player.motionY, 0.0)).size()
               > 0
            || mc.player.collidedVertically) {
            this.stage = 1;
         }

         this.speed = this.distance - this.distance / 159.0;
      }

      this.speed = Math.max(this.speed, MovementUtil.getSpeed());
      MovementUtil.strafe(event, this.speed);
      float moveForward = mc.player.movementInput.moveForward;
      float moveStrafe = mc.player.movementInput.moveStrafe;
      float rotationYaw = mc.player.rotationYaw;
      if (moveForward == 0.0F && moveStrafe == 0.0F) {
         event.setX(0.0);
         event.setZ(0.0);
      } else if (moveForward != 0.0F) {
         if (moveStrafe >= 1.0F) {
            rotationYaw += (float)(moveForward > 0.0F ? -45 : 45);
            moveStrafe = 0.0F;
         } else if (moveStrafe <= -1.0F) {
            rotationYaw += (float)(moveForward > 0.0F ? 45 : -45);
            moveStrafe = 0.0F;
         }

         if (moveForward > 0.0F) {
            moveForward = 1.0F;
         } else if (moveForward < 0.0F) {
            moveForward = -1.0F;
         }
      }

      double cos = Math.cos(Math.toRadians((double)(rotationYaw + 90.0F)));
      double sin = Math.sin(Math.toRadians((double)(rotationYaw + 90.0F)));
      event.setX((double)moveForward * this.speed * cos + (double)moveStrafe * this.speed * sin);
      event.setZ((double)moveForward * this.speed * sin - (double)moveStrafe * this.speed * cos);
   }

   public double getDistance(EntityPlayer player, double distance) {
      List<AxisAlignedBB> boundingBoxes = player.world.getCollisionBoxes(player, player.getEntityBoundingBox().offset(0.0, -distance, 0.0));
      if (boundingBoxes.isEmpty()) {
         return 0.0;
      } else {
         double y = 0.0;

         for(AxisAlignedBB boundingBox : boundingBoxes) {
            if (boundingBox.maxY > y) {
               y = boundingBox.maxY;
            }
         }

         return player.posY - y;
      }
   }
}
