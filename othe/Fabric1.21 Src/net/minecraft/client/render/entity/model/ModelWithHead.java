/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;

/**
 * Represents a model with a head.
 */
@Environment(value=EnvType.CLIENT)
public interface ModelWithHead {
    /**
     * Gets the head model part.
     * 
     * @return the head
     */
    public ModelPart getHead();
}

