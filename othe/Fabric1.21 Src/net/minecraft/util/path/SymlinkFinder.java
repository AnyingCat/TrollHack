/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.path;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.path.SymlinkEntry;

public class SymlinkFinder {
    private final PathMatcher matcher;

    public SymlinkFinder(PathMatcher matcher) {
        this.matcher = matcher;
    }

    public void validate(Path path, List<SymlinkEntry> results) throws IOException {
        Path path2 = Files.readSymbolicLink(path);
        if (!this.matcher.matches(path2)) {
            results.add(new SymlinkEntry(path, path2));
        }
    }

    public List<SymlinkEntry> validate(Path path) throws IOException {
        ArrayList<SymlinkEntry> list = new ArrayList<SymlinkEntry>();
        this.validate(path, list);
        return list;
    }

    public List<SymlinkEntry> collect(Path path, boolean resolveSymlink) throws IOException {
        BasicFileAttributes basicFileAttributes;
        ArrayList<SymlinkEntry> list = new ArrayList<SymlinkEntry>();
        try {
            basicFileAttributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (NoSuchFileException noSuchFileException) {
            return list;
        }
        if (basicFileAttributes.isRegularFile()) {
            throw new IOException("Path " + String.valueOf(path) + " is not a directory");
        }
        if (basicFileAttributes.isSymbolicLink()) {
            if (resolveSymlink) {
                path = Files.readSymbolicLink(path);
            } else {
                this.validate(path, list);
                return list;
            }
        }
        this.validateRecursively(path, list);
        return list;
    }

    public void validateRecursively(Path path, final List<SymlinkEntry> results) throws IOException {
        Files.walkFileTree(path, (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(){

            private void validate(Path path, BasicFileAttributes attributes) throws IOException {
                if (attributes.isSymbolicLink()) {
                    SymlinkFinder.this.validate(path, results);
                }
            }

            @Override
            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                this.validate(path, basicFileAttributes);
                return super.preVisitDirectory(path, basicFileAttributes);
            }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                this.validate(path, basicFileAttributes);
                return super.visitFile(path, basicFileAttributes);
            }

            @Override
            public /* synthetic */ FileVisitResult visitFile(Object path, BasicFileAttributes attributes) throws IOException {
                return this.visitFile((Path)path, attributes);
            }

            @Override
            public /* synthetic */ FileVisitResult preVisitDirectory(Object path, BasicFileAttributes attributes) throws IOException {
                return this.preVisitDirectory((Path)path, attributes);
            }
        });
    }
}

