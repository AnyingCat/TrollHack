/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.texture;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasHolder;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class StatusEffectSpriteManager
extends SpriteAtlasHolder {
    public StatusEffectSpriteManager(TextureManager textureManager) {
        super(textureManager, Identifier.ofVanilla("textures/atlas/mob_effects.png"), Identifier.ofVanilla("mob_effects"));
    }

    public Sprite getSprite(RegistryEntry<StatusEffect> effect) {
        return this.getSprite(effect.getKey().map(RegistryKey::getValue).orElseGet(MissingSprite::getMissingSpriteId));
    }
}

