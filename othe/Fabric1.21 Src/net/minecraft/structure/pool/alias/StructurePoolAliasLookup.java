/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure.pool.alias;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Objects;
import net.minecraft.registry.RegistryKey;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.alias.StructurePoolAliasBinding;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

@FunctionalInterface
public interface StructurePoolAliasLookup {
    public static final StructurePoolAliasLookup EMPTY = pool -> pool;

    public RegistryKey<StructurePool> lookup(RegistryKey<StructurePool> var1);

    public static StructurePoolAliasLookup create(List<StructurePoolAliasBinding> bindings, BlockPos pos, long seed) {
        if (bindings.isEmpty()) {
            return EMPTY;
        }
        Random random = Random.create(seed).nextSplitter().split(pos);
        ImmutableMap.Builder builder = ImmutableMap.builder();
        bindings.forEach(binding -> binding.forEach(random, builder::put));
        ImmutableMap map = builder.build();
        return alias -> Objects.requireNonNull(map.getOrDefault(alias, alias), () -> "alias " + String.valueOf(alias) + " was mapped to null value");
    }
}

