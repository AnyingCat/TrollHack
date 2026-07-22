/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import net.minecraft.util.path.SymlinkEntry;
import net.minecraft.util.path.SymlinkFinder;
import org.jetbrains.annotations.Nullable;

public abstract class ResourcePackOpener<T> {
    private final SymlinkFinder symlinkFinder;

    protected ResourcePackOpener(SymlinkFinder symlinkFinder) {
        this.symlinkFinder = symlinkFinder;
    }

    @Nullable
    public T open(Path path, List<SymlinkEntry> foundSymlinks) throws IOException {
        BasicFileAttributes basicFileAttributes;
        Path path2 = path;
        try {
            basicFileAttributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (NoSuchFileException noSuchFileException) {
            return null;
        }
        if (basicFileAttributes.isSymbolicLink()) {
            this.symlinkFinder.validate(path, foundSymlinks);
            if (!foundSymlinks.isEmpty()) {
                return null;
            }
            path2 = Files.readSymbolicLink(path);
            basicFileAttributes = Files.readAttributes(path2, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        }
        if (basicFileAttributes.isDirectory()) {
            this.symlinkFinder.validateRecursively(path2, foundSymlinks);
            if (!foundSymlinks.isEmpty()) {
                return null;
            }
            if (!Files.isRegularFile(path2.resolve("pack.mcmeta"), new LinkOption[0])) {
                return null;
            }
            return this.openDirectory(path2);
        }
        if (basicFileAttributes.isRegularFile() && path2.getFileName().toString().endsWith(".zip")) {
            return this.openZip(path2);
        }
        return null;
    }

    @Nullable
    protected abstract T openZip(Path var1) throws IOException;

    @Nullable
    protected abstract T openDirectory(Path var1) throws IOException;
}

