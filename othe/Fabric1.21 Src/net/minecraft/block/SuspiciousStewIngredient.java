/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.component.type.SuspiciousStewEffectsComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;

public interface SuspiciousStewIngredient {
    public SuspiciousStewEffectsComponent getStewEffects();

    public static List<SuspiciousStewIngredient> getAll() {
        return Registries.ITEM.stream().map(SuspiciousStewIngredient::of).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Nullable
    public static SuspiciousStewIngredient of(ItemConvertible item) {
        BlockItem blockItem;
        ItemConvertible itemConvertible = item.asItem();
        if (itemConvertible instanceof BlockItem && (itemConvertible = (blockItem = (BlockItem)itemConvertible).getBlock()) instanceof SuspiciousStewIngredient) {
            SuspiciousStewIngredient suspiciousStewIngredient = (SuspiciousStewIngredient)((Object)itemConvertible);
            return suspiciousStewIngredient;
        }
        Item item2 = item.asItem();
        if (item2 instanceof SuspiciousStewIngredient) {
            SuspiciousStewIngredient suspiciousStewIngredient2 = (SuspiciousStewIngredient)((Object)item2);
            return suspiciousStewIngredient2;
        }
        return null;
    }
}

