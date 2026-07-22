/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.placementmodifier;

import com.mojang.serialization.MapCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.placementmodifier.AbstractConditionalPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

public class BiomePlacementModifier
extends AbstractConditionalPlacementModifier {
    private static final BiomePlacementModifier INSTANCE = new BiomePlacementModifier();
    public static MapCodec<BiomePlacementModifier> MODIFIER_CODEC = MapCodec.unit(() -> INSTANCE);

    private BiomePlacementModifier() {
    }

    public static BiomePlacementModifier of() {
        return INSTANCE;
    }

    @Override
    protected boolean shouldPlace(FeaturePlacementContext context, Random random, BlockPos pos) {
        PlacedFeature placedFeature = context.getPlacedFeature().orElseThrow(() -> new IllegalStateException("Tried to biome check an unregistered feature, or a feature that should not restrict the biome"));
        RegistryEntry<Biome> registryEntry = context.getWorld().getBiome(pos);
        return context.getChunkGenerator().getGenerationSettings(registryEntry).isFeatureAllowed(placedFeature);
    }

    @Override
    public PlacementModifierType<?> getType() {
        return PlacementModifierType.BIOME;
    }
}

