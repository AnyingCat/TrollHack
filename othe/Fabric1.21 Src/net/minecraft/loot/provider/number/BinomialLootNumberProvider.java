/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.provider.number;

import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.util.math.random.Random;

public record BinomialLootNumberProvider(LootNumberProvider n, LootNumberProvider p) implements LootNumberProvider
{
    public static final MapCodec<BinomialLootNumberProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)LootNumberProviderTypes.CODEC.fieldOf("n")).forGetter(BinomialLootNumberProvider::n), ((MapCodec)LootNumberProviderTypes.CODEC.fieldOf("p")).forGetter(BinomialLootNumberProvider::p)).apply((Applicative<BinomialLootNumberProvider, ?>)instance, BinomialLootNumberProvider::new));

    @Override
    public LootNumberProviderType getType() {
        return LootNumberProviderTypes.BINOMIAL;
    }

    @Override
    public int nextInt(LootContext context) {
        int i = this.n.nextInt(context);
        float f = this.p.nextFloat(context);
        Random random = context.getRandom();
        int j = 0;
        for (int k = 0; k < i; ++k) {
            if (!(random.nextFloat() < f)) continue;
            ++j;
        }
        return j;
    }

    @Override
    public float nextFloat(LootContext context) {
        return this.nextInt(context);
    }

    public static BinomialLootNumberProvider create(int n, float p) {
        return new BinomialLootNumberProvider(ConstantLootNumberProvider.create(n), ConstantLootNumberProvider.create(p));
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return Sets.union(this.n.getRequiredParameters(), this.p.getRequiredParameters());
    }
}

