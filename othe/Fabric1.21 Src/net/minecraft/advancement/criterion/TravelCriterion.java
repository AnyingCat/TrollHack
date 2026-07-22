/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.advancement.criterion;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.predicate.entity.DistancePredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class TravelCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, Vec3d startPos) {
        Vec3d vec3d = player.getPos();
        this.trigger(player, (T conditions) -> conditions.matches(player.getServerWorld(), startPos, vec3d));
    }

    public record Conditions(Optional<LootContextPredicate> player, Optional<LocationPredicate> startPosition, Optional<DistancePredicate> distance) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), LocationPredicate.CODEC.optionalFieldOf("start_position").forGetter(Conditions::startPosition), DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(Conditions::distance)).apply((Applicative<Conditions, ?>)instance, Conditions::new));

        public static AdvancementCriterion<Conditions> fallFromHeight(EntityPredicate.Builder entity, DistancePredicate distance, LocationPredicate.Builder startPos) {
            return Criteria.FALL_FROM_HEIGHT.create(new Conditions(Optional.of(EntityPredicate.contextPredicateFromEntityPredicate(entity)), Optional.of(startPos.build()), Optional.of(distance)));
        }

        public static AdvancementCriterion<Conditions> rideEntityInLava(EntityPredicate.Builder entity, DistancePredicate distance) {
            return Criteria.RIDE_ENTITY_IN_LAVA.create(new Conditions(Optional.of(EntityPredicate.contextPredicateFromEntityPredicate(entity)), Optional.empty(), Optional.of(distance)));
        }

        public static AdvancementCriterion<Conditions> netherTravel(DistancePredicate distance) {
            return Criteria.NETHER_TRAVEL.create(new Conditions(Optional.empty(), Optional.empty(), Optional.of(distance)));
        }

        public boolean matches(ServerWorld world, Vec3d pos, Vec3d endPos) {
            if (this.startPosition.isPresent() && !this.startPosition.get().test(world, pos.x, pos.y, pos.z)) {
                return false;
            }
            return !this.distance.isPresent() || this.distance.get().test(pos.x, pos.y, pos.z, endPos.x, endPos.y, endPos.z);
        }
    }
}

