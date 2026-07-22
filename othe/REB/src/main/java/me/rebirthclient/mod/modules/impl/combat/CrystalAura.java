//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import me.rebirthclient.api.events.impl.MotionEvent;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.events.impl.UpdateWalkingPlayerEvent;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.CrystalUtil;
import me.rebirthclient.api.util.DamageUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.FadeUtils;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class CrystalAura extends Module {
   public static CrystalAura INSTANCE;
   private final Setting<CrystalAura.Pages> page = this.add(new Setting<>("Page", CrystalAura.Pages.General));
   private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true, v -> this.page.getValue() == CrystalAura.Pages.General));
   private final Setting<Boolean> noUsing = this.add(new Setting<>("NoUsing", true, v -> this.page.getValue() == CrystalAura.Pages.General).setParent());
   private final Setting<Boolean> onlyFood = this.add(
      new Setting<>("OnlyFood", false, v -> this.noUsing.isOpen() && this.page.getValue() == CrystalAura.Pages.General)
   );
   private final Setting<Integer> switchCooldown = this.add(
      new Setting<>("SwitchCooldown", 100, 0, 1000, v -> this.page.getValue() == CrystalAura.Pages.General)
   );
   private final Setting<Double> antiSuicide = this.add(new Setting<>("AntiSuicide", 3.0, 0.0, 10.0, v -> this.page.getValue() == CrystalAura.Pages.General));
   private final Setting<Double> wallRange = this.add(new Setting<>("WallRange", 3.0, 0.0, 6.0, v -> this.page.getValue() == CrystalAura.Pages.General));
   private final Setting<Integer> maxTarget = this.add(new Setting<>("MaxTarget", 3, 1, 6, v -> this.page.getValue() == CrystalAura.Pages.General));
   private final Setting<Double> targetRange = this.add(new Setting<>("TargetRange", 6.0, 0.0, 20.0, v -> this.page.getValue() == CrystalAura.Pages.General));
   private final Setting<Integer> updateDelay = this.add(new Setting<>("UpdateDelay", 50, 0, 1000, v -> this.page.getValue() == CrystalAura.Pages.General));
   private final Setting<Boolean> place = this.add(new Setting<>("Place", true, v -> this.page.getValue() == CrystalAura.Pages.Place));
   private final Setting<Integer> placeDelay = this.add(
      new Setting<>("PlaceDelay", 300, 0, 1000, v -> this.page.getValue() == CrystalAura.Pages.Place && this.place.getValue())
   );
   private final Setting<Double> placeRange = this.add(
      new Setting<>("PlaceRange", 5.0, 0.0, 6, v -> this.page.getValue() == CrystalAura.Pages.Place && this.place.getValue())
   );
   private final Setting<Double> placeMinDamage = this.add(
      new Setting<>("PlaceMin", 5.0, 0.0, 36.0, v -> this.page.getValue() == CrystalAura.Pages.Place && this.place.getValue())
   );
   private final Setting<Double> placeMaxSelf = this.add(
      new Setting<>("PlaceMaxSelf", 12.0, 0.0, 36.0, v -> this.page.getValue() == CrystalAura.Pages.Place && this.place.getValue())
   );
   private final Setting<CrystalAura.SwapMode> autoSwap = this.add(
      new Setting<>("AutoSwap", CrystalAura.SwapMode.OFF, v -> this.page.getValue() == CrystalAura.Pages.Place && this.place.getValue())
   );
   private final Setting<Boolean> extraPlace = this.add(
      new Setting<>("ExtraPlace", true, v -> this.page.getValue() == CrystalAura.Pages.Place && this.place.getValue())
   );
   private final Setting<Boolean> Break = this.add(new Setting<>("Break", true, v -> this.page.getValue() == CrystalAura.Pages.Break));
   private final Setting<Integer> breakDelay = this.add(
      new Setting<>("BreakDelay", 300, 0, 1000, v -> this.page.getValue() == CrystalAura.Pages.Break && this.Break.getValue())
   );
   private final Setting<Double> breakRange = this.add(
      new Setting<>("BreakRange", 5.0, 0.0, 6.0, v -> this.page.getValue() == CrystalAura.Pages.Break && this.Break.getValue())
   );
   private final Setting<Double> breakMinDamage = this.add(
      new Setting<>("BreakMin", 4.0, 0.0, 36.0, v -> this.page.getValue() == CrystalAura.Pages.Break && this.Break.getValue())
   );
   private final Setting<Double> breakMaxSelf = this.add(
      new Setting<>("SelfBreak", 12.0, 0.0, 36.0, v -> this.page.getValue() == CrystalAura.Pages.Break && this.Break.getValue())
   );
   private final Setting<Double> preferBreakDamage = this.add(
      new Setting<>("PreferBreak", 8.0, 0.0, 36.0, v -> this.page.getValue() == CrystalAura.Pages.Break && this.Break.getValue())
   );
   private final Setting<Boolean> breakOnlyHasCrystal = this.add(
      new Setting<>("OnlyHasCrystal", false, v -> this.page.getValue() == CrystalAura.Pages.Break && this.Break.getValue())
   );
   private final Setting<CrystalAura.TargetRenderMode> targetRender = this.add(
      new Setting<>("TargetRender", CrystalAura.TargetRenderMode.JELLO, v -> this.page.getValue() == CrystalAura.Pages.Render)
   );
   private final Setting<Color> targetColor = this.add(
      new Setting<>(
         "TargetColor",
         new Color(255, 255, 255, 255),
         v -> this.page.getValue() == CrystalAura.Pages.Render && this.targetRender.getValue() != CrystalAura.TargetRenderMode.OFF
      )
   );
   private final Setting<Boolean> render = this.add(new Setting<>("Render", true, v -> this.page.getValue() == CrystalAura.Pages.Render));
   private final Setting<Boolean> outline = this.add(
      new Setting<>("Outline", true, v -> this.page.getValue() == CrystalAura.Pages.Render && this.render.getValue()).setParent()
   );
   private final Setting<Integer> outlineAlpha = this.add(
      new Setting<>("OutlineAlpha", 150, 0, 255, v -> this.outline.isOpen() && this.page.getValue() == CrystalAura.Pages.Render && this.render.getValue())
   );
   private final Setting<Boolean> box = this.add(
      new Setting<>("Box", true, v -> this.page.getValue() == CrystalAura.Pages.Render && this.render.getValue()).setParent()
   );
   private final Setting<Integer> boxAlpha = this.add(
      new Setting<>("BoxAlpha", 70, 0, 255, v -> this.box.isOpen() && this.page.getValue() == CrystalAura.Pages.Render && this.render.getValue())
   );
   private final Setting<Boolean> text = this.add(
      new Setting<>("DamageText", true, v -> this.page.getValue() == CrystalAura.Pages.Render && this.render.getValue())
   );
   private final Setting<Boolean> reset = this.add(
      new Setting<>("Reset", true, v -> this.page.getValue() == CrystalAura.Pages.Render && this.render.getValue())
   );
   private final Setting<Color> color = this.add(
      new Setting<>("Color", new Color(255, 255, 255), v -> this.page.getValue() == CrystalAura.Pages.Render && this.render.getValue()).hideAlpha()
   );
   private final Setting<Integer> animationTime = this.add(
      new Setting<>("AnimationTime", 500, 1, 3000, v -> this.page.getValue() == CrystalAura.Pages.Render && this.render.getValue())
   );
   private final Setting<Integer> startTime = this.add(
      new Setting<>("StartFadeTime", 300, 0, 2000, v -> this.page.getValue() == CrystalAura.Pages.Render && this.render.getValue())
   );
   private final Setting<Integer> fadeTime = this.add(
      new Setting<>("FadeTime", 300, 0, 2000, v -> this.page.getValue() == CrystalAura.Pages.Render && this.render.getValue())
   );
   private final Setting<Integer> predictTicks = this.add(new Setting<>("PredictTicks", 4, 0, 10, v -> this.page.getValue() == CrystalAura.Pages.Predict));
   private final Setting<Boolean> collision = this.add(new Setting<>("Collision", false, v -> this.page.getValue() == CrystalAura.Pages.Predict));
   private final Setting<Boolean> terrainIgnore = this.add(new Setting<>("TerrainIgnore", false, v -> this.page.getValue() == CrystalAura.Pages.Predict));
   private final Setting<Boolean> slowFace = this.add(new Setting<>("SlowFace", true, v -> this.page.getValue() == CrystalAura.Pages.Misc).setParent());
   private final Setting<Integer> slowDelay = this.add(
      new Setting<>("SlowDelay", 600, 0, 2000, v -> this.page.getValue() == CrystalAura.Pages.Misc && this.slowFace.isOpen())
   );
   private final Setting<Double> slowMinDamage = this.add(
      new Setting<>("SlowMin", 3.0, 0.0, 36.0, v -> this.page.getValue() == CrystalAura.Pages.Misc && this.slowFace.isOpen())
   );
   private final Setting<Boolean> armorBreaker = this.add(new Setting<>("ArmorBreaker", true, v -> this.page.getValue() == CrystalAura.Pages.Misc).setParent());
   private final Setting<Integer> maxDura = this.add(
      new Setting<>("MaxDura", 8, 0, 100, v -> this.page.getValue() == CrystalAura.Pages.Misc && this.armorBreaker.isOpen())
   );
   private final Setting<Double> armorBreakerDamage = this.add(
      new Setting<>("BreakerDamage", 3.0, 0.0, 36.0, v -> this.page.getValue() == CrystalAura.Pages.Misc && this.armorBreaker.isOpen())
   );
   private final Setting<Boolean> test = this.add(new Setting<>("Test", false, v -> this.page.getValue() == CrystalAura.Pages.DEV));
   private final Timer switchTimer = new Timer();
   private final Timer delayTimer = new Timer();
   private final Timer placeTimer = new Timer();
   private final Timer faceTimer = new Timer();
   private EntityPlayer displayTarget;
   public static BlockPos lastPos;
   private float lastYaw = 0.0F;
   private float lastPitch = 0.0F;
   private int lastHotbar = -1;
   private BigDecimal lastDamage;
   private final Timer noPosTimer = new Timer();
   double lastSize = 0.0;
   private BlockPos renderPos = null;
   private AxisAlignedBB lastBB = null;
   private AxisAlignedBB nowBB = null;
   private final FadeUtils fadeUtils = new FadeUtils(500L);
   private final FadeUtils animation = new FadeUtils(500L);

   public CrystalAura() {
      super("CrystalAura", "broken", Category.COMBAT);
      INSTANCE = this;
   }

   @Override
   public void onTick() {
      this.update();
   }

   @Override
   public void onUpdate() {
      this.update();
   }

   @Override
   public void onRender3D(Render3DEvent event) {
      this.update();
      if (this.displayTarget != null) {
         if (this.targetRender.getValue() == CrystalAura.TargetRenderMode.OLD) {
            RenderUtil.drawEntityBoxESP(this.displayTarget, this.targetColor.getValue(), true, new Color(255, 255, 255, 130), 0.7F, true, true, 35);
         } else if (this.targetRender.getValue() == CrystalAura.TargetRenderMode.JELLO) {
            double everyTime = 1500.0;
            double drawTime = (double)System.currentTimeMillis() % everyTime;
            boolean drawMode = drawTime > everyTime / 2.0;
            double drawPercent = drawTime / (everyTime / 2.0);
            if (!drawMode) {
               drawPercent = 1.0 - drawPercent;
            } else {
               --drawPercent;
            }

            drawPercent = drawPercent < 0.5 ? 2.0 * drawPercent * drawPercent : 1.0 - Math.pow(-2.0 * drawPercent + 2.0, 2.0) / 2.0;
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
            double radius = (double)this.displayTarget.width;
            double height = (double)this.displayTarget.height + 0.1;
            double x = this.displayTarget.lastTickPosX
               + (this.displayTarget.posX - this.displayTarget.lastTickPosX) * (double)mc.getRenderPartialTicks()
               - mc.renderManager.viewerPosX;
            double y = this.displayTarget.lastTickPosY
               + (this.displayTarget.posY - this.displayTarget.lastTickPosY) * (double)mc.getRenderPartialTicks()
               - mc.renderManager.viewerPosY
               + height * drawPercent;
            double z = this.displayTarget.lastTickPosZ
               + (this.displayTarget.posZ - this.displayTarget.lastTickPosZ) * (double)mc.getRenderPartialTicks()
               - mc.renderManager.viewerPosZ;
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

      if (this.nowBB != null && this.render.getValue() && this.fadeUtils.easeOutQuad() < 1.0) {
         if (this.box.getValue()) {
            RenderUtil.drawBBFill(
               this.nowBB, this.color.getValue(), (int)((double)this.boxAlpha.getValue().intValue() * Math.abs(this.fadeUtils.easeOutQuad() - 1.0))
            );
         }

         if (this.outline.getValue()) {
            RenderUtil.drawBBBox(
               this.nowBB, this.color.getValue(), (int)((double)this.outlineAlpha.getValue().intValue() * Math.abs(this.fadeUtils.easeOutQuad() - 1.0))
            );
         }

         if (this.text.getValue() && lastPos != null) {
            RenderUtil.drawText(this.nowBB, String.valueOf(this.lastDamage));
         }
      } else if (this.reset.getValue()) {
         this.nowBB = null;
      }
   }

   @SubscribeEvent
   public void onUpdateWalk(UpdateWalkingPlayerEvent event) {
      this.update();
   }

   @SubscribeEvent(
      priority = EventPriority.NORMAL
   )
   public void onMotion(MotionEvent event) {
      if (!fullNullCheck()) {
         this.update();
         if (lastPos != null && this.rotate.getValue()) {
            event.setYaw(this.lastYaw);
            event.setPitch(this.lastPitch);
         }
      }
   }

   @SubscribeEvent
   public void onPacketSend(PacketEvent.Send event) {
      if (!fullNullCheck()) {
         if (event.getPacket() instanceof CPacketHeldItemChange && ((CPacketHeldItemChange)event.getPacket()).getSlotId() != this.lastHotbar) {
            this.lastHotbar = ((CPacketHeldItemChange)event.getPacket()).getSlotId();
            this.switchTimer.reset();
         }
      }
   }

   public static boolean canPlaceCrystal(BlockPos pos, boolean ignoreCrystal) {
      BlockPos obsPos = pos.down();
      BlockPos boost = obsPos.up();
      BlockPos boost2 = obsPos.up(2);
      return (getBlock(obsPos) == Blocks.BEDROCK || getBlock(obsPos) == Blocks.OBSIDIAN)
         && (getBlock(boost) == Blocks.AIR || getBlock(boost) == Blocks.FIRE && CombatSetting.INSTANCE.placeInFire.getValue())
         && noEntity(boost, ignoreCrystal)
         && getBlock(boost2) == Blocks.AIR
         && noEntity(boost2, ignoreCrystal);
   }

   public static boolean noEntity(BlockPos pos, boolean ignoreCrystal) {
      for(Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
         if (!(entity instanceof EntityEnderCrystal) || !ignoreCrystal) {
            return false;
         }
      }

      return true;
   }

   public static Block getBlock(BlockPos pos) {
      return mc.world.getBlockState(pos).getBlock();
   }

   private void update() {
      if (!fullNullCheck()) {
         this.fadeUtils.setLength((long)this.fadeTime.getValue().intValue());
         if (lastPos != null) {
            this.lastBB = mc.world
               .getBlockState(new BlockPos(lastPos.down()))
               .getSelectedBoundingBox(mc.world, new BlockPos(lastPos.down()));
            this.noPosTimer.reset();
            if (this.nowBB == null) {
               this.nowBB = this.lastBB;
            }

            if (this.renderPos == null || !this.renderPos.equals(lastPos)) {
               this.animation
                  .setLength(
                     this.animationTime.getValue() <= 0
                        ? 0L
                        : (
                           Math.abs(this.nowBB.minX - this.lastBB.minX)
                                    + Math.abs(this.nowBB.minY - this.lastBB.minY)
                                    + Math.abs(this.nowBB.minZ - this.lastBB.minZ)
                                 <= 6.0
                              ? (long)(
                                 (
                                       Math.abs(this.nowBB.minX - this.lastBB.minX)
                                          + Math.abs(this.nowBB.minY - this.lastBB.minY)
                                          + Math.abs(this.nowBB.minZ - this.lastBB.minZ)
                                    )
                                    * (double)this.animationTime.getValue().intValue()
                              )
                              : (long)this.animationTime.getValue().intValue() * 6L
                        )
                  );
               this.animation.reset();
               this.renderPos = lastPos;
            }
         }

         if (!this.noPosTimer.passedMs((long)this.startTime.getValue().intValue())) {
            this.fadeUtils.reset();
         }

         double size = this.animation.easeOutQuad();
         if (this.nowBB != null && this.lastBB != null) {
            if (Math.abs(this.nowBB.minX - this.lastBB.minX)
                  + Math.abs(this.nowBB.minY - this.lastBB.minY)
                  + Math.abs(this.nowBB.minZ - this.lastBB.minZ)
               > 16.0) {
               this.nowBB = this.lastBB;
            }

            if (this.lastSize != size) {
               this.nowBB = new AxisAlignedBB(
                  this.nowBB.minX + (this.lastBB.minX - this.nowBB.minX) * size,
                  this.nowBB.minY + (this.lastBB.minY - this.nowBB.minY) * size,
                  this.nowBB.minZ + (this.lastBB.minZ - this.nowBB.minZ) * size,
                  this.nowBB.maxX + (this.lastBB.maxX - this.nowBB.maxX) * size,
                  this.nowBB.maxY + (this.lastBB.maxY - this.nowBB.maxY) * size,
                  this.nowBB.maxZ + (this.lastBB.maxZ - this.nowBB.maxZ) * size
               );
               this.lastSize = size;
            }
         }

         if (this.delayTimer.passedMs((long)this.updateDelay.getValue().intValue())) {
            if (!this.noUsing.getValue() || !EntityUtil.isEating() && (!mc.player.isHandActive() || this.onlyFood.getValue())) {
               if (!this.switchTimer.passedMs((long)this.switchCooldown.getValue().intValue())) {
                  lastPos = null;
               } else if (this.breakOnlyHasCrystal.getValue()
                  && !mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL)
                  && !mc.player.getHeldItemOffhand().getItem().equals(Items.END_CRYSTAL)
                  && !this.findCrystal()) {
                  lastPos = null;
               } else {
                  this.delayTimer.reset();
                  int targetCount = 0;
                  EntityEnderCrystal bestBreakCrystal = null;
                  float bestBreakDamage = 0.0F;
                  BlockPos bestPlacePos = null;
                  float bestPlaceDamage = 0.0F;
                  EntityPlayer placeTarget = null;
                  EntityPlayer breakTarget = null;

                  for(EntityPlayer target : mc.world.playerEntities) {
                     if (!EntityUtil.invalid(target, this.targetRange.getValue())) {
                        if (targetCount >= this.maxTarget.getValue()) {
                           break;
                        }

                        ++targetCount;

                        for(Entity crystal : mc.world.loadedEntityList) {
                           if (crystal instanceof EntityEnderCrystal
                              && !((double)mc.player.getDistance(crystal) > this.breakRange.getValue())
                              && (mc.player.canEntityBeSeen(crystal) || !((double)mc.player.getDistance(crystal) > this.wallRange.getValue()))) {
                              float damage = CrystalUtil.calculateDamage(
                                 (EntityEnderCrystal)crystal, target, this.predictTicks.getValue(), this.collision.getValue(), this.terrainIgnore.getValue()
                              );
                              float selfDamage = DamageUtil.calculateDamage(crystal, mc.player);
                              if ((
                                    !((double)selfDamage > this.breakMaxSelf.getValue())
                                       || !(this.breakMaxSelf.getValue() < (double)(mc.player.getHealth() + mc.player.getAbsorptionAmount()))
                                 )
                                 && (
                                    !(this.antiSuicide.getValue() > 0.0)
                                       || !(
                                          (double)selfDamage
                                             > (double)(mc.player.getHealth() + mc.player.getAbsorptionAmount()) - this.antiSuicide.getValue()
                                       )
                                 )
                                 && (!((double)damage < this.getBreakDamage(target)) || !(damage < EntityUtil.getHealth(target)))
                                 && (bestBreakCrystal == null || damage > bestBreakDamage)) {
                                 breakTarget = target;
                                 bestBreakCrystal = (EntityEnderCrystal)crystal;
                                 bestBreakDamage = damage;
                              }
                           }
                        }

                        if (mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL)
                           || mc.player.getHeldItemOffhand().getItem().equals(Items.END_CRYSTAL)
                           || this.findCrystal()) {
                           for(BlockPos pos : BlockUtil.getBox(
                              (float)(this.placeRange.getValue() + 1.0), EntityUtil.getEntityPos(mc.player).down()
                           )) {
                              if (canPlaceCrystal(pos, false)
                                 && !behindWall(pos, INSTANCE.wallRange.getValue())
                                 && !(
                                    mc.player
                                          .getDistance((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5)
                                       > this.placeRange.getValue()
                                 )) {
                                 float damage = CrystalUtil.calculateDamage(
                                    pos.down(), target, this.predictTicks.getValue(), this.collision.getValue(), this.terrainIgnore.getValue()
                                 );
                                 float selfDamage = DamageUtil.calculateDamage(pos.down(), mc.player);
                                 if ((
                                       !((double)selfDamage > this.placeMaxSelf.getValue())
                                          || !((double)(mc.player.getHealth() + mc.player.getAbsorptionAmount()) > this.placeMaxSelf.getValue())
                                    )
                                    && (
                                       !(this.antiSuicide.getValue() > 0.0)
                                          || !(
                                             (double)selfDamage
                                                > (double)(mc.player.getHealth() + mc.player.getAbsorptionAmount())
                                                   - this.antiSuicide.getValue()
                                          )
                                    )
                                    && (!((double)damage < this.getPlaceDamage(target)) || !(damage < EntityUtil.getHealth(target)))
                                    && (bestPlacePos == null || damage > bestPlaceDamage)) {
                                    placeTarget = target;
                                    bestPlacePos = pos;
                                    bestPlaceDamage = damage;
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (bestPlacePos == null && bestBreakCrystal == null) {
                     lastPos = null;
                     this.displayTarget = null;
                  } else if (bestBreakCrystal == null) {
                     this.displayTarget = placeTarget;
                     this.doPlace(bestPlacePos, (double)bestPlaceDamage);
                  } else if (bestPlacePos == null) {
                     this.displayTarget = breakTarget;
                     this.doBreak(bestBreakCrystal, (double)bestBreakDamage);
                  } else {
                     if (!(bestBreakDamage >= bestPlaceDamage) && !((double)bestBreakDamage >= this.preferBreakDamage.getValue())) {
                        this.displayTarget = placeTarget;
                        this.doPlace(bestPlacePos, (double)bestPlaceDamage);
                     } else if (!CombatUtil.breakTimer.passedMs((long)this.breakDelay.getValue().intValue()) && bestBreakDamage < bestPlaceDamage) {
                        this.displayTarget = placeTarget;
                        this.doPlace(bestPlacePos, (double)bestPlaceDamage);
                     } else {
                        this.displayTarget = breakTarget;
                        this.doBreak(bestBreakCrystal, (double)bestBreakDamage);
                     }
                  }
               }
            } else {
               lastPos = null;
            }
         }
      }
   }

   private double getPlaceDamage(EntityPlayer target) {
      if (this.slowFace.getValue() && this.faceTimer.passedMs((long)this.slowDelay.getValue().intValue())) {
         return this.slowMinDamage.getValue();
      } else {
         if (this.armorBreaker.getValue()) {
            ItemStack helm = target.inventoryContainer.getSlot(5).getStack();
            ItemStack chest = target.inventoryContainer.getSlot(6).getStack();
            ItemStack legging = target.inventoryContainer.getSlot(7).getStack();
            ItemStack feet = target.inventoryContainer.getSlot(8).getStack();
            if (!helm.isEmpty && EntityUtil.getDamagePercent(helm) <= this.maxDura.getValue()
               || !chest.isEmpty && EntityUtil.getDamagePercent(chest) <= this.maxDura.getValue()
               || !legging.isEmpty && EntityUtil.getDamagePercent(legging) <= this.maxDura.getValue()
               || !feet.isEmpty && EntityUtil.getDamagePercent(feet) <= this.maxDura.getValue()) {
               return this.armorBreakerDamage.getValue();
            }
         }

         return this.placeMinDamage.getValue();
      }
   }

   private double getBreakDamage(EntityPlayer target) {
      if (this.slowFace.getValue() && this.faceTimer.passedMs((long)this.slowDelay.getValue().intValue())) {
         return this.slowMinDamage.getValue();
      } else {
         if (this.armorBreaker.getValue()) {
            ItemStack helm = target.inventoryContainer.getSlot(5).getStack();
            ItemStack chest = target.inventoryContainer.getSlot(6).getStack();
            ItemStack legging = target.inventoryContainer.getSlot(7).getStack();
            ItemStack feet = target.inventoryContainer.getSlot(8).getStack();
            if (!helm.isEmpty && EntityUtil.getDamagePercent(helm) <= this.maxDura.getValue()
               || !chest.isEmpty && EntityUtil.getDamagePercent(chest) <= this.maxDura.getValue()
               || !legging.isEmpty && EntityUtil.getDamagePercent(legging) <= this.maxDura.getValue()
               || !feet.isEmpty && EntityUtil.getDamagePercent(feet) <= this.maxDura.getValue()) {
               return this.armorBreakerDamage.getValue();
            }
         }

         return this.breakMinDamage.getValue();
      }
   }

   private boolean findCrystal() {
      if (this.autoSwap.getValue() == CrystalAura.SwapMode.OFF) {
         return false;
      } else if (this.autoSwap.getValue() != CrystalAura.SwapMode.NORMAL && this.autoSwap.getValue() != CrystalAura.SwapMode.SILENT) {
         return InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL, true, true) != -1;
      } else {
         return InventoryUtil.findItemInHotbar(Items.END_CRYSTAL) != -1;
      }
   }

   private void doBreak(EntityEnderCrystal entity, double bestBreakDamage) {
      this.faceTimer.reset();
      if (this.Break.getValue()) {
         lastPos = EntityUtil.getEntityPos(entity);
         if (this.rotate.getValue()) {
            this.faceVector(new Vec3d(entity.posX, entity.posY + 0.25, entity.posZ));
         }

         this.lastDamage = BigDecimal.valueOf(bestBreakDamage).setScale(1, RoundingMode.UP);
         if (CombatUtil.breakTimer.passedMs((long)this.breakDelay.getValue().intValue())) {
            CombatUtil.breakTimer.reset();
            mc.player.connection.sendPacket(new CPacketUseEntity(entity));
            mc.player.swingArm(EnumHand.MAIN_HAND);
            if (this.placeTimer.passedMs((long)this.placeDelay.getValue().intValue()) && this.extraPlace.getValue()) {
               int targetCount = 0;
               BlockPos bestPlacePos = null;
               float bestPlaceDamage = 0.0F;
               EntityPlayer placeTarget = null;

               for(EntityPlayer target : mc.world.playerEntities) {
                  if (!EntityUtil.invalid(target, this.targetRange.getValue())) {
                     if (targetCount >= this.maxTarget.getValue()) {
                        break;
                     }

                     ++targetCount;
                     if (mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL)
                        || mc.player.getHeldItemOffhand().getItem().equals(Items.END_CRYSTAL)
                        || this.findCrystal()) {
                        for(BlockPos pos : BlockUtil.getBox(
                           (float)(this.placeRange.getValue() + 1.0), EntityUtil.getEntityPos(mc.player).down()
                        )) {
                           if (canPlaceCrystal(pos, true)
                              && !behindWall(pos, INSTANCE.wallRange.getValue())
                              && !(
                                 mc.player
                                       .getDistance((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5)
                                    > this.placeRange.getValue()
                              )) {
                              float damage = CrystalUtil.calculateDamage(
                                 pos.down(), target, this.predictTicks.getValue(), this.collision.getValue(), this.terrainIgnore.getValue()
                              );
                              float selfDamage = DamageUtil.calculateDamage(pos.down(), mc.player);
                              if ((
                                    !((double)selfDamage > this.placeMaxSelf.getValue())
                                       || !((double)(mc.player.getHealth() + mc.player.getAbsorptionAmount()) > this.placeMaxSelf.getValue())
                                 )
                                 && (
                                    !(this.antiSuicide.getValue() > 0.0)
                                       || !(
                                          (double)selfDamage
                                             > (double)(mc.player.getHealth() + mc.player.getAbsorptionAmount()) - this.antiSuicide.getValue()
                                       )
                                 )
                                 && (!((double)damage < this.getPlaceDamage(target)) || !(damage < EntityUtil.getHealth(target)))
                                 && (bestPlacePos == null || damage > bestPlaceDamage)) {
                                 placeTarget = target;
                                 bestPlacePos = pos;
                                 bestPlaceDamage = damage;
                              }
                           }
                        }
                     }
                  }
               }

               if (bestPlacePos != null) {
                  this.displayTarget = placeTarget;
                  this.doPlace(bestPlacePos, (double)bestPlaceDamage);
               }
            }
         }
      }
   }

   private void doPlace(BlockPos pos, double bestPlaceDamage) {
      if (this.place.getValue()) {
         if (!mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL)
            && !mc.player.getHeldItemOffhand().getItem().equals(Items.END_CRYSTAL)
            && !this.findCrystal()) {
            lastPos = null;
         } else {
            lastPos = pos;
            pos = pos.down();
            this.lastDamage = BigDecimal.valueOf(bestPlaceDamage).setScale(1, RoundingMode.UP);
            if (this.placeTimer.passedMs((long)this.placeDelay.getValue().intValue())) {
               this.placeTimer.reset();
               if (mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL)
                  || mc.player.getHeldItemOffhand().getItem().equals(Items.END_CRYSTAL)) {
                  this.placeCrystal(pos.up(), this.rotate.getValue());
               } else if (this.findCrystal()) {
                  int old = mc.player.inventory.currentItem;
                  int crystal = -1;
                  if (this.autoSwap.getValue() == CrystalAura.SwapMode.NORMAL || this.autoSwap.getValue() == CrystalAura.SwapMode.SILENT) {
                     crystal = InventoryUtil.findItemInHotbar(Items.END_CRYSTAL);
                     if (crystal == -1) {
                        return;
                     }

                     InventoryUtil.doSwap(crystal);
                  }

                  if (this.autoSwap.getValue() == CrystalAura.SwapMode.BYPASS) {
                     crystal = InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL, true, true);
                     if (crystal == -1) {
                        return;
                     }

                     mc.playerController.windowClick(0, crystal, old, ClickType.SWAP, mc.player);
                  }

                  this.placeCrystal(pos.up(), this.rotate.getValue());
                  if (this.autoSwap.getValue() == CrystalAura.SwapMode.SILENT && crystal != -1) {
                     InventoryUtil.doSwap(old);
                  }

                  if (this.autoSwap.getValue() == CrystalAura.SwapMode.BYPASS && crystal != -1) {
                     mc.playerController.windowClick(0, crystal, old, ClickType.SWAP, mc.player);
                  }
               }
            }
         }
      }
   }

   public void placeCrystal(BlockPos pos, boolean rotate) {
      boolean offhand = mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
      BlockPos obsPos = pos.down();
      RayTraceResult result = mc.world
         .rayTraceBlocks(
            new Vec3d(mc.player.posX, mc.player.posY + (double)mc.player.getEyeHeight(), mc.player.posZ),
            new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() - 0.5, (double)pos.getZ() + 0.5)
         );
      EnumFacing facing = result != null && result.sideHit != null ? result.sideHit : EnumFacing.UP;
      Vec3d vec = new Vec3d(obsPos).add(0.5, 0.5, 0.5).add(new Vec3d(facing.getDirectionVec()).scale(0.5));
      if (rotate) {
         this.faceVector(vec);
      }

      mc.player
         .connection
         .sendPacket(new CPacketPlayerTryUseItemOnBlock(obsPos, facing, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0F, 0.0F, 0.0F));
      mc.player.swingArm(offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
   }

   public void faceVector(Vec3d vec) {
      float[] rotations = EntityUtil.getLegitRotations(vec);
      CombatSetting.vec = vec;
      CombatSetting.timer.reset();
      this.sendPlayerRot(rotations[0], rotations[1], mc.player.onGround);
   }

   public void sendPlayerRot(float yaw, float pitch, boolean onGround) {
      this.lastYaw = yaw;
      this.lastPitch = pitch;
      mc.player.connection.sendPacket(new Rotation(yaw, pitch, onGround));
   }

   public static boolean behindWall(BlockPos pos, double wallRange) {
      if (mc.world
            .rayTraceBlocks(
               new Vec3d(
                  mc.player.posX, mc.player.posY + (double)mc.player.getEyeHeight(), mc.player.posZ
               ),
               new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 1.7, (double)pos.getZ() + 0.5),
               false,
               true,
               false
            )
         == null) {
         return false;
      } else {
         return mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5) > wallRange;
      }
   }

   @Override
   public String getInfo() {
      if (this.displayTarget != null) {
         return this.lastDamage != null ? this.displayTarget.getName() + ", " + this.lastDamage : this.displayTarget.getName();
      } else {
         return null;
      }
   }

   public static enum Pages {
      General,
      Place,
      Break,
      Misc,
      Predict,
      Render,
      DEV;
   }

   public static enum SwapMode {
      OFF,
      NORMAL,
      SILENT,
      BYPASS;
   }

   private static enum TargetRenderMode {
      OLD,
      JELLO,
      OFF;
   }
}
