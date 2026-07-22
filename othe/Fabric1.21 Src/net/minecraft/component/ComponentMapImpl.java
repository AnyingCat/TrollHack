/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import org.jetbrains.annotations.Nullable;

public final class ComponentMapImpl
implements ComponentMap {
    private final ComponentMap baseComponents;
    private Reference2ObjectMap<ComponentType<?>, Optional<?>> changedComponents;
    private boolean copyOnWrite;

    public ComponentMapImpl(ComponentMap baseComponents) {
        this(baseComponents, Reference2ObjectMaps.emptyMap(), true);
    }

    private ComponentMapImpl(ComponentMap baseComponents, Reference2ObjectMap<ComponentType<?>, Optional<?>> changedComponents, boolean copyOnWrite) {
        this.baseComponents = baseComponents;
        this.changedComponents = changedComponents;
        this.copyOnWrite = copyOnWrite;
    }

    public static ComponentMapImpl create(ComponentMap baseComponents, ComponentChanges changes) {
        if (ComponentMapImpl.shouldReuseChangesMap(baseComponents, changes.changedComponents)) {
            return new ComponentMapImpl(baseComponents, changes.changedComponents, true);
        }
        ComponentMapImpl componentMapImpl = new ComponentMapImpl(baseComponents);
        componentMapImpl.applyChanges(changes);
        return componentMapImpl;
    }

    private static boolean shouldReuseChangesMap(ComponentMap baseComponents, Reference2ObjectMap<ComponentType<?>, Optional<?>> changedComponents) {
        for (Map.Entry entry : Reference2ObjectMaps.fastIterable(changedComponents)) {
            Object object = baseComponents.get((ComponentType)entry.getKey());
            Optional optional = (Optional)entry.getValue();
            if (optional.isPresent() && optional.get().equals(object)) {
                return false;
            }
            if (!optional.isEmpty() || object != null) continue;
            return false;
        }
        return true;
    }

    @Override
    @Nullable
    public <T> T get(ComponentType<? extends T> type) {
        Optional optional = (Optional)this.changedComponents.get(type);
        if (optional != null) {
            return optional.orElse(null);
        }
        return this.baseComponents.get(type);
    }

    @Nullable
    public <T> T set(ComponentType<? super T> type, @Nullable T value) {
        this.onWrite();
        T object = this.baseComponents.get(type);
        Optional<Object> optional = Objects.equals(value, object) ? this.changedComponents.remove(type) : this.changedComponents.put(type, Optional.ofNullable(value));
        if (optional != null) {
            return (T)optional.orElse(object);
        }
        return object;
    }

    @Nullable
    public <T> T remove(ComponentType<? extends T> type) {
        this.onWrite();
        T object = this.baseComponents.get(type);
        Optional<Object> optional = object != null ? this.changedComponents.put(type, Optional.empty()) : this.changedComponents.remove(type);
        if (optional != null) {
            return optional.orElse(null);
        }
        return object;
    }

    public void applyChanges(ComponentChanges changes) {
        this.onWrite();
        for (Map.Entry entry : Reference2ObjectMaps.fastIterable(changes.changedComponents)) {
            this.applyChange((ComponentType)entry.getKey(), (Optional)entry.getValue());
        }
    }

    private void applyChange(ComponentType<?> type, Optional<?> optional) {
        Object object = this.baseComponents.get(type);
        if (optional.isPresent()) {
            if (optional.get().equals(object)) {
                this.changedComponents.remove(type);
            } else {
                this.changedComponents.put(type, optional);
            }
        } else if (object != null) {
            this.changedComponents.put(type, Optional.empty());
        } else {
            this.changedComponents.remove(type);
        }
    }

    public void setChanges(ComponentChanges changes) {
        this.onWrite();
        this.changedComponents.clear();
        this.changedComponents.putAll(changes.changedComponents);
    }

    public void setAll(ComponentMap components) {
        for (Component<?> component : components) {
            component.apply(this);
        }
    }

    private void onWrite() {
        if (this.copyOnWrite) {
            this.changedComponents = new Reference2ObjectArrayMap(this.changedComponents);
            this.copyOnWrite = false;
        }
    }

    @Override
    public Set<ComponentType<?>> getTypes() {
        if (this.changedComponents.isEmpty()) {
            return this.baseComponents.getTypes();
        }
        ReferenceArraySet set = new ReferenceArraySet(this.baseComponents.getTypes());
        for (Reference2ObjectMap.Entry entry : Reference2ObjectMaps.fastIterable(this.changedComponents)) {
            Optional optional = (Optional)entry.getValue();
            if (optional.isPresent()) {
                set.add((ComponentType)entry.getKey());
                continue;
            }
            set.remove(entry.getKey());
        }
        return set;
    }

    @Override
    public Iterator<Component<?>> iterator() {
        if (this.changedComponents.isEmpty()) {
            return this.baseComponents.iterator();
        }
        ArrayList list = new ArrayList(this.changedComponents.size() + this.baseComponents.size());
        for (Reference2ObjectMap.Entry entry : Reference2ObjectMaps.fastIterable(this.changedComponents)) {
            if (!((Optional)entry.getValue()).isPresent()) continue;
            list.add(Component.of((ComponentType)entry.getKey(), ((Optional)entry.getValue()).get()));
        }
        for (Component<?> component : this.baseComponents) {
            if (this.changedComponents.containsKey(component.type())) continue;
            list.add(component);
        }
        return list.iterator();
    }

    @Override
    public int size() {
        int i = this.baseComponents.size();
        for (Reference2ObjectMap.Entry entry : Reference2ObjectMaps.fastIterable(this.changedComponents)) {
            boolean bl2;
            boolean bl = ((Optional)entry.getValue()).isPresent();
            if (bl == (bl2 = this.baseComponents.contains((ComponentType)entry.getKey()))) continue;
            i += bl ? 1 : -1;
        }
        return i;
    }

    public ComponentChanges getChanges() {
        if (this.changedComponents.isEmpty()) {
            return ComponentChanges.EMPTY;
        }
        this.copyOnWrite = true;
        return new ComponentChanges(this.changedComponents);
    }

    public ComponentMapImpl copy() {
        this.copyOnWrite = true;
        return new ComponentMapImpl(this.baseComponents, this.changedComponents, true);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ComponentMapImpl)) return false;
        ComponentMapImpl componentMapImpl = (ComponentMapImpl)o;
        if (!this.baseComponents.equals(componentMapImpl.baseComponents)) return false;
        if (!this.changedComponents.equals(componentMapImpl.changedComponents)) return false;
        return true;
    }

    public int hashCode() {
        return this.baseComponents.hashCode() + this.changedComponents.hashCode() * 31;
    }

    public String toString() {
        return "{" + this.stream().map(Component::toString).collect(Collectors.joining(", ")) + "}";
    }
}

