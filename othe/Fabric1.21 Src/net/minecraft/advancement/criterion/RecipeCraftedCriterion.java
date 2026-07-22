/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.advancement.criterion;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class RecipeCraftedCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, Identifier recipeId, List<ItemStack> ingredients) {
        this.trigger(player, conditions -> conditions.matches(recipeId, ingredients));
    }

    public record Conditions(Optional<LootContextPredicate> player, Identifier recipeId, List<ItemPredicate> ingredients) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create((RecordCodecBuilder.Instance<O> instance) -> instance.group(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), ((MapCodec)Identifier.CODEC.fieldOf("recipe_id")).forGetter(Conditions::recipeId), ItemPredicate.CODEC.listOf().optionalFieldOf("ingredients", List.of()).forGetter(Conditions::ingredients)).apply((Applicative<Conditions, ?>)instance, Conditions::new));

        public static AdvancementCriterion<Conditions> create(Identifier recipeId, List<ItemPredicate.Builder> ingredients) {
            return Criteria.RECIPE_CRAFTED.create(new Conditions(Optional.empty(), recipeId, ingredients.stream().map(ItemPredicate.Builder::build).toList()));
        }

        public static AdvancementCriterion<Conditions> create(Identifier recipeId) {
            return Criteria.RECIPE_CRAFTED.create(new Conditions(Optional.empty(), recipeId, List.of()));
        }

        public static AdvancementCriterion<Conditions> createCrafterRecipeCrafted(Identifier recipeId) {
            return Criteria.CRAFTER_RECIPE_CRAFTED.create(new Conditions(Optional.empty(), recipeId, List.of()));
        }

        boolean matches(Identifier recipeId, List<ItemStack> ingredients) {
            if (!recipeId.equals(this.recipeId)) {
                return false;
            }
            ArrayList<ItemStack> list = new ArrayList<ItemStack>(ingredients);
            for (ItemPredicate itemPredicate : this.ingredients) {
                boolean bl = false;
                Iterator iterator = list.iterator();
                while (iterator.hasNext()) {
                    if (!itemPredicate.test((ItemStack)iterator.next())) continue;
                    iterator.remove();
                    bl = true;
                    break;
                }
                if (bl) continue;
                return false;
            }
            return true;
        }
    }
}

