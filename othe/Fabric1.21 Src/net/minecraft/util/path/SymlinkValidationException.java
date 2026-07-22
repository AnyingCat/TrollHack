/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.path;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.util.path.SymlinkEntry;

public class SymlinkValidationException
extends Exception {
    private final Path path;
    private final List<SymlinkEntry> symlinks;

    public SymlinkValidationException(Path path, List<SymlinkEntry> symlinks) {
        this.path = path;
        this.symlinks = symlinks;
    }

    @Override
    public String getMessage() {
        return SymlinkValidationException.getMessage(this.path, this.symlinks);
    }

    public static String getMessage(Path path, List<SymlinkEntry> symlinks) {
        return "Failed to validate '" + String.valueOf(path) + "'. Found forbidden symlinks: " + symlinks.stream().map(symlink -> String.valueOf(symlink.link()) + "->" + String.valueOf(symlink.target())).collect(Collectors.joining(", "));
    }
}

