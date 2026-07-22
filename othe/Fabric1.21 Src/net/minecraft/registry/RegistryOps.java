/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.dynamic.ForwardingDynamicOps;

public class RegistryOps<T>
extends ForwardingDynamicOps<T> {
    private final RegistryInfoGetter registryInfoGetter;

    public static <T> RegistryOps<T> of(DynamicOps<T> delegate, RegistryWrapper.WrapperLookup wrapperLookup) {
        return RegistryOps.of(delegate, new CachedRegistryInfoGetter(wrapperLookup));
    }

    public static <T> RegistryOps<T> of(DynamicOps<T> delegate, RegistryInfoGetter registryInfoGetter) {
        return new RegistryOps<T>(delegate, registryInfoGetter);
    }

    public static <T> Dynamic<T> withRegistry(Dynamic<T> dynamic, RegistryWrapper.WrapperLookup registryLookup) {
        return new Dynamic(registryLookup.getOps(dynamic.getOps()), dynamic.getValue());
    }

    private RegistryOps(DynamicOps<T> delegate, RegistryInfoGetter registryInfoGetter) {
        super(delegate);
        this.registryInfoGetter = registryInfoGetter;
    }

    public <U> RegistryOps<U> withDelegate(DynamicOps<U> delegate) {
        if (delegate == this.delegate) {
            return this;
        }
        return new RegistryOps<U>(delegate, this.registryInfoGetter);
    }

    public <E> Optional<RegistryEntryOwner<E>> getOwner(RegistryKey<? extends Registry<? extends E>> registryRef) {
        return this.registryInfoGetter.getRegistryInfo(registryRef).map(RegistryInfo::owner);
    }

    public <E> Optional<RegistryEntryLookup<E>> getEntryLookup(RegistryKey<? extends Registry<? extends E>> registryRef) {
        return this.registryInfoGetter.getRegistryInfo(registryRef).map(RegistryInfo::entryLookup);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        RegistryOps registryOps = (RegistryOps)o;
        return this.delegate.equals(registryOps.delegate) && this.registryInfoGetter.equals(registryOps.registryInfoGetter);
    }

    public int hashCode() {
        return this.delegate.hashCode() * 31 + this.registryInfoGetter.hashCode();
    }

    public static <E, O> RecordCodecBuilder<O, RegistryEntryLookup<E>> getEntryLookupCodec(RegistryKey<? extends Registry<? extends E>> registryRef) {
        return Codecs.createContextRetrievalCodec(ops -> {
            if (ops instanceof RegistryOps) {
                RegistryOps registryOps = (RegistryOps)ops;
                return registryOps.registryInfoGetter.getRegistryInfo(registryRef).map(info -> DataResult.success(info.entryLookup(), info.elementsLifecycle())).orElseGet(() -> DataResult.error(() -> "Unknown registry: " + String.valueOf(registryRef)));
            }
            return DataResult.error(() -> "Not a registry ops");
        }).forGetter(object -> null);
    }

    public static <E, O> RecordCodecBuilder<O, RegistryEntry.Reference<E>> getEntryCodec(RegistryKey<E> key) {
        RegistryKey registryKey = RegistryKey.ofRegistry(key.getRegistry());
        return Codecs.createContextRetrievalCodec(ops -> {
            if (ops instanceof RegistryOps) {
                RegistryOps registryOps = (RegistryOps)ops;
                return registryOps.registryInfoGetter.getRegistryInfo(registryKey).flatMap(info -> info.entryLookup().getOptional(key)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Can't find value: " + String.valueOf(key)));
            }
            return DataResult.error(() -> "Not a registry ops");
        }).forGetter(object -> null);
    }

    static final class CachedRegistryInfoGetter
    implements RegistryInfoGetter {
        private final RegistryWrapper.WrapperLookup registriesLookup;
        private final Map<RegistryKey<? extends Registry<?>>, Optional<? extends RegistryInfo<?>>> cache = new ConcurrentHashMap();

        public CachedRegistryInfoGetter(RegistryWrapper.WrapperLookup registriesLookup) {
            this.registriesLookup = registriesLookup;
        }

        public <E> Optional<RegistryInfo<E>> getRegistryInfo(RegistryKey<? extends Registry<? extends E>> registryRef) {
            return this.cache.computeIfAbsent(registryRef, this::compute);
        }

        private Optional<RegistryInfo<Object>> compute(RegistryKey<? extends Registry<?>> registryRef) {
            return this.registriesLookup.getOptionalWrapper(registryRef).map(RegistryInfo::fromWrapper);
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof CachedRegistryInfoGetter)) return false;
            CachedRegistryInfoGetter cachedRegistryInfoGetter = (CachedRegistryInfoGetter)o;
            if (!this.registriesLookup.equals(cachedRegistryInfoGetter.registriesLookup)) return false;
            return true;
        }

        public int hashCode() {
            return this.registriesLookup.hashCode();
        }
    }

    public static interface RegistryInfoGetter {
        public <T> Optional<RegistryInfo<T>> getRegistryInfo(RegistryKey<? extends Registry<? extends T>> var1);
    }

    public record RegistryInfo<T>(RegistryEntryOwner<T> owner, RegistryEntryLookup<T> entryLookup, Lifecycle elementsLifecycle) {
        public static <T> RegistryInfo<T> fromWrapper(RegistryWrapper.Impl<T> wrapper) {
            return new RegistryInfo<T>(wrapper, wrapper, wrapper.getLifecycle());
        }
    }
}

