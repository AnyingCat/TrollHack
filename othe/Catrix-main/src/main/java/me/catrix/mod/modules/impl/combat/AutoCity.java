package me.catrix.mod.modules.impl.combat;

import me.catrix.api.utils.combat.CombatUtil;
import me.catrix.api.utils.entity.EntityUtil;
import me.catrix.api.utils.world.BlockPosX;
import me.catrix.api.utils.world.BlockUtil;
import me.catrix.mod.modules.Module;
import me.catrix.mod.modules.impl.exploit.Blink;
import me.catrix.mod.modules.impl.player.PacketMine;
import me.catrix.mod.modules.settings.impl.BooleanSetting;
import me.catrix.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.CobwebBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Comparator;

import static me.catrix.api.utils.world.BlockUtil.getBlock;

public class AutoCity extends Module {
    public static AutoCity INSTANCE;
    private final BooleanSetting burrow = add(new BooleanSetting("Burrow", true));
    private final BooleanSetting surround = add(new BooleanSetting("Surround", true));
    private final BooleanSetting smartPriority = add(new BooleanSetting("SmartPriority", true));
    private final BooleanSetting lowVersion = add(new BooleanSetting("1.12", false));
    public final SliderSetting targetRange =
            add(new SliderSetting("TargetRange", 6.0, 0.0, 8.0, 0.1).setSuffix("m"));
    public final SliderSetting range =
            add(new SliderSetting("Range", 6.0, 0.0, 8.0, 0.1).setSuffix("m"));

    private static final double[] XZ_OFFSETS = {0.3, -0.3};

    public AutoCity() {
        super("AutoCity", Category.Combat);
        setChinese("自动挖掘");
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if (AntiCrawl.INSTANCE.work) return;
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) return;
        PlayerEntity player = CombatUtil.getClosestEnemy(targetRange.getValue());
        if (player == null) return;
        doBreak(player);
    }

    private void doBreak(PlayerEntity player) {
        if (mc.player == null || mc.world == null) return;
        BlockPos pos = EntityUtil.getEntityPos(player, true);
        double[] yOffset = new double[]{-0.8, 0.5, 1.1};
        for (PlayerEntity entity : CombatUtil.getEnemies(targetRange.getValue())) {
            for (double y : yOffset) {
                for (double x : XZ_OFFSETS) {
                    for (double z : XZ_OFFSETS) {
                        BlockPos offsetPos = new BlockPosX(entity.getX() + x, entity.getY() + y, entity.getZ() + z);
                        if (canBreak(offsetPos) && offsetPos.equals(PacketMine.getBreakPos())) {
                            return;
                        }
                    }
                }
            }
        }
        if (burrow.getValue()) {
            for (double offset : XZ_OFFSETS) {
                BlockPos offsetPos = new BlockPosX(player.getX() + offset, player.getY() + 0.5, player.getZ() + offset);
                if (canBreak(offsetPos) && !isAlreadyMining(offsetPos)) {
                    PacketMine.INSTANCE.mine(offsetPos);
                    return;
                }
            }
            for (double offset : XZ_OFFSETS) {
                for (double offset2 : XZ_OFFSETS) {
                    BlockPos offsetPos = new BlockPosX(player.getX() + offset2, player.getY() + 0.5, player.getZ() + offset);
                    if (canBreak(offsetPos) && !isAlreadyMining(offsetPos)) {
                        PacketMine.INSTANCE.mine(offsetPos);
                        return;
                    }
                }
            }
        }
        if (surround.getValue()) {
            if (!lowVersion.getValue()) {
                for (Direction i : Direction.values()) {
                    if (i == Direction.UP || i == Direction.DOWN) continue;
                    if (Math.sqrt(mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range.getValue()) {
                        continue;
                    }
                    if ((mc.world.isAir(pos.offset(i)) || pos.offset(i).equals(PacketMine.getBreakPos())) && canPlaceCrystal(pos.offset(i), false)) {
                        return;
                    }
                }
                ArrayList<BlockPos> list = new ArrayList<>();
                for (Direction i : Direction.values()) {
                    if (i == Direction.UP || i == Direction.DOWN) continue;
                    if (Math.sqrt(mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range.getValue()) {
                        continue;
                    }
                    if (canBreak(pos.offset(i)) && canPlaceCrystal(pos.offset(i), true) && !isAlreadyMining(pos.offset(i))) {
                        list.add(pos.offset(i));
                    }
                }
                if (!list.isEmpty()) {
                    PacketMine.INSTANCE.mine(list.stream().min(Comparator.comparingDouble((E) -> E.getSquaredDistance(mc.player.getEyePos()))).get());
                    return;
                } else {
                    for (Direction i : Direction.values()) {
                        if (i == Direction.UP || i == Direction.DOWN) continue;
                        if (Math.sqrt(mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range.getValue()) {
                            continue;
                        }
                        if (canBreak(pos.offset(i)) && canPlaceCrystal(pos.offset(i), false) && !isAlreadyMining(pos.offset(i))) {
                            list.add(pos.offset(i));
                        }
                    }
                    if (!list.isEmpty()) {
                        PacketMine.INSTANCE.mine(list.stream().min(Comparator.comparingDouble((E) -> E.getSquaredDistance(mc.player.getEyePos()))).get());
                        return;
                    }
                }
            } else {
                for (Direction i : Direction.values()) {
                    if (i == Direction.UP || i == Direction.DOWN) continue;
                    if (mc.player.getEyePos().distanceTo(pos.offset(i).toCenterPos()) > range.getValue()) {
                        continue;
                    }
                    if ((mc.world.isAir(pos.offset(i)) && mc.world.isAir(pos.offset(i).up())) && canPlaceCrystal(pos.offset(i), false)) {
                        return;
                    }
                }

                ArrayList<BlockPos> list = new ArrayList<>();
                for (Direction i : Direction.values()) {
                    if (i == Direction.UP || i == Direction.DOWN) continue;
                    if (Math.sqrt(mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range.getValue()) {
                        continue;
                    }
                    if (canCrystal(pos.offset(i)) && !isAlreadyMining(pos.offset(i))) {
                        list.add(pos.offset(i));
                    }
                }

                int max = 0;
                BlockPos minePos = null;
                for (BlockPos cPos : list) {
                    int currentPriority;
                    if (smartPriority.getValue()) {
                        currentPriority = calculatePriority(cPos, player);
                    } else {
                        currentPriority = getBlockingScore(cPos);
                    }
                    if (currentPriority >= max) {
                        max = currentPriority;
                        minePos = cPos;
                    }
                }
                if (minePos != null) {
                    doMine(minePos);
                    return;
                }
            }
        }
        if (PacketMine.getBreakPos() == null && burrow.getValue()) {
            double[] yOffsetBurrow = new double[]{0.5, 1.1};
            for (double y : yOffsetBurrow) {
                for (double offset : XZ_OFFSETS) {
                    BlockPos offsetPos = new BlockPosX(player.getX() + offset, player.getY() + y, player.getZ() + offset);
                    if (isObsidian(offsetPos) && !isAlreadyMining(offsetPos)) {
                        PacketMine.INSTANCE.mine(offsetPos);
                        return;
                    }
                }
            }
            for (double y : yOffsetBurrow) {
                for (double offset : XZ_OFFSETS) {
                    for (double offset2 : XZ_OFFSETS) {
                        BlockPos offsetPos = new BlockPosX(player.getX() + offset2, player.getY() + y, player.getZ() + offset);
                        if (isObsidian(offsetPos) && !isAlreadyMining(offsetPos)) {
                            PacketMine.INSTANCE.mine(offsetPos);
                            return;
                        }
                    }
                }
            }
        }
    }

    private boolean isAlreadyMining(BlockPos pos) {
        return pos.equals(PacketMine.getBreakPos()) || pos.equals(PacketMine.secondPos);
    }

    private int calculatePriority(BlockPos pos, PlayerEntity target) {
        int priority = 0;
        priority += getBlockingScore(pos) * 2;
        double distanceToTarget = Math.sqrt(pos.getSquaredDistance(target.getBlockPos()));
        if (distanceToTarget <= 2.0) {
            priority += 4;
        } else if (distanceToTarget <= 3.0) {
            priority += 2;
        }
        BlockPos above = pos.up();
        if (isObsidian(above) || isObsidian(above.up()) ||
                getBlock(above) == Blocks.ENDER_CHEST ||
                getBlock(above.up()) == Blocks.ENDER_CHEST) {
            priority += 3;
        }
        BlockPos below = pos.down();
        if (canPlaceCrystal(below, true) && !canBreak(pos)) {
            priority += 5;
        }
        BlockPos enemyPos = target.getBlockPos();
        double enemyDistance = Math.sqrt(pos.getSquaredDistance(enemyPos));
        if (enemyDistance <= 1.5) {
            priority += 4;
        }
        return priority;
    }

    private int getBlockingScore(BlockPos pos) {
        int value = 0;
        if (!canBreak(pos)) {
            value++;
        }
        if (!canBreak(pos.up())) {
            value++;
        }
        return value;
    }

    private void doMine(BlockPos pos) {
        if (canBreak(pos) && !isAlreadyMining(pos)) {
            PacketMine.INSTANCE.mine(pos);
        } else if (canBreak(pos.up()) && !isAlreadyMining(pos.up())) {
            PacketMine.INSTANCE.mine(pos.up());
        }
    }

    private boolean canCrystal(BlockPos pos) {
        if (PacketMine.godBlocks.contains(getBlock(pos)) || getBlock(pos) instanceof BedBlock || getBlock(pos) instanceof CobwebBlock || !canPlaceCrystal(pos, true) || BlockUtil.getClickSideStrict(pos) == null) {
            return false;
        }
        if (PacketMine.godBlocks.contains(getBlock(pos.up())) || getBlock(pos.up()) instanceof BedBlock || getBlock(pos.up()) instanceof CobwebBlock || BlockUtil.getClickSideStrict(pos.up()) == null) {
            return false;
        }
        return true;
    }

    public boolean canPlaceCrystal(BlockPos pos, boolean block) {
        BlockPos obsPos = pos.down();
        BlockPos boost = obsPos.up();
        return (getBlock(obsPos) == Blocks.BEDROCK || getBlock(obsPos) == Blocks.OBSIDIAN || !block)
                && !BlockUtil.hasEntityBlockCrystal(boost, true, true)
                && !BlockUtil.hasEntityBlockCrystal(boost.up(), true, true)
                && (!lowVersion.getValue() || mc.world.isAir(boost.up()));
    }

    private boolean isObsidian(BlockPos pos) {
        return mc.player.getEyePos().distanceTo(pos.toCenterPos()) <= range.getValue() && (getBlock(pos) == Blocks.OBSIDIAN || getBlock(pos) == Blocks.ENDER_CHEST || getBlock(pos) == Blocks.NETHERITE_BLOCK || getBlock(pos) == Blocks.RESPAWN_ANCHOR) && BlockUtil.getClickSideStrict(pos) != null;
    }

    private boolean canBreak(BlockPos pos) {
        return isObsidian(pos) && (BlockUtil.getClickSideStrict(pos) != null || PacketMine.getBreakPos().equals(pos)) && (!pos.equals(PacketMine.secondPos) || !(mc.player.getMainHandStack().getItem() instanceof PickaxeItem));
    }
}