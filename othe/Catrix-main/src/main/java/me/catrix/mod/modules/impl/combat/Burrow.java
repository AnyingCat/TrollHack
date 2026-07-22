package me.catrix.mod.modules.impl.combat;

import me.catrix.Catrix;
import me.catrix.core.impl.CommandManager;
import me.catrix.api.utils.combat.CombatUtil;
import me.catrix.api.utils.entity.EntityUtil;
import me.catrix.api.utils.entity.InventoryUtil;
import me.catrix.api.utils.math.Timer;
import me.catrix.api.utils.world.BlockPosX;
import me.catrix.api.utils.world.BlockUtil;
import me.catrix.mod.modules.Module;
import me.catrix.mod.modules.impl.client.AntiCheat;
import me.catrix.mod.modules.impl.exploit.Blink;
import me.catrix.mod.modules.settings.impl.BooleanSetting;
import me.catrix.mod.modules.settings.impl.EnumSetting;
import me.catrix.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

import static me.catrix.api.utils.world.BlockUtil.canReplace;

public class Burrow extends Module {
    public static Burrow INSTANCE;
    private final Timer timer = new Timer();
    private final Timer webTimer = new Timer();
    private final BooleanSetting disable = add(new BooleanSetting("Disable", true));
    private final SliderSetting delay = add(new SliderSetting("Delay", 500, 0, 1000, () -> !disable.getValue()));
    private final SliderSetting webTime = add(new SliderSetting("WebTime", 0, 0, 500));
    private final BooleanSetting enderChest = add(new BooleanSetting("EnderChest", true));
    private final BooleanSetting antiLag = add(new BooleanSetting("AntiLag", false));
    private final BooleanSetting detectMine = add(new BooleanSetting("DetectMining", false));
    private final BooleanSetting headFill = add(new BooleanSetting("HeadFill", false));
    private final BooleanSetting usingPause = add(new BooleanSetting("UsingPause", false));
    private final BooleanSetting down = add(new BooleanSetting("Down", true));
    private final BooleanSetting noSelfPos = add(new BooleanSetting("NoSelfPos", false));
    private final BooleanSetting packetPlace = add(new BooleanSetting("PacketPlace", true));
    private final BooleanSetting sound = add(new BooleanSetting("Sound", true));
    private final SliderSetting blocksPer = add(new SliderSetting("BlocksPer", 4, 1, 4, 1));
    private final EnumSetting<RotateMode> rotate = add(new EnumSetting<>("RotateMode", RotateMode.Bypass));
    private final BooleanSetting breakCrystal = add(new BooleanSetting("Break", true));
    private final BooleanSetting wait = add(new BooleanSetting("Wait", true, () -> !disable.getValue()));
    private final BooleanSetting fakeMove = add(new BooleanSetting("FakeMove", true).setParent());
    private final BooleanSetting center = add(new BooleanSetting("AllowCenter", false, fakeMove::isOpen));
    private final BooleanSetting inventory = add(new BooleanSetting("InventorySwap", true));
    private final EnumSetting<LagBackMode> lagMode = add(new EnumSetting<>("LagMode", LagBackMode.TrollHack));
    private final EnumSetting<LagBackMode> aboveLagMode = add(new EnumSetting<>("MoveLagMode", LagBackMode.Smart));
    private final SliderSetting smartX = add(new SliderSetting("SmartXZ", 3, 0, 10, 0.1, () -> lagMode.getValue() == LagBackMode.Smart || aboveLagMode.getValue() == LagBackMode.Smart));
    private final SliderSetting smartUp = add(new SliderSetting("SmartUp", 3, 0, 10, 0.1, () -> lagMode.getValue() == LagBackMode.Smart || aboveLagMode.getValue() == LagBackMode.Smart));
    private final SliderSetting smartDown = add(new SliderSetting("SmartDown", 3, 0, 10, 0.1, () -> lagMode.getValue() == LagBackMode.Smart || aboveLagMode.getValue() == LagBackMode.Smart));
    private final SliderSetting smartDistance = add(new SliderSetting("SmartDistance", 2, 0, 10, 0.1, () -> lagMode.getValue() == LagBackMode.Smart || aboveLagMode.getValue() == LagBackMode.Smart));
    private int progress = 0;
    private final List<BlockPos> placePos = new ArrayList<>();

    public Burrow() {
        super("Burrow", Category.Combat);
        setChinese("卡黑曜石");
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if (Catrix.PLAYER.isInWeb(mc.player)) {
            webTimer.reset();
            return;
        }
        if (usingPause.getValue() && mc.player.isUsingItem()) {
            return;
        }
        if (!webTimer.passed(webTime.getValue())) {
            return;
        }
        if (!disable.getValue() && !timer.passed(delay.getValue())) {
            return;
        }

        if (!mc.player.isOnGround()) {
            return;
        }
        if (antiLag.getValue()) {
            BlockPos playerPos = EntityUtil.getPlayerPos(true);
            BlockPos belowPos = playerPos.down();
            if (!mc.world.getBlockState(belowPos).blocksMovement()) return;
            if (mc.player.isTouchingWater() || mc.player.isInLava()) return;
            if (playerPos.getY() < mc.world.getBottomY() || playerPos.getY() >= mc.world.getTopY()) return;
        }

        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) return;
        int oldSlot = mc.player.getInventory().selectedSlot;
        int block;
        if ((block = getBlock()) == -1) {
            CommandManager.sendChatMessageWidthId("§c§oObsidian" + (enderChest.getValue() ? "/EnderChest" : "") + "?", hashCode());
            disable();
            return;
        }
        progress = 0;
        placePos.clear();
        double offset = AntiCheat.getOffset();
        BlockPos playerPos = EntityUtil.getPlayerPos(true);

        BlockPos pos1 = new BlockPosX(mc.player.getX() + offset, mc.player.getY() + 0.5, mc.player.getZ() + offset);
        BlockPos pos2 = new BlockPosX(mc.player.getX() - offset, mc.player.getY() + 0.5, mc.player.getZ() + offset);
        BlockPos pos3 = new BlockPosX(mc.player.getX() + offset, mc.player.getY() + 0.5, mc.player.getZ() - offset);
        BlockPos pos4 = new BlockPosX(mc.player.getX() - offset, mc.player.getY() + 0.5, mc.player.getZ() - offset);
        BlockPos pos5 = new BlockPosX(mc.player.getX() + offset, mc.player.getY() + 1.5, mc.player.getZ() + offset);
        BlockPos pos6 = new BlockPosX(mc.player.getX() - offset, mc.player.getY() + 1.5, mc.player.getZ() + offset);
        BlockPos pos7 = new BlockPosX(mc.player.getX() + offset, mc.player.getY() + 1.5, mc.player.getZ() - offset);
        BlockPos pos8 = new BlockPosX(mc.player.getX() - offset, mc.player.getY() + 1.5, mc.player.getZ() - offset);
        BlockPos pos9 = new BlockPosX(mc.player.getX() + offset, mc.player.getY() - 1, mc.player.getZ() + offset);
        BlockPos pos10 = new BlockPosX(mc.player.getX() - offset, mc.player.getY() - 1, mc.player.getZ() + offset);
        BlockPos pos11 = new BlockPosX(mc.player.getX() + offset, mc.player.getY() - 1, mc.player.getZ() - offset);
        BlockPos pos12 = new BlockPosX(mc.player.getX() - offset, mc.player.getY() - 1, mc.player.getZ() - offset);

        boolean canPlaceMain = canPlace(pos1) || canPlace(pos2) || canPlace(pos3) || canPlace(pos4);
        boolean canPlaceHead = headFill.getValue() && (canPlace(pos5) || canPlace(pos6) || canPlace(pos7) || canPlace(pos8));
        boolean canPlaceDown = down.getValue() && (canPlace(pos9) || canPlace(pos10) || canPlace(pos11) || canPlace(pos12));

        boolean headFillNeeded = false;
        boolean aboveSituation = false;

        if (!canPlaceMain) {
            if (canPlaceHead) {
                headFillNeeded = true;
            } else if (!canPlaceDown) {
                if (!wait.getValue() && disable.getValue()) {
                    disable();
                }
                return;
            }
        }

        BlockPos headPos = playerPos.up(2);
        aboveSituation = headFillNeeded || mc.player.isCrawling() || isTrappedSituation(headPos);

        if (aboveSituation) {
            if (!fakeMove.getValue()) {
                if (!wait.getValue() && disable.getValue()) disable();
                return;
            }
            if (!handleAboveSituation(playerPos)) {
                if (!wait.getValue() && disable.getValue()) disable();
                return;
            }
        } else {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.4199999868869781, mc.player.getZ(), false));
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.7531999805212017, mc.player.getZ(), false));
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.9999957640154541, mc.player.getZ(), false));
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.1661092609382138, mc.player.getZ(), false));
        }

        timer.reset();

        boolean rotateCrystal = this.rotate.getValue() == RotateMode.Normal;
        CombatUtil.attackCrystal(pos1, rotateCrystal, false);
        CombatUtil.attackCrystal(pos2, rotateCrystal, false);
        CombatUtil.attackCrystal(pos3, rotateCrystal, false);
        CombatUtil.attackCrystal(pos4, rotateCrystal, false);

        doSwap(block);
        if (this.rotate.getValue() == RotateMode.Bypass) {
            Catrix.ROTATION.snapAt(Catrix.ROTATION.rotationYaw, 90);
        }

        boolean finalRotate = this.rotate.getValue() == RotateMode.Normal;
        placeBlock(playerPos, finalRotate);
        placeBlock(pos1, finalRotate);
        placeBlock(pos2, finalRotate);
        placeBlock(pos3, finalRotate);
        placeBlock(pos4, finalRotate);

        if (down.getValue()) {
            placeBlock(pos9, finalRotate);
            placeBlock(pos10, finalRotate);
            placeBlock(pos11, finalRotate);
            placeBlock(pos12, finalRotate);
        }

        if (headFillNeeded) {
            placeBlock(pos5, finalRotate);
            placeBlock(pos6, finalRotate);
            placeBlock(pos7, finalRotate);
            placeBlock(pos8, finalRotate);
        }

        if (inventory.getValue()) {
            doSwap(block);
            EntityUtil.syncInventory();
        } else {
            doSwap(oldSlot);
        }

        executeLagBackLogic(aboveSituation);

        if (disable.getValue()) disable();
    }

    private boolean handleAboveSituation(BlockPos playerPos) {
        if (isValidMovePosition(playerPos) && !canReplace(playerPos) &&
                (!headFill.getValue() || !canReplace(playerPos.up()))) {
            gotoPos(playerPos);
            return true;
        }

        for (Direction facing : Direction.Type.HORIZONTAL) {
            BlockPos adjacentPos = playerPos.offset(facing);
            if (isValidMovePosition(adjacentPos) && !canReplace(adjacentPos) &&
                    (!headFill.getValue() || !canReplace(adjacentPos.up()))) {
                gotoPos(adjacentPos);
                return true;
            }
        }

        for (Direction facing : Direction.Type.HORIZONTAL) {
            BlockPos adjacentPos = playerPos.offset(facing);
            if (isValidMovePosition(adjacentPos)) {
                gotoPos(adjacentPos);
                return true;
            }
        }

        if (center.getValue()) {
            for (Direction facing : Direction.Type.HORIZONTAL) {
                BlockPos adjacentPos = playerPos.offset(facing);
                if (canMove(adjacentPos)) {
                    gotoPos(adjacentPos);
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isValidMovePosition(BlockPos pos) {
        return checkSelf(pos) && mc.world.getWorldBorder().contains(pos);
    }

    private boolean isTrappedSituation(BlockPos headPos) {
        if (trapped(headPos)) return true;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                if (trapped(headPos.add(x, 0, z))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void executeLagBackLogic(boolean above) {
        LagBackMode currentMode = above ? aboveLagMode.getValue() : lagMode.getValue();

        switch (currentMode) {
            case Smart -> {
                ArrayList<BlockPos> list = new ArrayList<>();
                for (double x = mc.player.getPos().getX() - smartX.getValue(); x < mc.player.getPos().getX() + smartX.getValue(); ++x) {
                    for (double z = mc.player.getPos().getZ() - smartX.getValue(); z < mc.player.getPos().getZ() + smartX.getValue(); ++z) {
                        for (double y = mc.player.getPos().getY() - smartDown.getValue(); y < mc.player.getPos().getY() + smartUp.getValue(); ++y) {
                            list.add(new BlockPosX(x, y, z));
                        }
                    }
                }

                double distance = 0;
                BlockPos bestPos = null;
                for (BlockPos pos : list) {
                    if (!canMove(pos)) continue;
                    if (MathHelper.sqrt((float) mc.player.squaredDistanceTo(pos.toCenterPos().add(0, -0.5, 0))) < smartDistance.getValue()) continue;
                    if (bestPos == null || mc.player.squaredDistanceTo(pos.toCenterPos()) < distance) {
                        bestPos = pos;
                        distance = mc.player.squaredDistanceTo(pos.toCenterPos());
                    }
                }
                if (bestPos != null) {
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(bestPos.getX() + 0.5, bestPos.getY(), bestPos.getZ() + 0.5, false));
                }
            }
            case Invalid -> {
                for (int i = 0; i < 20; i++)
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1337, mc.player.getZ(), false));
            }
            case Fly -> {
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.16610926093821, mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.170005801788139, mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.2426308013947485, mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 2.3400880035762786, mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 2.6400880035762786, mc.player.getZ(), false));
            }
            case Glide -> {
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.0001, mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.0405, mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.0802, mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.1027, mc.player.getZ(), false));
            }
            case TrollHack -> mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 2.3400880035762786, mc.player.getZ(), false));
            case Normal -> mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.9, mc.player.getZ(), false));
            case ToVoid -> mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), -70, mc.player.getZ(), false));
            case ToVoid2 -> mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), -7, mc.player.getZ(), false));
            case Rotation -> {
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(-180, -90, false));
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(180, 90, false));
            }
        }
    }

    private void placeBlock(BlockPos pos, boolean rotate) {
        if (canPlace(pos) && !placePos.contains(pos) && progress < blocksPer.getValueInt()) {
            placePos.add(pos);
            if (BlockUtil.airPlace()) {
                progress++;
                BlockUtil.placedPos.add(pos);
                if (sound.getValue()) mc.world.playSound(mc.player, pos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
                BlockUtil.clickBlock(pos, Direction.DOWN, rotate, packetPlace.getValue());
                return;
            }
            Direction side;
            if ((side = BlockUtil.getPlaceSide(pos)) == null) return;
            progress++;
            BlockUtil.placedPos.add(pos);
            if (sound.getValue()) mc.world.playSound(mc.player, pos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
            BlockUtil.clickBlock(pos.offset(side), side.getOpposite(), rotate, packetPlace.getValue());
        }
    }

    private void doSwap(int slot) {
        if (inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private void gotoPos(BlockPos offPos) {
        if (rotate.getValue() == RotateMode.None) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(offPos.getX() + 0.5, mc.player.getY() + 0.1, offPos.getZ() + 0.5, false));
        } else {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(offPos.getX() + 0.5, mc.player.getY() + 0.1, offPos.getZ() + 0.5, Catrix.ROTATION.rotationYaw, 90, false));
        }
    }

    private boolean canMove(BlockPos pos) {
        return mc.world.isAir(pos) && mc.world.isAir(pos.up());
    }

    private boolean canPlace(BlockPos pos) {
        if (noSelfPos.getValue() && pos.equals(EntityUtil.getPlayerPos(true))) {
            return false;
        }
        if (!BlockUtil.airPlace() && BlockUtil.getPlaceSide(pos) == null) {
            return false;
        }
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        if (detectMine.getValue() && Catrix.BREAK.isMining(pos)) {
            return false;
        }
        return !hasEntity(pos);
    }

    private boolean hasEntity(BlockPos pos) {
        for (Entity entity : BlockUtil.getEntities(new Box(pos))) {
            if (entity == mc.player) continue;
            if (!entity.isAlive() || entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity || entity instanceof ExperienceBottleEntity || entity instanceof ArrowEntity || entity instanceof EndCrystalEntity && breakCrystal.getValue() || entity instanceof ArmorStandEntity && AntiCheat.INSTANCE.obsMode.getValue())
                continue;
            return true;
        }
        return false;
    }

    private boolean checkSelf(BlockPos pos) {
        return mc.player.getBoundingBox().intersects(new Box(pos));
    }

    private boolean trapped(BlockPos pos) {
        return (mc.world.canCollide(mc.player, new Box(pos)) || BlockUtil.getBlock(pos) == Blocks.COBWEB) && checkSelf(pos.down(2));
    }

    private int getBlock() {
        if (inventory.getValue()) {
            if (InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN) != -1 || !enderChest.getValue()) {
                return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
            }
            return InventoryUtil.findBlockInventorySlot(Blocks.ENDER_CHEST);
        } else {
            if (InventoryUtil.findBlock(Blocks.OBSIDIAN) != -1 || !enderChest.getValue()) {
                return InventoryUtil.findBlock(Blocks.OBSIDIAN);
            }
            return InventoryUtil.findBlock(Blocks.ENDER_CHEST);
        }
    }

    private enum RotateMode {Bypass, Normal, None}

    private enum LagBackMode {
        Smart, Invalid, TrollHack, ToVoid, ToVoid2, Normal, Rotation, Fly, Glide
    }
}