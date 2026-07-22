//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.player;

import java.util.Collection;
import java.util.Map;
import me.rebirthclient.api.events.impl.FreecamEntityEvent;
import me.rebirthclient.api.events.impl.FreecamEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Bind;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Freecam extends Module {
   public final Setting<Bind> movePlayer = this.add(new Setting<>("Control", new Bind(56)));
   private final Setting<Boolean> follow = this.add(new Setting<>("Follow", false));
   private final Setting<Boolean> copyInventory = this.add(new Setting<>("CopyInv", false));
   private final Setting<Float> hSpeed = this.add(new Setting<>("HSpeed", 1.0F, 0.2F, 2.0F));
   private final Setting<Float> vSpeed = this.add(new Setting<>("VSpeed", 1.0F, 0.2F, 2.0F));
   private final MovementInput cameraMovement = new MovementInputFromOptions(mc.gameSettings) {
      public void updatePlayerMoveState() {
         if (!Freecam.this.movePlayer.getValue().isDown()) {
            super.updatePlayerMoveState();
         } else {
            this.moveStrafe = 0.0F;
            this.moveForward = 0.0F;
            this.forwardKeyDown = false;
            this.backKeyDown = false;
            this.leftKeyDown = false;
            this.rightKeyDown = false;
            this.jump = false;
            this.sneak = false;
         }
      }
   };
   private final MovementInput playerMovement = new MovementInputFromOptions(mc.gameSettings) {
      public void updatePlayerMoveState() {
         if (Freecam.this.movePlayer.getValue().isDown()) {
            super.updatePlayerMoveState();
         } else {
            this.moveStrafe = 0.0F;
            this.moveForward = 0.0F;
            this.forwardKeyDown = false;
            this.backKeyDown = false;
            this.leftKeyDown = false;
            this.rightKeyDown = false;
            this.jump = false;
            this.sneak = false;
         }
      }
   };
   private Entity cachedActiveEntity = null;
   private int lastActiveTick = -1;
   private Entity oldRenderEntity = null;
   private Freecam.FreecamCamera camera = null;
   public static Freecam INSTANCE;

   public Freecam() {
      super("Freecam", "Control your camera separately to your body", Category.PLAYER);
      INSTANCE = this;
   }

   public Entity getActiveEntity() {
      if (this.cachedActiveEntity == null) {
         this.cachedActiveEntity = mc.player;
      }

      int currentTick = mc.player.ticksExisted;
      if (this.lastActiveTick != currentTick) {
         this.lastActiveTick = currentTick;
         if (this.isOn()) {
            if (this.movePlayer.getValue().isDown()) {
               this.cachedActiveEntity = mc.player;
            } else {
               this.cachedActiveEntity = (Entity)(mc.getRenderViewEntity() == null ? mc.player : mc.getRenderViewEntity());
            }
         } else {
            this.cachedActiveEntity = mc.player;
         }
      }

      return this.cachedActiveEntity;
   }

   @SubscribeEvent
   public void onWorldLoad(Unload event) {
      mc.setRenderViewEntity(mc.player);
      this.disable();
   }

   @SubscribeEvent
   public void onFreecam(FreecamEvent event) {
      event.setCanceled(true);
   }

   @SubscribeEvent
   public void onFreecamEntity(FreecamEntityEvent event) {
      if (!fullNullCheck()) {
         if (this.getActiveEntity() != null) {
            event.setEntity(this.getActiveEntity());
         }
      }
   }

   @Override
   public void onUpdate() {
      this.camera.setCopyInventory(this.copyInventory.getValue());
      this.camera.setFollow(this.follow.getValue());
      this.camera.sethSpeed(this.hSpeed.getValue());
      this.camera.setvSpeed(this.vSpeed.getValue());
   }

   @SubscribeEvent
   public void onRenderOverlay(RenderHandEvent event) {
      event.setCanceled(true);
   }

   @Override
   public void onLogin() {
      if (this.isOn()) {
         this.disable();
      }
   }

   @Override
   public void onEnable() {
      if (mc.player != null) {
         this.camera = new Freecam.FreecamCamera(this.copyInventory.getValue(), this.follow.getValue(), this.hSpeed.getValue(), this.vSpeed.getValue());
         this.camera.movementInput = this.cameraMovement;
         mc.player.movementInput = this.playerMovement;
         mc.world.addEntityToWorld(-921, this.camera);
         this.oldRenderEntity = mc.getRenderViewEntity();
         mc.setRenderViewEntity(this.camera);
         mc.renderChunksMany = false;
      }
   }

   @Override
   public void onDisable() {
      if (mc.player != null) {
         mc.world.removeEntityFromWorld(-1234567);
         if (this.camera != null) {
            mc.world.removeEntity(this.camera);
         }

         this.camera = null;
         mc.player.movementInput = new MovementInputFromOptions(mc.gameSettings);
         mc.setRenderViewEntity(this.oldRenderEntity);
         mc.renderChunksMany = true;
      }
   }

   public static class FreecamCamera extends EntityPlayerSP {
      private final Minecraft mc = Minecraft.getMinecraft();
      private boolean copyInventory;
      private boolean follow;
      private float hSpeed;
      private float vSpeed;

      public FreecamCamera(World world) {
         super(
            Minecraft.getMinecraft(),
            world,
            Minecraft.getMinecraft().getConnection(),
            Minecraft.getMinecraft().player.getStatFileWriter(),
            Minecraft.getMinecraft().player.getRecipeBook()
         );
      }

      public FreecamCamera(boolean copyInventory, boolean follow, float hSpeed, float vSpeed) {
         super(
            Minecraft.getMinecraft(),
            Minecraft.getMinecraft().world,
            Minecraft.getMinecraft().getConnection(),
            Minecraft.getMinecraft().player.getStatFileWriter(),
            Minecraft.getMinecraft().player.getRecipeBook()
         );
         this.copyInventory = copyInventory;
         this.follow = follow;
         this.hSpeed = hSpeed;
         this.vSpeed = vSpeed;
         this.noClip = true;
         this.setHealth(this.mc.player.getHealth());
         this.posX = this.mc.player.posX;
         this.posY = this.mc.player.posY;
         this.posZ = this.mc.player.posZ;
         this.prevPosX = this.mc.player.prevPosX;
         this.prevPosY = this.mc.player.prevPosY;
         this.prevPosZ = this.mc.player.prevPosZ;
         this.lastTickPosX = this.mc.player.lastTickPosX;
         this.lastTickPosY = this.mc.player.lastTickPosY;
         this.lastTickPosZ = this.mc.player.lastTickPosZ;
         this.rotationYaw = this.mc.player.rotationYaw;
         this.rotationPitch = this.mc.player.rotationPitch;
         this.rotationYawHead = this.mc.player.rotationYawHead;
         this.prevRotationYaw = this.mc.player.prevRotationYaw;
         this.prevRotationPitch = this.mc.player.prevRotationPitch;
         this.prevRotationYawHead = this.mc.player.prevRotationYawHead;
         if (this.copyInventory) {
            this.inventory = this.mc.player.inventory;
            this.inventoryContainer = this.mc.player.inventoryContainer;
            this.setHeldItem(EnumHand.MAIN_HAND, this.mc.player.getHeldItemMainhand());
            this.setHeldItem(EnumHand.OFF_HAND, this.mc.player.getHeldItemOffhand());
         }

         NBTTagCompound compound = new NBTTagCompound();
         this.mc.player.capabilities.writeCapabilitiesToNBT(compound);
         this.capabilities.readCapabilitiesFromNBT(compound);
         this.capabilities.isFlying = true;
         this.attackedAtYaw = this.mc.player.attackedAtYaw;
         this.movementInput = new MovementInputFromOptions(this.mc.gameSettings);
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
      }

      public boolean isInsideOfMaterial(Material material) {
         return this.mc.player.isInsideOfMaterial(material);
      }

      public Map<Potion, PotionEffect> getActivePotionMap() {
         return this.mc.player.getActivePotionMap();
      }

      public Collection<PotionEffect> getActivePotionEffects() {
         return this.mc.player.getActivePotionEffects();
      }

      public int getTotalArmorValue() {
         return this.mc.player.getTotalArmorValue();
      }

      public float getAbsorptionAmount() {
         return this.mc.player.getAbsorptionAmount();
      }

      public boolean isPotionActive(Potion potion) {
         return this.mc.player.isPotionActive(potion);
      }

      public PotionEffect getActivePotionEffect(Potion potion) {
         return this.mc.player.getActivePotionEffect(potion);
      }

      public boolean canTriggerWalking() {
         return false;
      }

      public AxisAlignedBB getCollisionBox(Entity entity) {
         return null;
      }

      public AxisAlignedBB getCollisionBoundingBox() {
         return null;
      }

      public AxisAlignedBB getEntityBoundingBox() {
         return new AxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
      }

      public boolean canBePushed() {
         return false;
      }

      public void applyEntityCollision(Entity entity) {
      }

      public boolean attackEntityFrom(DamageSource source, float amount) {
         return false;
      }

      public boolean canBeAttackedWithItem() {
         return false;
      }

      public boolean canBeCollidedWith() {
         return false;
      }

      public boolean canBeRidden(Entity entity) {
         return false;
      }

      public boolean canRenderOnFire() {
         return false;
      }

      public void doBlockCollisions() {
      }

      public void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
      }

      public boolean getIsInvulnerable() {
         return true;
      }

      public EnumPushReaction getPushReaction() {
         return EnumPushReaction.IGNORE;
      }

      public boolean hasNoGravity() {
         return true;
      }

      public void onLivingUpdate() {
         this.motionX = 0.0;
         this.motionY = 0.0;
         this.motionZ = 0.0;
         this.movementInput.updatePlayerMoveState();
         float up = this.movementInput.jump ? 1.0F : (this.movementInput.sneak ? -1.0F : 0.0F);
         this.setMotion(this.movementInput.moveStrafe, up, this.movementInput.moveForward);
         if (this.mc.gameSettings.keyBindSprint.isKeyDown()) {
            this.motionX *= 2.0;
            this.motionY *= 2.0;
            this.motionZ *= 2.0;
            this.setSprinting(true);
         } else {
            this.setSprinting(false);
         }

         if (this.follow) {
            if (Math.abs(this.motionX) <= 1.0E-8F) {
               this.posX += this.mc.player.posX - this.mc.player.prevPosX;
            }

            if (Math.abs(this.motionY) <= 1.0E-8F) {
               this.motionY += this.mc.player.posY - this.mc.player.prevPosY;
            }

            if (Math.abs(this.motionZ) <= 1.0E-8F) {
               this.motionZ += this.mc.player.posZ - this.mc.player.prevPosZ;
            }
         }

         this.setPosition(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
      }

      public void setMotion(float strafe, float up, float forward) {
         float f = strafe * strafe + up * up + forward * forward;
         if (f >= 1.0E-4F) {
            f = MathHelper.sqrt(f);
            if (f < 1.0F) {
               f = 1.0F;
            }

            f /= 2.0F;
            strafe *= f;
            up *= f;
            forward *= f;
            float f1 = MathHelper.sin(this.rotationYaw * (float) (Math.PI / 180.0));
            float f2 = MathHelper.cos(this.rotationYaw * (float) (Math.PI / 180.0));
            this.motionX = (double)((strafe * f2 - forward * f1) * this.hSpeed);
            this.motionY = (double)up * (double)this.vSpeed;
            this.motionZ = (double)((forward * f2 + strafe * f1) * this.hSpeed);
         }
      }

      public boolean isCopyInventory() {
         return this.copyInventory;
      }

      public void setCopyInventory(boolean copyInventory) {
         this.copyInventory = copyInventory;
      }

      public boolean isFollow() {
         return this.follow;
      }

      public void setFollow(boolean follow) {
         this.follow = follow;
      }

      public float gethSpeed() {
         return this.hSpeed;
      }

      public void sethSpeed(float hSpeed) {
         this.hSpeed = hSpeed;
      }

      public float getvSpeed() {
         return this.vSpeed;
      }

      public void setvSpeed(float vSpeed) {
         this.vSpeed = vSpeed;
      }
   }
}
