/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import net.minecraft.Bootstrap;
import net.minecraft.MinecraftVersion;
import net.minecraft.SharedConstants;
import net.minecraft.data.DataWriter;
import net.minecraft.data.dev.NbtProvider;
import net.minecraft.data.validate.StructureValidatorProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;

public class StructureUpdateEntrypoint {
    public static void main(String[] args) throws IOException {
        SharedConstants.setGameVersion(MinecraftVersion.CURRENT);
        Bootstrap.initialize();
        for (String string : args) {
            StructureUpdateEntrypoint.update(string);
        }
    }

    private static void update(String directory) throws IOException {
        try (Stream<Path> stream = Files.walk(Paths.get(directory, new String[0]), new FileVisitOption[0]);){
            stream.filter(path -> path.toString().endsWith(".snbt")).forEach(path -> {
                try {
                    String string = Files.readString(path);
                    NbtCompound nbtCompound = NbtHelper.fromNbtProviderString(string);
                    NbtCompound nbtCompound2 = StructureValidatorProvider.update(path.toString(), nbtCompound);
                    NbtProvider.writeTo(DataWriter.UNCACHED, path, NbtHelper.toNbtProviderString(nbtCompound2));
                } catch (CommandSyntaxException | IOException exception) {
                    throw new RuntimeException(exception);
                }
            });
        }
    }
}

