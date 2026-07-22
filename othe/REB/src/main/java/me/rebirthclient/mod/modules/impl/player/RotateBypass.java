//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.api.events.impl.MotionEvent;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RotateBypass extends Module {
   private final Setting<Integer> rotateTimer = this.add(new Setting<>("RotateTime", 300, 0, 1000));
   private final Setting<Boolean> onlyFreecam = this.add(new Setting<>("OnlyFreecam", false));
   private final Setting<Boolean> packet = this.add(new Setting<>("Packet", false));
   public final Timer timer = new Timer();
   public Vec3d vec;

   public RotateBypass() {
      super("RotateBypass", "trash", Category.PLAYER);
   }

   @SubscribeEvent
   public void onPacketReceive(PacketEvent.Send event) {
      if (!this.onlyFreecam.getValue() || !Freecam.INSTANCE.isOff()) {
         if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
            this.faceVector(
               new Vec3d(((CPacketPlayerTryUseItemOnBlock)event.getPacket()).getPos())
                  .add(0.5, 0.5, 0.5)
                  .add(new Vec3d(((CPacketPlayerTryUseItemOnBlock) event.getPacket()).placedBlockDirection.getDirectionVec()).scale(0.5))
            );
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.NORMAL
   )
   public final void onMotion(MotionEvent event) {
      if (!fullNullCheck()) {
         if (!this.onlyFreecam.getValue() || !Freecam.INSTANCE.isOff()) {
            if (!this.timer.passedMs((long)this.rotateTimer.getValue().intValue()) && this.vec != null) {
               this.faceVector(this.vec, event);
            }
         }
      }
   }

   public void faceVector(Vec3d vec) {
      this.vec = vec;
      this.timer.reset();
      float[] rotations = EntityUtil.getLegitRotations(vec);
      if (this.packet.getValue()) {
         sendPlayerRot(rotations[0], rotations[1], mc.player.onGround);
      }
   }

   public static void sendPlayerRot(float yaw, float pitch, boolean onGround) {
      mc.player.connection.sendPacket(new Rotation(yaw, pitch, onGround));
   }

   private void faceVector(Vec3d vec, MotionEvent event) {
      float[] rotations = EntityUtil.getLegitRotations(vec);
      event.setRotation(rotations[0], rotations[1]);
   }
}
