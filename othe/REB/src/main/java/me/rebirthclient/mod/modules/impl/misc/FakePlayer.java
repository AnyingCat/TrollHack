//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.misc;

import com.mojang.authlib.GameProfile;
import java.util.Random;
import java.util.UUID;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.util.DamageUtil;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.render.EarthPopChams;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.MoverType;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FakePlayer extends Module {
   private final Setting<String> name = this.add(new Setting<>("Name", ""));
   private final Setting<Boolean> move = this.add(new Setting<>("Move", false));
   private final Setting<Boolean> damage = this.add(new Setting<>("Damage", true).setParent());
   private final Setting<Integer> totemHurtTime = this.add(new Setting<>("TotemHurtTime", 25, 0, 50, v -> this.damage.isOpen()));
   private final Setting<Integer> hurtTime = this.add(new Setting<>("HurtTime", 10, 0, 50, v -> this.damage.isOpen()));
   private final Setting<Integer> gappleDelay = this.add(new Setting<>("GappleDelay", 100, 0, 200, v -> this.damage.isOpen()));
   EntityOtherPlayerMP player = null;
   int ticks3 = 0;
   boolean pop = false;
   int ticks = 0;
   int ticks2 = 0;

   public FakePlayer() {
      super("FakePlayer", "Summons a client-side fake player", Category.MISC);
   }

   @Override
   public String getInfo() {
      return this.name.getValue();
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public void onReceivePacket(PacketEvent.Receive event) {
      if (!fullNullCheck()) {
         if (!event.isCanceled()) {
            if (event.getPacket() instanceof SPacketExplosion && this.damage.getValue()) {
               SPacketExplosion packet = event.getPacket();
               if (this.player
                     .getDistance(packet.posX, packet.posY, packet.getZ())
                  > 8.0) {
                  return;
               }

               if (this.ticks3 > this.hurtTime.getValue() - 1 && !this.pop) {
                  BlockPos pos = new BlockPos(
                     ((SPacketExplosion)event.getPacket()).getX(),
                     ((SPacketExplosion)event.getPacket()).getY(),
                     ((SPacketExplosion)event.getPacket()).getZ()
                  );
                  float damage = DamageUtil.calculateDamage(pos.down(), this.player);
                  this.doPop(damage);
                  this.ticks3 = 0;
               } else if (this.ticks3 > this.totemHurtTime.getValue() - 1) {
                  BlockPos pos = new BlockPos(
                     ((SPacketExplosion)event.getPacket()).getX(),
                     ((SPacketExplosion)event.getPacket()).getY(),
                     ((SPacketExplosion)event.getPacket()).getZ()
                  );
                  float damage = DamageUtil.calculateDamage(pos.down(), this.player);
                  this.doPop(damage);
                  this.pop = false;
                  this.ticks3 = 0;
               }
            }
         }
      }
   }

   private void doPop(float damage) {
      float healthDamage = damage - this.player.getAbsorptionAmount();
      if ((double)(this.player.getHealth() - healthDamage) < 0.1) {
         EarthPopChams.INSTANCE.onTotemPop(this.player);
         mc.world
            .playSound(
               mc.player,
               this.player.posX,
               this.player.posY,
               this.player.posZ,
               SoundEvents.ITEM_TOTEM_USE,
               mc.player.getSoundCategory(),
               1.0F,
               1.0F
            );
         this.player.setHealth(2.0F);
         this.player.setAbsorptionAmount(8.0F);
         this.pop = true;
      } else {
         if (healthDamage < 0.0F) {
            healthDamage = 0.0F;
         }

         this.player.setHealth(this.player.getHealth() - healthDamage);
         float absorptionAmount = this.player.getAbsorptionAmount() - damage;
         if (absorptionAmount < 0.0F) {
            absorptionAmount = 0.0F;
         }

         this.player.setAbsorptionAmount(absorptionAmount);
      }
   }

   @Override
   public void onEnable() {
      this.sendMessage("Spawned a fakeplayer with the name " + (String)this.name.getValue() + ".");
      if (mc.player != null && !mc.player.isDead) {
         this.player = new EntityOtherPlayerMP(
            mc.world, new GameProfile(UUID.fromString("0f75a81d-70e5-43c5-b892-f33c524284f2"), this.name.getValue())
         );
         this.player.copyLocationAndAnglesFrom(mc.player);
         this.player.rotationYawHead = mc.player.rotationYawHead;
         this.player.rotationYaw = mc.player.rotationYaw;
         this.player.rotationPitch = mc.player.rotationPitch;
         this.player.setGameType(GameType.SURVIVAL);
         this.player.inventory.copyInventory(mc.player.inventory);
         this.player.setHealth(20.0F);
         this.player.setAbsorptionAmount(16.0F);
         mc.world.addEntityToWorld(-12345, this.player);
         this.player.onLivingUpdate();
      } else {
         this.disable();
      }
   }

   @Override
   public void onDisable() {
      if (mc.world != null) {
         mc.world.removeEntityFromWorld(-12345);
      }
   }

   @Override
   public void onLogout() {
      if (this.isOn()) {
         this.disable();
      }
   }

   @Override
   public void onLogin() {
      if (this.isOn()) {
         this.disable();
      }
   }

   @Override
   public void onTick() {
      ++this.ticks;
      ++this.ticks2;
      ++this.ticks3;
      if (this.player != null) {
         if (this.ticks > this.gappleDelay.getValue() - 1) {
            this.player.setAbsorptionAmount(16.0F);
            this.ticks = 0;
         }

         if (this.ticks2 > 19) {
            float health = this.player.getHealth() + 1.0F;
            if (health > 20.0F) {
               health = 20.0F;
            }

            this.player.setHealth(health);
            this.ticks2 = 0;
         }

         new Random();
         this.player.moveForward = 1.0F;
         this.player.moveStrafing = MathUtil.randomBetween(-1.0F, 1.0F);
         if (this.move.getValue()) {
            this.travel(this.player.moveStrafing, this.player.moveVertical, this.player.moveForward);
         }
      }
   }

   public void travel(float strafe, float vertical, float forward) {
      double d0 = this.player.posY;
      float f1 = 0.8F;
      float f2 = 0.02F;
      float f3 = (float)EnchantmentHelper.getDepthStriderModifier(this.player);
      if (f3 > 3.0F) {
         f3 = 3.0F;
      }

      if (!this.player.onGround) {
         f3 *= 0.5F;
      }

      if (f3 > 0.0F) {
         f1 += (0.54600006F - f1) * f3 / 3.0F;
         f2 += (this.player.getAIMoveSpeed() - f2) * f3 / 4.0F;
      }

      this.player.moveRelative(strafe, vertical, forward, f2);
      this.player.move(MoverType.SELF, this.player.motionX, this.player.motionY, this.player.motionZ);
      this.player.motionX *= (double)f1;
      this.player.motionY *= 0.8F;
      this.player.motionZ *= (double)f1;
      if (!this.player.hasNoGravity()) {
         this.player.motionY -= 0.02;
      }

      if (this.player.collidedHorizontally
         && this.player.isOffsetPositionInLiquid(this.player.motionX, this.player.motionY + 0.6F - this.player.posY + d0, this.player.motionZ)) {
         this.player.motionY = 0.3F;
      }
   }
}
