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
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.predicate.DamagePredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

public class EntityHurtPlayerCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, DamageSource source, float dealt, float taken, boolean blocked) {
        this.trigger(player, conditions -> conditions.matches(player, source, dealt, taken, blocked));
    }

    public record Conditions(Optional<LootContextPredicate> player, Optional<DamagePredicate> damage) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create((RecordCodecBuilder.Instance<O> instance) -> instance.group(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), DamagePredicate.CODEC.optionalFieldOf("damage").forGetter(Conditions::damage)).apply((Applicative<Conditions, ?>)instance, Conditions::new));

        public static AdvancementCriterion<Conditions> create() {
            return Criteria.ENTITY_HURT_PLAYER.create(new Conditions(Optional.empty(), Optional.empty()));
        }

        public static AdvancementCriterion<Conditions> create(DamagePredicate predicate) {
            return Criteria.ENTITY_HURT_PLAYER.create(new Conditions(Optional.empty(), Optional.of(predicate)));
        }

        public static AdvancementCriterion<Conditions> create(DamagePredicate.Builder damageBuilder) {
            return Criteria.ENTITY_HURT_PLAYER.create(new Conditions(Optional.empty(), Optional.of(damageBuilder.build())));
        }

        public boolean matches(ServerPlayerEntity player, DamageSource damageSource, float dealt, float taken, boolean blocked) {
            return !this.damage.isPresent() || this.damage.get().test(player, damageSource, dealt, taken, blocked);
        }
    }
}

