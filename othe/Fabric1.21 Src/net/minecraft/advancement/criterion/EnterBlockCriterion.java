/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.advancement.criterion;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;

public class EnterBlockCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, BlockState state) {
        this.trigger(player, (T conditions) -> conditions.matches(state));
    }

    public record Conditions(Optional<LootContextPredicate> player, Optional<RegistryEntry<Block>> block, Optional<StatePredicate> state) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), Registries.BLOCK.getEntryCodec().optionalFieldOf("block").forGetter(Conditions::block), StatePredicate.CODEC.optionalFieldOf("state").forGetter(Conditions::state)).apply((Applicative<Conditions, ?>)instance, Conditions::new)).validate(Conditions::validate);

        private static DataResult<Conditions> validate(Conditions conditions) {
            return conditions.block.flatMap(block -> conditions.state.flatMap(state -> state.findMissing(((Block)block.value()).getStateManager())).map(property -> DataResult.error(() -> "Block" + String.valueOf(block) + " has no property " + property))).orElseGet(() -> DataResult.success(conditions));
        }

        public static AdvancementCriterion<Conditions> block(Block block) {
            return Criteria.ENTER_BLOCK.create(new Conditions(Optional.empty(), Optional.of(block.getRegistryEntry()), Optional.empty()));
        }

        public boolean matches(BlockState state) {
            if (this.block.isPresent() && !state.isOf(this.block.get())) {
                return false;
            }
            return !this.state.isPresent() || this.state.get().test(state);
        }
    }
}

