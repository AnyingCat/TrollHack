//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.util.render.entity;

import me.rebirthclient.api.util.Wrapper;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelBiped.ArmPose;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;

public class StaticModelPlayer extends ModelPlayer implements Wrapper {
   private final EntityPlayer player;
   private float limbSwing;
   private float limbSwingAmount;
   private float yaw;
   private float yawHead;
   private float pitch;

   public StaticModelPlayer(EntityPlayer playerIn, boolean smallArms, float modelSize) {
      super(modelSize, smallArms);
      this.player = playerIn;
      this.limbSwing = this.player.limbSwing;
      this.limbSwingAmount = this.player.limbSwingAmount;
      this.yaw = this.player.rotationYaw;
      this.yawHead = this.player.rotationYawHead;
      this.pitch = this.player.rotationPitch;
      this.isSneak = this.player.isSneaking();
      this.rightArmPose = getArmPose(
         this.player, this.player.getPrimaryHand() == EnumHandSide.RIGHT ? this.player.getHeldItemMainhand() : this.player.getHeldItemOffhand()
      );
      this.leftArmPose = getArmPose(
         this.player, this.player.getPrimaryHand() == EnumHandSide.RIGHT ? this.player.getHeldItemOffhand() : this.player.getHeldItemMainhand()
      );
      this.swingProgress = this.player.swingProgress;
      this.setLivingAnimations(this.player, this.limbSwing, this.limbSwingAmount, mc.getRenderPartialTicks());
   }

   private static ArmPose getArmPose(EntityPlayer player, ItemStack stack) {
      if (stack.isEmpty()) {
         return ArmPose.EMPTY;
      } else {
         return stack.getItem() instanceof ItemBow && player.getItemInUseCount() > 0 ? ArmPose.BOW_AND_ARROW : ArmPose.ITEM;
      }
   }

   public void render(float scale) {
      this.render(this.player, this.limbSwing, this.limbSwingAmount, (float)this.player.ticksExisted, this.yawHead, this.pitch, scale);
   }

   public void disableArmorLayers() {
      this.bipedBodyWear.showModel = false;
      this.bipedLeftLegwear.showModel = false;
      this.bipedRightLegwear.showModel = false;
      this.bipedLeftArmwear.showModel = false;
      this.bipedRightArmwear.showModel = false;
      this.bipedHeadwear.showModel = true;
      this.bipedHead.showModel = false;
   }

   public EntityPlayer getPlayer() {
      return this.player;
   }

   public float getLimbSwing() {
      return this.limbSwing;
   }

   public void setLimbSwing(float limbSwing) {
      this.limbSwing = limbSwing;
   }

   public float getLimbSwingAmount() {
      return this.limbSwingAmount;
   }

   public void setLimbSwingAmount(float limbSwingAmount) {
      this.limbSwingAmount = limbSwingAmount;
   }

   public float getYaw() {
      return this.yaw;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   public float getYawHead() {
      return this.yawHead;
   }

   public void setYawHead(float yawHead) {
      this.yawHead = yawHead;
   }

   public float getPitch() {
      return this.pitch;
   }

   public void setPitch(float pitch) {
      this.pitch = pitch;
   }
}
