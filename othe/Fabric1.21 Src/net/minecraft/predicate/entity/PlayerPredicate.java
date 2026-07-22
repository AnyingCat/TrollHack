/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate.entity;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.CriterionProgress;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.EntitySubPredicate;
import net.minecraft.predicate.entity.EntitySubPredicateTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerRecipeBook;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.StatType;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameModeList;
import org.jetbrains.annotations.Nullable;

public record PlayerPredicate(NumberRange.IntRange experienceLevel, GameModeList gameMode, List<StatMatcher<?>> stats, Object2BooleanMap<Identifier> recipes, Map<Identifier, AdvancementPredicate> advancements, Optional<EntityPredicate> lookingAt) implements EntitySubPredicate
{
    public static final int LOOKING_AT_DISTANCE = 100;
    public static final MapCodec<PlayerPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(NumberRange.IntRange.CODEC.optionalFieldOf("level", NumberRange.IntRange.ANY).forGetter(PlayerPredicate::experienceLevel), GameModeList.CODEC.optionalFieldOf("gamemode", GameModeList.ALL).forGetter(PlayerPredicate::gameMode), StatMatcher.CODEC.listOf().optionalFieldOf("stats", List.of()).forGetter(PlayerPredicate::stats), Codecs.object2BooleanMap(Identifier.CODEC).optionalFieldOf("recipes", Object2BooleanMaps.emptyMap()).forGetter(PlayerPredicate::recipes), Codec.unboundedMap(Identifier.CODEC, AdvancementPredicate.CODEC).optionalFieldOf("advancements", Map.of()).forGetter(PlayerPredicate::advancements), EntityPredicate.CODEC.optionalFieldOf("looking_at").forGetter(PlayerPredicate::lookingAt)).apply((Applicative<PlayerPredicate, ?>)instance, PlayerPredicate::new));

    @Override
    public boolean test(Entity entity, ServerWorld world, @Nullable Vec3d pos) {
        if (!(entity instanceof ServerPlayerEntity)) {
            return false;
        }
        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
        if (!this.experienceLevel.test(serverPlayerEntity.experienceLevel)) {
            return false;
        }
        if (!this.gameMode.contains(serverPlayerEntity.interactionManager.getGameMode())) {
            return false;
        }
        ServerStatHandler statHandler = serverPlayerEntity.getStatHandler();
        for (StatMatcher<?> statMatcher : this.stats) {
            if (statMatcher.test(statHandler)) continue;
            return false;
        }
        ServerRecipeBook recipeBook = serverPlayerEntity.getRecipeBook();
        for (Object2BooleanMap.Entry entry : this.recipes.object2BooleanEntrySet()) {
            if (recipeBook.contains((Identifier)entry.getKey()) == entry.getBooleanValue()) continue;
            return false;
        }
        if (!this.advancements.isEmpty()) {
            PlayerAdvancementTracker playerAdvancementTracker = serverPlayerEntity.getAdvancementTracker();
            ServerAdvancementLoader serverAdvancementLoader = serverPlayerEntity.getServer().getAdvancementLoader();
            for (Map.Entry<Identifier, AdvancementPredicate> entry2 : this.advancements.entrySet()) {
                AdvancementEntry advancementEntry = serverAdvancementLoader.get(entry2.getKey());
                if (advancementEntry != null && entry2.getValue().test(playerAdvancementTracker.getProgress(advancementEntry))) continue;
                return false;
            }
        }
        if (this.lookingAt.isPresent()) {
            Vec3d vec3d = serverPlayerEntity.getEyePos();
            Vec3d vec3d2 = serverPlayerEntity.getRotationVec(1.0f);
            Vec3d vec3d3 = vec3d.add(vec3d2.x * 100.0, vec3d2.y * 100.0, vec3d2.z * 100.0);
            EntityHitResult entityHitResult = ProjectileUtil.getEntityCollision(serverPlayerEntity.getWorld(), serverPlayerEntity, vec3d, vec3d3, new Box(vec3d, vec3d3).expand(1.0), hitEntity -> !hitEntity.isSpectator(), 0.0f);
            if (entityHitResult == null || entityHitResult.getType() != HitResult.Type.ENTITY) {
                return false;
            }
            Entity entity2 = entityHitResult.getEntity();
            if (!this.lookingAt.get().test(serverPlayerEntity, entity2) || !serverPlayerEntity.canSee(entity2)) {
                return false;
            }
        }
        return true;
    }

    public MapCodec<PlayerPredicate> getCodec() {
        return EntitySubPredicateTypes.PLAYER;
    }

    record StatMatcher<T>(StatType<T> type, RegistryEntry<T> value, NumberRange.IntRange range, Supplier<Stat<T>> stat) {
        public static final Codec<StatMatcher<?>> CODEC = Registries.STAT_TYPE.getCodec().dispatch(StatMatcher::type, StatMatcher::createCodec);

        public StatMatcher(StatType<T> type, RegistryEntry<T> value, NumberRange.IntRange range) {
            this(type, value, range, Suppliers.memoize(() -> type.getOrCreateStat(value.value())));
        }

        private static <T> MapCodec<StatMatcher<T>> createCodec(StatType<T> type) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)type.getRegistry().getEntryCodec().fieldOf("stat")).forGetter(StatMatcher::value), NumberRange.IntRange.CODEC.optionalFieldOf("value", NumberRange.IntRange.ANY).forGetter(StatMatcher::range)).apply((Applicative<StatMatcher, ?>)instance, (registryEntry, intRange) -> new StatMatcher(type, registryEntry, (NumberRange.IntRange)intRange)));
        }

        public boolean test(StatHandler statHandler) {
            return this.range.test(statHandler.getStat(this.stat.get()));
        }
    }

    static interface AdvancementPredicate
    extends Predicate<AdvancementProgress> {
        public static final Codec<AdvancementPredicate> CODEC = Codec.either(CompletedAdvancementPredicate.CODEC, AdvancementCriteriaPredicate.CODEC).xmap(Either::unwrap, predicate -> {
            if (predicate instanceof CompletedAdvancementPredicate) {
                CompletedAdvancementPredicate completedAdvancementPredicate = (CompletedAdvancementPredicate)predicate;
                return Either.left(completedAdvancementPredicate);
            }
            if (predicate instanceof AdvancementCriteriaPredicate) {
                AdvancementCriteriaPredicate advancementCriteriaPredicate = (AdvancementCriteriaPredicate)predicate;
                return Either.right(advancementCriteriaPredicate);
            }
            throw new UnsupportedOperationException();
        });
    }

    public static class Builder {
        private NumberRange.IntRange experienceLevel = NumberRange.IntRange.ANY;
        private GameModeList gameMode = GameModeList.ALL;
        private final ImmutableList.Builder<StatMatcher<?>> stats = ImmutableList.builder();
        private final Object2BooleanMap<Identifier> recipes = new Object2BooleanOpenHashMap<Identifier>();
        private final Map<Identifier, AdvancementPredicate> advancements = Maps.newHashMap();
        private Optional<EntityPredicate> lookingAt = Optional.empty();

        public static Builder create() {
            return new Builder();
        }

        public Builder experienceLevel(NumberRange.IntRange experienceLevel) {
            this.experienceLevel = experienceLevel;
            return this;
        }

        public <T> Builder stat(StatType<T> statType, RegistryEntry.Reference<T> value, NumberRange.IntRange range) {
            this.stats.add((Object)new StatMatcher<T>(statType, value, range));
            return this;
        }

        public Builder recipe(Identifier id, boolean unlocked) {
            this.recipes.put(id, unlocked);
            return this;
        }

        public Builder gameMode(GameModeList gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        public Builder lookingAt(EntityPredicate.Builder lookingAt) {
            this.lookingAt = Optional.of(lookingAt.build());
            return this;
        }

        public Builder advancement(Identifier id, boolean done) {
            this.advancements.put(id, new CompletedAdvancementPredicate(done));
            return this;
        }

        public Builder advancement(Identifier id, Map<String, Boolean> criteria) {
            this.advancements.put(id, new AdvancementCriteriaPredicate(new Object2BooleanOpenHashMap<String>(criteria)));
            return this;
        }

        public PlayerPredicate build() {
            return new PlayerPredicate(this.experienceLevel, this.gameMode, (List<StatMatcher<?>>)((Object)this.stats.build()), this.recipes, this.advancements, this.lookingAt);
        }
    }

    record AdvancementCriteriaPredicate(Object2BooleanMap<String> criteria) implements AdvancementPredicate
    {
        public static final Codec<AdvancementCriteriaPredicate> CODEC = Codecs.object2BooleanMap(Codec.STRING).xmap(AdvancementCriteriaPredicate::new, AdvancementCriteriaPredicate::criteria);

        @Override
        public boolean test(AdvancementProgress advancementProgress) {
            for (Object2BooleanMap.Entry entry : this.criteria.object2BooleanEntrySet()) {
                CriterionProgress criterionProgress = advancementProgress.getCriterionProgress((String)entry.getKey());
                if (criterionProgress != null && criterionProgress.isObtained() == entry.getBooleanValue()) continue;
                return false;
            }
            return true;
        }

        @Override
        public /* synthetic */ boolean test(Object progress) {
            return this.test((AdvancementProgress)progress);
        }
    }

    record CompletedAdvancementPredicate(boolean done) implements AdvancementPredicate
    {
        public static final Codec<CompletedAdvancementPredicate> CODEC = Codec.BOOL.xmap(CompletedAdvancementPredicate::new, CompletedAdvancementPredicate::done);

        @Override
        public boolean test(AdvancementProgress advancementProgress) {
            return advancementProgress.isDone() == this.done;
        }

        @Override
        public /* synthetic */ boolean test(Object progress) {
            return this.test((AdvancementProgress)progress);
        }
    }
}

