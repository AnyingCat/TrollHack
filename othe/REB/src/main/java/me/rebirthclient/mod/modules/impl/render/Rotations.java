//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.render;

import me.rebirthclient.api.events.impl.MotionEvent;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.player.Freecam;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Rotations extends Module {
   public static Rotations INSTANCE;
   private final Setting<Boolean> onlyThird = this.add(new Setting<>("OnlyThird", true));
   private final Setting<Boolean> onlyPacket = this.add(new Setting<>("OnlyPacket", true));
   public final Setting<Boolean> onlyHead = this.add(new Setting<>("OnlyHead", false));
   private static float renderPitch;
   private static float renderYawOffset;
   private static float prevPitch;
   private static float prevRenderYawOffset;
   private static float prevRotationYawHead;
   private static float rotationYawHead;
   private int ticksExisted;
   private float lastYaw = 0.0F;
   private float lastPitch = 0.0F;

   public Rotations() {
      super("Rotations", "show rotation", Category.RENDER);
      INSTANCE = this;
   }

   public boolean check() {
      return this.isOn() && (mc.gameSettings.thirdPersonView != 0 || !this.onlyThird.getValue() || Freecam.INSTANCE.isOn());
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public void onPacketSend(PacketEvent.Send event) {
      if (event.getPacket() instanceof CPacketPlayer && ((CPacketPlayer) event.getPacket()).rotating) {
         this.lastYaw = ((CPacketPlayer) event.getPacket()).yaw;
         this.lastPitch = ((CPacketPlayer) event.getPacket()).pitch;
         this.set(((CPacketPlayer) event.getPacket()).yaw, ((CPacketPlayer) event.getPacket()).pitch);
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public void invoke(MotionEvent event) {
      if (this.onlyPacket.getValue()) {
         this.set(this.lastYaw, this.lastPitch);
      } else {
         this.set(event.getYaw(), event.getPitch());
      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public void onReceivePacket(PacketEvent.Receive event) {
      if (!event.isCanceled() && !fullNullCheck()) {
         if (event.getStage() == 0 && event.getPacket() instanceof SPacketPlayerPosLook) {
            SPacketPlayerPosLook packet = event.getPacket();
            this.lastYaw = packet.yaw;
            this.lastPitch = packet.pitch;
            this.set(packet.yaw, packet.pitch);
         }
      }
   }

   private void set(float yaw, float pitch) {
      if (mc.player.ticksExisted != this.ticksExisted) {
         this.ticksExisted = mc.player.ticksExisted;
         prevPitch = renderPitch;
         prevRenderYawOffset = renderYawOffset;
         renderYawOffset = this.getRenderYawOffset(yaw, prevRenderYawOffset);
         prevRotationYawHead = rotationYawHead;
         rotationYawHead = yaw;
         renderPitch = pitch;
      }
   }

   public static float getRenderPitch() {
      return renderPitch;
   }

   public static float getRotationYawHead() {
      return rotationYawHead;
   }

   public static float getRenderYawOffset() {
      return renderYawOffset;
   }

   public static float getPrevPitch() {
      return prevPitch;
   }

   public static float getPrevRotationYawHead() {
      return prevRotationYawHead;
   }

   public static float getPrevRenderYawOffset() {
      return prevRenderYawOffset;
   }

   private float getRenderYawOffset(float yaw, float offsetIn) {
      float result = offsetIn;
      double xDif = mc.player.posX - mc.player.prevPosX;
      double zDif = mc.player.posZ - mc.player.prevPosZ;
      if (xDif * xDif + zDif * zDif > 0.0025000002F) {
         float offset = (float)MathHelper.atan2(zDif, xDif) * (180.0F / (float)Math.PI) - 90.0F;
         float wrap = MathHelper.abs(MathHelper.wrapDegrees(yaw) - offset);
         if (95.0F < wrap && wrap < 265.0F) {
            result = offset - 180.0F;
         } else {
            result = offset;
         }
      }

      if (mc.player.swingProgress > 0.0F) {
         result = yaw;
      }

      result = offsetIn + MathHelper.wrapDegrees(result - offsetIn) * 0.3F;
      float offset = MathHelper.wrapDegrees(yaw - result);
      if (offset < -75.0F) {
         offset = -75.0F;
      } else if (offset >= 75.0F) {
         offset = 75.0F;
      }

      result = yaw - offset;
      if (offset * offset > 2500.0F) {
         result += offset * 0.2F;
      }

      return result;
   }
}
