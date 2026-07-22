/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.item.ArmorItem;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/**
 * Defines the material stats of an {@link ArmorItem} item.
 * 
 * <p>
 * To view available vanilla armor materials, visit {@link ArmorMaterials}.
 */
public record ArmorMaterial(Map<ArmorItem.Type, Integer> defense, int enchantability, RegistryEntry<SoundEvent> equipSound, Supplier<Ingredient> repairIngredient, List<Layer> layers, float toughness, float knockbackResistance) {
    public static final Codec<RegistryEntry<ArmorMaterial>> CODEC = Registries.ARMOR_MATERIAL.getEntryCodec();

    /**
     * Returns the amount of armor protection points offered by an {@link ArmorItem}
     * using this {@link ArmorMaterial} while it is worn by a player.
     * 
     * <p>
     * The protection value returned here is applied as an {@link net.minecraft.entity.attribute.EntityAttributeModifier}
     * to a player wearing the {@link ArmorItem} piece via the {@link net.minecraft.entity.attribute.EntityAttributeModifier.Operation#ADD_VALUE} modifier.
     * 
     * @return the amount of armor protection points offered by an {@link ArmorItem} with this {@link ArmorMaterial}
     * 
     * @param type the {@link ArmorItem.Type} of the {@link Item} with this {@link ArmorMaterial}
     */
    public int getProtection(ArmorItem.Type type) {
        return this.defense.getOrDefault(type, 0);
    }

    public static final class Layer {
        private final Identifier id;
        private final String suffix;
        private final boolean dyeable;
        private final Identifier layer2Texture;
        private final Identifier layer1Texture;

        public Layer(Identifier id, String suffix, boolean dyeable) {
            this.id = id;
            this.suffix = suffix;
            this.dyeable = dyeable;
            this.layer2Texture = this.getTextureId(true);
            this.layer1Texture = this.getTextureId(false);
        }

        public Layer(Identifier id) {
            this(id, "", false);
        }

        private Identifier getTextureId(boolean secondLayer) {
            return this.id.withPath(path -> "textures/models/armor/" + this.id.getPath() + "_layer_" + (secondLayer ? 2 : 1) + this.suffix + ".png");
        }

        public Identifier getTexture(boolean secondLayer) {
            return secondLayer ? this.layer2Texture : this.layer1Texture;
        }

        public boolean isDyeable() {
            return this.dyeable;
        }
    }
}

