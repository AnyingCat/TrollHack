/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.text;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.BlockNbtDataSource;
import net.minecraft.text.EntityNbtDataSource;
import net.minecraft.text.StorageNbtDataSource;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.StringIdentifiable;

/**
 * A data source for the NBT text content. Unmodifiable.
 */
public interface NbtDataSource {
    public static final MapCodec<NbtDataSource> CODEC = TextCodecs.dispatchingCodec((StringIdentifiable[])new Type[]{EntityNbtDataSource.TYPE, BlockNbtDataSource.TYPE, StorageNbtDataSource.TYPE}, Type::codec, NbtDataSource::getType, (String)"source");

    public Stream<NbtCompound> get(ServerCommandSource var1) throws CommandSyntaxException;

    public Type<?> getType();

    public record Type<T extends NbtDataSource>(MapCodec<T> codec, String id) implements StringIdentifiable
    {
        @Override
        public String asString() {
            return this.id;
        }
    }
}

