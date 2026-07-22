/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource.metadata;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.JsonHelper;

public interface ResourceMetadata {
    public static final ResourceMetadata NONE = new ResourceMetadata(){

        @Override
        public <T> Optional<T> decode(ResourceMetadataReader<T> reader) {
            return Optional.empty();
        }
    };
    public static final InputSupplier<ResourceMetadata> NONE_SUPPLIER = () -> NONE;

    public static ResourceMetadata create(InputStream stream) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));){
            final JsonObject jsonObject = JsonHelper.deserialize(bufferedReader);
            ResourceMetadata resourceMetadata = new ResourceMetadata(){

                @Override
                public <T> Optional<T> decode(ResourceMetadataReader<T> reader) {
                    String string = reader.getKey();
                    return jsonObject.has(string) ? Optional.of(reader.fromJson(JsonHelper.getObject(jsonObject, string))) : Optional.empty();
                }
            };
            return resourceMetadata;
        }
    }

    public <T> Optional<T> decode(ResourceMetadataReader<T> var1);

    default public ResourceMetadata copy(Collection<ResourceMetadataReader<?>> readers) {
        Builder builder = new Builder();
        for (ResourceMetadataReader<?> resourceMetadataReader : readers) {
            this.decodeAndAdd(builder, resourceMetadataReader);
        }
        return builder.build();
    }

    private <T> void decodeAndAdd(Builder builder, ResourceMetadataReader<T> reader) {
        this.decode(reader).ifPresent(value -> builder.add(reader, value));
    }

    public static class Builder {
        private final ImmutableMap.Builder<ResourceMetadataReader<?>, Object> values = ImmutableMap.builder();

        public <T> Builder add(ResourceMetadataReader<T> reader, T value) {
            this.values.put(reader, value);
            return this;
        }

        public ResourceMetadata build() {
            final ImmutableMap<ResourceMetadataReader<?>, Object> immutableMap = this.values.build();
            if (immutableMap.isEmpty()) {
                return NONE;
            }
            return new ResourceMetadata(){

                @Override
                public <T> Optional<T> decode(ResourceMetadataReader<T> reader) {
                    return Optional.ofNullable(immutableMap.get(reader));
                }
            };
        }
    }
}

