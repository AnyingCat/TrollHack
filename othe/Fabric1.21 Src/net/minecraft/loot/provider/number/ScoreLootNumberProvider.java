/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.provider.number;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.loot.provider.score.ContextLootScoreProvider;
import net.minecraft.loot.provider.score.LootScoreProvider;
import net.minecraft.loot.provider.score.LootScoreProviderTypes;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;

public record ScoreLootNumberProvider(LootScoreProvider target, String score, float scale) implements LootNumberProvider
{
    public static final MapCodec<ScoreLootNumberProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)LootScoreProviderTypes.CODEC.fieldOf("target")).forGetter(ScoreLootNumberProvider::target), ((MapCodec)Codec.STRING.fieldOf("score")).forGetter(ScoreLootNumberProvider::score), ((MapCodec)Codec.FLOAT.fieldOf("scale")).orElse(Float.valueOf(1.0f)).forGetter(ScoreLootNumberProvider::scale)).apply((Applicative<ScoreLootNumberProvider, ?>)instance, ScoreLootNumberProvider::new));

    @Override
    public LootNumberProviderType getType() {
        return LootNumberProviderTypes.SCORE;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return this.target.getRequiredParameters();
    }

    public static ScoreLootNumberProvider create(LootContext.EntityTarget target, String score) {
        return ScoreLootNumberProvider.create(target, score, 1.0f);
    }

    public static ScoreLootNumberProvider create(LootContext.EntityTarget target, String score, float scale) {
        return new ScoreLootNumberProvider(ContextLootScoreProvider.create(target), score, scale);
    }

    @Override
    public float nextFloat(LootContext context) {
        ScoreHolder scoreHolder = this.target.getScoreHolder(context);
        if (scoreHolder == null) {
            return 0.0f;
        }
        ServerScoreboard scoreboard = context.getWorld().getScoreboard();
        ScoreboardObjective scoreboardObjective = scoreboard.getNullableObjective(this.score);
        if (scoreboardObjective == null) {
            return 0.0f;
        }
        ReadableScoreboardScore readableScoreboardScore = scoreboard.getScore(scoreHolder, scoreboardObjective);
        if (readableScoreboardScore == null) {
            return 0.0f;
        }
        return (float)readableScoreboardScore.getScore() * this.scale;
    }
}

