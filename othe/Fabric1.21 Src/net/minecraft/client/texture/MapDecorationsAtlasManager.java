/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.texture;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasHolder;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.item.map.MapDecoration;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class MapDecorationsAtlasManager
extends SpriteAtlasHolder {
    public MapDecorationsAtlasManager(TextureManager manager) {
        super(manager, Identifier.ofVanilla("textures/atlas/map_decorations.png"), Identifier.ofVanilla("map_decorations"));
    }

    public Sprite getSprite(MapDecoration decoration) {
        return this.getSprite(decoration.getAssetId());
    }
}

