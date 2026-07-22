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
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class ChangedDimensionCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, RegistryKey<World> from, RegistryKey<World> to) {
        this.trigger(player, conditions -> conditions.matches(from, to));
    }

    public record Conditions(Optional<LootContextPredicate> player, Optional<RegistryKey<World>> from, Optional<RegistryKey<World>> to) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), RegistryKey.createCodec(RegistryKeys.WORLD).optionalFieldOf("from").forGetter(Conditions::from), RegistryKey.createCodec(RegistryKeys.WORLD).optionalFieldOf("to").forGetter(Conditions::to)).apply((Applicative<Conditions, ?>)instance, Conditions::new));

        public static AdvancementCriterion<Conditions> create() {
            return Criteria.CHANGED_DIMENSION.create(new Conditions(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static AdvancementCriterion<Conditions> create(RegistryKey<World> from, RegistryKey<World> to) {
            return Criteria.CHANGED_DIMENSION.create(new Conditions(Optional.empty(), Optional.of(from), Optional.of(to)));
        }

        public static AdvancementCriterion<Conditions> to(RegistryKey<World> to) {
            return Criteria.CHANGED_DIMENSION.create(new Conditions(Optional.empty(), Optional.empty(), Optional.of(to)));
        }

        public static AdvancementCriterion<Conditions> from(RegistryKey<World> from) {
            return Criteria.CHANGED_DIMENSION.create(new Conditions(Optional.empty(), Optional.of(from), Optional.empty()));
        }

        public boolean matches(RegistryKey<World> from, RegistryKey<World> to) {
            if (this.from.isPresent() && this.from.get() != from) {
                return false;
            }
            return !this.to.isPresent() || this.to.get() == to;
        }
    }
}

