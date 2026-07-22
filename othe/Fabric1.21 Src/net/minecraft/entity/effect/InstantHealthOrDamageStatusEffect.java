/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.effect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import org.jetbrains.annotations.Nullable;

class InstantHealthOrDamageStatusEffect
extends InstantStatusEffect {
    private final boolean damage;

    public InstantHealthOrDamageStatusEffect(StatusEffectCategory category, int color, boolean damage) {
        super(category, color);
        this.damage = damage;
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (this.damage == entity.hasInvertedHealingAndHarm()) {
            entity.heal(Math.max(4 << amplifier, 0));
        } else {
            entity.damage(entity.getDamageSources().magic(), 6 << amplifier);
        }
        return true;
    }

    @Override
    public void applyInstantEffect(@Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {
        if (this.damage == target.hasInvertedHealingAndHarm()) {
            int i = (int)(proximity * (double)(4 << amplifier) + 0.5);
            target.heal(i);
        } else {
            int i = (int)(proximity * (double)(6 << amplifier) + 0.5);
            if (source == null) {
                target.damage(target.getDamageSources().magic(), i);
            } else {
                target.damage(target.getDamageSources().indirectMagic(source, attacker), i);
            }
        }
    }
}

