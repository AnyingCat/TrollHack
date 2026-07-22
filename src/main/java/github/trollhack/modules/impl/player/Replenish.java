package github.trollhack.modules.impl.player;

import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.settings.impl.FloatSetting;
import github.trollhack.settings.impl.IntegerSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class Replenish extends Module {
    public static final Replenish INSTANCE = new Replenish();

    private final FloatSetting delay = floatSetting("Delay", 2.0f, 0.0f, 5.0f, 0.1f);
    private final IntegerSetting min = integerSetting("Min", 50, 1, 64, 1);
    private final FloatSetting forceDelay = floatSetting("ForceDelay", 0.2f, 0.0f, 4.0f, 0.1f);
    private final IntegerSetting forceMin = integerSetting("ForceMin", 16, 1, 64, 1);

    private long lastReplenishTime = 0;

    public Replenish() {
        super("Replenish", Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        if (nullCheck()) return;
        if (mc.currentScreen != null) return;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !stack.isStackable()) continue;
            if (stack.getCount() >= stack.getMaxCount() || stack.getCount() > min.getValue()) continue;

            for (int j = 9; j < 36; j++) {
                ItemStack invStack = mc.player.getInventory().getStack(j);
                if (invStack.isEmpty() || stack.getItem() != invStack.getItem()) continue;
                if (!ItemStack.areItemsAndComponentsEqual(stack, invStack)) continue;

                long now = System.currentTimeMillis();
                float elapsedSec = (now - lastReplenishTime) / 1000.0f;
                if (stack.getCount() > forceMin.getValue()) {
                    if (elapsedSec < delay.getValue()) return;
                } else {
                    if (elapsedSec < forceDelay.getValue()) return;
                }

                int syncId = mc.player.currentScreenHandler.syncId;
                mc.interactionManager.clickSlot(syncId, j, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(syncId, i + 36, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(syncId, j, 0, SlotActionType.PICKUP, mc.player);
                lastReplenishTime = System.currentTimeMillis();
                return;
            }
        }
    }
}
