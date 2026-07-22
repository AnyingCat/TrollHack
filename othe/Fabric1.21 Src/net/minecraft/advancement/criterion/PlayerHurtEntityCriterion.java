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
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.DamagePredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.entity.LootContextPredicateValidator;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerHurtEntityCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, Entity entity, DamageSource damage, float dealt, float taken, boolean blocked) {
        LootContext lootContext = EntityPredicate.createAdvancementEntityLootContext(player, entity);
        this.trigger(player, conditions -> conditions.matches(player, lootContext, damage, dealt, taken, blocked));
    }

    public record Conditions(Optional<LootContextPredicate> player, Optional<DamagePredicate> damage, Optional<LootContextPredicate> entity) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create((RecordCodecBuilder.Instance<O> instance) -> instance.group(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), DamagePredicate.CODEC.optionalFieldOf("damage").forGetter(Conditions::damage), EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("entity").forGetter(Conditions::entity)).apply((Applicative<Conditions, ?>)instance, Conditions::new));

        public static AdvancementCriterion<Conditions> create() {
            return Criteria.PLAYER_HURT_ENTITY.create(new Conditions(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static AdvancementCriterion<Conditions> createDamage(Optional<DamagePredicate> damage) {
            return Criteria.PLAYER_HURT_ENTITY.create(new Conditions(Optional.empty(), damage, Optional.empty()));
        }

        public static AdvancementCriterion<Conditions> create(DamagePredicate.Builder damage) {
            return Criteria.PLAYER_HURT_ENTITY.create(new Conditions(Optional.empty(), Optional.of(damage.build()), Optional.empty()));
        }

        public static AdvancementCriterion<Conditions> createEntity(Optional<EntityPredicate> entity) {
            return Criteria.PLAYER_HURT_ENTITY.create(new Conditions(Optional.empty(), Optional.empty(), EntityPredicate.contextPredicateFromEntityPredicate(entity)));
        }

        public static AdvancementCriterion<Conditions> create(Optional<DamagePredicate> damage, Optional<EntityPredicate> entity) {
            return Criteria.PLAYER_HURT_ENTITY.create(new Conditions(Optional.empty(), damage, EntityPredicate.contextPredicateFromEntityPredicate(entity)));
        }

        public static AdvancementCriterion<Conditions> create(DamagePredicate.Builder damage, Optional<EntityPredicate> entity) {
            return Criteria.PLAYER_HURT_ENTITY.create(new Conditions(Optional.empty(), Optional.of(damage.build()), EntityPredicate.contextPredicateFromEntityPredicate(entity)));
        }

        public boolean matches(ServerPlayerEntity player, LootContext entity, DamageSource damageSource, float dealt, float taken, boolean blocked) {
            if (this.damage.isPresent() && !this.damage.get().test(player, damageSource, dealt, taken, blocked)) {
                return false;
            }
            return !this.entity.isPresent() || this.entity.get().test(entity);
        }

        @Override
        public void validate(LootContextPredicateValidator validator) {
            AbstractCriterion.Conditions.super.validate(validator);
            validator.validateEntityPredicate(this.entity, ".entity");
        }
    }
}

