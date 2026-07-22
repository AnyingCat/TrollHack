/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.provider.number;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;

public record ConstantLootNumberProvider(float value) implements LootNumberProvider
{
    public static final MapCodec<ConstantLootNumberProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.FLOAT.fieldOf("value")).forGetter(ConstantLootNumberProvider::value)).apply((Applicative<ConstantLootNumberProvider, ?>)instance, ConstantLootNumberProvider::new));
    public static final Codec<ConstantLootNumberProvider> INLINE_CODEC = Codec.FLOAT.xmap(ConstantLootNumberProvider::new, ConstantLootNumberProvider::value);

    @Override
    public LootNumberProviderType getType() {
        return LootNumberProviderTypes.CONSTANT;
    }

    @Override
    public float nextFloat(LootContext context) {
        return this.value;
    }

    public static ConstantLootNumberProvider create(float value) {
        return new ConstantLootNumberProvider(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        return Float.compare(((ConstantLootNumberProvider)o).value, this.value) == 0;
    }

    @Override
    public int hashCode() {
        return this.value != 0.0f ? Float.floatToIntBits(this.value) : 0;
    }
}

