/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientDynamicRegistryType;
import net.minecraft.client.network.ClientTagLoader;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.SerializableRegistries;
import net.minecraft.registry.tag.TagPacketSerializer;
import net.minecraft.resource.ResourceFactory;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ClientRegistries {
    @Nullable
    private DynamicRegistries dynamicRegistries;
    @Nullable
    private ClientTagLoader tagLoader;

    public void putDynamicRegistry(RegistryKey<? extends Registry<?>> registryRef, List<SerializableRegistries.SerializedRegistryEntry> entries) {
        if (this.dynamicRegistries == null) {
            this.dynamicRegistries = new DynamicRegistries();
        }
        this.dynamicRegistries.put(registryRef, entries);
    }

    public void putTags(Map<RegistryKey<? extends Registry<?>>, TagPacketSerializer.Serialized> tags) {
        if (this.tagLoader == null) {
            this.tagLoader = new ClientTagLoader();
        }
        tags.forEach(this.tagLoader::put);
    }

    public DynamicRegistryManager.Immutable createRegistryManager(ResourceFactory factory, DynamicRegistryManager registryManager, boolean local) {
        DynamicRegistryManager dynamicRegistryManager;
        CombinedDynamicRegistries<ClientDynamicRegistryType> combinedDynamicRegistries = ClientDynamicRegistryType.createCombinedDynamicRegistries();
        if (this.dynamicRegistries != null) {
            DynamicRegistryManager.Immutable immutable = combinedDynamicRegistries.getPrecedingRegistryManagers(ClientDynamicRegistryType.REMOTE);
            DynamicRegistryManager.Immutable immutable2 = this.dynamicRegistries.load(factory, immutable).toImmutable();
            dynamicRegistryManager = combinedDynamicRegistries.with(ClientDynamicRegistryType.REMOTE, immutable2).getCombinedRegistryManager();
        } else {
            dynamicRegistryManager = registryManager;
        }
        if (this.tagLoader != null) {
            this.tagLoader.load(dynamicRegistryManager, local);
        }
        return dynamicRegistryManager.toImmutable();
    }

    @Environment(value=EnvType.CLIENT)
    static class DynamicRegistries {
        private final Map<RegistryKey<? extends Registry<?>>, List<SerializableRegistries.SerializedRegistryEntry>> dynamicRegistries = new HashMap();

        DynamicRegistries() {
        }

        public void put(RegistryKey<? extends Registry<?>> registryRef, List<SerializableRegistries.SerializedRegistryEntry> entries) {
            this.dynamicRegistries.computeIfAbsent(registryRef, registries -> new ArrayList()).addAll(entries);
        }

        public DynamicRegistryManager load(ResourceFactory factory, DynamicRegistryManager registryManager) {
            return RegistryLoader.loadFromNetwork(this.dynamicRegistries, factory, registryManager, RegistryLoader.SYNCED_REGISTRIES);
        }
    }
}

