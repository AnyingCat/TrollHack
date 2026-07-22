/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate.item;

import com.mojang.serialization.Codec;
import java.util.Map;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

public interface ItemSubPredicate {
    public static final Codec<Map<Type<?>, ItemSubPredicate>> PREDICATES_MAP_CODEC = Codec.dispatchedMap(Registries.ITEM_SUB_PREDICATE_TYPE.getCodec(), Type::codec);

    public boolean test(ItemStack var1);

    public record Type<T extends ItemSubPredicate>(Codec<T> codec) {
    }
}

