/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math.floatprovider;

import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.floatprovider.ClampedNormalFloatProvider;
import net.minecraft.util.math.floatprovider.ConstantFloatProvider;
import net.minecraft.util.math.floatprovider.FloatProvider;
import net.minecraft.util.math.floatprovider.TrapezoidFloatProvider;
import net.minecraft.util.math.floatprovider.UniformFloatProvider;

public interface FloatProviderType<P extends FloatProvider> {
    public static final FloatProviderType<ConstantFloatProvider> CONSTANT = FloatProviderType.register("constant", ConstantFloatProvider.CODEC);
    public static final FloatProviderType<UniformFloatProvider> UNIFORM = FloatProviderType.register("uniform", UniformFloatProvider.CODEC);
    public static final FloatProviderType<ClampedNormalFloatProvider> CLAMPED_NORMAL = FloatProviderType.register("clamped_normal", ClampedNormalFloatProvider.CODEC);
    public static final FloatProviderType<TrapezoidFloatProvider> TRAPEZOID = FloatProviderType.register("trapezoid", TrapezoidFloatProvider.CODEC);

    public MapCodec<P> codec();

    public static <P extends FloatProvider> FloatProviderType<P> register(String id, MapCodec<P> codec) {
        return Registry.register(Registries.FLOAT_PROVIDER_TYPE, id, () -> codec);
    }
}

