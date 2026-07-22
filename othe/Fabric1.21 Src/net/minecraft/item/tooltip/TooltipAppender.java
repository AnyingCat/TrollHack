/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item.tooltip;

import java.util.function.Consumer;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

public interface TooltipAppender {
    public void appendTooltip(Item.TooltipContext var1, Consumer<Text> var2, TooltipType var3);
}

