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
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

public class ItemDurabilityChangedCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, ItemStack stack, int durability) {
        this.trigger(player, conditions -> conditions.matches(stack, durability));
    }

    public record Conditions(Optional<LootContextPredicate> player, Optional<ItemPredicate> item, NumberRange.IntRange durability, NumberRange.IntRange delta) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), ItemPredicate.CODEC.optionalFieldOf("item").forGetter(Conditions::item), NumberRange.IntRange.CODEC.optionalFieldOf("durability", NumberRange.IntRange.ANY).forGetter(Conditions::durability), NumberRange.IntRange.CODEC.optionalFieldOf("delta", NumberRange.IntRange.ANY).forGetter(Conditions::delta)).apply((Applicative<Conditions, ?>)instance, Conditions::new));

        public static AdvancementCriterion<Conditions> create(Optional<ItemPredicate> item, NumberRange.IntRange durability) {
            return Conditions.create(Optional.empty(), item, durability);
        }

        public static AdvancementCriterion<Conditions> create(Optional<LootContextPredicate> playerPredicate, Optional<ItemPredicate> item, NumberRange.IntRange durability) {
            return Criteria.ITEM_DURABILITY_CHANGED.create(new Conditions(playerPredicate, item, durability, NumberRange.IntRange.ANY));
        }

        public boolean matches(ItemStack stack, int durability) {
            if (this.item.isPresent() && !this.item.get().test(stack)) {
                return false;
            }
            if (!this.durability.test(stack.getMaxDamage() - durability)) {
                return false;
            }
            return this.delta.test(stack.getDamage() - durability);
        }
    }
}

