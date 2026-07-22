/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.advancement.criterion;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.entity.LootContextPredicateValidator;
import net.minecraft.server.network.ServerPlayerEntity;

public class ChanneledLightningCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    @Override
    public void trigger(ServerPlayerEntity player, Collection<? extends Entity> victims) {
        List list = victims.stream().map(entity -> EntityPredicate.createAdvancementEntityLootContext(player, entity)).collect(Collectors.toList());
        this.trigger(player, (T conditions) -> conditions.matches(list));
    }

    public record Conditions(Optional<LootContextPredicate> player, List<LootContextPredicate> victims) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create((RecordCodecBuilder.Instance<O> instance) -> instance.group(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.listOf().optionalFieldOf("victims", List.of()).forGetter(Conditions::victims)).apply((Applicative<Conditions, ?>)instance, Conditions::new));

        public static AdvancementCriterion<Conditions> create(EntityPredicate.Builder ... victims) {
            return Criteria.CHANNELED_LIGHTNING.create(new Conditions(Optional.empty(), EntityPredicate.contextPredicateFromEntityPredicates(victims)));
        }

        public boolean matches(Collection<? extends LootContext> victims) {
            for (LootContextPredicate lootContextPredicate : this.victims) {
                boolean bl = false;
                for (LootContext lootContext : victims) {
                    if (!lootContextPredicate.test(lootContext)) continue;
                    bl = true;
                    break;
                }
                if (bl) continue;
                return false;
            }
            return true;
        }

        @Override
        public void validate(LootContextPredicateValidator validator) {
            AbstractCriterion.Conditions.super.validate(validator);
            validator.validateEntityPredicates(this.victims, ".victims");
        }
    }
}

