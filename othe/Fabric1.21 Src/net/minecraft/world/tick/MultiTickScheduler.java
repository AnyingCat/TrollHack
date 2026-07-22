/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.tick;

import java.util.function.Function;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.tick.BasicTickScheduler;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.QueryableTickScheduler;

public class MultiTickScheduler<T>
implements QueryableTickScheduler<T> {
    private final Function<BlockPos, BasicTickScheduler<T>> mapper;

    public MultiTickScheduler(Function<BlockPos, BasicTickScheduler<T>> mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean isQueued(BlockPos pos, T type) {
        return this.mapper.apply(pos).isQueued(pos, type);
    }

    @Override
    public void scheduleTick(OrderedTick<T> orderedTick) {
        this.mapper.apply(orderedTick.pos()).scheduleTick(orderedTick);
    }

    @Override
    public boolean isTicking(BlockPos pos, T type) {
        return false;
    }

    @Override
    public int getTickCount() {
        return 0;
    }
}

