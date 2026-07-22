/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryCloner;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

public class RegistryBuilder {
    private final List<RegistryInfo<?>> registries = new ArrayList();

    static <T> RegistryEntryLookup<T> toLookup(final RegistryWrapper.Impl<T> wrapper) {
        return new EntryListCreatingLookup<T>(wrapper){

            @Override
            public Optional<RegistryEntry.Reference<T>> getOptional(RegistryKey<T> key) {
                return wrapper.getOptional(key);
            }
        };
    }

    static <T> RegistryWrapper.Impl<T> createWrapper(final RegistryKey<? extends Registry<? extends T>> registryRef, final Lifecycle lifecycle, RegistryEntryOwner<T> owner, final Map<RegistryKey<T>, RegistryEntry.Reference<T>> entries) {
        return new UntaggedLookup<T>(owner){

            @Override
            public RegistryKey<? extends Registry<? extends T>> getRegistryKey() {
                return registryRef;
            }

            @Override
            public Lifecycle getLifecycle() {
                return lifecycle;
            }

            @Override
            public Optional<RegistryEntry.Reference<T>> getOptional(RegistryKey<T> key) {
                return Optional.ofNullable((RegistryEntry.Reference)entries.get(key));
            }

            @Override
            public Stream<RegistryEntry.Reference<T>> streamEntries() {
                return entries.values().stream();
            }
        };
    }

    public <T> RegistryBuilder addRegistry(RegistryKey<? extends Registry<T>> registryRef, Lifecycle lifecycle, BootstrapFunction<T> bootstrapFunction) {
        this.registries.add(new RegistryInfo<T>(registryRef, lifecycle, bootstrapFunction));
        return this;
    }

    public <T> RegistryBuilder addRegistry(RegistryKey<? extends Registry<T>> registryRef, BootstrapFunction<T> bootstrapFunction) {
        return this.addRegistry(registryRef, Lifecycle.stable(), bootstrapFunction);
    }

    private Registries createBootstrappedRegistries(DynamicRegistryManager registryManager) {
        Registries registries = Registries.of(registryManager, this.registries.stream().map(RegistryInfo::key));
        this.registries.forEach(registry -> registry.runBootstrap(registries));
        return registries;
    }

    private static RegistryWrapper.WrapperLookup createWrapperLookup(AnyOwner entryOwner, DynamicRegistryManager registryManager, Stream<RegistryWrapper.Impl<?>> wrappers) {
        record WrapperInfoPair<T>(RegistryWrapper.Impl<T> lookup, RegistryOps.RegistryInfo<T> opsInfo) {
            public static <T> WrapperInfoPair<T> of(RegistryWrapper.Impl<T> wrapper) {
                return new WrapperInfoPair<T>(new UntaggedDelegatingLookup<T>(wrapper, wrapper), RegistryOps.RegistryInfo.fromWrapper(wrapper));
            }

            public static <T> WrapperInfoPair<T> of(AnyOwner owner, RegistryWrapper.Impl<T> wrapper) {
                return new WrapperInfoPair(new UntaggedDelegatingLookup(owner.downcast(), wrapper), new RegistryOps.RegistryInfo(owner.downcast(), wrapper, wrapper.getLifecycle()));
            }
        }
        final HashMap map = new HashMap();
        registryManager.streamAllRegistries().forEach(registry -> map.put(registry.key(), WrapperInfoPair.of(registry.value().getReadOnlyWrapper())));
        wrappers.forEach(wrapper -> map.put(wrapper.getRegistryKey(), WrapperInfoPair.of(entryOwner, wrapper)));
        return new RegistryWrapper.WrapperLookup(){

            @Override
            public Stream<RegistryKey<? extends Registry<?>>> streamAllRegistryKeys() {
                return map.keySet().stream();
            }

            <T> Optional<WrapperInfoPair<T>> get(RegistryKey<? extends Registry<? extends T>> registryRef) {
                return Optional.ofNullable((WrapperInfoPair)map.get(registryRef));
            }

            @Override
            public <T> Optional<RegistryWrapper.Impl<T>> getOptionalWrapper(RegistryKey<? extends Registry<? extends T>> registryRef) {
                return this.get(registryRef).map(WrapperInfoPair::lookup);
            }

            @Override
            public <V> RegistryOps<V> getOps(DynamicOps<V> delegate) {
                return RegistryOps.of(delegate, new RegistryOps.RegistryInfoGetter(){

                    @Override
                    public <T> Optional<RegistryOps.RegistryInfo<T>> getRegistryInfo(RegistryKey<? extends Registry<? extends T>> registryRef) {
                        return this.get(registryRef).map(WrapperInfoPair::opsInfo);
                    }
                });
            }
        };
    }

    public RegistryWrapper.WrapperLookup createWrapperLookup(DynamicRegistryManager registryManager) {
        Registries registries = this.createBootstrappedRegistries(registryManager);
        Stream<RegistryWrapper.Impl<?>> stream = this.registries.stream().map(info -> info.init(registries).toWrapper(registries.owner));
        RegistryWrapper.WrapperLookup wrapperLookup = RegistryBuilder.createWrapperLookup(registries.owner, registryManager, stream);
        registries.checkUnreferencedKeys();
        registries.checkOrphanedValues();
        registries.throwErrors();
        return wrapperLookup;
    }

    private RegistryWrapper.WrapperLookup createFullWrapperLookup(DynamicRegistryManager registryManager, RegistryWrapper.WrapperLookup base, RegistryCloner.CloneableRegistries cloneableRegistries, Map<RegistryKey<? extends Registry<?>>, InitializedRegistry<?>> initializedRegistries, RegistryWrapper.WrapperLookup patches) {
        AnyOwner anyOwner = new AnyOwner();
        MutableObject<RegistryWrapper.WrapperLookup> mutableObject = new MutableObject<RegistryWrapper.WrapperLookup>();
        List list = initializedRegistries.keySet().stream().map(registryRef -> this.applyPatches(anyOwner, cloneableRegistries, (RegistryKey)registryRef, patches, base, mutableObject)).collect(Collectors.toUnmodifiableList());
        RegistryWrapper.WrapperLookup wrapperLookup = RegistryBuilder.createWrapperLookup(anyOwner, registryManager, list.stream());
        mutableObject.setValue(wrapperLookup);
        return wrapperLookup;
    }

    private <T> RegistryWrapper.Impl<T> applyPatches(RegistryEntryOwner<T> owner, RegistryCloner.CloneableRegistries cloneableRegistries, RegistryKey<? extends Registry<? extends T>> registryRef, RegistryWrapper.WrapperLookup patches, RegistryWrapper.WrapperLookup base, MutableObject<RegistryWrapper.WrapperLookup> lazyWrapper) {
        RegistryCloner registryCloner = cloneableRegistries.get(registryRef);
        if (registryCloner == null) {
            throw new NullPointerException("No cloner for " + String.valueOf(registryRef.getValue()));
        }
        HashMap map = new HashMap();
        RegistryWrapper.Impl impl = patches.getWrapperOrThrow(registryRef);
        impl.streamEntries().forEach(entry -> {
            RegistryKey registryKey = entry.registryKey();
            LazyReferenceEntry lazyReferenceEntry = new LazyReferenceEntry(owner, registryKey);
            lazyReferenceEntry.supplier = () -> registryCloner.clone(entry.value(), patches, (RegistryWrapper.WrapperLookup)lazyWrapper.getValue());
            map.put(registryKey, lazyReferenceEntry);
        });
        RegistryWrapper.Impl impl2 = base.getWrapperOrThrow(registryRef);
        impl2.streamEntries().forEach(entry -> {
            RegistryKey registryKey = entry.registryKey();
            map.computeIfAbsent(registryKey, key -> {
                LazyReferenceEntry lazyReferenceEntry = new LazyReferenceEntry(owner, registryKey);
                lazyReferenceEntry.supplier = () -> registryCloner.clone(entry.value(), base, (RegistryWrapper.WrapperLookup)lazyWrapper.getValue());
                return lazyReferenceEntry;
            });
        });
        Lifecycle lifecycle = impl.getLifecycle().add(impl2.getLifecycle());
        return RegistryBuilder.createWrapper(registryRef, lifecycle, owner, map);
    }

    public FullPatchesRegistriesPair createWrapperLookup(DynamicRegistryManager baseRegistryManager, RegistryWrapper.WrapperLookup wrapperLookup, RegistryCloner.CloneableRegistries cloneableRegistries) {
        Registries registries = this.createBootstrappedRegistries(baseRegistryManager);
        HashMap map = new HashMap();
        this.registries.stream().map(info -> info.init(registries)).forEach(registry -> map.put((RegistryKey<Registry<?>>)registry.key, (InitializedRegistry<?>)registry));
        Set set = baseRegistryManager.streamAllRegistryKeys().collect(Collectors.toUnmodifiableSet());
        wrapperLookup.streamAllRegistryKeys().filter(key -> !set.contains(key)).forEach(key -> map.putIfAbsent((RegistryKey<Registry<?>>)key, new InitializedRegistry(key, Lifecycle.stable(), Map.of())));
        Stream<RegistryWrapper.Impl<?>> stream = map.values().stream().map(registry -> registry.toWrapper(registries.owner));
        RegistryWrapper.WrapperLookup wrapperLookup2 = RegistryBuilder.createWrapperLookup(registries.owner, baseRegistryManager, stream);
        registries.checkOrphanedValues();
        registries.throwErrors();
        RegistryWrapper.WrapperLookup wrapperLookup3 = this.createFullWrapperLookup(baseRegistryManager, wrapperLookup, cloneableRegistries, map, wrapperLookup2);
        return new FullPatchesRegistriesPair(wrapperLookup3, wrapperLookup2);
    }

    record RegistryInfo<T>(RegistryKey<? extends Registry<T>> key, Lifecycle lifecycle, BootstrapFunction<T> bootstrap) {
        void runBootstrap(Registries registries) {
            this.bootstrap.run(registries.createRegisterable());
        }

        public InitializedRegistry<T> init(Registries registries) {
            HashMap map = new HashMap();
            Iterator<Map.Entry<RegistryKey<?>, RegisteredValue<?>>> iterator = registries.registeredValues.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<RegistryKey<?>, RegisteredValue<?>> entry = iterator.next();
                RegistryKey<?> registryKey = entry.getKey();
                if (!registryKey.isOf(this.key)) continue;
                RegistryKey<?> registryKey2 = registryKey;
                RegisteredValue<?> registeredValue = entry.getValue();
                RegistryEntry.Reference<Object> reference = registries.lookup.keysToEntries.remove(registryKey);
                map.put(registryKey2, new EntryAssociatedValue(registeredValue, Optional.ofNullable(reference)));
                iterator.remove();
            }
            return new InitializedRegistry(this.key, this.lifecycle, map);
        }
    }

    @FunctionalInterface
    public static interface BootstrapFunction<T> {
        public void run(Registerable<T> var1);
    }

    record Registries(AnyOwner owner, StandAloneEntryCreatingLookup lookup, Map<Identifier, RegistryEntryLookup<?>> registries, Map<RegistryKey<?>, RegisteredValue<?>> registeredValues, List<RuntimeException> errors) {
        public static Registries of(DynamicRegistryManager dynamicRegistryManager, Stream<RegistryKey<? extends Registry<?>>> registryRefs) {
            AnyOwner anyOwner = new AnyOwner();
            ArrayList<RuntimeException> list = new ArrayList<RuntimeException>();
            StandAloneEntryCreatingLookup standAloneEntryCreatingLookup = new StandAloneEntryCreatingLookup(anyOwner);
            ImmutableMap.Builder builder = ImmutableMap.builder();
            dynamicRegistryManager.streamAllRegistries().forEach(entry -> builder.put(entry.key().getValue(), RegistryBuilder.toLookup(entry.value().getReadOnlyWrapper())));
            registryRefs.forEach(registryRef -> builder.put(registryRef.getValue(), standAloneEntryCreatingLookup));
            return new Registries(anyOwner, standAloneEntryCreatingLookup, builder.build(), new HashMap(), list);
        }

        public <T> Registerable<T> createRegisterable() {
            return new Registerable<T>(){

                @Override
                public RegistryEntry.Reference<T> register(RegistryKey<T> key, T value, Lifecycle lifecycle) {
                    RegisteredValue registeredValue = registeredValues.put(key, new RegisteredValue(value, lifecycle));
                    if (registeredValue != null) {
                        errors.add(new IllegalStateException("Duplicate registration for " + String.valueOf(key) + ", new=" + String.valueOf(value) + ", old=" + String.valueOf(registeredValue.value)));
                    }
                    return lookup.getOrCreate(key);
                }

                @Override
                public <S> RegistryEntryLookup<S> getRegistryLookup(RegistryKey<? extends Registry<? extends S>> registryRef) {
                    return registries.getOrDefault(registryRef.getValue(), lookup);
                }
            };
        }

        public void checkOrphanedValues() {
            this.registeredValues.forEach((key, value) -> this.errors.add(new IllegalStateException("Orpaned value " + String.valueOf(value.value) + " for key " + String.valueOf(key))));
        }

        public void checkUnreferencedKeys() {
            for (RegistryKey<Object> registryKey : this.lookup.keysToEntries.keySet()) {
                this.errors.add(new IllegalStateException("Unreferenced key: " + String.valueOf(registryKey)));
            }
        }

        public void throwErrors() {
            if (!this.errors.isEmpty()) {
                IllegalStateException illegalStateException = new IllegalStateException("Errors during registry creation");
                for (RuntimeException runtimeException : this.errors) {
                    illegalStateException.addSuppressed(runtimeException);
                }
                throw illegalStateException;
            }
        }
    }

    static class AnyOwner
    implements RegistryEntryOwner<Object> {
        AnyOwner() {
        }

        public <T> RegistryEntryOwner<T> downcast() {
            return this;
        }
    }

    public record FullPatchesRegistriesPair(RegistryWrapper.WrapperLookup full, RegistryWrapper.WrapperLookup patches) {
    }

    record InitializedRegistry<T>(RegistryKey<? extends Registry<? extends T>> key, Lifecycle lifecycle, Map<RegistryKey<T>, EntryAssociatedValue<T>> values) {
        public RegistryWrapper.Impl<T> toWrapper(AnyOwner anyOwner) {
            Map map = this.values.entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> {
                EntryAssociatedValue entryAssociatedValue = (EntryAssociatedValue)entry.getValue();
                RegistryEntry.Reference reference = entryAssociatedValue.entry().orElseGet(() -> RegistryEntry.Reference.standAlone(anyOwner.downcast(), (RegistryKey)entry.getKey()));
                reference.setValue(entryAssociatedValue.value().value());
                return reference;
            }));
            return RegistryBuilder.createWrapper(this.key, this.lifecycle, anyOwner.downcast(), map);
        }
    }

    static class LazyReferenceEntry<T>
    extends RegistryEntry.Reference<T> {
        @Nullable
        Supplier<T> supplier;

        protected LazyReferenceEntry(RegistryEntryOwner<T> owner, @Nullable RegistryKey<T> key) {
            super(RegistryEntry.Reference.Type.STAND_ALONE, owner, key, null);
        }

        @Override
        protected void setValue(T value) {
            super.setValue(value);
            this.supplier = null;
        }

        @Override
        public T value() {
            if (this.supplier != null) {
                this.setValue(this.supplier.get());
            }
            return super.value();
        }
    }

    record EntryAssociatedValue<T>(RegisteredValue<T> value, Optional<RegistryEntry.Reference<T>> entry) {
    }

    record RegisteredValue<T>(T value, Lifecycle lifecycle) {
    }

    static class StandAloneEntryCreatingLookup
    extends EntryListCreatingLookup<Object> {
        final Map<RegistryKey<Object>, RegistryEntry.Reference<Object>> keysToEntries = new HashMap<RegistryKey<Object>, RegistryEntry.Reference<Object>>();

        public StandAloneEntryCreatingLookup(RegistryEntryOwner<Object> registryEntryOwner) {
            super(registryEntryOwner);
        }

        @Override
        public Optional<RegistryEntry.Reference<Object>> getOptional(RegistryKey<Object> key) {
            return Optional.of(this.getOrCreate(key));
        }

        <T> RegistryEntry.Reference<T> getOrCreate(RegistryKey<T> key) {
            return this.keysToEntries.computeIfAbsent(key, key2 -> RegistryEntry.Reference.standAlone(this.entryOwner, key2));
        }
    }

    static class UntaggedDelegatingLookup<T>
    extends UntaggedLookup<T>
    implements RegistryWrapper.Impl.Delegating<T> {
        private final RegistryWrapper.Impl<T> base;

        UntaggedDelegatingLookup(RegistryEntryOwner<T> entryOwner, RegistryWrapper.Impl<T> base) {
            super(entryOwner);
            this.base = base;
        }

        @Override
        public RegistryWrapper.Impl<T> getBase() {
            return this.base;
        }
    }

    static abstract class UntaggedLookup<T>
    extends EntryListCreatingLookup<T>
    implements RegistryWrapper.Impl<T> {
        protected UntaggedLookup(RegistryEntryOwner<T> registryEntryOwner) {
            super(registryEntryOwner);
        }

        @Override
        public Stream<RegistryEntryList.Named<T>> streamTags() {
            throw new UnsupportedOperationException("Tags are not available in datagen");
        }
    }

    static abstract class EntryListCreatingLookup<T>
    implements RegistryEntryLookup<T> {
        protected final RegistryEntryOwner<T> entryOwner;

        protected EntryListCreatingLookup(RegistryEntryOwner<T> entryOwner) {
            this.entryOwner = entryOwner;
        }

        @Override
        public Optional<RegistryEntryList.Named<T>> getOptional(TagKey<T> tag) {
            return Optional.of(RegistryEntryList.of(this.entryOwner, tag));
        }
    }
}

