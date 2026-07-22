/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate.item;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.item.ComponentSubPredicate;

public record DamagePredicate(NumberRange.IntRange durability, NumberRange.IntRange damage) implements ComponentSubPredicate<Integer>
{
    public static final Codec<DamagePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(NumberRange.IntRange.CODEC.optionalFieldOf("durability", NumberRange.IntRange.ANY).forGetter(DamagePredicate::durability), NumberRange.IntRange.CODEC.optionalFieldOf("damage", NumberRange.IntRange.ANY).forGetter(DamagePredicate::damage)).apply((Applicative<DamagePredicate, ?>)instance, DamagePredicate::new));

    @Override
    public ComponentType<Integer> getComponentType() {
        return DataComponentTypes.DAMAGE;
    }

    @Override
    public boolean test(ItemStack itemStack, Integer integer) {
        if (!this.durability.test(itemStack.getMaxDamage() - integer)) {
            return false;
        }
        return this.damage.test(integer);
    }

    public static DamagePredicate durability(NumberRange.IntRange durability) {
        return new DamagePredicate(durability, NumberRange.IntRange.ANY);
    }
}

