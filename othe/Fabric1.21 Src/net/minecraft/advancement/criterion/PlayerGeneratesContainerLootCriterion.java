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
import net.minecraft.loot.LootTable;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerGeneratesContainerLootCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    @Override
    public void trigger(ServerPlayerEntity player, RegistryKey<LootTable> lootTable) {
        this.trigger(player, (T conditions) -> conditions.test(lootTable));
    }

    public record Conditions(Optional<LootContextPredicate> player, RegistryKey<LootTable> lootTable) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create((RecordCodecBuilder.Instance<O> instance) -> instance.group(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), ((MapCodec)RegistryKey.createCodec(RegistryKeys.LOOT_TABLE).fieldOf("loot_table")).forGetter(Conditions::lootTable)).apply((Applicative<Conditions, ?>)instance, Conditions::new));

        public static AdvancementCriterion<Conditions> create(RegistryKey<LootTable> registryKey) {
            return Criteria.PLAYER_GENERATES_CONTAINER_LOOT.create(new Conditions(Optional.empty(), registryKey));
        }

        public boolean test(RegistryKey<LootTable> lootTable) {
            return this.lootTable == lootTable;
        }
    }
}

