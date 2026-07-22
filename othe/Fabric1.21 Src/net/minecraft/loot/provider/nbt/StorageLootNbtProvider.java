/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.provider.nbt;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.provider.nbt.LootNbtProvider;
import net.minecraft.loot.provider.nbt.LootNbtProviderType;
import net.minecraft.loot.provider.nbt.LootNbtProviderTypes;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public record StorageLootNbtProvider(Identifier source) implements LootNbtProvider
{
    public static final MapCodec<StorageLootNbtProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("source")).forGetter(StorageLootNbtProvider::source)).apply((Applicative<StorageLootNbtProvider, ?>)instance, StorageLootNbtProvider::new));

    @Override
    public LootNbtProviderType getType() {
        return LootNbtProviderTypes.STORAGE;
    }

    @Override
    @Nullable
    public NbtElement getNbt(LootContext context) {
        return context.getWorld().getServer().getDataCommandStorage().get(this.source);
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return ImmutableSet.of();
    }
}

