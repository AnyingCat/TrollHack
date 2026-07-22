//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.asm.mixins;

import me.rebirthclient.api.events.impl.FreecamEvent;
import me.rebirthclient.api.events.impl.MotionEvent;
import me.rebirthclient.api.events.impl.MoveEvent;
import me.rebirthclient.api.events.impl.PushEvent;
import me.rebirthclient.api.events.impl.UpdateWalkingPlayerEvent;
import me.rebirthclient.mod.modules.impl.exploit.BetterPortal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.MoverType;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {EntityPlayerSP.class},
   priority = 9998
)
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer {
   @Shadow
   @Final
   public NetHandlerPlayClient connection;
   @Shadow
   protected Minecraft mc;
   @Shadow
   private boolean serverSprintState;
   @Shadow
   private boolean serverSneakState;
   @Shadow
   private double lastReportedPosX;
   @Shadow
   private double lastReportedPosY;
   @Shadow
   private double lastReportedPosZ;
   @Shadow
   private float lastReportedYaw;
   @Shadow
   private float lastReportedPitch;
   @Shadow
   private int positionUpdateTicks;
   @Shadow
   private boolean autoJumpEnabled;
   @Shadow
   private boolean prevOnGround;

   public MixinEntityPlayerSP(
      Minecraft p_i47378_1_, World p_i47378_2_, NetHandlerPlayClient p_i47378_3_, StatisticsManager p_i47378_4_, RecipeBook p_i47378_5_
   ) {
      super(p_i47378_2_, p_i47378_3_.getGameProfile());
   }

   @Redirect(
      method = {"onLivingUpdate"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/entity/EntityPlayerSP;closeScreen()V"
)
   )
   public void closeScreenHook(EntityPlayerSP entityPlayerSP) {
      if (!BetterPortal.INSTANCE.isOn() || !BetterPortal.INSTANCE.portalChat.getValue()) {
         entityPlayerSP.closeScreen();
      }
   }

   @Redirect(
      method = {"onLivingUpdate"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/Minecraft;displayGuiScreen(Lnet/minecraft/client/gui/GuiScreen;)V"
)
   )
   public void displayGuiScreenHook(Minecraft mc, GuiScreen screen) {
      if (!BetterPortal.INSTANCE.isOn() || !BetterPortal.INSTANCE.portalChat.getValue()) {
         mc.displayGuiScreen(screen);
      }
   }

   @Inject(
      method = {"pushOutOfBlocks"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void pushOutOfBlocksHook(double x, double y, double z, CallbackInfoReturnable<Boolean> ci) {
      PushEvent event = new PushEvent(1);
      MinecraftForge.EVENT_BUS.post(event);
      if (event.isCanceled()) {
         ci.setReturnValue(false);
      }
   }

   @Redirect(
      method = {"move"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/entity/AbstractClientPlayer;move(Lnet/minecraft/entity/MoverType;DDD)V"
)
   )
   public void move(AbstractClientPlayer player, MoverType moverType, double x, double y, double z) {
      MoveEvent event = new MoveEvent(0, moverType, x, y, z);
      MinecraftForge.EVENT_BUS.post(event);
      if (!event.isCanceled()) {
         super.move(event.getType(), event.getX(), event.getY(), event.getZ());
      }
   }

   @Inject(
      method = {"onUpdateWalkingPlayer"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void preMotion(CallbackInfo info) {
      UpdateWalkingPlayerEvent event = new UpdateWalkingPlayerEvent(0);
      MinecraftForge.EVENT_BUS.post(event);
   }

   @Inject(
      method = {"onUpdateWalkingPlayer"},
      at = {@At("RETURN")}
   )
   private void postMotion(CallbackInfo info) {
      UpdateWalkingPlayerEvent event = new UpdateWalkingPlayerEvent(1);
      MinecraftForge.EVENT_BUS.post(event);
   }

   @Redirect(
      method = {"onUpdateWalkingPlayer"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/entity/EntityPlayerSP;isCurrentViewEntity()Z"
)
   )
   private boolean redirectIsCurrentViewEntity(EntityPlayerSP entityPlayerSP) {
      Minecraft mc = Minecraft.getMinecraft();
      FreecamEvent event = new FreecamEvent();
      MinecraftForge.EVENT_BUS.post(event);
      if (event.isCanceled()) {
         return entityPlayerSP == mc.player;
      } else {
         return mc.getRenderViewEntity() == entityPlayerSP;
      }
   }

   @Redirect(
      method = {"updateEntityActionState"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/entity/EntityPlayerSP;isCurrentViewEntity()Z"
)
   )
   private boolean redirectIsCurrentViewEntity2(EntityPlayerSP entityPlayerSP) {
      Minecraft mc = Minecraft.getMinecraft();
      FreecamEvent event = new FreecamEvent();
      MinecraftForge.EVENT_BUS.post(event);
      if (event.isCanceled()) {
         return entityPlayerSP == mc.player;
      } else {
         return mc.getRenderViewEntity() == entityPlayerSP;
      }
   }

   @Shadow
   protected boolean isCurrentViewEntity() {
      return false;
   }

   /**
    * @author makecat
    * @reason makecat
    */
   @Overwrite
   private void onUpdateWalkingPlayer() {
      boolean flag = this.isSprinting();
      MotionEvent pre = new MotionEvent(
         0, this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch, this.onGround
      );
      MinecraftForge.EVENT_BUS.post(pre);
      if (pre.isCanceled()) {
         MotionEvent post = new MotionEvent(1, pre.getX(), pre.getY(), pre.getZ(), pre.getYaw(), pre.getPitch(), pre.isOnGround());
         MinecraftForge.EVENT_BUS.post(post);
      } else {
         if (flag != this.serverSprintState) {
            if (flag) {
               this.connection.sendPacket(new CPacketEntityAction(Minecraft.getMinecraft().player, Action.START_SPRINTING));
            } else {
               this.connection.sendPacket(new CPacketEntityAction(Minecraft.getMinecraft().player, Action.STOP_SPRINTING));
            }

            this.serverSprintState = flag;
         }

         boolean flag2 = this.isSneaking();
         if (flag2 != this.serverSneakState) {
            if (flag2) {
               this.connection.sendPacket(new CPacketEntityAction(Minecraft.getMinecraft().player, Action.START_SNEAKING));
            } else {
               this.connection.sendPacket(new CPacketEntityAction(Minecraft.getMinecraft().player, Action.STOP_SNEAKING));
            }

            this.serverSneakState = flag2;
         }

         if (this.isCurrentViewEntity()) {
            double d0 = this.posX - this.lastReportedPosX;
            double d2 = this.getEntityBoundingBox().minY - this.lastReportedPosY;
            double d3 = this.posZ - this.lastReportedPosZ;
            double d4 = (double)(pre.getYaw() - this.lastReportedYaw);
            double d5 = (double)(pre.getPitch() - this.lastReportedPitch);
            boolean flag3 = d0 * d0 + d2 * d2 + d3 * d3 > 9.0E-4 || this.positionUpdateTicks >= 20;
            boolean flag4 = d4 != 0.0 || d5 != 0.0;
            if (this.ridingEntity == null) {
               if (flag3 && flag4) {
                  this.connection.sendPacket(new PositionRotation(pre.getX(), pre.getY(), pre.getZ(), pre.getYaw(), pre.getPitch(), pre.isOnGround()));
               } else if (flag3) {
                  this.connection.sendPacket(new Position(pre.getX(), pre.getY(), pre.getZ(), pre.isOnGround()));
               } else if (flag4) {
                  this.connection.sendPacket(new Rotation(pre.getYaw(), pre.getPitch(), pre.isOnGround()));
               } else {
                  this.connection.sendPacket(new CPacketPlayer(pre.isOnGround()));
               }
            } else {
               this.connection
                  .sendPacket(new PositionRotation(this.motionX, -999.0, this.motionZ, pre.getYaw(), pre.getPitch(), pre.isOnGround()));
               flag3 = false;
            }

            ++this.positionUpdateTicks;
            if (flag3) {
               this.lastReportedPosX = pre.getX();
               this.lastReportedPosY = pre.getY();
               this.lastReportedPosZ = pre.getZ();
               this.positionUpdateTicks = 0;
            }

            if (flag4) {
               this.lastReportedYaw = pre.getYaw();
               this.lastReportedPitch = pre.getPitch();
            }

            this.prevOnGround = this.onGround;
            this.autoJumpEnabled = this.mc.gameSettings.autoJump;
            MotionEvent post2 = new MotionEvent(1, pre.getX(), pre.getY(), pre.getZ(), pre.getYaw(), pre.getPitch(), pre.isOnGround());
            MinecraftForge.EVENT_BUS.post(post2);
         }
      }
   }
}
