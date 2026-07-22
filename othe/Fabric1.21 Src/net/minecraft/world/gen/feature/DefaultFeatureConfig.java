/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.feature.FeatureConfig;

public class DefaultFeatureConfig
implements FeatureConfig {
    public static final Codec<DefaultFeatureConfig> CODEC = Codec.unit(() -> INSTANCE);
    public static final DefaultFeatureConfig INSTANCE = new DefaultFeatureConfig();
}

