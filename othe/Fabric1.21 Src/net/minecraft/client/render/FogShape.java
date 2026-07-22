/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public enum FogShape {
    SPHERE(0),
    CYLINDER(1);

    private final int id;

    private FogShape(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}

