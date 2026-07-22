/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.util;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public record ModelIdentifier(Identifier id, String variant) {
    public static final String INVENTORY_VARIANT = "inventory";

    public ModelIdentifier {
        variant = ModelIdentifier.toLowerCase(variant);
    }

    public static ModelIdentifier ofVanilla(String path, String variant) {
        return new ModelIdentifier(Identifier.ofVanilla(path), variant);
    }

    public static ModelIdentifier ofInventoryVariant(Identifier id) {
        return new ModelIdentifier(id, INVENTORY_VARIANT);
    }

    private static String toLowerCase(String string) {
        return string.toLowerCase(Locale.ROOT);
    }

    public String getVariant() {
        return this.variant;
    }

    @Override
    public String toString() {
        return String.valueOf(this.id) + "#" + this.variant;
    }
}

