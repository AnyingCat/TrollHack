/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure.pool.alias;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.alias.StructurePoolAliasBinding;
import net.minecraft.util.math.random.Random;

record DirectStructurePoolAliasBinding(RegistryKey<StructurePool> alias, RegistryKey<StructurePool> target) implements StructurePoolAliasBinding
{
    static MapCodec<DirectStructurePoolAliasBinding> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)RegistryKey.createCodec(RegistryKeys.TEMPLATE_POOL).fieldOf("alias")).forGetter(DirectStructurePoolAliasBinding::alias), ((MapCodec)RegistryKey.createCodec(RegistryKeys.TEMPLATE_POOL).fieldOf("target")).forGetter(DirectStructurePoolAliasBinding::target)).apply((Applicative<DirectStructurePoolAliasBinding, ?>)instance, DirectStructurePoolAliasBinding::new));

    @Override
    public void forEach(Random random, BiConsumer<RegistryKey<StructurePool>, RegistryKey<StructurePool>> aliasConsumer) {
        aliasConsumer.accept(this.alias, this.target);
    }

    @Override
    public Stream<RegistryKey<StructurePool>> streamTargets() {
        return Stream.of(this.target);
    }

    public MapCodec<DirectStructurePoolAliasBinding> getCodec() {
        return CODEC;
    }
}

