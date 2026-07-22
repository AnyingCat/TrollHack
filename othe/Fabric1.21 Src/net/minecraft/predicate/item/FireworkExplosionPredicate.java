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
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.item.ComponentSubPredicate;

public record FireworkExplosionPredicate(Predicate predicate) implements ComponentSubPredicate<FireworkExplosionComponent>
{
    public static final Codec<FireworkExplosionPredicate> CODEC = Predicate.CODEC.xmap(FireworkExplosionPredicate::new, FireworkExplosionPredicate::predicate);

    @Override
    public ComponentType<FireworkExplosionComponent> getComponentType() {
        return DataComponentTypes.FIREWORK_EXPLOSION;
    }

    @Override
    public boolean test(ItemStack itemStack, FireworkExplosionComponent fireworkExplosionComponent) {
        return this.predicate.test(fireworkExplosionComponent);
    }

    public record Predicate(Optional<FireworkExplosionComponent.Type> shape, Optional<Boolean> twinkle, Optional<Boolean> trail) implements java.util.function.Predicate<FireworkExplosionComponent>
    {
        public static final Codec<Predicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(FireworkExplosionComponent.Type.CODEC.optionalFieldOf("shape").forGetter(Predicate::shape), Codec.BOOL.optionalFieldOf("has_twinkle").forGetter(Predicate::twinkle), Codec.BOOL.optionalFieldOf("has_trail").forGetter(Predicate::trail)).apply((Applicative<Predicate, ?>)instance, Predicate::new));

        @Override
        public boolean test(FireworkExplosionComponent fireworkExplosionComponent) {
            if (this.shape.isPresent() && this.shape.get() != fireworkExplosionComponent.shape()) {
                return false;
            }
            if (this.twinkle.isPresent() && this.twinkle.get().booleanValue() != fireworkExplosionComponent.hasTwinkle()) {
                return false;
            }
            return !this.trail.isPresent() || this.trail.get().booleanValue() == fireworkExplosionComponent.hasTrail();
        }

        @Override
        public /* synthetic */ boolean test(Object fireworkExplosionComponent) {
            return this.test((FireworkExplosionComponent)fireworkExplosionComponent);
        }
    }
}

