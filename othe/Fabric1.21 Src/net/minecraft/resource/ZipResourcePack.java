/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.resource.AbstractFileResourcePack;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.OverlayResourcePack;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ZipResourcePack
extends AbstractFileResourcePack {
    static final Logger LOGGER = LogUtils.getLogger();
    private final ZipFileWrapper zipFile;
    private final String overlay;

    ZipResourcePack(ResourcePackInfo info, ZipFileWrapper zipFile, String overlay) {
        super(info);
        this.zipFile = zipFile;
        this.overlay = overlay;
    }

    private static String toPath(ResourceType type, Identifier id) {
        return String.format(Locale.ROOT, "%s/%s/%s", type.getDirectory(), id.getNamespace(), id.getPath());
    }

    @Override
    @Nullable
    public InputSupplier<InputStream> openRoot(String ... segments) {
        return this.openFile(String.join((CharSequence)"/", segments));
    }

    @Override
    public InputSupplier<InputStream> open(ResourceType type, Identifier id) {
        return this.openFile(ZipResourcePack.toPath(type, id));
    }

    private String appendOverlayPrefix(String path) {
        if (this.overlay.isEmpty()) {
            return path;
        }
        return this.overlay + "/" + path;
    }

    @Nullable
    private InputSupplier<InputStream> openFile(String path) {
        ZipFile zipFile = this.zipFile.open();
        if (zipFile == null) {
            return null;
        }
        ZipEntry zipEntry = zipFile.getEntry(this.appendOverlayPrefix(path));
        if (zipEntry == null) {
            return null;
        }
        return InputSupplier.create(zipFile, zipEntry);
    }

    @Override
    public Set<String> getNamespaces(ResourceType type) {
        ZipFile zipFile = this.zipFile.open();
        if (zipFile == null) {
            return Set.of();
        }
        Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
        HashSet<String> set = Sets.newHashSet();
        String string = this.appendOverlayPrefix(type.getDirectory() + "/");
        while (enumeration.hasMoreElements()) {
            ZipEntry zipEntry = enumeration.nextElement();
            String string2 = zipEntry.getName();
            String string3 = ZipResourcePack.getNamespace(string, string2);
            if (string3.isEmpty()) continue;
            if (Identifier.isNamespaceValid(string3)) {
                set.add(string3);
                continue;
            }
            LOGGER.warn("Non [a-z0-9_.-] character in namespace {} in pack {}, ignoring", (Object)string3, (Object)this.zipFile.file);
        }
        return set;
    }

    @VisibleForTesting
    public static String getNamespace(String prefix, String entryName) {
        if (!entryName.startsWith(prefix)) {
            return "";
        }
        int i = prefix.length();
        int j = entryName.indexOf(47, i);
        if (j == -1) {
            return entryName.substring(i);
        }
        return entryName.substring(i, j);
    }

    @Override
    public void close() {
        this.zipFile.close();
    }

    @Override
    public void findResources(ResourceType type, String namespace, String prefix, ResourcePack.ResultConsumer consumer) {
        ZipFile zipFile = this.zipFile.open();
        if (zipFile == null) {
            return;
        }
        Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
        String string = this.appendOverlayPrefix(type.getDirectory() + "/" + namespace + "/");
        String string2 = string + prefix + "/";
        while (enumeration.hasMoreElements()) {
            String string3;
            ZipEntry zipEntry = enumeration.nextElement();
            if (zipEntry.isDirectory() || !(string3 = zipEntry.getName()).startsWith(string2)) continue;
            String string4 = string3.substring(string.length());
            Identifier identifier = Identifier.tryParse(namespace, string4);
            if (identifier != null) {
                consumer.accept(identifier, InputSupplier.create(zipFile, zipEntry));
                continue;
            }
            LOGGER.warn("Invalid path in datapack: {}:{}, ignoring", (Object)namespace, (Object)string4);
        }
    }

    static class ZipFileWrapper
    implements AutoCloseable {
        final File file;
        @Nullable
        private ZipFile zip;
        private boolean closed;

        ZipFileWrapper(File file) {
            this.file = file;
        }

        @Nullable
        ZipFile open() {
            if (this.closed) {
                return null;
            }
            if (this.zip == null) {
                try {
                    this.zip = new ZipFile(this.file);
                } catch (IOException iOException) {
                    LOGGER.error("Failed to open pack {}", (Object)this.file, (Object)iOException);
                    this.closed = true;
                    return null;
                }
            }
            return this.zip;
        }

        @Override
        public void close() {
            if (this.zip != null) {
                IOUtils.closeQuietly((Closeable)this.zip);
                this.zip = null;
            }
        }

        protected void finalize() throws Throwable {
            this.close();
            super.finalize();
        }
    }

    public static class ZipBackedFactory
    implements ResourcePackProfile.PackFactory {
        private final File file;

        public ZipBackedFactory(Path path) {
            this(path.toFile());
        }

        public ZipBackedFactory(File file) {
            this.file = file;
        }

        @Override
        public ResourcePack open(ResourcePackInfo info) {
            ZipFileWrapper zipFileWrapper = new ZipFileWrapper(this.file);
            return new ZipResourcePack(info, zipFileWrapper, "");
        }

        @Override
        public ResourcePack openWithOverlays(ResourcePackInfo info, ResourcePackProfile.Metadata metadata) {
            ZipFileWrapper zipFileWrapper = new ZipFileWrapper(this.file);
            ZipResourcePack resourcePack = new ZipResourcePack(info, zipFileWrapper, "");
            List<String> list = metadata.overlays();
            if (list.isEmpty()) {
                return resourcePack;
            }
            ArrayList<ResourcePack> list2 = new ArrayList<ResourcePack>(list.size());
            for (String string : list) {
                list2.add(new ZipResourcePack(info, zipFileWrapper, string));
            }
            return new OverlayResourcePack(resourcePack, list2);
        }
    }
}

