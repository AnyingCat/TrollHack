/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.VerticallyAttachableBlockItem;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;

public class PlayerHeadItem
extends VerticallyAttachableBlockItem {
    public PlayerHeadItem(Block block, Block wallBlock, Item.Settings settings) {
        super(block, wallBlock, settings, Direction.DOWN);
    }

    @Override
    public Text getName(ItemStack stack) {
        ProfileComponent profileComponent = stack.get(DataComponentTypes.PROFILE);
        if (profileComponent != null && profileComponent.name().isPresent()) {
            return Text.translatable(this.getTranslationKey() + ".named", profileComponent.name().get());
        }
        return super.getName(stack);
    }

    @Override
    public void postProcessComponents(ItemStack stack) {
        ProfileComponent profileComponent = stack.get(DataComponentTypes.PROFILE);
        if (profileComponent != null && !profileComponent.isCompleted()) {
            profileComponent.getFuture().thenAcceptAsync(profile -> stack.set(DataComponentTypes.PROFILE, profile), SkullBlockEntity.EXECUTOR);
        }
    }
}

