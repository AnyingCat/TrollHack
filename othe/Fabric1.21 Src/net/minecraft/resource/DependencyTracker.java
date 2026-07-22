/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DependencyTracker<K, V extends Dependencies<K>> {
    private final Map<K, V> underlying = new HashMap();

    public DependencyTracker<K, V> add(K key, V value) {
        this.underlying.put(key, value);
        return this;
    }

    private void traverse(Multimap<K, K> parentChild, Set<K> visited, K rootKey, BiConsumer<K, V> callback) {
        if (!visited.add(rootKey)) {
            return;
        }
        parentChild.get(rootKey).forEach(child -> this.traverse(parentChild, visited, child, callback));
        Dependencies dependencies = (Dependencies)this.underlying.get(rootKey);
        if (dependencies != null) {
            callback.accept(rootKey, dependencies);
        }
    }

    private static <K> boolean containsReverseDependency(Multimap<K, K> dependencies, K key, K dependency) {
        Collection<K> collection = dependencies.get(dependency);
        if (collection.contains(key)) {
            return true;
        }
        return collection.stream().anyMatch(subdependency -> DependencyTracker.containsReverseDependency(dependencies, key, subdependency));
    }

    private static <K> void addDependency(Multimap<K, K> dependencies, K key, K dependency) {
        if (!DependencyTracker.containsReverseDependency(dependencies, key, dependency)) {
            dependencies.put(key, dependency);
        }
    }

    public void traverse(BiConsumer<K, V> callback) {
        HashMultimap multimap = HashMultimap.create();
        this.underlying.forEach((key, value) -> value.forDependencies(dependency -> DependencyTracker.addDependency(multimap, key, dependency)));
        this.underlying.forEach((key, value) -> value.forOptionalDependencies(dependency -> DependencyTracker.addDependency(multimap, key, dependency)));
        HashSet set = new HashSet();
        this.underlying.keySet().forEach(key -> this.traverse(multimap, set, key, callback));
    }

    public static interface Dependencies<K> {
        public void forDependencies(Consumer<K> var1);

        public void forOptionalDependencies(Consumer<K> var1);
    }
}

