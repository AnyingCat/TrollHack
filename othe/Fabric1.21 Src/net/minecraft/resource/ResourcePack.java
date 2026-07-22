/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.registry.VersionedIdentifier;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * A resource pack, providing resources to resource managers.
 * 
 * <p>They are single-use in the lifecycle of a {@linkplain LifecycledResourceManagerImpl
 * lifecycled resource manager}. A {@link ResourcePackProfile} is a persistent
 * representation of the resource packs, and can be used to recreate the packs
 * on demand.
 */
public interface ResourcePack
extends AutoCloseable {
    public static final String METADATA_PATH_SUFFIX = ".mcmeta";
    public static final String PACK_METADATA_NAME = "pack.mcmeta";

    @Nullable
    public InputSupplier<InputStream> openRoot(String ... var1);

    @Nullable
    public InputSupplier<InputStream> open(ResourceType var1, Identifier var2);

    public void findResources(ResourceType var1, String var2, String var3, ResultConsumer var4);

    public Set<String> getNamespaces(ResourceType var1);

    @Nullable
    public <T> T parseMetadata(ResourceMetadataReader<T> var1) throws IOException;

    public ResourcePackInfo getInfo();

    default public String getId() {
        return this.getInfo().id();
    }

    default public Optional<VersionedIdentifier> getKnownPackInfo() {
        return this.getInfo().knownPackInfo();
    }

    @Override
    public void close();

    @FunctionalInterface
    public static interface ResultConsumer
    extends BiConsumer<Identifier, InputSupplier<InputStream>> {
    }
}

