/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate.entity;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.predicate.entity.EntitySubPredicate;
import net.minecraft.predicate.entity.EntitySubPredicateTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public record FishingHookPredicate(Optional<Boolean> inOpenWater) implements EntitySubPredicate
{
    public static final FishingHookPredicate ALL = new FishingHookPredicate(Optional.empty());
    public static final MapCodec<FishingHookPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.BOOL.optionalFieldOf("in_open_water").forGetter(FishingHookPredicate::inOpenWater)).apply((Applicative<FishingHookPredicate, ?>)instance, FishingHookPredicate::new));

    public static FishingHookPredicate of(boolean inOpenWater) {
        return new FishingHookPredicate(Optional.of(inOpenWater));
    }

    public MapCodec<FishingHookPredicate> getCodec() {
        return EntitySubPredicateTypes.FISHING_HOOK;
    }

    @Override
    public boolean test(Entity entity, ServerWorld world, @Nullable Vec3d pos) {
        if (this.inOpenWater.isEmpty()) {
            return true;
        }
        if (entity instanceof FishingBobberEntity) {
            FishingBobberEntity fishingBobberEntity = (FishingBobberEntity)entity;
            return this.inOpenWater.get().booleanValue() == fishingBobberEntity.isInOpenWater();
        }
        return false;
    }
}

