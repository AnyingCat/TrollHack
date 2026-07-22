/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math.intprovider;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.IntProviderType;
import net.minecraft.util.math.random.Random;

public class WeightedListIntProvider
extends IntProvider {
    public static final MapCodec<WeightedListIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)DataPool.createCodec(IntProvider.VALUE_CODEC).fieldOf("distribution")).forGetter(provider -> provider.weightedList)).apply((Applicative<WeightedListIntProvider, ?>)instance, WeightedListIntProvider::new));
    private final DataPool<IntProvider> weightedList;
    private final int min;
    private final int max;

    public WeightedListIntProvider(DataPool<IntProvider> weightedList) {
        this.weightedList = weightedList;
        List list = weightedList.getEntries();
        int i = Integer.MAX_VALUE;
        int j = Integer.MIN_VALUE;
        for (Weighted.Present present : list) {
            int k = ((IntProvider)present.data()).getMin();
            int l = ((IntProvider)present.data()).getMax();
            i = Math.min(i, k);
            j = Math.max(j, l);
        }
        this.min = i;
        this.max = j;
    }

    @Override
    public int get(Random random) {
        return this.weightedList.getDataOrEmpty(random).orElseThrow(IllegalStateException::new).get(random);
    }

    @Override
    public int getMin() {
        return this.min;
    }

    @Override
    public int getMax() {
        return this.max;
    }

    @Override
    public IntProviderType<?> getType() {
        return IntProviderType.WEIGHTED_LIST;
    }
}

