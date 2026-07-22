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
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;

public class BeeNestDestroyedCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, BlockState state, ItemStack stack, int beeCount) {
        this.trigger(player, conditions -> conditions.test(state, stack, beeCount));
    }

    public record Conditions(Optional<LootContextPredicate> player, Optional<RegistryEntry<Block>> block, Optional<ItemPredicate> item, NumberRange.IntRange beesInside) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), Registries.BLOCK.getEntryCodec().optionalFieldOf("block").forGetter(Conditions::block), ItemPredicate.CODEC.optionalFieldOf("item").forGetter(Conditions::item), NumberRange.IntRange.CODEC.optionalFieldOf("num_bees_inside", NumberRange.IntRange.ANY).forGetter(Conditions::beesInside)).apply((Applicative<Conditions, ?>)instance, Conditions::new));

        public static AdvancementCriterion<Conditions> create(Block block, ItemPredicate.Builder itemPredicateBuilder, NumberRange.IntRange beeCountRange) {
            return Criteria.BEE_NEST_DESTROYED.create(new Conditions(Optional.empty(), Optional.of(block.getRegistryEntry()), Optional.of(itemPredicateBuilder.build()), beeCountRange));
        }

        public boolean test(BlockState state, ItemStack stack, int count) {
            if (this.block.isPresent() && !state.isOf(this.block.get())) {
                return false;
            }
            if (this.item.isPresent() && !this.item.get().test(stack)) {
                return false;
            }
            return this.beesInside.test(count);
        }
    }
}

