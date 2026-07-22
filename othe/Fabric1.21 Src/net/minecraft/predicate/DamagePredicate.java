/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.DamageSourcePredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

public record DamagePredicate(NumberRange.DoubleRange dealt, NumberRange.DoubleRange taken, Optional<EntityPredicate> sourceEntity, Optional<Boolean> blocked, Optional<DamageSourcePredicate> type) {
    public static final Codec<DamagePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(NumberRange.DoubleRange.CODEC.optionalFieldOf("dealt", NumberRange.DoubleRange.ANY).forGetter(DamagePredicate::dealt), NumberRange.DoubleRange.CODEC.optionalFieldOf("taken", NumberRange.DoubleRange.ANY).forGetter(DamagePredicate::taken), EntityPredicate.CODEC.optionalFieldOf("source_entity").forGetter(DamagePredicate::sourceEntity), Codec.BOOL.optionalFieldOf("blocked").forGetter(DamagePredicate::blocked), DamageSourcePredicate.CODEC.optionalFieldOf("type").forGetter(DamagePredicate::type)).apply((Applicative<DamagePredicate, ?>)instance, DamagePredicate::new));

    public boolean test(ServerPlayerEntity player, DamageSource source, float dealt, float taken, boolean blocked) {
        if (!this.dealt.test(dealt)) {
            return false;
        }
        if (!this.taken.test(taken)) {
            return false;
        }
        if (this.sourceEntity.isPresent() && !this.sourceEntity.get().test(player, source.getAttacker())) {
            return false;
        }
        if (this.blocked.isPresent() && this.blocked.get() != blocked) {
            return false;
        }
        return !this.type.isPresent() || this.type.get().test(player, source);
    }

    public static class Builder {
        private NumberRange.DoubleRange dealt = NumberRange.DoubleRange.ANY;
        private NumberRange.DoubleRange taken = NumberRange.DoubleRange.ANY;
        private Optional<EntityPredicate> sourceEntity = Optional.empty();
        private Optional<Boolean> blocked = Optional.empty();
        private Optional<DamageSourcePredicate> type = Optional.empty();

        public static Builder create() {
            return new Builder();
        }

        public Builder dealt(NumberRange.DoubleRange dealt) {
            this.dealt = dealt;
            return this;
        }

        public Builder taken(NumberRange.DoubleRange taken) {
            this.taken = taken;
            return this;
        }

        public Builder sourceEntity(EntityPredicate sourceEntity) {
            this.sourceEntity = Optional.of(sourceEntity);
            return this;
        }

        public Builder blocked(Boolean blocked) {
            this.blocked = Optional.of(blocked);
            return this;
        }

        public Builder type(DamageSourcePredicate type) {
            this.type = Optional.of(type);
            return this;
        }

        public Builder type(DamageSourcePredicate.Builder builder) {
            this.type = Optional.of(builder.build());
            return this;
        }

        public DamagePredicate build() {
            return new DamagePredicate(this.dealt, this.taken, this.sourceEntity, this.blocked, this.type);
        }
    }
}

