/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.advancement.criterion;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.entity.LootContextPredicateValidator;
import net.minecraft.server.network.ServerPlayerEntity;

public abstract class AbstractCriterion<T extends Conditions>
implements Criterion<T> {
    private final Map<PlayerAdvancementTracker, Set<Criterion.ConditionsContainer<T>>> progressions = Maps.newIdentityHashMap();

    @Override
    public final void beginTrackingCondition(PlayerAdvancementTracker manager, Criterion.ConditionsContainer<T> conditions) {
        this.progressions.computeIfAbsent(manager, managerx -> Sets.newHashSet()).add(conditions);
    }

    @Override
    public final void endTrackingCondition(PlayerAdvancementTracker manager, Criterion.ConditionsContainer<T> conditions) {
        Set<Criterion.ConditionsContainer<T>> set = this.progressions.get(manager);
        if (set != null) {
            set.remove(conditions);
            if (set.isEmpty()) {
                this.progressions.remove(manager);
            }
        }
    }

    @Override
    public final void endTracking(PlayerAdvancementTracker tracker) {
        this.progressions.remove(tracker);
    }

    protected void trigger(ServerPlayerEntity player, Predicate<T> predicate) {
        PlayerAdvancementTracker playerAdvancementTracker = player.getAdvancementTracker();
        Set<Criterion.ConditionsContainer<T>> set = this.progressions.get(playerAdvancementTracker);
        if (set == null || set.isEmpty()) {
            return;
        }
        LootContext lootContext = EntityPredicate.createAdvancementEntityLootContext(player, player);
        ArrayList<Criterion.ConditionsContainer<T>> list = null;
        for (Criterion.ConditionsContainer<T> conditionsContainer : set) {
            Optional<LootContextPredicate> optional;
            Conditions conditions = (Conditions)conditionsContainer.conditions();
            if (!predicate.test(conditions) || !(optional = conditions.player()).isEmpty() && !optional.get().test(lootContext)) continue;
            if (list == null) {
                list = Lists.newArrayList();
            }
            list.add(conditionsContainer);
        }
        if (list != null) {
            for (Criterion.ConditionsContainer<Object> conditionsContainer : list) {
                conditionsContainer.grant(playerAdvancementTracker);
            }
        }
    }

    public static interface Conditions
    extends CriterionConditions {
        @Override
        default public void validate(LootContextPredicateValidator validator) {
            validator.validateEntityPredicate(this.player(), ".player");
        }

        public Optional<LootContextPredicate> player();
    }
}

