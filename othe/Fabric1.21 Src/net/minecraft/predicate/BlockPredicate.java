/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public record BlockPredicate(Optional<RegistryEntryList<Block>> blocks, Optional<StatePredicate> state, Optional<NbtPredicate> nbt) {
    public static final Codec<BlockPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(RegistryCodecs.entryList(RegistryKeys.BLOCK).optionalFieldOf("blocks").forGetter(BlockPredicate::blocks), StatePredicate.CODEC.optionalFieldOf("state").forGetter(BlockPredicate::state), NbtPredicate.CODEC.optionalFieldOf("nbt").forGetter(BlockPredicate::nbt)).apply((Applicative<BlockPredicate, ?>)instance, BlockPredicate::new));
    public static final PacketCodec<RegistryByteBuf, BlockPredicate> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.optional(PacketCodecs.registryEntryList(RegistryKeys.BLOCK)), BlockPredicate::blocks, PacketCodecs.optional(StatePredicate.PACKET_CODEC), BlockPredicate::state, PacketCodecs.optional(NbtPredicate.PACKET_CODEC), BlockPredicate::nbt, BlockPredicate::new);

    public boolean test(ServerWorld world, BlockPos pos) {
        if (!world.canSetBlock(pos)) {
            return false;
        }
        if (!this.testState(world.getBlockState(pos))) {
            return false;
        }
        return !this.nbt.isPresent() || BlockPredicate.testBlockEntity(world, world.getBlockEntity(pos), this.nbt.get());
    }

    public boolean test(CachedBlockPosition pos) {
        if (!this.testState(pos.getBlockState())) {
            return false;
        }
        return !this.nbt.isPresent() || BlockPredicate.testBlockEntity(pos.getWorld(), pos.getBlockEntity(), this.nbt.get());
    }

    private boolean testState(BlockState state) {
        if (this.blocks.isPresent() && !state.isIn(this.blocks.get())) {
            return false;
        }
        return !this.state.isPresent() || this.state.get().test(state);
    }

    private static boolean testBlockEntity(WorldView world, @Nullable BlockEntity blockEntity, NbtPredicate nbtPredicate) {
        return blockEntity != null && nbtPredicate.test(blockEntity.createNbtWithIdentifyingData(world.getRegistryManager()));
    }

    public boolean hasNbt() {
        return this.nbt.isPresent();
    }

    public static class Builder {
        private Optional<RegistryEntryList<Block>> blocks = Optional.empty();
        private Optional<StatePredicate> state = Optional.empty();
        private Optional<NbtPredicate> nbt = Optional.empty();

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder blocks(Block ... blocks) {
            this.blocks = Optional.of(RegistryEntryList.of(Block::getRegistryEntry, blocks));
            return this;
        }

        public Builder blocks(Collection<Block> blocks) {
            this.blocks = Optional.of(RegistryEntryList.of(Block::getRegistryEntry, blocks));
            return this;
        }

        public Builder tag(TagKey<Block> tag) {
            this.blocks = Optional.of(Registries.BLOCK.getOrCreateEntryList(tag));
            return this;
        }

        public Builder nbt(NbtCompound nbt) {
            this.nbt = Optional.of(new NbtPredicate(nbt));
            return this;
        }

        public Builder state(StatePredicate.Builder state) {
            this.state = state.build();
            return this;
        }

        public BlockPredicate build() {
            return new BlockPredicate(this.blocks, this.state, this.nbt);
        }
    }
}

