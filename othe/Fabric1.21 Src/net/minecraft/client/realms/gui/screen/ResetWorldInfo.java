/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.realms.gui.screen;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.gui.screen.RealmsWorldGeneratorType;

@Environment(value=EnvType.CLIENT)
public record ResetWorldInfo(String seed, RealmsWorldGeneratorType levelType, boolean generateStructures, Set<String> experiments) {
}

