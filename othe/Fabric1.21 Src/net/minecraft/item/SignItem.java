/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.VerticallyAttachableBlockItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SignItem
extends VerticallyAttachableBlockItem {
    public SignItem(Item.Settings settings, Block standingBlock, Block wallBlock) {
        super(standingBlock, wallBlock, settings, Direction.DOWN);
    }

    public SignItem(Item.Settings settings, Block standingBlock, Block wallBlock, Direction verticalAttachmentDirection) {
        super(standingBlock, wallBlock, settings, verticalAttachmentDirection);
    }

    @Override
    protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
        Object object;
        boolean bl = super.postPlacement(pos, world, player, stack, state);
        if (!world.isClient && !bl && player != null && (object = world.getBlockEntity(pos)) instanceof SignBlockEntity) {
            SignBlockEntity signBlockEntity = (SignBlockEntity)object;
            object = world.getBlockState(pos).getBlock();
            if (object instanceof AbstractSignBlock) {
                AbstractSignBlock abstractSignBlock = (AbstractSignBlock)object;
                abstractSignBlock.openEditScreen(player, signBlockEntity, true);
            }
        }
        return bl;
    }
}

