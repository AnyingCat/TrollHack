/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.profiler;

import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.util.profiler.ReadableProfiler;
import net.minecraft.util.profiler.Sampler;

public interface SamplerSource {
    public Set<Sampler> getSamplers(Supplier<ReadableProfiler> var1);
}

