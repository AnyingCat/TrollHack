//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat;

import java.awt.Color;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.events.impl.UpdateWalkingPlayerEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.managers.impl.SneakManager;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class Aura extends Module {
   public static Aura INSTANCE;
   public static Entity target;
   private final Setting<Aura.Page> page = this.add(new Setting<>("Settings", Aura.Page.GLOBAL));
   public final Setting<Float> range = this.add(new Setting<>("Range", 6.0F, 0.1F, 7.0F, v -> this.page.getValue() == Aura.Page.GLOBAL));
   private final Setting<Aura.TargetMode> targetMode = this.add(
      new Setting<>("Filter", Aura.TargetMode.DISTANCE, v -> this.page.getValue() == Aura.Page.GLOBAL)
   );
   private final Setting<Float> wallRange = this.add(new Setting<>("WallRange", 3.0F, 0.1F, 7.0F, v -> this.page.getValue() == Aura.Page.GLOBAL));
   private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true, v -> this.page.getValue() == Aura.Page.GLOBAL).setParent());
   private final Setting<Boolean> lookBack = this.add(new Setting<>("LookBack", true, v -> this.page.getValue() == Aura.Page.GLOBAL && this.rotate.isOpen()));
   private final Setting<Float> yawStep = this.add(
      new Setting<>("YawStep", 0.3F, 0.1F, 1.0F, v -> this.page.getValue() == Aura.Page.GLOBAL && this.rotate.isOpen())
   );
   private final Setting<Float> pitchAdd = this.add(
      new Setting<>("PitchAdd", 0.0F, 0.0F, 25.0F, v -> this.page.getValue() == Aura.Page.GLOBAL && this.rotate.isOpen())
   );
   private final Setting<Boolean> randomPitch = this.add(
      new Setting<>("RandomizePitch", false, v -> this.page.getValue() == Aura.Page.GLOBAL && this.rotate.isOpen())
   );
   private final Setting<Float> amplitude = this.add(
      new Setting<>("Amplitude", 3.0F, -5.0F, 5.0F, v -> this.page.getValue() == Aura.Page.GLOBAL && this.rotate.isOpen() && this.randomPitch.getValue())
   );
   private final Setting<Boolean> oneEight = this.add(new Setting<>("OneEight", false, v -> this.page.getValue() == Aura.Page.GLOBAL).setParent());
   private final Setting<Float> minCps = this.add(
      new Setting<>("MinCps", 6.0F, 0.0F, 20.0F, v -> this.page.getValue() == Aura.Page.GLOBAL && this.oneEight.isOpen())
   );
   private final Setting<Float> maxCps = this.add(
      new Setting<>("MaxCps", 9.0F, 0.0F, 20.0F, v -> this.page.getValue() == Aura.Page.GLOBAL && this.oneEight.isOpen())
   );
   private final Setting<Float> randomDelay = this.add(new Setting<>("RandomDelay", 0.0F, 0.0F, 5.0F, v -> this.page.getValue() == Aura.Page.GLOBAL));
   private final Setting<Boolean> fovCheck = this.add(new Setting<>("FovCheck", false, v -> this.page.getValue() == Aura.Page.GLOBAL).setParent());
   private final Setting<Float> angle = this.add(
      new Setting<>("Angle", 180.0F, 0.0F, 180.0F, v -> this.page.getValue() == Aura.Page.GLOBAL && this.fovCheck.isOpen())
   );
   private final Setting<Boolean> stopSprint = this.add(new Setting<>("StopSprint", true, v -> this.page.getValue() == Aura.Page.GLOBAL));
   private final Setting<Boolean> armorBreak = this.add(new Setting<>("ArmorBreak", false, v -> this.page.getValue() == Aura.Page.GLOBAL));
   private final Setting<Boolean> whileEating = this.add(new Setting<>("WhileEating", true, v -> this.page.getValue() == Aura.Page.GLOBAL));
   private final Setting<Boolean> weaponOnly = this.add(new Setting<>("WeaponOnly", true, v -> this.page.getValue() == Aura.Page.GLOBAL));
   private final Setting<Boolean> tpsSync = this.add(new Setting<>("TpsSync", true, v -> this.page.getValue() == Aura.Page.GLOBAL));
   private final Setting<Boolean> packet = this.add(new Setting<>("Packet", false, v -> this.page.getValue() == Aura.Page.ADVANCED));
   private final Setting<Boolean> swing = this.add(new Setting<>("Swing", true, v -> this.page.getValue() == Aura.Page.GLOBAL));
   private final Setting<Boolean> sneak = this.add(new Setting<>("Sneak", false, v -> this.page.getValue() == Aura.Page.ADVANCED));
   private final Setting<Aura.RenderMode> render = this.add(new Setting<>("Render", Aura.RenderMode.JELLO, v -> this.page.getValue() == Aura.Page.GLOBAL));
   private final Setting<Color> targetColor = this.add(
      new Setting<>(
         "TargetColor", new Color(255, 255, 255, 255), v -> this.page.getValue() == Aura.Page.GLOBAL && this.render.getValue() != Aura.RenderMode.OFF
      )
   );
   private final Setting<Boolean> players = this.add(new Setting<>("Players", true, v -> this.page.getValue() == Aura.Page.TARGETS));
   private final Setting<Boolean> animals = this.add(new Setting<>("Animals", false, v -> this.page.getValue() == Aura.Page.TARGETS));
   private final Setting<Boolean> neutrals = this.add(new Setting<>("Neutrals", false, v -> this.page.getValue() == Aura.Page.TARGETS));
   private final Setting<Boolean> others = this.add(new Setting<>("Others", false, v -> this.page.getValue() == Aura.Page.TARGETS));
   private final Setting<Boolean> projectiles = this.add(new Setting<>("Projectiles", false, v -> this.page.getValue() == Aura.Page.TARGETS));
   private final Setting<Boolean> hostiles = this.add(new Setting<>("Hostiles", true, v -> this.page.getValue() == Aura.Page.TARGETS).setParent());
   private final Setting<Boolean> onlyGhasts = this.add(
      new Setting<>("OnlyGhasts", false, v -> this.hostiles.isOpen() && this.page.getValue() == Aura.Page.TARGETS)
   );
   private final Setting<Boolean> delay32k = this.add(new Setting<>("32kDelay", false, v -> this.page.getValue() == Aura.Page.ADVANCED));
   private final Setting<Integer> packetAmount32k = this.add(
      new Setting<>("32kPackets", 2, v -> !this.delay32k.getValue() && this.page.getValue() == Aura.Page.ADVANCED)
   );
   private final Setting<Integer> time32k = this.add(new Setting<>("32kTime", 5, 1, 50, v -> this.page.getValue() == Aura.Page.ADVANCED));
   private final Setting<Boolean> multi32k = this.add(new Setting<>("Multi32k", false, v -> this.page.getValue() == Aura.Page.ADVANCED));
   private final Timer timer = new Timer();
   private float lastDelay = 0.0F;

   public Aura() {
      super("KnifeBot", "Attacks entities in radius", Category.COMBAT);
      INSTANCE = this;
   }

   @Override
   public String getInfo() {
      String modeInfo = Managers.TEXT.normalizeCases(this.targetMode.getValue());
      String targetInfo = target instanceof EntityPlayer ? ", " + target.getName() : "";
      return modeInfo + targetInfo;
   }

   @Override
   public void onRender3D(Render3DEvent event) {
      if (target != null) {
         if (this.render.getValue() == Aura.RenderMode.OLD) {
            RenderUtil.drawEntityBoxESP(target, this.targetColor.getValue(), true, new Color(255, 255, 255, 130), 0.7F, true, true, 35);
         } else if (this.render.getValue() == Aura.RenderMode.JELLO) {
            double everyTime = 1500.0;
            double drawTime = (double)System.currentTimeMillis() % everyTime;
            boolean drawMode = drawTime > everyTime / 2.0;
            double drawPercent = drawTime / (everyTime / 2.0);
            if (!drawMode) {
               drawPercent = 1.0 - drawPercent;
            } else {
               --drawPercent;
            }

            drawPercent = this.easeInOutQuad(drawPercent);
            mc.entityRenderer.disableLightmap();
            GL11.glPushMatrix();
            GL11.glDisable(3553);
            GL11.glBlendFunc(770, 771);
            GL11.glEnable(2848);
            GL11.glEnable(3042);
            GL11.glDisable(2929);
            GL11.glDisable(2884);
            GL11.glShadeModel(7425);
            mc.entityRenderer.disableLightmap();
            double radius = (double)target.width;
            double height = (double)target.height + 0.1;
            double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * (double)mc.getRenderPartialTicks() - mc.renderManager.viewerPosX;
            double y = target.lastTickPosY
               + (target.posY - target.lastTickPosY) * (double)mc.getRenderPartialTicks()
               - mc.renderManager.viewerPosY
               + height * drawPercent;
            double z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * (double)mc.getRenderPartialTicks() - mc.renderManager.viewerPosZ;
            double eased = height / 3.0 * (drawPercent > 0.5 ? 1.0 - drawPercent : drawPercent) * (double)(drawMode ? -1 : 1);

            for(int segments = 0; segments < 360; segments += 5) {
               Color color = this.targetColor.getValue();
               double x1 = x - Math.sin((double)segments * Math.PI / 180.0) * radius;
               double z1 = z + Math.cos((double)segments * Math.PI / 180.0) * radius;
               double x2 = x - Math.sin((double)(segments - 5) * Math.PI / 180.0) * radius;
               double z2 = z + Math.cos((double)(segments - 5) * Math.PI / 180.0) * radius;
               GL11.glBegin(7);
               GL11.glColor4f(
                  (float)ColorUtil.pulseColor(color, 200, 1).getRed() / 255.0F,
                  (float)ColorUtil.pulseColor(color, 200, 1).getGreen() / 255.0F,
                  (float)ColorUtil.pulseColor(color, 200, 1).getBlue() / 255.0F,
                  0.0F
               );
               GL11.glVertex3d(x1, y + eased, z1);
               GL11.glVertex3d(x2, y + eased, z2);
               GL11.glColor4f(
                  (float)ColorUtil.pulseColor(color, 200, 1).getRed() / 255.0F,
                  (float)ColorUtil.pulseColor(color, 200, 1).getGreen() / 255.0F,
                  (float)ColorUtil.pulseColor(color, 200, 1).getBlue() / 255.0F,
                  200.0F
               );
               GL11.glVertex3d(x2, y, z2);
               GL11.glVertex3d(x1, y, z1);
               GL11.glEnd();
               GL11.glBegin(2);
               GL11.glVertex3d(x2, y, z2);
               GL11.glVertex3d(x1, y, z1);
               GL11.glEnd();
            }

            GL11.glEnable(2884);
            GL11.glShadeModel(7424);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(2929);
            GL11.glDisable(2848);
            GL11.glDisable(3042);
            GL11.glEnable(3553);
            GL11.glPopMatrix();
         }
      }
   }

   @Override
   public void onTick() {
      if (!this.rotate.getValue()) {
         this.doAura();
      }

      if (this.maxCps.getValue() < this.minCps.getValue()) {
         this.maxCps.setValue(this.minCps.getValue());
      }
   }

   @SubscribeEvent
   public void onUpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent event) {
      if (!fullNullCheck()) {
         if (event.getStage() == 0 && this.rotate.getValue() && target != null) {
            float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), target.getPositionEyes(mc.getRenderPartialTicks()));
            float[] newAngle = Managers.ROTATIONS.injectYawStep(angle, this.yawStep.getValue());
            Managers.ROTATIONS
               .setRotations(
                  newAngle[0],
                  newAngle[1] + this.pitchAdd.getValue() + (this.randomPitch.getValue() ? (float)Math.random() * this.amplitude.getValue() : 0.0F)
               );
         }

         this.doAura();
         if (this.rotate.getValue() && this.lookBack.getValue()) {
            Managers.ROTATIONS.resetRotations();
         }
      }
   }

   private void doAura() {
      if (this.weaponOnly.getValue() && !EntityUtil.isHoldingWeapon(mc.player)) {
         target = null;
      } else {
         int wait = (int)(
            (float)EntityUtil.getHitCoolDown(mc.player)
               + (float)Math.random() * this.randomDelay.getValue() * 100.0F * (this.tpsSync.getValue() ? Managers.SERVER.getTpsFactor() : 1.0F)
         );
         if ((this.oneEight.getValue() || EntityUtil.isHolding32k(mc.player) && !this.delay32k.getValue() || this.timer.passedMs((long)wait))
            && (
               this.whileEating.getValue()
                  || !mc.player.isHandActive()
                  || mc.player.getHeldItemOffhand().getItem().equals(Items.SHIELD) && mc.player.getActiveHand() == EnumHand.OFF_HAND
            )) {
            if (this.oneEight.getValue() || EntityUtil.isHolding32k(mc.player) && !this.delay32k.getValue()) {
               if (this.lastDelay == 0.0F) {
                  this.lastDelay = 20.0F / MathUtil.randomBetween(this.minCps.getValue(), this.maxCps.getValue()) * 50.0F;
               }

               if (!this.timer.passedMs((long)this.lastDelay)) {
                  return;
               }

               this.lastDelay = 0.0F;
            }

            target = this.getTarget();
            if (target != null) {
               if (EntityUtil.isHolding32k(mc.player) && !this.delay32k.getValue()) {
                  if (this.multi32k.getValue()) {
                     for(EntityPlayer player : mc.world.playerEntities) {
                        if (EntityUtil.isValid(player, (double)this.range.getValue().floatValue())) {
                           this.teekayAttack(player);
                        }
                     }
                  } else {
                     this.teekayAttack(target);
                  }

                  this.timer.reset();
               } else {
                  if (this.armorBreak.getValue()) {
                     mc.playerController
                        .windowClick(
                           mc.player.inventoryContainer.windowId, 9, mc.player.inventory.currentItem, ClickType.SWAP, mc.player
                        );
                     Managers.INTERACTIONS.attackEntity(target, this.packet.getValue(), this.swing.getValue());
                     mc.playerController
                        .windowClick(
                           mc.player.inventoryContainer.windowId, 9, mc.player.inventory.currentItem, ClickType.SWAP, mc.player
                        );
                     Managers.INTERACTIONS.attackEntity(target, this.packet.getValue(), this.swing.getValue());
                  } else {
                     boolean sneaking = SneakManager.isSneaking;
                     boolean sprinting = mc.player.isSprinting();
                     if (this.sneak.getValue()) {
                        if (sneaking) {
                           mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
                        }

                        if (sprinting) {
                           mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SPRINTING));
                        }
                     }

                     Managers.INTERACTIONS.attackEntity(target, this.packet.getValue(), this.swing.getValue());
                     if (this.sneak.getValue()) {
                        if (sprinting) {
                           mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SPRINTING));
                        }

                        if (sneaking) {
                           mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
                        }
                     }

                     if (this.stopSprint.getValue()) {
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SPRINTING));
                     }
                  }

                  this.timer.reset();
               }
            }
         }
      }
   }

   private void teekayAttack(Entity entity) {
      for(int i = 0; i < this.packetAmount32k.getValue(); ++i) {
         this.startEntityAttackThread(entity, i * this.time32k.getValue());
      }
   }

   private void startEntityAttackThread(Entity entity, int time) {
      new Thread(() -> {
         Timer timer = new Timer();
         timer.reset();

         try {
            Thread.sleep((long)time);
         } catch (InterruptedException var5) {
            Thread.currentThread().interrupt();
         }

         Managers.INTERACTIONS.attackEntity(entity, true, this.swing.getValue());
      }).start();
   }

   private Entity getTarget() {
      Entity target = null;
      double distance = 0.0;
      double maxHealth = 36.0;

      for(Entity entity : mc.world.loadedEntityList) {
         if ((
               this.players.getValue() && entity instanceof EntityPlayer
                  || this.animals.getValue() && EntityUtil.isPassive(entity)
                  || this.neutrals.getValue() && EntityUtil.isNeutralMob(entity)
                  || this.hostiles.getValue() && EntityUtil.isMobAggressive(entity)
                  || this.hostiles.getValue() && this.onlyGhasts.getValue() && entity instanceof EntityGhast
                  || this.others.getValue() && EntityUtil.isVehicle(entity)
                  || this.projectiles.getValue() && EntityUtil.isProjectile(entity)
            )
            && !EntityUtil.invalid(entity, (double)this.range.getValue().floatValue())
            && (EntityUtil.canEntityBeSeen(entity) || !(mc.player.getDistance(entity) > this.wallRange.getValue()))
            && (!this.fovCheck.getValue() || this.isInFov(entity, (float)this.angle.getValue().intValue()))) {
            if (target == null) {
               target = entity;
               distance = (double)mc.player.getDistance(entity);
               maxHealth = (double)EntityUtil.getHealth(entity);
            } else {
               if (entity instanceof EntityPlayer && EntityUtil.isArmorLow((EntityPlayer)entity, 10)) {
                  target = entity;
                  break;
               }

               if (this.targetMode.getValue() == Aura.TargetMode.HEALTH && (double)EntityUtil.getHealth(entity) < maxHealth) {
                  target = entity;
                  distance = (double)mc.player.getDistance(entity);
                  maxHealth = (double)EntityUtil.getHealth(entity);
               } else if (this.targetMode.getValue() == Aura.TargetMode.DISTANCE && (double)mc.player.getDistance(entity) < distance) {
                  target = entity;
                  distance = (double)mc.player.getDistance(entity);
                  maxHealth = (double)EntityUtil.getHealth(entity);
               }
            }
         }
      }

      return target;
   }

   private boolean isInFov(Entity entity, float angle) {
      double x = entity.posX - mc.player.posX;
      double z = entity.posZ - mc.player.posZ;
      double yaw = Math.atan2(x, z) * (180.0 / Math.PI);
      yaw = -yaw;
      angle = (float)((double)angle * 0.5);
      double angleDifference = (((double)mc.player.rotationYaw - yaw) % 360.0 + 540.0) % 360.0 - 180.0;
      return angleDifference > 0.0 && angleDifference < (double)angle || (double)(-angle) < angleDifference && angleDifference < 0.0;
   }

   private double easeInOutQuad(double x) {
      return x < 0.5 ? 2.0 * x * x : 1.0 - Math.pow(-2.0 * x + 2.0, 2.0) / 2.0;
   }

   private static enum Page {
      GLOBAL,
      TARGETS,
      ADVANCED;
   }

   private static enum RenderMode {
      OLD,
      JELLO,
      OFF;
   }

   private static enum TargetMode {
      DISTANCE,
      HEALTH;
   }
}
