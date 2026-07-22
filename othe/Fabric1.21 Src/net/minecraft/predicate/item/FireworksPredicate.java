/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate.item;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.collection.CollectionPredicate;
import net.minecraft.predicate.item.ComponentSubPredicate;
import net.minecraft.predicate.item.FireworkExplosionPredicate;

public record FireworksPredicate(Optional<CollectionPredicate<FireworkExplosionComponent, FireworkExplosionPredicate.Predicate>> explosions, NumberRange.IntRange flightDuration) implements ComponentSubPredicate<FireworksComponent>
{
    public static final Codec<FireworksPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(CollectionPredicate.createCodec(FireworkExplosionPredicate.Predicate.CODEC).optionalFieldOf("explosions").forGetter(FireworksPredicate::explosions), NumberRange.IntRange.CODEC.optionalFieldOf("flight_duration", NumberRange.IntRange.ANY).forGetter(FireworksPredicate::flightDuration)).apply((Applicative<FireworksPredicate, ?>)instance, FireworksPredicate::new));

    @Override
    public ComponentType<FireworksComponent> getComponentType() {
        return DataComponentTypes.FIREWORKS;
    }

    @Override
    public boolean test(ItemStack itemStack, FireworksComponent fireworksComponent) {
        if (this.explosions.isPresent() && !this.explosions.get().test(fireworksComponent.explosions())) {
            return false;
        }
        return this.flightDuration.test(fireworksComponent.flightDuration());
    }
}

