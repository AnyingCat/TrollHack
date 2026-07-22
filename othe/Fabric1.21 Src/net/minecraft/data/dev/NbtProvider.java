/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data.dev;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.logging.LogUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.util.FixedBufferInputStream;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class NbtProvider
implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Iterable<Path> paths;
    private final DataOutput output;

    public NbtProvider(DataOutput output, Collection<Path> paths) {
        this.paths = paths;
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        Path path = this.output.getPath();
        ArrayList<CompletionStage> list = new ArrayList<CompletionStage>();
        for (Path path2 : this.paths) {
            list.add(CompletableFuture.supplyAsync(() -> {
                CompletableFuture<Void> completableFuture;
                block8: {
                    Stream<Path> stream = Files.walk(path2, new FileVisitOption[0]);
                    try {
                        completableFuture = CompletableFuture.allOf((CompletableFuture[])stream.filter(path -> path.toString().endsWith(".nbt")).map(path -> CompletableFuture.runAsync(() -> NbtProvider.convertNbtToSnbt(writer, path, NbtProvider.getLocation(path2, path), path), Util.getIoWorkerExecutor())).toArray(CompletableFuture[]::new));
                        if (stream == null) break block8;
                    } catch (Throwable throwable) {
                        try {
                            if (stream != null) {
                                try {
                                    stream.close();
                                } catch (Throwable throwable2) {
                                    throwable.addSuppressed(throwable2);
                                }
                            }
                            throw throwable;
                        } catch (IOException iOException) {
                            LOGGER.error("Failed to read structure input directory", iOException);
                            return CompletableFuture.completedFuture(null);
                        }
                    }
                    stream.close();
                }
                return completableFuture;
            }, Util.getMainWorkerExecutor()).thenCompose(future -> future));
        }
        return CompletableFuture.allOf((CompletableFuture[])list.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "NBT -> SNBT";
    }

    private static String getLocation(Path inputPath, Path filePath) {
        String string = inputPath.relativize(filePath).toString().replaceAll("\\\\", "/");
        return string.substring(0, string.length() - ".nbt".length());
    }

    /*
     * Enabled aggressive exception aggregation
     */
    @Nullable
    public static Path convertNbtToSnbt(DataWriter writer, Path inputPath, String filename, Path outputPath) {
        try (InputStream inputStream = Files.newInputStream(inputPath, new OpenOption[0]);){
            Path path;
            try (FixedBufferInputStream inputStream2 = new FixedBufferInputStream(inputStream);){
                Path path2 = outputPath.resolve(filename + ".snbt");
                NbtProvider.writeTo(writer, path2, NbtHelper.toNbtProviderString(NbtIo.readCompressed(inputStream2, NbtSizeTracker.ofUnlimitedBytes())));
                LOGGER.info("Converted {} from NBT to SNBT", (Object)filename);
                path = path2;
            }
            return path;
        } catch (IOException iOException) {
            LOGGER.error("Couldn't convert {} from NBT to SNBT at {}", filename, inputPath, iOException);
            return null;
        }
    }

    public static void writeTo(DataWriter writer, Path path, String content) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        HashingOutputStream hashingOutputStream = new HashingOutputStream(Hashing.sha1(), byteArrayOutputStream);
        hashingOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
        hashingOutputStream.write(10);
        writer.write(path, byteArrayOutputStream.toByteArray(), hashingOutputStream.hash());
    }
}

