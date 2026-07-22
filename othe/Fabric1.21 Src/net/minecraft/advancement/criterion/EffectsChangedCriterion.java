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
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.EntityEffectPredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.entity.LootContextPredicateValidator;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class EffectsChangedCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, @Nullable Entity source) {
        LootContext lootContext = source != null ? EntityPredicate.createAdvancementEntityLootContext(player, source) : null;
        this.trigger(player, (T conditions) -> conditions.matches(player, lootContext));
    }

    public record Conditions(Optional<LootContextPredicate> player, Optional<EntityEffectPredicate> effects, Optional<LootContextPredicate> source) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create((RecordCodecBuilder.Instance<O> instance) -> instance.group(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), EntityEffectPredicate.CODEC.optionalFieldOf("effects").forGetter(Conditions::effects), EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("source").forGetter(Conditions::source)).apply((Applicative<Conditions, ?>)instance, Conditions::new));

        public static AdvancementCriterion<Conditions> create(EntityEffectPredicate.Builder effects) {
            return Criteria.EFFECTS_CHANGED.create(new Conditions(Optional.empty(), effects.build(), Optional.empty()));
        }

        public static AdvancementCriterion<Conditions> create(EntityPredicate.Builder source) {
            return Criteria.EFFECTS_CHANGED.create(new Conditions(Optional.empty(), Optional.empty(), Optional.of(EntityPredicate.asLootContextPredicate(source.build()))));
        }

        public boolean matches(ServerPlayerEntity player, @Nullable LootContext context) {
            if (this.effects.isPresent() && !this.effects.get().test(player)) {
                return false;
            }
            return !this.source.isPresent() || context != null && this.source.get().test(context);
        }

        @Override
        public void validate(LootContextPredicateValidator validator) {
            AbstractCriterion.Conditions.super.validate(validator);
            validator.validateEntityPredicate(this.source, ".source");
        }
    }
}

