/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Util;

public record MapDecorationsComponent(Map<String, Decoration> decorations) {
    public static final MapDecorationsComponent DEFAULT = new MapDecorationsComponent(Map.of());
    public static final Codec<MapDecorationsComponent> CODEC = Codec.unboundedMap(Codec.STRING, Decoration.CODEC).xmap(MapDecorationsComponent::new, MapDecorationsComponent::decorations);

    public MapDecorationsComponent with(String id, Decoration decoration) {
        return new MapDecorationsComponent(Util.mapWith(this.decorations, id, decoration));
    }

    public record Decoration(RegistryEntry<MapDecorationType> type, double x, double z, float rotation) {
        public static final Codec<Decoration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)MapDecorationType.CODEC.fieldOf("type")).forGetter(Decoration::type), ((MapCodec)Codec.DOUBLE.fieldOf("x")).forGetter(Decoration::x), ((MapCodec)Codec.DOUBLE.fieldOf("z")).forGetter(Decoration::z), ((MapCodec)Codec.FLOAT.fieldOf("rotation")).forGetter(Decoration::rotation)).apply((Applicative<Decoration, ?>)instance, Decoration::new));
    }
}

