/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.path;

import java.nio.file.Path;

public record SymlinkEntry(Path link, Path target) {
}

