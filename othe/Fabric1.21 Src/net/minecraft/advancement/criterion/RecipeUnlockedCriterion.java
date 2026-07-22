/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.advancement.criterion;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class RecipeUnlockedCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    @Override
    public void trigger(ServerPlayerEntity player, RecipeEntry<?> recipe) {
        ((AbstractCriterion)this).trigger(player, (T conditions) -> conditions.matches(recipe));
    }

    public static AdvancementCriterion<Conditions> create(Identifier id) {
        return Criteria.RECIPE_UNLOCKED.create(new Conditions(Optional.empty(), id));
    }

    public record Conditions(Optional<LootContextPredicate> player, Identifier recipe) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), ((MapCodec)Identifier.CODEC.fieldOf("recipe")).forGetter(Conditions::recipe)).apply((Applicative<Conditions, ?>)instance, Conditions::new));

        public boolean matches(RecipeEntry<?> recipe) {
            return this.recipe.equals(recipe.id());
        }
    }
}

