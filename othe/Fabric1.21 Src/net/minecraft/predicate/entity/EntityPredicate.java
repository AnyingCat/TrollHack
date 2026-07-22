/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate.entity;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.predicate.entity.DistancePredicate;
import net.minecraft.predicate.entity.EntityEffectPredicate;
import net.minecraft.predicate.entity.EntityEquipmentPredicate;
import net.minecraft.predicate.entity.EntityFlagsPredicate;
import net.minecraft.predicate.entity.EntitySubPredicate;
import net.minecraft.predicate.entity.EntityTypePredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.entity.MovementPredicate;
import net.minecraft.predicate.entity.SlotsPredicate;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public record EntityPredicate(Optional<EntityTypePredicate> type, Optional<DistancePredicate> distance, Optional<MovementPredicate> movement, PositionalPredicates location, Optional<EntityEffectPredicate> effects, Optional<NbtPredicate> nbt, Optional<EntityFlagsPredicate> flags, Optional<EntityEquipmentPredicate> equipment, Optional<EntitySubPredicate> typeSpecific, Optional<Integer> periodicTick, Optional<EntityPredicate> vehicle, Optional<EntityPredicate> passenger, Optional<EntityPredicate> targetedEntity, Optional<String> team, Optional<SlotsPredicate> slots) {
    public static final Codec<EntityPredicate> CODEC = Codec.recursive("EntityPredicate", entityPredicateCodec -> RecordCodecBuilder.create(instance -> instance.group(EntityTypePredicate.CODEC.optionalFieldOf("type").forGetter(EntityPredicate::type), DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(EntityPredicate::distance), MovementPredicate.CODEC.optionalFieldOf("movement").forGetter(EntityPredicate::movement), PositionalPredicates.CODEC.forGetter(EntityPredicate::location), EntityEffectPredicate.CODEC.optionalFieldOf("effects").forGetter(EntityPredicate::effects), NbtPredicate.CODEC.optionalFieldOf("nbt").forGetter(EntityPredicate::nbt), EntityFlagsPredicate.CODEC.optionalFieldOf("flags").forGetter(EntityPredicate::flags), EntityEquipmentPredicate.CODEC.optionalFieldOf("equipment").forGetter(EntityPredicate::equipment), EntitySubPredicate.CODEC.optionalFieldOf("type_specific").forGetter(EntityPredicate::typeSpecific), Codecs.POSITIVE_INT.optionalFieldOf("periodic_tick").forGetter(EntityPredicate::periodicTick), entityPredicateCodec.optionalFieldOf("vehicle").forGetter(EntityPredicate::vehicle), entityPredicateCodec.optionalFieldOf("passenger").forGetter(EntityPredicate::passenger), entityPredicateCodec.optionalFieldOf("targeted_entity").forGetter(EntityPredicate::targetedEntity), Codec.STRING.optionalFieldOf("team").forGetter(EntityPredicate::team), SlotsPredicate.CODEC.optionalFieldOf("slots").forGetter(EntityPredicate::slots)).apply((Applicative<EntityPredicate, ?>)instance, EntityPredicate::new)));
    public static final Codec<LootContextPredicate> LOOT_CONTEXT_PREDICATE_CODEC = Codec.withAlternative(LootContextPredicate.CODEC, CODEC, EntityPredicate::asLootContextPredicate);

    public static LootContextPredicate contextPredicateFromEntityPredicate(Builder builder) {
        return EntityPredicate.asLootContextPredicate(builder.build());
    }

    public static Optional<LootContextPredicate> contextPredicateFromEntityPredicate(Optional<EntityPredicate> entityPredicate) {
        return entityPredicate.map(EntityPredicate::asLootContextPredicate);
    }

    public static List<LootContextPredicate> contextPredicateFromEntityPredicates(Builder ... builders) {
        return Stream.of(builders).map(EntityPredicate::contextPredicateFromEntityPredicate).toList();
    }

    public static LootContextPredicate asLootContextPredicate(EntityPredicate predicate) {
        LootCondition lootCondition = EntityPropertiesLootCondition.builder(LootContext.EntityTarget.THIS, predicate).build();
        return new LootContextPredicate(List.of(lootCondition));
    }

    public boolean test(ServerPlayerEntity player, @Nullable Entity entity) {
        return this.test(player.getServerWorld(), player.getPos(), entity);
    }

    public boolean test(ServerWorld world, @Nullable Vec3d pos, @Nullable Entity entity) {
        Team abstractTeam;
        Vec3d vec3d;
        if (entity == null) {
            return false;
        }
        if (this.type.isPresent() && !this.type.get().matches(entity.getType())) {
            return false;
        }
        if (pos == null ? this.distance.isPresent() : this.distance.isPresent() && !this.distance.get().test(pos.x, pos.y, pos.z, entity.getX(), entity.getY(), entity.getZ())) {
            return false;
        }
        if (this.movement.isPresent()) {
            vec3d = entity.getMovement();
            Vec3d vec3d2 = vec3d.multiply(20.0);
            if (!this.movement.get().test(vec3d2.x, vec3d2.y, vec3d2.z, entity.fallDistance)) {
                return false;
            }
        }
        if (this.location.located.isPresent() && !this.location.located.get().test(world, entity.getX(), entity.getY(), entity.getZ())) {
            return false;
        }
        if (this.location.steppingOn.isPresent()) {
            vec3d = Vec3d.ofCenter(entity.getSteppingPos());
            if (!this.location.steppingOn.get().test(world, vec3d.getX(), vec3d.getY(), vec3d.getZ())) {
                return false;
            }
        }
        if (this.location.affectsMovement.isPresent()) {
            vec3d = Vec3d.ofCenter(entity.getVelocityAffectingPos());
            if (!this.location.affectsMovement.get().test(world, vec3d.getX(), vec3d.getY(), vec3d.getZ())) {
                return false;
            }
        }
        if (this.effects.isPresent() && !this.effects.get().test(entity)) {
            return false;
        }
        if (this.flags.isPresent() && !this.flags.get().test(entity)) {
            return false;
        }
        if (this.equipment.isPresent() && !this.equipment.get().test(entity)) {
            return false;
        }
        if (this.typeSpecific.isPresent() && !this.typeSpecific.get().test(entity, world, pos)) {
            return false;
        }
        if (this.vehicle.isPresent() && !this.vehicle.get().test(world, pos, entity.getVehicle())) {
            return false;
        }
        if (this.passenger.isPresent() && entity.getPassengerList().stream().noneMatch(entityx -> this.passenger.get().test(world, pos, (Entity)entityx))) {
            return false;
        }
        if (this.targetedEntity.isPresent() && !this.targetedEntity.get().test(world, pos, entity instanceof MobEntity ? ((MobEntity)entity).getTarget() : null)) {
            return false;
        }
        if (this.periodicTick.isPresent() && entity.age % this.periodicTick.get() != 0) {
            return false;
        }
        if (this.team.isPresent() && ((abstractTeam = entity.getScoreboardTeam()) == null || !this.team.get().equals(((AbstractTeam)abstractTeam).getName()))) {
            return false;
        }
        if (this.slots.isPresent() && !this.slots.get().matches(entity)) {
            return false;
        }
        return !this.nbt.isPresent() || this.nbt.get().test(entity);
    }

    public static LootContext createAdvancementEntityLootContext(ServerPlayerEntity player, Entity target) {
        LootContextParameterSet lootContextParameterSet = new LootContextParameterSet.Builder(player.getServerWorld()).add(LootContextParameters.THIS_ENTITY, target).add(LootContextParameters.ORIGIN, player.getPos()).build(LootContextTypes.ADVANCEMENT_ENTITY);
        return new LootContext.Builder(lootContextParameterSet).build(Optional.empty());
    }

    public record PositionalPredicates(Optional<LocationPredicate> located, Optional<LocationPredicate> steppingOn, Optional<LocationPredicate> affectsMovement) {
        public static final MapCodec<PositionalPredicates> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(LocationPredicate.CODEC.optionalFieldOf("location").forGetter(PositionalPredicates::located), LocationPredicate.CODEC.optionalFieldOf("stepping_on").forGetter(PositionalPredicates::steppingOn), LocationPredicate.CODEC.optionalFieldOf("movement_affected_by").forGetter(PositionalPredicates::affectsMovement)).apply((Applicative<PositionalPredicates, ?>)instance, PositionalPredicates::new));
    }

    public static class Builder {
        private Optional<EntityTypePredicate> type = Optional.empty();
        private Optional<DistancePredicate> distance = Optional.empty();
        private Optional<DistancePredicate> field_51572 = Optional.empty();
        private Optional<MovementPredicate> movement = Optional.empty();
        private Optional<PositionalPredicates> positional = Optional.empty();
        private Optional<LocationPredicate> location = Optional.empty();
        private Optional<LocationPredicate> steppingOn = Optional.empty();
        private Optional<LocationPredicate> movementAffectedBy = Optional.empty();
        private Optional<EntityEffectPredicate> effects = Optional.empty();
        private Optional<NbtPredicate> nbt = Optional.empty();
        private Optional<EntityFlagsPredicate> flags = Optional.empty();
        private Optional<EntityEquipmentPredicate> equipment = Optional.empty();
        private Optional<EntitySubPredicate> typeSpecific = Optional.empty();
        private Optional<Integer> periodicTick = Optional.empty();
        private Optional<EntityPredicate> vehicle = Optional.empty();
        private Optional<EntityPredicate> passenger = Optional.empty();
        private Optional<EntityPredicate> targetedEntity = Optional.empty();
        private Optional<String> team = Optional.empty();
        private Optional<SlotsPredicate> slots = Optional.empty();

        public static Builder create() {
            return new Builder();
        }

        public Builder type(EntityType<?> type) {
            this.type = Optional.of(EntityTypePredicate.create(type));
            return this;
        }

        public Builder type(TagKey<EntityType<?>> tag) {
            this.type = Optional.of(EntityTypePredicate.create(tag));
            return this;
        }

        public Builder type(EntityTypePredicate type) {
            this.type = Optional.of(type);
            return this;
        }

        public Builder distance(DistancePredicate distance) {
            this.distance = Optional.of(distance);
            return this;
        }

        public Builder movement(MovementPredicate movement) {
            this.movement = Optional.of(movement);
            return this;
        }

        public Builder location(LocationPredicate.Builder location) {
            this.location = Optional.of(location.build());
            return this;
        }

        public Builder steppingOn(LocationPredicate.Builder steppingOn) {
            this.steppingOn = Optional.of(steppingOn.build());
            return this;
        }

        public Builder movementAffectedBy(LocationPredicate.Builder movementAffectedBy) {
            this.movementAffectedBy = Optional.of(movementAffectedBy.build());
            return this;
        }

        public Builder effects(EntityEffectPredicate.Builder effects) {
            this.effects = effects.build();
            return this;
        }

        public Builder nbt(NbtPredicate nbt) {
            this.nbt = Optional.of(nbt);
            return this;
        }

        public Builder flags(EntityFlagsPredicate.Builder flags) {
            this.flags = Optional.of(flags.build());
            return this;
        }

        public Builder equipment(EntityEquipmentPredicate.Builder equipment) {
            this.equipment = Optional.of(equipment.build());
            return this;
        }

        public Builder equipment(EntityEquipmentPredicate equipment) {
            this.equipment = Optional.of(equipment);
            return this;
        }

        public Builder typeSpecific(EntitySubPredicate typeSpecific) {
            this.typeSpecific = Optional.of(typeSpecific);
            return this;
        }

        public Builder periodicTick(int periodicTick) {
            this.periodicTick = Optional.of(periodicTick);
            return this;
        }

        public Builder vehicle(Builder vehicle) {
            this.vehicle = Optional.of(vehicle.build());
            return this;
        }

        public Builder passenger(Builder passenger) {
            this.passenger = Optional.of(passenger.build());
            return this;
        }

        public Builder targetedEntity(Builder targetedEntity) {
            this.targetedEntity = Optional.of(targetedEntity.build());
            return this;
        }

        public Builder team(String team) {
            this.team = Optional.of(team);
            return this;
        }

        public Builder slots(SlotsPredicate slots) {
            this.slots = Optional.of(slots);
            return this;
        }

        public EntityPredicate build() {
            return new EntityPredicate(this.type, this.distance, this.movement, new PositionalPredicates(this.location, this.steppingOn, this.movementAffectedBy), this.effects, this.nbt, this.flags, this.equipment, this.typeSpecific, this.periodicTick, this.vehicle, this.passenger, this.targetedEntity, this.team, this.slots);
        }
    }
}

