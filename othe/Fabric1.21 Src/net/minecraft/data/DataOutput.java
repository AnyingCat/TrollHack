/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data;

import java.nio.file.Path;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class DataOutput {
    private final Path path;

    public DataOutput(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return this.path;
    }

    public Path resolvePath(OutputType outputType) {
        return this.getPath().resolve(outputType.path);
    }

    public PathResolver getResolver(OutputType outputType, String directoryName) {
        return new PathResolver(this, outputType, directoryName);
    }

    public PathResolver getResolver(RegistryKey<? extends Registry<?>> registryRef) {
        return this.getResolver(OutputType.DATA_PACK, RegistryKeys.getPath(registryRef));
    }

    public PathResolver getTagResolver(RegistryKey<? extends Registry<?>> registryRef) {
        return this.getResolver(OutputType.DATA_PACK, RegistryKeys.getTagPath(registryRef));
    }

    public static enum OutputType {
        DATA_PACK("data"),
        RESOURCE_PACK("assets"),
        REPORTS("reports");

        final String path;

        private OutputType(String path) {
            this.path = path;
        }
    }

    public static class PathResolver {
        private final Path rootPath;
        private final String directoryName;

        PathResolver(DataOutput dataGenerator, OutputType outputType, String directoryName) {
            this.rootPath = dataGenerator.resolvePath(outputType);
            this.directoryName = directoryName;
        }

        public Path resolve(Identifier id, String fileExtension) {
            return this.rootPath.resolve(id.getNamespace()).resolve(this.directoryName).resolve(id.getPath() + "." + fileExtension);
        }

        public Path resolveJson(Identifier id) {
            return this.rootPath.resolve(id.getNamespace()).resolve(this.directoryName).resolve(id.getPath() + ".json");
        }
    }
}

