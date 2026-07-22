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
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.predicate.entity.EntityEquipmentPredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

public class TickCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player) {
        this.trigger(player, conditions -> true);
    }

    public record Conditions(Optional<LootContextPredicate> player) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player)).apply((Applicative<Conditions, ?>)instance, Conditions::new));

        public static AdvancementCriterion<Conditions> createLocation(LocationPredicate.Builder location) {
            return Criteria.LOCATION.create(new Conditions(Optional.of(EntityPredicate.contextPredicateFromEntityPredicate(EntityPredicate.Builder.create().location(location)))));
        }

        public static AdvancementCriterion<Conditions> createLocation(EntityPredicate.Builder entity) {
            return Criteria.LOCATION.create(new Conditions(Optional.of(EntityPredicate.asLootContextPredicate(entity.build()))));
        }

        public static AdvancementCriterion<Conditions> createLocation(Optional<EntityPredicate> entity) {
            return Criteria.LOCATION.create(new Conditions(EntityPredicate.contextPredicateFromEntityPredicate(entity)));
        }

        public static AdvancementCriterion<Conditions> createSleptInBed() {
            return Criteria.SLEPT_IN_BED.create(new Conditions(Optional.empty()));
        }

        public static AdvancementCriterion<Conditions> createHeroOfTheVillage() {
            return Criteria.HERO_OF_THE_VILLAGE.create(new Conditions(Optional.empty()));
        }

        public static AdvancementCriterion<Conditions> createAvoidVibration() {
            return Criteria.AVOID_VIBRATION.create(new Conditions(Optional.empty()));
        }

        public static AdvancementCriterion<Conditions> createTick() {
            return Criteria.TICK.create(new Conditions(Optional.empty()));
        }

        public static AdvancementCriterion<Conditions> createLocation(Block block, Item item) {
            return Conditions.createLocation(EntityPredicate.Builder.create().equipment(EntityEquipmentPredicate.Builder.create().feet(ItemPredicate.Builder.create().items(item))).steppingOn(LocationPredicate.Builder.create().block(BlockPredicate.Builder.create().blocks(block))));
        }
    }
}

