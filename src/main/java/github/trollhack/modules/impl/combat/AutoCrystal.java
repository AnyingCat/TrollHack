package github.trollhack.modules.impl.combat;

import github.trollhack.core.Managers;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.settings.impl.ColorSetting;
import github.trollhack.settings.impl.EnumSetting;
import github.trollhack.settings.impl.FloatSetting;
import github.trollhack.settings.impl.IntegerSetting;
import github.trollhack.utils.math.RotationUtils;
import github.trollhack.utils.render.ESPRenderer;
import github.trollhack.utils.render.ProjectionUtil;
import github.trollhack.utils.render.Render3DUtil;
import github.trollhack.utils.render.font.FontRenderers;
import github.trollhack.utils.world.BlockUtil;
import github.trollhack.utils.world.EntityUtil;
import github.trollhack.utils.world.ExplosionUtil;
import github.trollhack.utils.world.InventoryUtil;
import github.trollhack.utils.world.PredictionUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoCrystal extends Module {
    public static final AutoCrystal INSTANCE = new AutoCrystal();

    public enum Page { GENERAL, PLACE, BREAK, TARGET, SWITCH, ROTATE, RENDER }
    public enum TargetMode { CLOSEST, HEALTH, DAMAGE }
    public enum SwitchMode { NONE, NORMAL, SILENT, INVENTORY }
    public enum RotateMode { NONE, PLACE, BREAK, BOTH }

    public final EnumSetting<Page> page = enumSetting("Page", Page.GENERAL);

    public final BooleanSetting place = booleanSetting("Place", true, () -> page.getValue() == Page.PLACE);
    public final FloatSetting placeRange = floatSetting("Place Range", 4.5f, 1.0f, 6.0f, 0.1f, () -> page.getValue() == Page.PLACE);
    public final IntegerSetting placeDelay = integerSetting("Place Delay", 0, 0, 1000, 10, () -> page.getValue() == Page.PLACE);
    public final FloatSetting minDamage = floatSetting("Min Damage", 6.0f, 0.0f, 36.0f, 0.5f, () -> page.getValue() == Page.PLACE);
    public final FloatSetting maxSelfDamage = floatSetting("Max Self Damage", 8.0f, 0.0f, 36.0f, 0.5f, () -> page.getValue() == Page.PLACE);
    public final BooleanSetting noSuicide = booleanSetting("No Suicide", true, () -> page.getValue() == Page.PLACE);
    public final BooleanSetting placeSwing = booleanSetting("Place Swing", true, () -> page.getValue() == Page.PLACE);
    public final BooleanSetting antiSurround = booleanSetting("Anti Surround", true, () -> page.getValue() == Page.PLACE);

    public final BooleanSetting break_ = booleanSetting("Break", true, () -> page.getValue() == Page.BREAK);
    public final FloatSetting breakRange = floatSetting("Break Range", 4.5f, 1.0f, 6.0f, 0.1f, () -> page.getValue() == Page.BREAK);
    public final IntegerSetting breakDelay = integerSetting("Break Delay", 0, 0, 1000, 10, () -> page.getValue() == Page.BREAK);
    public final FloatSetting breakMinDamage = floatSetting("Break Min Damage", 2.0f, 0.0f, 36.0f, 0.5f, () -> page.getValue() == Page.BREAK);
    public final BooleanSetting breakSwing = booleanSetting("Break Swing", true, () -> page.getValue() == Page.BREAK);
    public final IntegerSetting breakAge = integerSetting("Break Age", 0, 0, 20, 1, () -> page.getValue() == Page.BREAK);
    public final BooleanSetting breakOnlyKilling = booleanSetting("Break Only Killing", false, () -> page.getValue() == Page.BREAK);
    public final BooleanSetting smartBreak = booleanSetting("Smart Break", true, () -> page.getValue() == Page.BREAK);

    public final FloatSetting targetRange = floatSetting("Target Range", 12.0f, 1.0f, 20.0f, 0.5f, () -> page.getValue() == Page.TARGET);
    public final EnumSetting<TargetMode> targetMode = enumSetting("Target Mode", TargetMode.CLOSEST, () -> page.getValue() == Page.TARGET);
    public final IntegerSetting predictTicks = integerSetting("Predict Ticks", 0, 0, 20, 1, () -> page.getValue() == Page.TARGET);
    public final FloatSetting healthThreshold = floatSetting("Health Threshold", 0.0f, 0.0f, 36.0f, 1.0f, () -> page.getValue() == Page.TARGET);

    public final EnumSetting<SwitchMode> switchMode = enumSetting("Switch Mode", SwitchMode.NONE, () -> page.getValue() == Page.SWITCH);
    public final IntegerSetting switchDelay = integerSetting("Switch Delay", 100, 0, 1000, 10, () -> page.getValue() == Page.SWITCH && switchMode.getValue() != SwitchMode.NONE);

    public final EnumSetting<RotateMode> rotateMode = enumSetting("Rotate Mode", RotateMode.NONE, () -> page.getValue() == Page.ROTATE);
    public final FloatSetting placeAngle = floatSetting("Place Angle", 30.0f, 0.0f, 180.0f, 5.0f, () -> page.getValue() == Page.ROTATE && rotateMode.getValue() != RotateMode.NONE);
    public final FloatSetting breakAngle = floatSetting("Break Angle", 30.0f, 0.0f, 180.0f, 5.0f, () -> page.getValue() == Page.ROTATE && rotateMode.getValue() != RotateMode.NONE);
    public final BooleanSetting rotateBack = booleanSetting("Rotate Back", true, () -> page.getValue() == Page.ROTATE && rotateMode.getValue() != RotateMode.NONE);
    public final IntegerSetting rotateBackDelay = integerSetting("Back Delay", 100, 0, 1000, 10, () -> page.getValue() == Page.ROTATE && rotateBack.getValue());
    public final BooleanSetting checkFov = booleanSetting("Check FOV", false, () -> page.getValue() == Page.ROTATE && rotateMode.getValue() != RotateMode.NONE);
    public final FloatSetting fov = floatSetting("FOV", 90.0f, 0.0f, 180.0f, 5.0f, () -> page.getValue() == Page.ROTATE && checkFov.getValue());

    public final BooleanSetting render = booleanSetting("Render", true, () -> page.getValue() == Page.RENDER);
    public final BooleanSetting filled = booleanSetting("Filled", true, () -> page.getValue() == Page.RENDER && render.getValue());
    public final BooleanSetting outline = booleanSetting("Outline", true, () -> page.getValue() == Page.RENDER && render.getValue());
    public final BooleanSetting showDamage = booleanSetting("Damage", true, () -> page.getValue() == Page.RENDER && render.getValue());
    public final BooleanSetting showSelfDamage = booleanSetting("Self Damage", false, () -> page.getValue() == Page.RENDER && render.getValue());
    public final FloatSetting animationScale = floatSetting("Animation Scale", 1.0f, 0.0f, 2.0f, 0.1f, () -> page.getValue() == Page.RENDER && render.getValue());
    public final FloatSetting renderRange = floatSetting("Range", 16.0f, 0.0f, 16.0f, 0.5f, () -> page.getValue() == Page.RENDER && render.getValue());
    public final ColorSetting color = colorSetting("Color", new Color(133, 255, 200), () -> page.getValue() == Page.RENDER && render.getValue());
    public final IntegerSetting aFilled = integerSetting("Filled Alpha", 47, 0, 255, 1, () -> page.getValue() == Page.RENDER && render.getValue() && filled.getValue());
    public final IntegerSetting aOutline = integerSetting("Outline Alpha", 127, 0, 255, 1, () -> page.getValue() == Page.RENDER && render.getValue() && outline.getValue());
    public final FloatSetting width = floatSetting("Width", 2.0f, 0.25f, 4.0f, 0.25f, () -> page.getValue() == Page.RENDER && render.getValue() && outline.getValue());

    public final BooleanSetting concurrent = booleanSetting("Concurrent", true, () -> page.getValue() == Page.GENERAL);
    public final BooleanSetting pauseIfEating = booleanSetting("Pause If Eating", false, () -> page.getValue() == Page.GENERAL);
    public final BooleanSetting pauseIfMining = booleanSetting("Pause If Mining", false, () -> page.getValue() == Page.GENERAL);
    public final BooleanSetting debugInfo = booleanSetting("Debug Info", false, () -> page.getValue() == Page.GENERAL);

    private long lastPlaceTime, lastBreakTime, lastSwitchTime, rotateBackTime;
    private int originalSlot = -1, lastSwapSlot = -1;
    private boolean switched, pendingRotateBack;
    private BlockPos renderPlacePos;
    private float renderPlaceDamage;
    private final Map<BlockPos, float[]> renderCrystalMap = new HashMap<>();

    private BlockPos lastPlacePos;
    private long lastPlacePosTime;
    private static final long PLACE_POS_STICKY_TIME = 400L;
    private static final float SAFETY_DAMAGE_DELTA = 1.5f;

    private EndCrystalEntity obstructingCrystal;

    private static final class PlaceCandidate {
        final BlockPos pos;
        final float damage;
        final float selfDamage;
        final double distSq;
        final boolean hasCrystal;

        PlaceCandidate(BlockPos pos, float damage, float selfDamage, double distSq, boolean hasCrystal) {
            this.pos = pos;
            this.damage = damage;
            this.selfDamage = selfDamage;
            this.distSq = distSq;
            this.hasCrystal = hasCrystal;
        }
    }

    public AutoCrystal() {
        super("AutoCrystal", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        lastPlaceTime = lastBreakTime = lastSwitchTime = rotateBackTime = 0;
        originalSlot = lastSwapSlot = -1;
        switched = pendingRotateBack = false;
        renderPlacePos = null;
        renderCrystalMap.clear();
        lastPlacePos = null;
        lastPlacePosTime = 0;
        obstructingCrystal = null;
        PredictionUtil.clear();
    }

    @Override
    public void onDisable() {
        if (switched && originalSlot != -1 && mc.player != null) {
            SwitchMode mode = switchMode.getValue();
            if (mode == SwitchMode.NORMAL || mode == SwitchMode.SILENT) {
                InventoryUtil.switchToSlot(originalSlot);
            } else if (mode == SwitchMode.INVENTORY && lastSwapSlot != -1) {
                InventoryUtil.inventorySwap(lastSwapSlot, mc.player.getInventory().selectedSlot);
                InventoryUtil.syncSelectedSlot();
            }
            switched = false;
            originalSlot = lastSwapSlot = -1;
        }
        pendingRotateBack = false;
        renderPlacePos = null;
        renderCrystalMap.clear();
        lastPlacePos = null;
        lastPlacePosTime = 0;
        obstructingCrystal = null;
        PredictionUtil.clear();
        if (Managers.ROTATION != null) Managers.ROTATION.stopRotating();
    }

    @Override
    public void onUpdate() {
        PredictionUtil.tick();

        if (pendingRotateBack && mc.player != null && mc.getNetworkHandler() != null
                && System.currentTimeMillis() - rotateBackTime >= rotateBackDelay.getValue()) {
            pendingRotateBack = false;
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                    mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround()));
        }

        boolean canAct = !nullCheck()
                && !(pauseIfEating.getValue() && mc.player.isUsingItem())
                && !(pauseIfMining.getValue() && mc.interactionManager != null && mc.interactionManager.isBreakingBlock())
                && (InventoryUtil.isHoldingCrystal()
                    || (switchMode.getValue() == SwitchMode.INVENTORY
                        ? InventoryUtil.hasItemInInventory(Items.END_CRYSTAL)
                        : InventoryUtil.hasCrystal()));

        if (canAct) {
            boolean didBreak = false;
            boolean actedThisTick = false;
            if (break_.getValue()) {
                EndCrystalEntity crystal = getBestBreakCrystal();
                if (crystal != null) {
                    actedThisTick = true;
                    if (System.currentTimeMillis() - lastBreakTime >= breakDelay.getValue()) {
                        if (handleRotation(crystal.getPos().add(0, 0.5, 0), RotateMode.BREAK)) {
                            if (mc.player != null && mc.interactionManager != null) {
                                mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
                                if (breakSwing.getValue()) mc.player.swingHand(Hand.MAIN_HAND);
                                if (lastPlacePos != null) {
                                    BlockPos crystalBase = BlockPos.ofFloored(crystal.getPos()).down();
                                    if (crystalBase.equals(lastPlacePos)) {
                                        lastPlacePos = null;
                                        lastPlacePosTime = 0;
                                    }
                                }
                                if (crystal == obstructingCrystal) {
                                    obstructingCrystal = null;
                                }
                            }
                            lastBreakTime = System.currentTimeMillis();
                            didBreak = true;
                        }
                    }
                }
            }
            if (place.getValue() && (!break_.getValue() || concurrent.getValue() || !didBreak)) {
                BlockPos pos = getBestPlacePos();
                renderPlacePos = pos;
                if (pos != null) {
                    actedThisTick = true;
                    if (System.currentTimeMillis() - lastPlaceTime >= placeDelay.getValue()
                            && handleRotation(ExplosionUtil.posToVec(pos), RotateMode.PLACE)) {
                        doPlace(pos);
                        lastPlaceTime = System.currentTimeMillis();
                    }
                }
            } else {
                renderPlacePos = null;
            }
            tryStopRotate(actedThisTick);
        } else {
            renderPlacePos = null;
            tryStopRotate(false);
        }

        if (render.getValue() && mc.player != null && mc.world != null) {
            Map<BlockPos, float[]> newMap = new HashMap<>();
            float rangeSq = renderRange.getValue() * renderRange.getValue();
            if (renderPlacePos != null) {
                Vec3d center = new Vec3d(renderPlacePos.getX() + 0.5, renderPlacePos.getY() + 0.5, renderPlacePos.getZ() + 0.5);
                if (mc.player.squaredDistanceTo(center) <= rangeSq) {
                    newMap.put(renderPlacePos, new float[]{renderPlaceDamage, ExplosionUtil.getSelfDamage(renderPlacePos), 0.0f, 0.0f});
                }
            }
            float scale = animationScale.getValue() <= 0.0f ? 1.0f : 1.0f / animationScale.getValue();
            for (Map.Entry<BlockPos, float[]> entry : renderCrystalMap.entrySet()) {
                BlockPos pos = entry.getKey();
                float[] old = entry.getValue();
                float oldProgress = old[3];
                if (newMap.containsKey(pos)) {
                    float[] newData = newMap.get(pos);
                    newData[2] = oldProgress;
                    newData[3] = Math.min(oldProgress + 0.4f * scale, 1.0f);
                } else if (oldProgress < 2.0f) {
                    newMap.put(pos, new float[]{old[0], old[1], oldProgress, Math.min(oldProgress + 0.2f * scale, 2.0f)});
                }
            }
            renderCrystalMap.clear();
            renderCrystalMap.putAll(newMap);
        }
    }

    private boolean handleRotation(Vec3d target, RotateMode actionType) {
        if (Managers.ROTATION == null || mc.player == null || rotateMode.getValue() == RotateMode.NONE) return true;
        boolean needRotate = switch (rotateMode.getValue()) {
            case PLACE -> actionType == RotateMode.PLACE;
            case BREAK -> actionType == RotateMode.BREAK;
            case BOTH -> true;
            default -> false;
        };
        if (!needRotate) return true;

        float[] targetRot = Managers.ROTATION.getRotation(target);
        boolean rotating = Managers.ROTATION.isRotating();
        float angleThreshold = actionType == RotateMode.PLACE ? placeAngle.getValue() : breakAngle.getValue();

        float currentYaw = rotating ? Managers.ROTATION.getYaw() : mc.player.getYaw();
        float currentPitch = rotating ? Managers.ROTATION.getPitch() : mc.player.getPitch();
        float diff = RotationUtils.angleDiff(currentYaw, currentPitch, targetRot[0], targetRot[1]);

        if (checkFov.getValue() && diff > fov.getValue()) {
            Managers.ROTATION.lookAt(target);
            return false;
        }

        if (diff > angleThreshold) {
            Managers.ROTATION.lookAt(target);
        }
        return true;
    }

    private void tryStopRotate(boolean actedThisTick) {
        if (Managers.ROTATION == null) return;
        if (actedThisTick) {
            pendingRotateBack = false;
            return;
        }
        if (Managers.ROTATION.isRotating()) {
            Managers.ROTATION.stopRotating();
            if (rotateBack.getValue() && mc.player != null && mc.getNetworkHandler() != null) {
                pendingRotateBack = true;
                rotateBackTime = System.currentTimeMillis();
            }
        }
    }

    private PlayerEntity getTarget() {
        List<PlayerEntity> targets = EntityUtil.getTargets(targetRange.getValue(), false);
        if (targets.isEmpty()) return null;
        float threshold = healthThreshold.getValue();
        if (threshold > 0.0f) {
            targets.removeIf(p -> (p.getHealth() + p.getAbsorptionAmount()) > threshold);
            if (targets.isEmpty()) return null;
        }
        if (targetMode.getValue() == TargetMode.CLOSEST) {
            targets.sort(Comparator.comparingDouble(p -> mc.player.squaredDistanceTo(p)));
        } else if (targetMode.getValue() == TargetMode.HEALTH) {
            targets.sort(Comparator.comparingDouble(PlayerEntity::getHealth));
        }
        return targets.get(0);
    }

    private Vec3d getPredictedPos(PlayerEntity target) {
        int ticks = predictTicks.getValue();
        return ticks > 0 ? EntityUtil.predictPos(target, ticks) : target.getPos();
    }

    private EndCrystalEntity findCrystalAt(BlockPos base) {
        if (mc.world == null) return null;
        BlockPos up = base.up();
        Box box = new Box(up.getX(), up.getY(), up.getZ(),
                up.getX() + 1.0, up.getY() + 2.0, up.getZ() + 1.0);
        for (EndCrystalEntity crystal : mc.world.getEntitiesByClass(EndCrystalEntity.class, box, c -> !c.isRemoved())) {
            return crystal;
        }
        return null;
    }

    private EndCrystalEntity getBestBreakCrystal() {
        if (mc.player == null || mc.world == null) return null;
        List<EndCrystalEntity> crystals = EntityUtil.getCrystalsInRange(breakRange.getValue());
        if (crystals.isEmpty()) return null;
        PlayerEntity target = getTarget();
        if (target == null && smartBreak.getValue()) return null;
        Vec3d predictedPos = target != null ? getPredictedPos(target) : null;

        if (obstructingCrystal != null && !obstructingCrystal.isRemoved()
                && crystals.contains(obstructingCrystal)) {
            float selfDamage = ExplosionUtil.getSelfDamage(obstructingCrystal.getPos());
            if (!(noSuicide.getValue() && ExplosionUtil.willKill(mc.player, selfDamage))
                    && selfDamage <= maxSelfDamage.getValue()) {
                return obstructingCrystal;
            }
            obstructingCrystal = null;
        } else {
            obstructingCrystal = null;
        }

        EndCrystalEntity best = null;
        float bestDamage = breakMinDamage.getValue();
        for (EndCrystalEntity crystal : crystals) {
            if (breakAge.getValue() > 0 && crystal.endCrystalAge < breakAge.getValue()) continue;
            Vec3d crystalPos = crystal.getPos();
            if (target != null) {
                float damage = ExplosionUtil.getDamagePredicted(crystalPos, target, predictedPos, true);
                if (breakOnlyKilling.getValue() && !ExplosionUtil.willKill(target, damage)) continue;
                if (smartBreak.getValue()) {
                    float selfDamage = ExplosionUtil.getSelfDamage(crystalPos);
                    if (noSuicide.getValue() && ExplosionUtil.willKill(mc.player, selfDamage)) continue;
                    if (selfDamage > maxSelfDamage.getValue()) continue;
                }
                if (damage > bestDamage) {
                    bestDamage = damage;
                    best = crystal;
                }
            } else {
                float selfDamage = ExplosionUtil.getSelfDamage(crystalPos);
                if (noSuicide.getValue() && ExplosionUtil.willKill(mc.player, selfDamage)) continue;
                if (selfDamage > maxSelfDamage.getValue()) continue;
                if (best == null) best = crystal;
            }
        }
        return best;
    }

    private BlockPos getBestPlacePos() {
        if (mc.player == null || mc.world == null) return null;
        PlayerEntity target = getTarget();
        if (target == null) return null;

        Vec3d predictedTargetPos = getPredictedPos(target);
        Vec3d eye = mc.player.getEyePos();
        float pRange = placeRange.getValue();
        double maxDistSq = pRange * pRange;
        float minDmg = minDamage.getValue();
        float maxSelfDmg = maxSelfDamage.getValue();
        boolean usePredict = predictTicks.getValue() > 0;
        boolean suicide = noSuicide.getValue();

        boolean hasCrystalAtLast = lastPlacePos != null
                && System.currentTimeMillis() - lastPlacePosTime < PLACE_POS_STICKY_TIME
                && BlockUtil.hasExistingCrystal(lastPlacePos);

        if (hasCrystalAtLast) {
            lastPlacePos = null;
            lastPlacePosTime = 0;
        }

        List<PlaceCandidate> candidates = new ArrayList<>();
        for (BlockPos pos : BlockUtil.getSphere(mc.player.getBlockPos(), pRange, true)) {
            if (!BlockUtil.canPlaceCrystal(pos, true)) continue;
            Vec3d placeVec = ExplosionUtil.posToVec(pos);
            double distSq = eye.squaredDistanceTo(placeVec);
            if (distSq > maxDistSq) continue;

            float damage = usePredict
                    ? ExplosionUtil.getDamagePredicted(placeVec, target, predictedTargetPos, true)
                    : ExplosionUtil.getDamage(placeVec, target, true);
            if (damage < 1.5f) continue;

            float selfDamage = ExplosionUtil.getSelfDamage(placeVec);
            if (selfDamage > maxSelfDmg) continue;
            if (suicide && ExplosionUtil.willKill(mc.player, selfDamage)) continue;

            boolean hasCrystal = BlockUtil.hasExistingCrystal(pos);
            candidates.add(new PlaceCandidate(pos, damage, selfDamage, distSq, hasCrystal));
        }

        if (!candidates.isEmpty()) {
            candidates.sort((a, b) -> {
                if (a.hasCrystal != b.hasCrystal) return a.hasCrystal ? 1 : -1;
                if (Math.abs(a.damage - b.damage) < SAFETY_DAMAGE_DELTA) {
                    int selfCmp = Float.compare(a.selfDamage, b.selfDamage);
                    if (selfCmp != 0) return selfCmp;
                    return Double.compare(a.distSq, b.distSq);
                }
                return Float.compare(b.damage, a.damage);
            });

            PlaceCandidate best = candidates.get(0);

            if (lastPlacePos != null
                    && System.currentTimeMillis() - lastPlacePosTime < PLACE_POS_STICKY_TIME
                    && !hasCrystalAtLast) {
                for (PlaceCandidate c : candidates) {
                    if (c.pos.equals(lastPlacePos)
                            && !c.hasCrystal
                            && best.damage - c.damage < SAFETY_DAMAGE_DELTA) {
                        best = c;
                        break;
                    }
                }
            }

            if (best.hasCrystal) {
                obstructingCrystal = findCrystalAt(best.pos);
                renderPlaceDamage = best.damage;
                return null;
            }

            obstructingCrystal = null;

            if (best.damage < minDmg) {
                if (antiSurround.getValue()) {
                    BlockPos antiPos = getAntiSurroundPos(target, eye, maxDistSq);
                    if (antiPos != null) {
                        renderPlaceDamage = ExplosionUtil.getDamage(ExplosionUtil.posToVec(antiPos), target, true);
                        return antiPos;
                    }
                }
                renderPlaceDamage = best.damage;
                return best.pos;
            }

            renderPlaceDamage = best.damage;
            return best.pos;
        }

        if (antiSurround.getValue()) {
            BlockPos antiPos = getAntiSurroundPos(target, eye, maxDistSq);
            if (antiPos != null) {
                renderPlaceDamage = ExplosionUtil.getDamage(ExplosionUtil.posToVec(antiPos), target, true);
                return antiPos;
            }
        }

        renderPlaceDamage = 0.0f;
        return null;
    }

    private BlockPos getAntiSurroundPos(PlayerEntity target, Vec3d eye, double maxDistSq) {
        if (mc.player == null || mc.world == null) return null;
        BlockPos targetPos = target.getBlockPos();
        BlockPos bestAnti = null;
        float bestAntiDamage = 0.0f;

        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP || dir == Direction.DOWN) continue;
            BlockPos surroundBlock = targetPos.offset(dir);
            if (BlockUtil.isAir(surroundBlock) || BlockUtil.isBedrock(surroundBlock)) continue;

            BlockPos[] candidates = {
                    surroundBlock.offset(dir),
                    surroundBlock.offset(Direction.NORTH),
                    surroundBlock.offset(Direction.SOUTH),
                    surroundBlock.offset(Direction.EAST),
                    surroundBlock.offset(Direction.WEST)
            };

            for (BlockPos candidate : candidates) {
                BlockPos found = tryAntiPlace(candidate, eye, maxDistSq);
                if (found != null) {
                    float dmg = ExplosionUtil.getDamage(ExplosionUtil.posToVec(found), target, true);
                    if (dmg > bestAntiDamage) {
                        bestAntiDamage = dmg;
                        bestAnti = found;
                    }
                }
            }
        }
        return bestAnti;
    }

    private BlockPos tryAntiPlace(BlockPos pos, Vec3d eye, double maxDistSq) {
        if (!BlockUtil.canPlaceCrystal(pos, true)) return null;
        Vec3d placeVec = ExplosionUtil.posToVec(pos);
        if (eye.squaredDistanceTo(placeVec) > maxDistSq) return null;
        float selfDamage = ExplosionUtil.getSelfDamage(placeVec);
        if (selfDamage > maxSelfDamage.getValue()) return null;
        if (noSuicide.getValue() && ExplosionUtil.willKill(mc.player, selfDamage)) return null;
        return pos;
    }

    private void doPlace(BlockPos pos) {
        if (mc.player == null || mc.interactionManager == null) return;
        boolean mainHandCrystal = mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL;
        boolean offHandCrystal = mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
        Hand hand = mainHandCrystal ? Hand.MAIN_HAND : (offHandCrystal ? Hand.OFF_HAND : Hand.MAIN_HAND);
        int originalHand = mc.player.getInventory().selectedSlot;
        boolean needSwitch = !mainHandCrystal && !offHandCrystal;

        if (needSwitch) {
            SwitchMode mode = switchMode.getValue();
            if (mode == SwitchMode.NONE) return;
            if (System.currentTimeMillis() - lastSwitchTime < switchDelay.getValue()) return;
            int crystalSlot = switch (mode) {
                case NORMAL, SILENT -> InventoryUtil.getCrystalSlot();
                case INVENTORY -> InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL);
                default -> -1;
            };
            if (crystalSlot == -1) return;
            if (!switched) originalSlot = originalHand;
            lastSwapSlot = crystalSlot;
            if (mode == SwitchMode.NORMAL || mode == SwitchMode.SILENT) {
                InventoryUtil.switchToSlot(crystalSlot);
            } else {
                InventoryUtil.inventorySwap(crystalSlot, mc.player.getInventory().selectedSlot);
            }
            switched = true;
            lastSwitchTime = System.currentTimeMillis();
        }

        mc.interactionManager.interactBlock(mc.player, hand, BlockUtil.createPlaceHit(pos, BlockUtil.getPlaceSide(pos)));
        if (placeSwing.getValue()) mc.player.swingHand(hand);

        lastPlacePos = pos.toImmutable();
        lastPlacePosTime = System.currentTimeMillis();

        if (needSwitch) {
            SwitchMode mode = switchMode.getValue();
            if (mode == SwitchMode.SILENT) {
                InventoryUtil.switchToSlot(originalHand);
                switched = false;
                originalSlot = lastSwapSlot = -1;
            } else if (mode == SwitchMode.INVENTORY && lastSwapSlot != -1) {
                InventoryUtil.inventorySwap(lastSwapSlot, mc.player.getInventory().selectedSlot);
                InventoryUtil.syncSelectedSlot();
                switched = false;
                originalSlot = lastSwapSlot = -1;
            }
        }
    }

    @Override
    public void onRender3D(MatrixStack matrices) {
        if (!render.getValue() || nullCheck() || mc.player == null || renderCrystalMap.isEmpty()) return;
        ESPRenderer renderer = new ESPRenderer();
        renderer.aFilled = filled.getValue() ? aFilled.getValue() : 0;
        renderer.aOutline = outline.getValue() ? aOutline.getValue() : 0;
        renderer.thickness = width.getValue();
        renderer.through = true;
        float partialTicks = Render3DUtil.getTickDelta();
        Color baseColor = color.getValue();
        for (Map.Entry<BlockPos, float[]> entry : renderCrystalMap.entrySet()) {
            BlockPos pos = entry.getKey();
            float[] data = entry.getValue();
            float interpolated = data[2] + (data[3] - data[2]) * partialTicks;
            float animProgress = (float) Math.sin(interpolated * 0.5 * Math.PI);
            double shrink = 0.5 - animProgress * 0.5;
            Box box = new Box(pos);
            renderer.add(new Box(
                    box.minX + shrink, box.minY + shrink, box.minZ + shrink,
                    box.maxX - shrink, box.maxY - shrink, box.maxZ - shrink
            ), new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (int) (animProgress * 255.0f)));
        }
        renderer.render(matrices, false);
    }

    @Override
    public void onRender2D(DrawContext context) {
        if (!render.getValue() || nullCheck() || mc.player == null) return;
        if (!showDamage.getValue() && !showSelfDamage.getValue() || renderCrystalMap.isEmpty()) return;
        float partialTicks = Render3DUtil.getTickDelta();
        for (Map.Entry<BlockPos, float[]> entry : renderCrystalMap.entrySet()) {
            BlockPos pos = entry.getKey();
            float[] data = entry.getValue();
            float interpolated = data[2] + (data[3] - data[2]) * partialTicks;
            int alpha = (int) ((float) Math.sin(interpolated * 0.5 * Math.PI) * 255.0f);
            if (alpha <= 0) continue;
            Vec3d screenPos = ProjectionUtil.worldToScreen(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
            if (screenPos == null || screenPos.z > 1.0) continue;
            StringBuilder text = new StringBuilder();
            if (showDamage.getValue()) text.append(String.format("%.1f", Math.abs(data[0])));
            if (showSelfDamage.getValue()) {
                if (text.length() > 0) text.append('/');
                text.append(String.format("%.1f", Math.abs(data[1])));
            }
            String textStr = text.toString();
            float textWidth = FontRenderers.ducksans.getStringWidth(textStr, 1.0f);
            FontRenderers.ducksans.drawText(context.getMatrices(), textStr,
                    (float) screenPos.x - textWidth / 2.0f,
                    (float) screenPos.y - FontRenderers.ducksans.getStringHeight(1.0f) / 2.0f,
                    1.0f, new Color(255, 255, 255, alpha));
        }
    }

    @Override
    public String getHudInfo() {
        return debugInfo.getValue() ? InventoryUtil.getCrystalCount() + "c" : "";
    }
}
