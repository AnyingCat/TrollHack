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
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.entity.LootContextPredicateValidator;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

public class VillagerTradeCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, MerchantEntity merchant, ItemStack stack) {
        LootContext lootContext = EntityPredicate.createAdvancementEntityLootContext(player, merchant);
        this.trigger(player, conditions -> conditions.matches(lootContext, stack));
    }

    public record Conditions(Optional<LootContextPredicate> player, Optional<LootContextPredicate> villager, Optional<ItemPredicate> item) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create((RecordCodecBuilder.Instance<O> instance) -> instance.group(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("villager").forGetter(Conditions::villager), ItemPredicate.CODEC.optionalFieldOf("item").forGetter(Conditions::item)).apply((Applicative<Conditions, ?>)instance, Conditions::new));

        public static AdvancementCriterion<Conditions> any() {
            return Criteria.VILLAGER_TRADE.create(new Conditions(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static AdvancementCriterion<Conditions> create(EntityPredicate.Builder playerPredicate) {
            return Criteria.VILLAGER_TRADE.create(new Conditions(Optional.of(EntityPredicate.contextPredicateFromEntityPredicate(playerPredicate)), Optional.empty(), Optional.empty()));
        }

        public boolean matches(LootContext villager, ItemStack stack) {
            if (this.villager.isPresent() && !this.villager.get().test(villager)) {
                return false;
            }
            return !this.item.isPresent() || this.item.get().test(stack);
        }

        @Override
        public void validate(LootContextPredicateValidator validator) {
            AbstractCriterion.Conditions.super.validate(validator);
            validator.validateEntityPredicate(this.villager, ".villager");
        }
    }
}

