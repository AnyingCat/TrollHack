/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.advancement.criterion;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.entity.LootContextPredicateValidator;
import net.minecraft.server.network.ServerPlayerEntity;

public class LightningStrikeCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, LightningEntity lightning, List<Entity> bystanders) {
        List list = bystanders.stream().map(bystander -> EntityPredicate.createAdvancementEntityLootContext(player, bystander)).collect(Collectors.toList());
        LootContext lootContext = EntityPredicate.createAdvancementEntityLootContext(player, lightning);
        this.trigger(player, conditions -> conditions.test(lootContext, list));
    }

    public record Conditions(Optional<LootContextPredicate> player, Optional<LootContextPredicate> lightning, Optional<LootContextPredicate> bystander) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("lightning").forGetter(Conditions::lightning), EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("bystander").forGetter(Conditions::bystander)).apply((Applicative<Conditions, ?>)instance, Conditions::new));

        public static AdvancementCriterion<Conditions> create(Optional<EntityPredicate> lightning, Optional<EntityPredicate> bystander) {
            return Criteria.LIGHTNING_STRIKE.create(new Conditions(Optional.empty(), EntityPredicate.contextPredicateFromEntityPredicate(lightning), EntityPredicate.contextPredicateFromEntityPredicate(bystander)));
        }

        public boolean test(LootContext lightning, List<LootContext> bystanders) {
            if (this.lightning.isPresent() && !this.lightning.get().test(lightning)) {
                return false;
            }
            if (this.bystander.isPresent()) {
                if (bystanders.stream().noneMatch(this.bystander.get()::test)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void validate(LootContextPredicateValidator validator) {
            AbstractCriterion.Conditions.super.validate(validator);
            validator.validateEntityPredicate(this.lightning, ".lightning");
            validator.validateEntityPredicate(this.bystander, ".bystander");
        }
    }
}

