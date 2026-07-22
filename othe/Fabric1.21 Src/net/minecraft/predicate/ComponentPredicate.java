/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public final class ComponentPredicate
implements Predicate<ComponentMap> {
    public static final Codec<ComponentPredicate> CODEC = ComponentType.TYPE_TO_VALUE_MAP_CODEC.xmap(map -> new ComponentPredicate(map.entrySet().stream().map(Component::of).collect(Collectors.toList())), predicate -> predicate.components.stream().filter(component -> !component.type().shouldSkipSerialization()).collect(Collectors.toMap(Component::type, Component::value)));
    public static final PacketCodec<RegistryByteBuf, ComponentPredicate> PACKET_CODEC = Component.PACKET_CODEC.collect(PacketCodecs.toList()).xmap(ComponentPredicate::new, predicate -> predicate.components);
    public static final ComponentPredicate EMPTY = new ComponentPredicate(List.of());
    private final List<Component<?>> components;

    ComponentPredicate(List<Component<?>> components) {
        this.components = components;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ComponentPredicate of(ComponentMap components) {
        return new ComponentPredicate(ImmutableList.copyOf(components));
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        if (!(o instanceof ComponentPredicate)) return false;
        ComponentPredicate componentPredicate = (ComponentPredicate)o;
        if (!this.components.equals(componentPredicate.components)) return false;
        return true;
    }

    public int hashCode() {
        return this.components.hashCode();
    }

    public String toString() {
        return this.components.toString();
    }

    @Override
    public boolean test(ComponentMap componentMap) {
        for (Component<?> component : this.components) {
            Object object = componentMap.get(component.type());
            if (Objects.equals(component.value(), object)) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean test(ComponentHolder holder) {
        return this.test(holder.getComponents());
    }

    public boolean isEmpty() {
        return this.components.isEmpty();
    }

    public ComponentChanges toChanges() {
        ComponentChanges.Builder builder = ComponentChanges.builder();
        for (Component<?> component : this.components) {
            builder.add(component);
        }
        return builder.build();
    }

    @Override
    public /* synthetic */ boolean test(Object components) {
        return this.test((ComponentMap)components);
    }

    public static class Builder {
        private final List<Component<?>> components = new ArrayList();

        Builder() {
        }

        public <T> Builder add(ComponentType<? super T> type, T value) {
            for (Component<?> component : this.components) {
                if (component.type() != type) continue;
                throw new IllegalArgumentException("Predicate already has component of type: '" + String.valueOf(type) + "'");
            }
            this.components.add(new Component<T>(type, value));
            return this;
        }

        public ComponentPredicate build() {
            return new ComponentPredicate(List.copyOf(this.components));
        }
    }
}

