/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record SkinTextures(Identifier texture, @Nullable String textureUrl, @Nullable Identifier capeTexture, @Nullable Identifier elytraTexture, Model model, boolean secure) {
    @Nullable
    public String textureUrl() {
        return this.textureUrl;
    }

    @Nullable
    public Identifier capeTexture() {
        return this.capeTexture;
    }

    @Nullable
    public Identifier elytraTexture() {
        return this.elytraTexture;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Model {
        SLIM("slim"),
        WIDE("default");

        private final String name;

        private Model(String name) {
            this.name = name;
        }

        public static Model fromName(@Nullable String name) {
            if (name == null) {
                return WIDE;
            }
            return switch (name) {
                case "slim" -> SLIM;
                default -> WIDE;
            };
        }

        public String getName() {
            return this.name;
        }
    }
}

