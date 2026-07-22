/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;

public class WorldPreset {
    public static final Codec<WorldPreset> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.unboundedMap(RegistryKey.createCodec(RegistryKeys.DIMENSION), DimensionOptions.CODEC).fieldOf("dimensions")).forGetter(preset -> preset.dimensions)).apply((Applicative<WorldPreset, ?>)instance, WorldPreset::new)).validate(WorldPreset::validate);
    public static final Codec<RegistryEntry<WorldPreset>> ENTRY_CODEC = RegistryElementCodec.of(RegistryKeys.WORLD_PRESET, CODEC);
    private final Map<RegistryKey<DimensionOptions>, DimensionOptions> dimensions;

    public WorldPreset(Map<RegistryKey<DimensionOptions>, DimensionOptions> dimensions) {
        this.dimensions = dimensions;
    }

    private ImmutableMap<RegistryKey<DimensionOptions>, DimensionOptions> collectDimensions() {
        ImmutableMap.Builder builder = ImmutableMap.builder();
        DimensionOptionsRegistryHolder.streamAll(this.dimensions.keySet().stream()).forEach(dimensionKey -> {
            DimensionOptions dimensionOptions = this.dimensions.get(dimensionKey);
            if (dimensionOptions != null) {
                builder.put(dimensionKey, dimensionOptions);
            }
        });
        return builder.build();
    }

    public DimensionOptionsRegistryHolder createDimensionsRegistryHolder() {
        return new DimensionOptionsRegistryHolder(this.collectDimensions());
    }

    public Optional<DimensionOptions> getOverworld() {
        return Optional.ofNullable(this.dimensions.get(DimensionOptions.OVERWORLD));
    }

    private static DataResult<WorldPreset> validate(WorldPreset preset) {
        if (preset.getOverworld().isEmpty()) {
            return DataResult.error(() -> "Missing overworld dimension");
        }
        return DataResult.success(preset, Lifecycle.stable());
    }
}

