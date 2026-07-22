/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;

@Environment(value=EnvType.CLIENT)
public interface Narratable {
    public void appendNarrations(NarrationMessageBuilder var1);
}

