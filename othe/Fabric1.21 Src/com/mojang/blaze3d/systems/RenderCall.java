/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package com.mojang.blaze3d.systems;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public interface RenderCall {
    public void execute();
}

