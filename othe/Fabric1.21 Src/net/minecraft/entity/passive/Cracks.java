/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import net.minecraft.item.ItemStack;

public class Cracks {
    public static final Cracks IRON_GOLEM = new Cracks(0.75f, 0.5f, 0.25f);
    public static final Cracks WOLF_ARMOR = new Cracks(0.95f, 0.69f, 0.32f);
    private final float lowCrackThreshold;
    private final float mediumCrackThreshold;
    private final float highCrackThreshold;

    private Cracks(float lowCrackThreshold, float mediumCrackThreshold, float highCrackThreshold) {
        this.lowCrackThreshold = lowCrackThreshold;
        this.mediumCrackThreshold = mediumCrackThreshold;
        this.highCrackThreshold = highCrackThreshold;
    }

    public CrackLevel getCrackLevel(float health) {
        if (health < this.highCrackThreshold) {
            return CrackLevel.HIGH;
        }
        if (health < this.mediumCrackThreshold) {
            return CrackLevel.MEDIUM;
        }
        if (health < this.lowCrackThreshold) {
            return CrackLevel.LOW;
        }
        return CrackLevel.NONE;
    }

    public CrackLevel getCrackLevel(ItemStack stack) {
        if (!stack.isDamageable()) {
            return CrackLevel.NONE;
        }
        return this.getCrackLevel(stack.getDamage(), stack.getMaxDamage());
    }

    public CrackLevel getCrackLevel(int currentDamage, int maxDamage) {
        return this.getCrackLevel((float)(maxDamage - currentDamage) / (float)maxDamage);
    }

    public static enum CrackLevel {
        NONE,
        LOW,
        MEDIUM,
        HIGH;

    }
}

