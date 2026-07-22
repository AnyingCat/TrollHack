/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.condition;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.operator.BoundedIntUnaryOperator;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;

public record EntityScoresLootCondition(Map<String, BoundedIntUnaryOperator> scores, LootContext.EntityTarget entity) implements LootCondition
{
    public static final MapCodec<EntityScoresLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.unboundedMap(Codec.STRING, BoundedIntUnaryOperator.CODEC).fieldOf("scores")).forGetter(EntityScoresLootCondition::scores), ((MapCodec)LootContext.EntityTarget.CODEC.fieldOf("entity")).forGetter(EntityScoresLootCondition::entity)).apply((Applicative<EntityScoresLootCondition, ?>)instance, EntityScoresLootCondition::new));

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.ENTITY_SCORES;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return Stream.concat(Stream.of(this.entity.getParameter()), this.scores.values().stream().flatMap(operator -> operator.getRequiredParameters().stream())).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.get(this.entity.getParameter());
        if (entity == null) {
            return false;
        }
        ServerScoreboard scoreboard = lootContext.getWorld().getScoreboard();
        for (Map.Entry<String, BoundedIntUnaryOperator> entry : this.scores.entrySet()) {
            if (this.entityScoreIsInRange(lootContext, entity, scoreboard, entry.getKey(), entry.getValue())) continue;
            return false;
        }
        return true;
    }

    protected boolean entityScoreIsInRange(LootContext context, Entity entity, Scoreboard scoreboard, String objectiveName, BoundedIntUnaryOperator range) {
        ScoreboardObjective scoreboardObjective = scoreboard.getNullableObjective(objectiveName);
        if (scoreboardObjective == null) {
            return false;
        }
        ReadableScoreboardScore readableScoreboardScore = scoreboard.getScore(entity, scoreboardObjective);
        if (readableScoreboardScore == null) {
            return false;
        }
        return range.test(context, readableScoreboardScore.getScore());
    }

    public static Builder create(LootContext.EntityTarget target) {
        return new Builder(target);
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }

    public static class Builder
    implements LootCondition.Builder {
        private final ImmutableMap.Builder<String, BoundedIntUnaryOperator> scores = ImmutableMap.builder();
        private final LootContext.EntityTarget target;

        public Builder(LootContext.EntityTarget target) {
            this.target = target;
        }

        public Builder score(String name, BoundedIntUnaryOperator value) {
            this.scores.put(name, value);
            return this;
        }

        @Override
        public LootCondition build() {
            return new EntityScoresLootCondition(this.scores.build(), this.target);
        }
    }
}

