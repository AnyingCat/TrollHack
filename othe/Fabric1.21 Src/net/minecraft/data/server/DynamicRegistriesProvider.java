/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data.server;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;

public class DynamicRegistriesProvider
implements DataProvider {
    private final DataOutput output;
    private final CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture;

    public DynamicRegistriesProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture) {
        this.registryLookupFuture = registryLookupFuture;
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return this.registryLookupFuture.thenCompose(lookup -> {
            RegistryOps<JsonElement> dynamicOps = lookup.getOps(JsonOps.INSTANCE);
            return CompletableFuture.allOf((CompletableFuture[])RegistryLoader.DYNAMIC_REGISTRIES.stream().flatMap(entry -> this.writeRegistryEntries(writer, (RegistryWrapper.WrapperLookup)lookup, (DynamicOps<JsonElement>)dynamicOps, (RegistryLoader.Entry)entry).stream()).toArray(CompletableFuture[]::new));
        });
    }

    private <T> Optional<CompletableFuture<?>> writeRegistryEntries(DataWriter writer, RegistryWrapper.WrapperLookup lookup, DynamicOps<JsonElement> ops, RegistryLoader.Entry<T> registry) {
        RegistryKey registryKey = registry.key();
        return lookup.getOptionalWrapper(registryKey).map(wrapper -> {
            DataOutput.PathResolver pathResolver = this.output.getResolver(registryKey);
            return CompletableFuture.allOf((CompletableFuture[])wrapper.streamEntries().map(entry -> DynamicRegistriesProvider.writeToPath(pathResolver.resolveJson(entry.registryKey().getValue()), writer, ops, registry.elementCodec(), entry.value())).toArray(CompletableFuture[]::new));
        });
    }

    private static <E> CompletableFuture<?> writeToPath(Path path, DataWriter cache, DynamicOps<JsonElement> json, Encoder<E> encoder, E value) {
        return encoder.encodeStart(json, value).mapOrElse(jsonElement -> DataProvider.writeToPath(cache, jsonElement, path), error -> CompletableFuture.failedFuture(new IllegalStateException("Couldn't generate file '" + String.valueOf(path) + "': " + error.message())));
    }

    @Override
    public String getName() {
        return "Registries";
    }
}

