/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Vector2ic;

@Environment(value=EnvType.CLIENT)
public interface TooltipPositioner {
    public Vector2ic getPosition(int var1, int var2, int var3, int var4, int var5, int var6);
}

