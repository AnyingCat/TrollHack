/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class OverlayResourcePack
implements ResourcePack {
    private final ResourcePack base;
    private final List<ResourcePack> overlaysAndBase;

    public OverlayResourcePack(ResourcePack base, List<ResourcePack> overlays) {
        this.base = base;
        ArrayList<ResourcePack> list = new ArrayList<ResourcePack>(overlays.size() + 1);
        list.addAll(Lists.reverse(overlays));
        list.add(base);
        this.overlaysAndBase = List.copyOf(list);
    }

    @Override
    @Nullable
    public InputSupplier<InputStream> openRoot(String ... segments) {
        return this.base.openRoot(segments);
    }

    @Override
    @Nullable
    public InputSupplier<InputStream> open(ResourceType type, Identifier id) {
        for (ResourcePack resourcePack : this.overlaysAndBase) {
            InputSupplier<InputStream> inputSupplier = resourcePack.open(type, id);
            if (inputSupplier == null) continue;
            return inputSupplier;
        }
        return null;
    }

    @Override
    public void findResources(ResourceType type, String namespace, String prefix, ResourcePack.ResultConsumer consumer) {
        HashMap<Identifier, InputSupplier<InputStream>> map = new HashMap<Identifier, InputSupplier<InputStream>>();
        for (ResourcePack resourcePack : this.overlaysAndBase) {
            resourcePack.findResources(type, namespace, prefix, map::putIfAbsent);
        }
        map.forEach(consumer);
    }

    @Override
    public Set<String> getNamespaces(ResourceType type) {
        HashSet<String> set = new HashSet<String>();
        for (ResourcePack resourcePack : this.overlaysAndBase) {
            set.addAll(resourcePack.getNamespaces(type));
        }
        return set;
    }

    @Override
    @Nullable
    public <T> T parseMetadata(ResourceMetadataReader<T> metaReader) throws IOException {
        return this.base.parseMetadata(metaReader);
    }

    @Override
    public ResourcePackInfo getInfo() {
        return this.base.getInfo();
    }

    @Override
    public void close() {
        this.overlaysAndBase.forEach(ResourcePack::close);
    }
}

