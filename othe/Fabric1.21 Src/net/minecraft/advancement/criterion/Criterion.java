/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.advancement.criterion;

import com.mojang.serialization.Codec;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.CriterionConditions;

public interface Criterion<T extends CriterionConditions> {
    public void beginTrackingCondition(PlayerAdvancementTracker var1, ConditionsContainer<T> var2);

    public void endTrackingCondition(PlayerAdvancementTracker var1, ConditionsContainer<T> var2);

    public void endTracking(PlayerAdvancementTracker var1);

    public Codec<T> getConditionsCodec();

    default public AdvancementCriterion<T> create(T conditions) {
        return new AdvancementCriterion<T>(this, conditions);
    }

    public record ConditionsContainer<T extends CriterionConditions>(T conditions, AdvancementEntry advancement, String id) {
        public void grant(PlayerAdvancementTracker tracker) {
            tracker.grantCriterion(this.advancement, this.id);
        }
    }
}

