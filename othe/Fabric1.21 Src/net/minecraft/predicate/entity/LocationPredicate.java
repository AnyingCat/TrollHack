/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate.entity;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.block.CampfireBlock;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.predicate.FluidPredicate;
import net.minecraft.predicate.LightPredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.Structure;

public record LocationPredicate(Optional<PositionRange> position, Optional<RegistryEntryList<Biome>> biomes, Optional<RegistryEntryList<Structure>> structures, Optional<RegistryKey<World>> dimension, Optional<Boolean> smokey, Optional<LightPredicate> light, Optional<BlockPredicate> block, Optional<FluidPredicate> fluid, Optional<Boolean> canSeeSky) {
    public static final Codec<LocationPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(PositionRange.CODEC.optionalFieldOf("position").forGetter(LocationPredicate::position), RegistryCodecs.entryList(RegistryKeys.BIOME).optionalFieldOf("biomes").forGetter(LocationPredicate::biomes), RegistryCodecs.entryList(RegistryKeys.STRUCTURE).optionalFieldOf("structures").forGetter(LocationPredicate::structures), RegistryKey.createCodec(RegistryKeys.WORLD).optionalFieldOf("dimension").forGetter(LocationPredicate::dimension), Codec.BOOL.optionalFieldOf("smokey").forGetter(LocationPredicate::smokey), LightPredicate.CODEC.optionalFieldOf("light").forGetter(LocationPredicate::light), BlockPredicate.CODEC.optionalFieldOf("block").forGetter(LocationPredicate::block), FluidPredicate.CODEC.optionalFieldOf("fluid").forGetter(LocationPredicate::fluid), Codec.BOOL.optionalFieldOf("can_see_sky").forGetter(LocationPredicate::canSeeSky)).apply((Applicative<LocationPredicate, ?>)instance, LocationPredicate::new));

    public boolean test(ServerWorld world, double x, double y, double z) {
        if (this.position.isPresent() && !this.position.get().test(x, y, z)) {
            return false;
        }
        if (this.dimension.isPresent() && this.dimension.get() != world.getRegistryKey()) {
            return false;
        }
        BlockPos blockPos = BlockPos.ofFloored(x, y, z);
        boolean bl = world.canSetBlock(blockPos);
        if (!(!this.biomes.isPresent() || bl && this.biomes.get().contains(world.getBiome(blockPos)))) {
            return false;
        }
        if (!(!this.structures.isPresent() || bl && world.getStructureAccessor().getStructureContaining(blockPos, this.structures.get()).hasChildren())) {
            return false;
        }
        if (this.smokey.isPresent() && (!bl || this.smokey.get() != CampfireBlock.isLitCampfireInRange(world, blockPos))) {
            return false;
        }
        if (this.light.isPresent() && !this.light.get().test(world, blockPos)) {
            return false;
        }
        if (this.block.isPresent() && !this.block.get().test(world, blockPos)) {
            return false;
        }
        if (this.fluid.isPresent() && !this.fluid.get().test(world, blockPos)) {
            return false;
        }
        return !this.canSeeSky.isPresent() || this.canSeeSky.get().booleanValue() == world.isSkyVisible(blockPos);
    }

    record PositionRange(NumberRange.DoubleRange x, NumberRange.DoubleRange y, NumberRange.DoubleRange z) {
        public static final Codec<PositionRange> CODEC = RecordCodecBuilder.create(instance -> instance.group(NumberRange.DoubleRange.CODEC.optionalFieldOf("x", NumberRange.DoubleRange.ANY).forGetter(PositionRange::x), NumberRange.DoubleRange.CODEC.optionalFieldOf("y", NumberRange.DoubleRange.ANY).forGetter(PositionRange::y), NumberRange.DoubleRange.CODEC.optionalFieldOf("z", NumberRange.DoubleRange.ANY).forGetter(PositionRange::z)).apply((Applicative<PositionRange, ?>)instance, PositionRange::new));

        static Optional<PositionRange> create(NumberRange.DoubleRange x, NumberRange.DoubleRange y, NumberRange.DoubleRange z) {
            if (x.isDummy() && y.isDummy() && z.isDummy()) {
                return Optional.empty();
            }
            return Optional.of(new PositionRange(x, y, z));
        }

        public boolean test(double x, double y, double z) {
            return this.x.test(x) && this.y.test(y) && this.z.test(z);
        }
    }

    public static class Builder {
        private NumberRange.DoubleRange x = NumberRange.DoubleRange.ANY;
        private NumberRange.DoubleRange y = NumberRange.DoubleRange.ANY;
        private NumberRange.DoubleRange z = NumberRange.DoubleRange.ANY;
        private Optional<RegistryEntryList<Biome>> biome = Optional.empty();
        private Optional<RegistryEntryList<Structure>> feature = Optional.empty();
        private Optional<RegistryKey<World>> dimension = Optional.empty();
        private Optional<Boolean> smokey = Optional.empty();
        private Optional<LightPredicate> light = Optional.empty();
        private Optional<BlockPredicate> block = Optional.empty();
        private Optional<FluidPredicate> fluid = Optional.empty();
        private Optional<Boolean> canSeeSky = Optional.empty();

        public static Builder create() {
            return new Builder();
        }

        public static Builder createBiome(RegistryEntry<Biome> biome) {
            return Builder.create().biome(RegistryEntryList.of(biome));
        }

        public static Builder createDimension(RegistryKey<World> dimension) {
            return Builder.create().dimension(dimension);
        }

        public static Builder createStructure(RegistryEntry<Structure> structure) {
            return Builder.create().structure(RegistryEntryList.of(structure));
        }

        public static Builder createY(NumberRange.DoubleRange y) {
            return Builder.create().y(y);
        }

        public Builder x(NumberRange.DoubleRange x) {
            this.x = x;
            return this;
        }

        public Builder y(NumberRange.DoubleRange y) {
            this.y = y;
            return this;
        }

        public Builder z(NumberRange.DoubleRange z) {
            this.z = z;
            return this;
        }

        public Builder biome(RegistryEntryList<Biome> biome) {
            this.biome = Optional.of(biome);
            return this;
        }

        public Builder structure(RegistryEntryList<Structure> structure) {
            this.feature = Optional.of(structure);
            return this;
        }

        public Builder dimension(RegistryKey<World> dimension) {
            this.dimension = Optional.of(dimension);
            return this;
        }

        public Builder light(LightPredicate.Builder light) {
            this.light = Optional.of(light.build());
            return this;
        }

        public Builder block(BlockPredicate.Builder block) {
            this.block = Optional.of(block.build());
            return this;
        }

        public Builder fluid(FluidPredicate.Builder fluid) {
            this.fluid = Optional.of(fluid.build());
            return this;
        }

        public Builder smokey(boolean smokey) {
            this.smokey = Optional.of(smokey);
            return this;
        }

        public Builder canSeeSky(boolean canSeeSky) {
            this.canSeeSky = Optional.of(canSeeSky);
            return this;
        }

        public LocationPredicate build() {
            Optional<PositionRange> optional = PositionRange.create(this.x, this.y, this.z);
            return new LocationPredicate(optional, this.biome, this.feature, this.dimension, this.smokey, this.light, this.block, this.fluid, this.canSeeSky);
        }
    }
}

