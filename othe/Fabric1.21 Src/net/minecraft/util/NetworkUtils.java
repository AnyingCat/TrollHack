/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import com.google.common.hash.Funnels;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Map;
import java.util.OptionalLong;
import net.minecraft.util.PathUtil;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * A few networking utilities.
 */
public class NetworkUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    private NetworkUtils() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    public static Path download(Path path, URL url, Map<String, String> headers, HashFunction hashFunction, @Nullable HashCode hashCode, int maxBytes, Proxy proxy, DownloadListener listener) {
        InputStream inputStream;
        HttpURLConnection httpURLConnection;
        block21: {
            Path path2;
            httpURLConnection = null;
            inputStream = null;
            listener.onStart();
            if (hashCode != null) {
                path2 = NetworkUtils.resolve(path, hashCode);
                try {
                    if (NetworkUtils.validateHash(path2, hashFunction, hashCode)) {
                        LOGGER.info("Returning cached file since actual hash matches requested");
                        listener.onFinish(true);
                        NetworkUtils.updateModificationTime(path2);
                        return path2;
                    }
                } catch (IOException iOException) {
                    LOGGER.warn("Failed to check cached file {}", (Object)path2, (Object)iOException);
                }
                try {
                    LOGGER.warn("Existing file {} not found or had mismatched hash", (Object)path2);
                    Files.deleteIfExists(path2);
                } catch (IOException iOException) {
                    listener.onFinish(false);
                    throw new UncheckedIOException("Failed to remove existing file " + String.valueOf(path2), iOException);
                }
            }
            path2 = null;
            httpURLConnection = (HttpURLConnection)url.openConnection(proxy);
            httpURLConnection.setInstanceFollowRedirects(true);
            headers.forEach(httpURLConnection::setRequestProperty);
            inputStream = httpURLConnection.getInputStream();
            long l = httpURLConnection.getContentLengthLong();
            OptionalLong optionalLong = l != -1L ? OptionalLong.of(l) : OptionalLong.empty();
            PathUtil.createDirectories(path);
            listener.onContentLength(optionalLong);
            if (optionalLong.isPresent() && optionalLong.getAsLong() > (long)maxBytes) {
                throw new IOException("Filesize is bigger than maximum allowed (file is " + String.valueOf(optionalLong) + ", limit is " + maxBytes + ")");
            }
            if (path2 == null) break block21;
            HashCode hashCode2 = NetworkUtils.write(hashFunction, maxBytes, listener, inputStream, path2);
            if (!hashCode2.equals(hashCode)) {
                throw new IOException("Hash of downloaded file (" + String.valueOf(hashCode2) + ") did not match requested (" + String.valueOf(hashCode) + ")");
            }
            listener.onFinish(true);
            Path path3 = path2;
            IOUtils.closeQuietly(inputStream);
            return path3;
        }
        Path path3 = Files.createTempFile(path, "download", ".tmp", new FileAttribute[0]);
        HashCode hashCode3 = NetworkUtils.write(hashFunction, maxBytes, listener, inputStream, path3);
        Path path4 = NetworkUtils.resolve(path, hashCode3);
        if (!NetworkUtils.validateHash(path4, hashFunction, hashCode3)) {
            Files.move(path3, path4, StandardCopyOption.REPLACE_EXISTING);
        } else {
            NetworkUtils.updateModificationTime(path4);
        }
        listener.onFinish(true);
        Path path5 = path4;
        Files.deleteIfExists(path3);
        IOUtils.closeQuietly(inputStream);
        return path5;
        {
            catch (Throwable throwable) {
                try {
                    try {
                        Files.deleteIfExists(path3);
                        throw throwable;
                    } catch (Throwable throwable2) {
                        InputStream inputStream2;
                        if (httpURLConnection != null && (inputStream2 = httpURLConnection.getErrorStream()) != null) {
                            try {
                                LOGGER.error("HTTP response error: {}", (Object)IOUtils.toString(inputStream2, StandardCharsets.UTF_8));
                            } catch (Exception exception) {
                                LOGGER.error("Failed to read response from server");
                            }
                        }
                        listener.onFinish(false);
                        throw new IllegalStateException("Failed to download file " + String.valueOf(url), throwable2);
                    }
                } catch (Throwable throwable3) {
                    IOUtils.closeQuietly(inputStream);
                    throw throwable3;
                }
            }
        }
    }

    private static void updateModificationTime(Path path) {
        try {
            Files.setLastModifiedTime(path, FileTime.from(Instant.now()));
        } catch (IOException iOException) {
            LOGGER.warn("Failed to update modification time of {}", (Object)path, (Object)iOException);
        }
    }

    private static HashCode hash(Path path, HashFunction hashFunction) throws IOException {
        Hasher hasher = hashFunction.newHasher();
        try (OutputStream outputStream = Funnels.asOutputStream(hasher);
             InputStream inputStream = Files.newInputStream(path, new OpenOption[0]);){
            inputStream.transferTo(outputStream);
        }
        return hasher.hash();
    }

    private static boolean validateHash(Path path, HashFunction hashFunction, HashCode hashCode) throws IOException {
        if (Files.exists(path, new LinkOption[0])) {
            HashCode hashCode2 = NetworkUtils.hash(path, hashFunction);
            if (hashCode2.equals(hashCode)) {
                return true;
            }
            LOGGER.warn("Mismatched hash of file {}, expected {} but found {}", path, hashCode, hashCode2);
        }
        return false;
    }

    private static Path resolve(Path path, HashCode hashCode) {
        return path.resolve(hashCode.toString());
    }

    private static HashCode write(HashFunction hashFunction, int maxBytes, DownloadListener listener, InputStream stream, Path path) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE);){
            int i;
            Hasher hasher = hashFunction.newHasher();
            byte[] bs = new byte[8196];
            long l = 0L;
            while ((i = stream.read(bs)) >= 0) {
                listener.onProgress(l += (long)i);
                if (l > (long)maxBytes) {
                    throw new IOException("Filesize was bigger than maximum allowed (got >= " + l + ", limit was " + maxBytes + ")");
                }
                if (Thread.interrupted()) {
                    LOGGER.error("INTERRUPTED");
                    throw new IOException("Download interrupted");
                }
                outputStream.write(bs, 0, i);
                hasher.putBytes(bs, 0, i);
            }
            HashCode hashCode = hasher.hash();
            return hashCode;
        }
    }

    public static int findLocalPort() {
        int n;
        ServerSocket serverSocket = new ServerSocket(0);
        try {
            n = serverSocket.getLocalPort();
        } catch (Throwable throwable) {
            try {
                try {
                    serverSocket.close();
                } catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            } catch (IOException iOException) {
                return 25564;
            }
        }
        serverSocket.close();
        return n;
    }

    public static boolean isPortAvailable(int port) {
        boolean bl;
        if (port < 0 || port > 65535) {
            return false;
        }
        ServerSocket serverSocket = new ServerSocket(port);
        try {
            bl = serverSocket.getLocalPort() == port;
        } catch (Throwable throwable) {
            try {
                try {
                    serverSocket.close();
                } catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            } catch (IOException iOException) {
                return false;
            }
        }
        serverSocket.close();
        return bl;
    }

    public static interface DownloadListener {
        public void onStart();

        public void onContentLength(OptionalLong var1);

        public void onProgress(long var1);

        public void onFinish(boolean var1);
    }
}

