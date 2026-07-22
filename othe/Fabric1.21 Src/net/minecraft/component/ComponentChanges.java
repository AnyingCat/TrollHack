/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

public final class ComponentChanges {
    public static final ComponentChanges EMPTY = new ComponentChanges(Reference2ObjectMaps.emptyMap());
    public static final Codec<ComponentChanges> CODEC = Codec.dispatchedMap(Type.CODEC, Type::getValueCodec).xmap(changes -> {
        if (changes.isEmpty()) {
            return EMPTY;
        }
        Reference2ObjectArrayMap reference2ObjectMap = new Reference2ObjectArrayMap(changes.size());
        for (Map.Entry entry : changes.entrySet()) {
            Type type = (Type)entry.getKey();
            if (type.removed()) {
                reference2ObjectMap.put(type.type(), Optional.empty());
                continue;
            }
            reference2ObjectMap.put(type.type(), Optional.of(entry.getValue()));
        }
        return new ComponentChanges(reference2ObjectMap);
    }, changes -> {
        Reference2ObjectArrayMap<Type, Object> reference2ObjectMap = new Reference2ObjectArrayMap<Type, Object>(changes.changedComponents.size());
        for (Map.Entry entry : Reference2ObjectMaps.fastIterable(changes.changedComponents)) {
            ComponentType componentType = (ComponentType)entry.getKey();
            if (componentType.shouldSkipSerialization()) continue;
            Optional optional = (Optional)entry.getValue();
            if (optional.isPresent()) {
                reference2ObjectMap.put(new Type(componentType, false), optional.get());
                continue;
            }
            reference2ObjectMap.put(new Type(componentType, true), (Object)Unit.INSTANCE);
        }
        return reference2ObjectMap;
    });
    public static final PacketCodec<RegistryByteBuf, ComponentChanges> PACKET_CODEC = new PacketCodec<RegistryByteBuf, ComponentChanges>(){

        @Override
        public ComponentChanges decode(RegistryByteBuf registryByteBuf) {
            ComponentType componentType;
            int l;
            int i = registryByteBuf.readVarInt();
            int j = registryByteBuf.readVarInt();
            if (i == 0 && j == 0) {
                return EMPTY;
            }
            int k = i + j;
            Reference2ObjectArrayMap reference2ObjectMap = new Reference2ObjectArrayMap(Math.min(k, 65536));
            for (l = 0; l < i; ++l) {
                componentType = (ComponentType)ComponentType.PACKET_CODEC.decode(registryByteBuf);
                Object object = componentType.getPacketCodec().decode(registryByteBuf);
                reference2ObjectMap.put(componentType, Optional.of(object));
            }
            for (l = 0; l < j; ++l) {
                componentType = (ComponentType)ComponentType.PACKET_CODEC.decode(registryByteBuf);
                reference2ObjectMap.put(componentType, Optional.empty());
            }
            return new ComponentChanges(reference2ObjectMap);
        }

        @Override
        public void encode(RegistryByteBuf registryByteBuf, ComponentChanges componentChanges) {
            if (componentChanges.isEmpty()) {
                registryByteBuf.writeVarInt(0);
                registryByteBuf.writeVarInt(0);
                return;
            }
            int i = 0;
            int j = 0;
            for (Reference2ObjectMap.Entry entry : Reference2ObjectMaps.fastIterable(componentChanges.changedComponents)) {
                if (((Optional)entry.getValue()).isPresent()) {
                    ++i;
                    continue;
                }
                ++j;
            }
            registryByteBuf.writeVarInt(i);
            registryByteBuf.writeVarInt(j);
            for (Reference2ObjectMap.Entry entry : Reference2ObjectMaps.fastIterable(componentChanges.changedComponents)) {
                Optional optional = (Optional)entry.getValue();
                if (!optional.isPresent()) continue;
                ComponentType componentType = (ComponentType)entry.getKey();
                ComponentType.PACKET_CODEC.encode(registryByteBuf, componentType);
                _1.encode(registryByteBuf, componentType, optional.get());
            }
            for (Reference2ObjectMap.Entry entry : Reference2ObjectMaps.fastIterable(componentChanges.changedComponents)) {
                if (!((Optional)entry.getValue()).isEmpty()) continue;
                ComponentType componentType2 = (ComponentType)entry.getKey();
                ComponentType.PACKET_CODEC.encode(registryByteBuf, componentType2);
            }
        }

        private static <T> void encode(RegistryByteBuf buf, ComponentType<T> type, Object value) {
            type.getPacketCodec().encode(buf, value);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((RegistryByteBuf)object, (ComponentChanges)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((RegistryByteBuf)object);
        }
    };
    private static final String REMOVE_PREFIX = "!";
    final Reference2ObjectMap<ComponentType<?>, Optional<?>> changedComponents;

    ComponentChanges(Reference2ObjectMap<ComponentType<?>, Optional<?>> changedComponents) {
        this.changedComponents = changedComponents;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Nullable
    public <T> Optional<? extends T> get(ComponentType<? extends T> type) {
        return (Optional)this.changedComponents.get(type);
    }

    public Set<Map.Entry<ComponentType<?>, Optional<?>>> entrySet() {
        return this.changedComponents.entrySet();
    }

    public int size() {
        return this.changedComponents.size();
    }

    public ComponentChanges withRemovedIf(Predicate<ComponentType<?>> removedTypePredicate) {
        if (this.isEmpty()) {
            return EMPTY;
        }
        Reference2ObjectArrayMap reference2ObjectMap = new Reference2ObjectArrayMap(this.changedComponents);
        reference2ObjectMap.keySet().removeIf(removedTypePredicate);
        if (reference2ObjectMap.isEmpty()) {
            return EMPTY;
        }
        return new ComponentChanges(reference2ObjectMap);
    }

    public boolean isEmpty() {
        return this.changedComponents.isEmpty();
    }

    public AddedRemovedPair toAddedRemovedPair() {
        if (this.isEmpty()) {
            return AddedRemovedPair.EMPTY;
        }
        ComponentMap.Builder builder = ComponentMap.builder();
        Set<ComponentType<?>> set = Sets.newIdentityHashSet();
        this.changedComponents.forEach((type, value) -> {
            if (value.isPresent()) {
                builder.put(type, value.get());
            } else {
                set.add((ComponentType<?>)type);
            }
        });
        return new AddedRemovedPair(builder.build(), set);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ComponentChanges)) return false;
        ComponentChanges componentChanges = (ComponentChanges)o;
        if (!this.changedComponents.equals(componentChanges.changedComponents)) return false;
        return true;
    }

    public int hashCode() {
        return this.changedComponents.hashCode();
    }

    public String toString() {
        return ComponentChanges.toString(this.changedComponents);
    }

    static String toString(Reference2ObjectMap<ComponentType<?>, Optional<?>> changes) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('{');
        boolean bl = true;
        for (Map.Entry entry : Reference2ObjectMaps.fastIterable(changes)) {
            if (bl) {
                bl = false;
            } else {
                stringBuilder.append(", ");
            }
            Optional optional = (Optional)entry.getValue();
            if (optional.isPresent()) {
                stringBuilder.append(entry.getKey());
                stringBuilder.append("=>");
                stringBuilder.append(optional.get());
                continue;
            }
            stringBuilder.append(REMOVE_PREFIX);
            stringBuilder.append(entry.getKey());
        }
        stringBuilder.append('}');
        return stringBuilder.toString();
    }

    public static class Builder {
        private final Reference2ObjectMap<ComponentType<?>, Optional<?>> changes = new Reference2ObjectArrayMap();

        Builder() {
        }

        public <T> Builder add(ComponentType<T> type, T value) {
            this.changes.put(type, Optional.of(value));
            return this;
        }

        public <T> Builder remove(ComponentType<T> type) {
            this.changes.put(type, Optional.empty());
            return this;
        }

        public <T> Builder add(Component<T> component) {
            return this.add(component.type(), component.value());
        }

        public ComponentChanges build() {
            if (this.changes.isEmpty()) {
                return EMPTY;
            }
            return new ComponentChanges(this.changes);
        }
    }

    public record AddedRemovedPair(ComponentMap added, Set<ComponentType<?>> removed) {
        public static final AddedRemovedPair EMPTY = new AddedRemovedPair(ComponentMap.EMPTY, Set.of());
    }

    record Type(ComponentType<?> type, boolean removed) {
        public static final Codec<Type> CODEC = Codec.STRING.flatXmap(id -> {
            Identifier identifier;
            ComponentType<?> componentType;
            boolean bl = id.startsWith(ComponentChanges.REMOVE_PREFIX);
            if (bl) {
                id = id.substring(ComponentChanges.REMOVE_PREFIX.length());
            }
            if ((componentType = Registries.DATA_COMPONENT_TYPE.get(identifier = Identifier.tryParse(id))) == null) {
                return DataResult.error(() -> "No component with type: '" + String.valueOf(identifier) + "'");
            }
            if (componentType.shouldSkipSerialization()) {
                return DataResult.error(() -> "'" + String.valueOf(identifier) + "' is not a persistent component");
            }
            return DataResult.success(new Type(componentType, bl));
        }, type -> {
            ComponentType<?> componentType = type.type();
            Identifier identifier = Registries.DATA_COMPONENT_TYPE.getId(componentType);
            if (identifier == null) {
                return DataResult.error(() -> "Unregistered component: " + String.valueOf(componentType));
            }
            return DataResult.success(type.removed() ? ComponentChanges.REMOVE_PREFIX + String.valueOf(identifier) : identifier.toString());
        });

        public Codec<?> getValueCodec() {
            return this.removed ? Codec.EMPTY.codec() : this.type.getCodecOrThrow();
        }
    }
}

