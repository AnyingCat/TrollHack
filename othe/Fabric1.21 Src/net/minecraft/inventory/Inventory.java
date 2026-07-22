/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.inventory;

import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A container of {@link ItemStack}s. In general, when a player stores an item stack
 * and can retrieve the same item stack back, that stack is stored in an
 * inventory. The inventory can be persistent, like chests or donkeys, or it can
 * be created without backing storage, like the slots in crafting tables.
 * It is the responsibility of the user to sync or save the contents of the
 * inventory.
 * 
 * <p>Entities and block entities that can hold item stacks generally
 * implement this interface themselves, allowing hopper interactions. Call {@link
 * net.minecraft.entity.player.PlayerEntity#getInventory} to get the player's
 * inventory (including armors and offhand).
 * 
 * <p>An inventory has a fixed size, and each element in the inventory is identified
 * by the slot number, which is between zero and {@code size() - 1} like arrays.
 * When a slot of the inventory is empty, it should be filled with {@link
 * ItemStack#EMPTY}.
 * 
 * <p>An implementation of this interface should have a field of {@link
 * net.minecraft.util.collection.DefaultedList#ofSize(int, Object)} with the second
 * argument as {@link ItemStack#EMPTY}, and implement methods by delegating to the
 * list. The list itself should not be modified directly, and the list's size
 * should remain constant throughout the lifetime of the inventory.
 * Implementations must call {@link #markDirty} when the inventory is modified.
 * 
 * @apiNote If an inventory is needed for temporary storage, use {@link
 * SimpleInventory}. For persistent storage in entities or block entities,
 * use {@link net.minecraft.entity.vehicle.VehicleInventory} or
 * {@link net.minecraft.block.entity.LockableContainerBlockEntity}.
 * 
 * @see net.minecraft.entity.vehicle.VehicleInventory
 * @see net.minecraft.block.entity.LockableContainerBlockEntity
 */
public interface Inventory
extends Clearable {
    public static final float DEFAULT_MAX_INTERACTION_RANGE = 4.0f;

    /**
     * {@return the size of the inventory}
     * 
     * <p>The inventory should support the slot ID from {@code 0} to {@code size() - 1}.
     * This should remain constant throughout the inventory's lifetime.
     */
    public int size();

    /**
     * {@return whether the inventory consists entirely of {@linkplain ItemStack#isEmpty
     * empty item stacks}}
     */
    public boolean isEmpty();

    /**
     * {@return the stack currently stored at {@code slot}}
     * 
     * <p>If the slot is empty, or is outside the bounds of this inventory,
     * this returns {@link ItemStack#EMPTY}.
     */
    public ItemStack getStack(int var1);

    /**
     * Removes a specific number of items from {@code slot}.
     * 
     * @return the removed items as a stack
     */
    public ItemStack removeStack(int var1, int var2);

    /**
     * Removes the stack currently stored at {@code slot}.
     * 
     * @return the stack previously stored at the indicated slot
     */
    public ItemStack removeStack(int var1);

    /**
     * Sets the stack stored at {@code slot} to {@code stack}.
     */
    public void setStack(int var1, ItemStack var2);

    /**
     * {@return the maximum {@linkplain ItemStack#getCount number of items} a stack
     * can contain when placed inside this inventory}
     * 
     * <p>No slots may have more than this number of items. It is effectively the
     * stacking limit for this inventory's slots.
     */
    default public int getMaxCountPerStack() {
        return 99;
    }

    default public int getMaxCount(ItemStack stack) {
        return Math.min(this.getMaxCountPerStack(), stack.getMaxCount());
    }

    /**
     * Marks the inventory as modified. Implementations should call this method
     * every time the inventory is changed in any way.
     * 
     * @apiNote Implementations should mark the inventory for synchronization or
     * saving in this method. Since this is called frequently, it is not recommended to
     * synchronize or save the inventory directly in this method. If this inventory is
     * implemented in a block entity, then it should <strong>always</strong> call
     * {@code super.markDirty();} to ensure the block entity gets saved.
     * 
     * @see net.minecraft.block.entity.BlockEntity#markDirty
     */
    public void markDirty();

    /**
     * {@return whether {@code player} can use this inventory}
     * 
     * <p>This is called by {@link net.minecraft.screen.ScreenHandler#canUse}.
     * 
     * @apiNote Implementations should check the distance between the inventory
     * holder and {@code player}. For convenience, this interface offers two methods
     * used by block entities to implement this check.
     * 
     * @see #canPlayerUse(BlockEntity, PlayerEntity)
     * @see #canPlayerUse(BlockEntity, PlayerEntity, int)
     */
    public boolean canPlayerUse(PlayerEntity var1);

    /**
     * Called when the inventory is opened. Specifically, this is called inside the
     * {@link net.minecraft.screen.ScreenHandler} constructor. This does nothing
     * by default.
     * 
     * <p>The method is called in both the client and the server. However, because
     * clientside screen handler is created with a {@link net.minecraft.inventory.SimpleInventory},
     * other implementations can (and the vanilla code does) assume that the method is called
     * in the server.
     */
    default public void onOpen(PlayerEntity player) {
    }

    /**
     * Called when the inventory is closed. Specifically, this is called inside
     * {@link net.minecraft.screen.ScreenHandler#onClosed}. This does nothing
     * by default.
     * 
     * <p>The method is called in both the client and the server. However, because
     * clientside screen handler is created with a {@link net.minecraft.inventory.SimpleInventory},
     * other implementations can (and the vanilla code does) assume that the method is called
     * in the server.
     */
    default public void onClose(PlayerEntity player) {
    }

    /**
     * {@return whether {@code stack} is valid for the {@code slot}}
     * 
     * <p>Implementations can, for example, use this to check whether the item
     * is in a specific tag. This returns {@code true} by default.
     */
    default public boolean isValid(int slot, ItemStack stack) {
        return true;
    }

    /**
     * {@return whether a hopper can transfer {@code stack} from {@code slot} to
     * the hopper}
     * 
     * <p>This returns {@code true} by default.
     */
    default public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
        return true;
    }

    /**
     * {@return the number of times {@code item} occurs in this inventory
     * across all stored stacks}
     */
    default public int count(Item item) {
        int i = 0;
        for (int j = 0; j < this.size(); ++j) {
            ItemStack itemStack = this.getStack(j);
            if (!itemStack.getItem().equals(item)) continue;
            i += itemStack.getCount();
        }
        return i;
    }

    /**
     * {@return whether this inventory contains any of {@code items}}
     * 
     * @see #containsAny(Predicate)
     */
    default public boolean containsAny(Set<Item> items) {
        return this.containsAny((ItemStack stack) -> !stack.isEmpty() && items.contains(stack.getItem()));
    }

    /**
     * {@return whether this inventory contains any of the stacks matching {@code
     * predicate}}
     * 
     * @see #containsAny(Set)
     */
    default public boolean containsAny(Predicate<ItemStack> predicate) {
        for (int i = 0; i < this.size(); ++i) {
            ItemStack itemStack = this.getStack(i);
            if (!predicate.test(itemStack)) continue;
            return true;
        }
        return false;
    }

    /**
     * {@return whether {@code player} can use this {@code blockEntity}}
     * 
     * @apiNote This is used by block entities to implement {@link
     * #canPlayerUse(PlayerEntity)}.
     * 
     * @implNote This method checks whether the given block entity exists and whether
     * the player is within 8 blocks of the block entity.
     * 
     * @see #canPlayerUse(BlockEntity, PlayerEntity, int)
     */
    public static boolean canPlayerUse(BlockEntity blockEntity, PlayerEntity player) {
        return Inventory.canPlayerUse(blockEntity, player, 4.0f);
    }

    /**
     * {@return whether {@code player} can use this {@code blockEntity}}
     * 
     * @apiNote This is used by block entities to implement {@link
     * #canPlayerUse(PlayerEntity)}.
     * 
     * @implNote This method checks whether the given block entity exists and whether
     * the player is within {@code range} blocks of the block entity.
     * 
     * @see #canPlayerUse(BlockEntity, PlayerEntity)
     */
    public static boolean canPlayerUse(BlockEntity blockEntity, PlayerEntity player, float range) {
        World world = blockEntity.getWorld();
        BlockPos blockPos = blockEntity.getPos();
        if (world == null) {
            return false;
        }
        if (world.getBlockEntity(blockPos) != blockEntity) {
            return false;
        }
        return player.canInteractWithBlockAt(blockPos, range);
    }
}

