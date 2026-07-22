/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.advancement.criterion;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.entity.LootContextPredicateValidator;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

public class FishingRodHookedCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, ItemStack rod, FishingBobberEntity bobber, Collection<ItemStack> fishingLoots) {
        LootContext lootContext = EntityPredicate.createAdvancementEntityLootContext(player, bobber.getHookedEntity() != null ? bobber.getHookedEntity() : bobber);
        this.trigger(player, conditions -> conditions.matches(rod, lootContext, fishingLoots));
    }

    public record Conditions(Optional<LootContextPredicate> player, Optional<ItemPredicate> rod, Optional<LootContextPredicate> entity, Optional<ItemPredicate> item) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), ItemPredicate.CODEC.optionalFieldOf("rod").forGetter(Conditions::rod), EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("entity").forGetter(Conditions::entity), ItemPredicate.CODEC.optionalFieldOf("item").forGetter(Conditions::item)).apply((Applicative<Conditions, ?>)instance, Conditions::new));

        public static AdvancementCriterion<Conditions> create(Optional<ItemPredicate> rod, Optional<EntityPredicate> hookedEntity, Optional<ItemPredicate> caughtItem) {
            return Criteria.FISHING_ROD_HOOKED.create(new Conditions(Optional.empty(), rod, EntityPredicate.contextPredicateFromEntityPredicate(hookedEntity), caughtItem));
        }

        public boolean matches(ItemStack rodStack, LootContext hookedEntity, Collection<ItemStack> fishingLoots) {
            if (this.rod.isPresent() && !this.rod.get().test(rodStack)) {
                return false;
            }
            if (this.entity.isPresent() && !this.entity.get().test(hookedEntity)) {
                return false;
            }
            if (this.item.isPresent()) {
                boolean bl = false;
                Entity entity = hookedEntity.get(LootContextParameters.THIS_ENTITY);
                if (entity instanceof ItemEntity) {
                    ItemEntity itemEntity = (ItemEntity)entity;
                    if (this.item.get().test(itemEntity.getStack())) {
                        bl = true;
                    }
                }
                for (ItemStack itemStack : fishingLoots) {
                    if (!this.item.get().test(itemStack)) continue;
                    bl = true;
                    break;
                }
                if (!bl) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void validate(LootContextPredicateValidator validator) {
            AbstractCriterion.Conditions.super.validate(validator);
            validator.validateEntityPredicate(this.entity, ".entity");
        }
    }
}

