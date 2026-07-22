/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.screen;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class GrindstoneScreenHandler
extends ScreenHandler {
    public static final int field_30793 = 35;
    public static final int INPUT_1_ID = 0;
    public static final int INPUT_2_ID = 1;
    public static final int OUTPUT_ID = 2;
    private static final int INVENTORY_START = 3;
    private static final int INVENTORY_END = 30;
    private static final int HOTBAR_START = 30;
    private static final int HOTBAR_END = 39;
    private final Inventory result = new CraftingResultInventory();
    final Inventory input = new SimpleInventory(2){

        @Override
        public void markDirty() {
            super.markDirty();
            GrindstoneScreenHandler.this.onContentChanged(this);
        }
    };
    private final ScreenHandlerContext context;

    public GrindstoneScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public GrindstoneScreenHandler(int syncId, PlayerInventory playerInventory, final ScreenHandlerContext context) {
        super(ScreenHandlerType.GRINDSTONE, syncId);
        int i;
        this.context = context;
        this.addSlot(new Slot(this, this.input, 0, 49, 19){

            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isDamageable() || EnchantmentHelper.hasEnchantments(stack);
            }
        });
        this.addSlot(new Slot(this, this.input, 1, 49, 40){

            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isDamageable() || EnchantmentHelper.hasEnchantments(stack);
            }
        });
        this.addSlot(new Slot(this.result, 2, 129, 34){

            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                context.run((world, pos) -> {
                    if (world instanceof ServerWorld) {
                        ExperienceOrbEntity.spawn((ServerWorld)world, Vec3d.ofCenter(pos), this.getExperience((World)world));
                    }
                    world.syncWorldEvent(WorldEvents.GRINDSTONE_USED, (BlockPos)pos, 0);
                });
                GrindstoneScreenHandler.this.input.setStack(0, ItemStack.EMPTY);
                GrindstoneScreenHandler.this.input.setStack(1, ItemStack.EMPTY);
            }

            private int getExperience(World world) {
                int i = 0;
                i += this.getExperience(GrindstoneScreenHandler.this.input.getStack(0));
                if ((i += this.getExperience(GrindstoneScreenHandler.this.input.getStack(1))) > 0) {
                    int j = (int)Math.ceil((double)i / 2.0);
                    return j + world.random.nextInt(j);
                }
                return 0;
            }

            private int getExperience(ItemStack stack) {
                int i = 0;
                ItemEnchantmentsComponent itemEnchantmentsComponent = EnchantmentHelper.getEnchantments(stack);
                for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemEnchantmentsComponent.getEnchantmentEntries()) {
                    RegistryEntry registryEntry = (RegistryEntry)entry.getKey();
                    int j = entry.getIntValue();
                    if (registryEntry.isIn(EnchantmentTags.CURSE)) continue;
                    i += ((Enchantment)registryEntry.value()).getMinPower(j);
                }
                return i;
            }
        });
        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
        if (inventory == this.input) {
            this.updateResult();
        }
    }

    private void updateResult() {
        this.result.setStack(0, this.getOutputStack(this.input.getStack(0), this.input.getStack(1)));
        this.sendContentUpdates();
    }

    private ItemStack getOutputStack(ItemStack firstInput, ItemStack secondInput) {
        boolean bl2;
        boolean bl;
        boolean bl3 = bl = !firstInput.isEmpty() || !secondInput.isEmpty();
        if (!bl) {
            return ItemStack.EMPTY;
        }
        if (firstInput.getCount() > 1 || secondInput.getCount() > 1) {
            return ItemStack.EMPTY;
        }
        boolean bl4 = bl2 = !firstInput.isEmpty() && !secondInput.isEmpty();
        if (!bl2) {
            ItemStack itemStack;
            ItemStack itemStack2 = itemStack = !firstInput.isEmpty() ? firstInput : secondInput;
            if (!EnchantmentHelper.hasEnchantments(itemStack)) {
                return ItemStack.EMPTY;
            }
            return this.grind(itemStack.copy());
        }
        return this.combineItems(firstInput, secondInput);
    }

    private ItemStack combineItems(ItemStack firstInput, ItemStack secondInput) {
        ItemStack itemStack;
        if (!firstInput.isOf(secondInput.getItem())) {
            return ItemStack.EMPTY;
        }
        int i = Math.max(firstInput.getMaxDamage(), secondInput.getMaxDamage());
        int j = firstInput.getMaxDamage() - firstInput.getDamage();
        int k = secondInput.getMaxDamage() - secondInput.getDamage();
        int l = j + k + i * 5 / 100;
        int m = 1;
        if (!firstInput.isDamageable()) {
            if (firstInput.getMaxCount() < 2 || !ItemStack.areEqual(firstInput, secondInput)) {
                return ItemStack.EMPTY;
            }
            m = 2;
        }
        if ((itemStack = firstInput.copyWithCount(m)).isDamageable()) {
            itemStack.set(DataComponentTypes.MAX_DAMAGE, i);
            itemStack.setDamage(Math.max(i - l, 0));
        }
        this.transferEnchantments(itemStack, secondInput);
        return this.grind(itemStack);
    }

    private void transferEnchantments(ItemStack target, ItemStack source) {
        EnchantmentHelper.apply(target, components -> {
            ItemEnchantmentsComponent itemEnchantmentsComponent = EnchantmentHelper.getEnchantments(source);
            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemEnchantmentsComponent.getEnchantmentEntries()) {
                RegistryEntry registryEntry = (RegistryEntry)entry.getKey();
                if (registryEntry.isIn(EnchantmentTags.CURSE) && components.getLevel(registryEntry) != 0) continue;
                components.add(registryEntry, entry.getIntValue());
            }
        });
    }

    private ItemStack grind(ItemStack item) {
        ItemEnchantmentsComponent itemEnchantmentsComponent = EnchantmentHelper.apply(item, components -> components.remove(enchantment -> !enchantment.isIn(EnchantmentTags.CURSE)));
        if (item.isOf(Items.ENCHANTED_BOOK) && itemEnchantmentsComponent.isEmpty()) {
            item = item.withItem(Items.BOOK);
        }
        int i = 0;
        for (int j = 0; j < itemEnchantmentsComponent.getSize(); ++j) {
            i = AnvilScreenHandler.getNextCost(i);
        }
        item.set(DataComponentTypes.REPAIR_COST, i);
        return item;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.context.run((world, pos) -> this.dropInventory(player, this.input));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return GrindstoneScreenHandler.canUse(this.context, player, Blocks.GRINDSTONE);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot2 = (Slot)this.slots.get(slot);
        if (slot2 != null && slot2.hasStack()) {
            ItemStack itemStack2 = slot2.getStack();
            itemStack = itemStack2.copy();
            ItemStack itemStack3 = this.input.getStack(0);
            ItemStack itemStack4 = this.input.getStack(1);
            if (slot == 2) {
                if (!this.insertItem(itemStack2, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot2.onQuickTransfer(itemStack2, itemStack);
            } else if (slot == 0 || slot == 1 ? !this.insertItem(itemStack2, 3, 39, false) : (itemStack3.isEmpty() || itemStack4.isEmpty() ? !this.insertItem(itemStack2, 0, 2, false) : (slot >= 3 && slot < 30 ? !this.insertItem(itemStack2, 30, 39, false) : slot >= 30 && slot < 39 && !this.insertItem(itemStack2, 3, 30, false)))) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot2.setStack(ItemStack.EMPTY);
            } else {
                slot2.markDirty();
            }
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot2.onTakeItem(player, itemStack2);
        }
        return itemStack;
    }
}

