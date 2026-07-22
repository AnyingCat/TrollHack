package me.catrix.mod.modules.impl.misc;

import me.catrix.Catrix;
import me.catrix.api.utils.entity.EntityUtil;
import me.catrix.api.utils.entity.InventoryUtil;
import me.catrix.api.utils.math.Timer;
import me.catrix.api.utils.world.BlockUtil;
import me.catrix.mod.modules.Module;
import me.catrix.mod.modules.settings.impl.BooleanSetting;
import me.catrix.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.util.math.BlockPos;

public class LavaFiller extends Module {
    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap", true));
    private final SliderSetting range =
            add(new SliderSetting("Range", 5, 0, 8,.1));
    public final SliderSetting placeDelay =
            add(new SliderSetting("PlaceDelay", 50, 0, 500).setSuffix("ms"));
    private final SliderSetting blocksPer =
            add(new SliderSetting("BlocksPer", 1, 1, 8));
    private final BooleanSetting detectMining =
            add(new BooleanSetting("DetectMining", false));
    private final BooleanSetting usingPause =
            add(new BooleanSetting("UsingPause", true));
    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", true));
    private final BooleanSetting packetPlace =
            add(new BooleanSetting("PacketPlace", true));
    private final Timer timer = new Timer();

    int progress = 0;
    public LavaFiller() {
        super("LavaFiller", Category.Misc);
        setChinese("自动填岩浆");
    }

    @Override
    public void onUpdate() {
        if (!timer.passedMs((long) placeDelay.getValue())) return;
        progress = 0;

        if (getBlock() == -1) {
            return;
        }
        if (usingPause.getValue() && mc.player.isUsingItem()) {
            return;
        }
        for (BlockPos pos : BlockUtil.getSphere(range.getValueFloat())) {
            if (mc.world.getBlockState(pos).getBlock() == Blocks.LAVA) {
                if (mc.world.getBlockState(pos).getFluidState().getFluid() instanceof LavaFluid.Still) {
                    tryPlaceBlock(pos);
                }
            }
        }
    }


    private void tryPlaceBlock(BlockPos pos) {
        if (pos == null) return;
        if (detectMining.getValue() && Catrix.BREAK.isMining(pos)) return;
        if (!(progress < blocksPer.getValue())) return;
        int block = getBlock();
        if (block == -1) return;

        if (!BlockUtil.canPlace(pos, range.getValue(), false)) return;
        int old = mc.player.getInventory().selectedSlot;
        doSwap(block);
        BlockUtil.placeBlock(pos, rotate.getValue(), packetPlace.getValue());
        if (inventory.getValue()) {
            doSwap(block);
            EntityUtil.syncInventory();
        } else {
            doSwap(old);
        }
        progress++;
        timer.reset();
    }

    private void doSwap(int slot) {
        if (inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private int getBlock() {
        if (inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
        } else {
            return InventoryUtil.findBlock(Blocks.OBSIDIAN);
        }
    }
}