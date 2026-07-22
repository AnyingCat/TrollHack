/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.provider.score;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.provider.score.LootScoreProvider;
import net.minecraft.loot.provider.score.LootScoreProviderType;
import net.minecraft.loot.provider.score.LootScoreProviderTypes;
import net.minecraft.scoreboard.ScoreHolder;

public record FixedLootScoreProvider(String name) implements LootScoreProvider
{
    public static final MapCodec<FixedLootScoreProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("name")).forGetter(FixedLootScoreProvider::name)).apply((Applicative<FixedLootScoreProvider, ?>)instance, FixedLootScoreProvider::new));

    public static LootScoreProvider create(String name) {
        return new FixedLootScoreProvider(name);
    }

    @Override
    public LootScoreProviderType getType() {
        return LootScoreProviderTypes.FIXED;
    }

    @Override
    public ScoreHolder getScoreHolder(LootContext context) {
        return ScoreHolder.fromName(this.name);
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return ImmutableSet.of();
    }
}

