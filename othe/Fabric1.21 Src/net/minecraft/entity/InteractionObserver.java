/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityInteraction;

public interface InteractionObserver {
    public void onInteractionWith(EntityInteraction var1, Entity var2);
}

