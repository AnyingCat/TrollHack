/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.placementmodifier;

import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;

public abstract class AbstractConditionalPlacementModifier
extends PlacementModifier {
    @Override
    public final Stream<BlockPos> getPositions(FeaturePlacementContext context, Random random, BlockPos pos) {
        if (this.shouldPlace(context, random, pos)) {
            return Stream.of(pos);
        }
        return Stream.of(new BlockPos[0]);
    }

    protected abstract boolean shouldPlace(FeaturePlacementContext var1, Random var2, BlockPos var3);
}

