/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

public class FireworkStarItem
extends Item {
    public FireworkStarItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        FireworkExplosionComponent fireworkExplosionComponent = stack.get(DataComponentTypes.FIREWORK_EXPLOSION);
        if (fireworkExplosionComponent != null) {
            fireworkExplosionComponent.appendTooltip(context, tooltip::add, type);
        }
    }
}

