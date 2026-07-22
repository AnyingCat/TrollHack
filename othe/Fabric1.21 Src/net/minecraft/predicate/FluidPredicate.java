/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public record FluidPredicate(Optional<RegistryEntryList<Fluid>> fluids, Optional<StatePredicate> state) {
    public static final Codec<FluidPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(RegistryCodecs.entryList(RegistryKeys.FLUID).optionalFieldOf("fluids").forGetter(FluidPredicate::fluids), StatePredicate.CODEC.optionalFieldOf("state").forGetter(FluidPredicate::state)).apply((Applicative<FluidPredicate, ?>)instance, FluidPredicate::new));

    public boolean test(ServerWorld world, BlockPos pos) {
        if (!world.canSetBlock(pos)) {
            return false;
        }
        FluidState fluidState = world.getFluidState(pos);
        if (this.fluids.isPresent() && !fluidState.isIn(this.fluids.get())) {
            return false;
        }
        return !this.state.isPresent() || this.state.get().test(fluidState);
    }

    public static class Builder {
        private Optional<RegistryEntryList<Fluid>> tag = Optional.empty();
        private Optional<StatePredicate> state = Optional.empty();

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder fluid(Fluid fluid) {
            this.tag = Optional.of(RegistryEntryList.of(fluid.getRegistryEntry()));
            return this;
        }

        public Builder tag(RegistryEntryList<Fluid> tag) {
            this.tag = Optional.of(tag);
            return this;
        }

        public Builder state(StatePredicate state) {
            this.state = Optional.of(state);
            return this;
        }

        public FluidPredicate build() {
            return new FluidPredicate(this.tag, this.state);
        }
    }
}

