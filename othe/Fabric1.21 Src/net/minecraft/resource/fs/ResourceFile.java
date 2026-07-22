/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource.fs;

import java.nio.file.Path;
import java.util.Map;
import net.minecraft.resource.fs.ResourcePath;

interface ResourceFile {
    public static final ResourceFile EMPTY = new ResourceFile(){

        public String toString() {
            return "empty";
        }
    };
    public static final ResourceFile RELATIVE = new ResourceFile(){

        public String toString() {
            return "relative";
        }
    };

    public record Directory(Map<String, ResourcePath> children) implements ResourceFile
    {
    }

    public record File(Path contents) implements ResourceFile
    {
    }
}

