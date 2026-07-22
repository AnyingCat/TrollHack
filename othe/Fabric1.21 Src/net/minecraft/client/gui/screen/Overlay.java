/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Drawable;

@Environment(value=EnvType.CLIENT)
public abstract class Overlay
implements Drawable {
    public boolean pausesGame() {
        return true;
    }
}

