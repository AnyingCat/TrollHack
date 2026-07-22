/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate.entity;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntitySubPredicate;
import net.minecraft.predicate.entity.EntitySubPredicateTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public record SlimePredicate(NumberRange.IntRange size) implements EntitySubPredicate
{
    public static final MapCodec<SlimePredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(NumberRange.IntRange.CODEC.optionalFieldOf("size", NumberRange.IntRange.ANY).forGetter(SlimePredicate::size)).apply((Applicative<SlimePredicate, ?>)instance, SlimePredicate::new));

    public static SlimePredicate of(NumberRange.IntRange size) {
        return new SlimePredicate(size);
    }

    @Override
    public boolean test(Entity entity, ServerWorld world, @Nullable Vec3d pos) {
        if (entity instanceof SlimeEntity) {
            SlimeEntity slimeEntity = (SlimeEntity)entity;
            return this.size.test(slimeEntity.getSize());
        }
        return false;
    }

    public MapCodec<SlimePredicate> getCodec() {
        return EntitySubPredicateTypes.SLIME;
    }
}

