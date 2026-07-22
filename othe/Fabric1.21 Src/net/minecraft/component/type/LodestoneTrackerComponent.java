/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.poi.PointOfInterestTypes;

public record LodestoneTrackerComponent(Optional<GlobalPos> target, boolean tracked) {
    public static final Codec<LodestoneTrackerComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(GlobalPos.CODEC.optionalFieldOf("target").forGetter(LodestoneTrackerComponent::target), Codec.BOOL.optionalFieldOf("tracked", true).forGetter(LodestoneTrackerComponent::tracked)).apply((Applicative<LodestoneTrackerComponent, ?>)instance, LodestoneTrackerComponent::new));
    public static final PacketCodec<ByteBuf, LodestoneTrackerComponent> PACKET_CODEC = PacketCodec.tuple(GlobalPos.PACKET_CODEC.collect(PacketCodecs::optional), LodestoneTrackerComponent::target, PacketCodecs.BOOL, LodestoneTrackerComponent::tracked, LodestoneTrackerComponent::new);

    public LodestoneTrackerComponent forWorld(ServerWorld world) {
        if (!this.tracked || this.target.isEmpty()) {
            return this;
        }
        if (this.target.get().dimension() != world.getRegistryKey()) {
            return this;
        }
        BlockPos blockPos = this.target.get().pos();
        if (!world.isInBuildLimit(blockPos) || !world.getPointOfInterestStorage().hasTypeAt(PointOfInterestTypes.LODESTONE, blockPos)) {
            return new LodestoneTrackerComponent(Optional.empty(), true);
        }
        return this;
    }
}

