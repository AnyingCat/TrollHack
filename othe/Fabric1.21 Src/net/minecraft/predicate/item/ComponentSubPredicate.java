/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate.item;

import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.item.ItemSubPredicate;

public interface ComponentSubPredicate<T>
extends ItemSubPredicate {
    @Override
    default public boolean test(ItemStack stack) {
        T object = stack.get(this.getComponentType());
        return object != null && this.test(stack, object);
    }

    public ComponentType<T> getComponentType();

    public boolean test(ItemStack var1, T var2);
}

