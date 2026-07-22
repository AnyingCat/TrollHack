/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.util.NetworkUtils;
import net.minecraft.util.PathUtil;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.logging.LogWriter;
import net.minecraft.util.path.CacheFiles;
import net.minecraft.util.thread.TaskExecutor;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class Downloader
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_RETAINED_CACHE_FILES = 20;
    private final Path directory;
    private final LogWriter<LogEntry> logWriter;
    private final TaskExecutor<Runnable> executor = TaskExecutor.create(Util.getDownloadWorkerExecutor(), "download-queue");

    public Downloader(Path directory) throws IOException {
        this.directory = directory;
        PathUtil.createDirectories(directory);
        this.logWriter = LogWriter.create(LogEntry.CODEC, directory.resolve("log.json"));
        CacheFiles.clear(directory, 20);
    }

    private DownloadResult download(Config config, Map<UUID, DownloadEntry> entries) {
        DownloadResult downloadResult = new DownloadResult();
        entries.forEach((id, entry) -> {
            Path path = this.directory.resolve(id.toString());
            Path path2 = null;
            try {
                path2 = NetworkUtils.download(path, entry.url, config.headers, config.hashFunction, entry.hash, config.maxSize, config.proxy, config.listener);
                downloadResult.downloaded.put((UUID)id, path2);
            } catch (Exception exception) {
                LOGGER.error("Failed to download {}", (Object)entry.url, (Object)exception);
                downloadResult.failed.add((UUID)id);
            }
            try {
                this.logWriter.write(new LogEntry((UUID)id, entry.url.toString(), Instant.now(), Optional.ofNullable(entry.hash).map(HashCode::toString), path2 != null ? this.getFileInfo(path2) : Either.left("download_failed")));
            } catch (Exception exception) {
                LOGGER.error("Failed to log download of {}", (Object)entry.url, (Object)exception);
            }
        });
        return downloadResult;
    }

    private Either<String, FileInfo> getFileInfo(Path path) {
        try {
            long l = Files.size(path);
            Path path2 = this.directory.relativize(path);
            return Either.right(new FileInfo(path2.toString(), l));
        } catch (IOException iOException) {
            LOGGER.error("Failed to get file size of {}", (Object)path, (Object)iOException);
            return Either.left("no_access");
        }
    }

    public CompletableFuture<DownloadResult> downloadAsync(Config config, Map<UUID, DownloadEntry> entries) {
        return CompletableFuture.supplyAsync(() -> this.download(config, entries), this.executor::send);
    }

    @Override
    public void close() throws IOException {
        this.executor.close();
        this.logWriter.close();
    }

    record LogEntry(UUID id, String url, Instant time, Optional<String> hash, Either<String, FileInfo> errorOrFileInfo) {
        public static final Codec<LogEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Uuids.STRING_CODEC.fieldOf("id")).forGetter(LogEntry::id), ((MapCodec)Codec.STRING.fieldOf("url")).forGetter(LogEntry::url), ((MapCodec)Codecs.INSTANT.fieldOf("time")).forGetter(LogEntry::time), Codec.STRING.optionalFieldOf("hash").forGetter(LogEntry::hash), Codec.mapEither(Codec.STRING.fieldOf("error"), FileInfo.CODEC.fieldOf("file")).forGetter(LogEntry::errorOrFileInfo)).apply((Applicative<LogEntry, ?>)instance, LogEntry::new));
    }

    public record DownloadResult(Map<UUID, Path> downloaded, Set<UUID> failed) {
        public DownloadResult() {
            this(new HashMap<UUID, Path>(), new HashSet<UUID>());
        }
    }

    public record Config(HashFunction hashFunction, int maxSize, Map<String, String> headers, Proxy proxy, NetworkUtils.DownloadListener listener) {
    }

    record FileInfo(String name, long size) {
        public static final Codec<FileInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("name")).forGetter(FileInfo::name), ((MapCodec)Codec.LONG.fieldOf("size")).forGetter(FileInfo::size)).apply((Applicative<FileInfo, ?>)instance, FileInfo::new));
    }

    public record DownloadEntry(URL url, @Nullable HashCode hash) {
        @Nullable
        public HashCode hash() {
            return this.hash;
        }
    }
}

