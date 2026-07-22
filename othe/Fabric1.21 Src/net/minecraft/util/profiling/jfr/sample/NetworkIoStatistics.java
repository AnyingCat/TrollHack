/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.profiling.jfr.sample;

import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;

public final class NetworkIoStatistics<T> {
    private final PacketStatistics combinedStatistics;
    private final List<Pair<T, PacketStatistics>> topContributors;
    private final Duration duration;

    public NetworkIoStatistics(Duration duration, List<Pair<T, PacketStatistics>> packetsToStatistics) {
        this.duration = duration;
        this.combinedStatistics = packetsToStatistics.stream().map(Pair::getSecond).reduce(new PacketStatistics(0L, 0L), PacketStatistics::add);
        this.topContributors = packetsToStatistics.stream().sorted(Comparator.comparing(Pair::getSecond, PacketStatistics.COMPARATOR)).limit(10L).toList();
    }

    public double getCountPerSecond() {
        return (double)this.combinedStatistics.totalCount / (double)this.duration.getSeconds();
    }

    public double getBytesPerSecond() {
        return (double)this.combinedStatistics.totalSize / (double)this.duration.getSeconds();
    }

    public long getTotalCount() {
        return this.combinedStatistics.totalCount;
    }

    public long getTotalSize() {
        return this.combinedStatistics.totalSize;
    }

    public List<Pair<T, PacketStatistics>> getTopContributors() {
        return this.topContributors;
    }

    public record PacketStatistics(long totalCount, long totalSize) {
        static final Comparator<PacketStatistics> COMPARATOR = Comparator.comparing(PacketStatistics::totalSize).thenComparing(PacketStatistics::totalCount).reversed();

        PacketStatistics add(PacketStatistics statistics) {
            return new PacketStatistics(this.totalCount + statistics.totalCount, this.totalSize + statistics.totalSize);
        }

        public float getAverageSize() {
            return (float)this.totalSize / (float)this.totalCount;
        }
    }
}

