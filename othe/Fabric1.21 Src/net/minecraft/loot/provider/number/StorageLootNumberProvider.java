/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.provider.number;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public record StorageLootNumberProvider(Identifier storage, NbtPathArgumentType.NbtPath path) implements LootNumberProvider
{
    public static final MapCodec<StorageLootNumberProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("storage")).forGetter(StorageLootNumberProvider::storage), ((MapCodec)NbtPathArgumentType.NbtPath.CODEC.fieldOf("path")).forGetter(StorageLootNumberProvider::path)).apply((Applicative<StorageLootNumberProvider, ?>)instance, StorageLootNumberProvider::new));

    @Override
    public LootNumberProviderType getType() {
        return LootNumberProviderTypes.STORAGE;
    }

    private Optional<AbstractNbtNumber> getNumber(LootContext context) {
        NbtCompound nbtCompound = context.getWorld().getServer().getDataCommandStorage().get(this.storage);
        try {
            NbtElement nbtElement;
            List<NbtElement> list = this.path.get(nbtCompound);
            if (list.size() == 1 && (nbtElement = list.get(0)) instanceof AbstractNbtNumber) {
                AbstractNbtNumber abstractNbtNumber = (AbstractNbtNumber)nbtElement;
                return Optional.of(abstractNbtNumber);
            }
        } catch (CommandSyntaxException commandSyntaxException) {
            // empty catch block
        }
        return Optional.empty();
    }

    @Override
    public float nextFloat(LootContext context) {
        return this.getNumber(context).map(AbstractNbtNumber::floatValue).orElse(Float.valueOf(0.0f)).floatValue();
    }

    @Override
    public int nextInt(LootContext context) {
        return this.getNumber(context).map(AbstractNbtNumber::intValue).orElse(0);
    }
}

