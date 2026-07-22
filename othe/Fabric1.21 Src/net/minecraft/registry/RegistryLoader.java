/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.provider.EnchantmentProvider;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.passive.WolfVariant;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.message.MessageType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.SerializableRegistries;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.VersionedIdentifier;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.FlatLevelGeneratorPreset;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.structure.Structure;
import org.slf4j.Logger;

public class RegistryLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final RegistryEntryInfo EXPERIMENTAL_ENTRY_INFO = new RegistryEntryInfo(Optional.empty(), Lifecycle.experimental());
    private static final Function<Optional<VersionedIdentifier>, RegistryEntryInfo> RESOURCE_ENTRY_INFO_GETTER = Util.memoize(knownPacks -> {
        Lifecycle lifecycle = knownPacks.map(VersionedIdentifier::isVanilla).map(vanilla -> Lifecycle.stable()).orElse(Lifecycle.experimental());
        return new RegistryEntryInfo((Optional<VersionedIdentifier>)knownPacks, lifecycle);
    });
    public static final List<Entry<?>> DYNAMIC_REGISTRIES = List.of(new Entry<DimensionType>(RegistryKeys.DIMENSION_TYPE, DimensionType.CODEC), new Entry<Biome>(RegistryKeys.BIOME, Biome.CODEC), new Entry<MessageType>(RegistryKeys.MESSAGE_TYPE, MessageType.CODEC), new Entry(RegistryKeys.CONFIGURED_CARVER, ConfiguredCarver.CODEC), new Entry(RegistryKeys.CONFIGURED_FEATURE, ConfiguredFeature.CODEC), new Entry<PlacedFeature>(RegistryKeys.PLACED_FEATURE, PlacedFeature.CODEC), new Entry<Structure>(RegistryKeys.STRUCTURE, Structure.STRUCTURE_CODEC), new Entry<StructureSet>(RegistryKeys.STRUCTURE_SET, StructureSet.CODEC), new Entry<StructureProcessorList>(RegistryKeys.PROCESSOR_LIST, StructureProcessorType.PROCESSORS_CODEC), new Entry<StructurePool>(RegistryKeys.TEMPLATE_POOL, StructurePool.CODEC), new Entry<ChunkGeneratorSettings>(RegistryKeys.CHUNK_GENERATOR_SETTINGS, ChunkGeneratorSettings.CODEC), new Entry<DoublePerlinNoiseSampler.NoiseParameters>(RegistryKeys.NOISE_PARAMETERS, DoublePerlinNoiseSampler.NoiseParameters.CODEC), new Entry<DensityFunction>(RegistryKeys.DENSITY_FUNCTION, DensityFunction.CODEC), new Entry<WorldPreset>(RegistryKeys.WORLD_PRESET, WorldPreset.CODEC), new Entry<FlatLevelGeneratorPreset>(RegistryKeys.FLAT_LEVEL_GENERATOR_PRESET, FlatLevelGeneratorPreset.CODEC), new Entry<ArmorTrimPattern>(RegistryKeys.TRIM_PATTERN, ArmorTrimPattern.CODEC), new Entry<ArmorTrimMaterial>(RegistryKeys.TRIM_MATERIAL, ArmorTrimMaterial.CODEC), new Entry<WolfVariant>(RegistryKeys.WOLF_VARIANT, WolfVariant.CODEC, true), new Entry<PaintingVariant>(RegistryKeys.PAINTING_VARIANT, PaintingVariant.CODEC, true), new Entry<DamageType>(RegistryKeys.DAMAGE_TYPE, DamageType.CODEC), new Entry<MultiNoiseBiomeSourceParameterList>(RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, MultiNoiseBiomeSourceParameterList.CODEC), new Entry<BannerPattern>(RegistryKeys.BANNER_PATTERN, BannerPattern.CODEC), new Entry<Enchantment>(RegistryKeys.ENCHANTMENT, Enchantment.CODEC), new Entry<EnchantmentProvider>(RegistryKeys.ENCHANTMENT_PROVIDER, EnchantmentProvider.CODEC), new Entry<JukeboxSong>(RegistryKeys.JUKEBOX_SONG, JukeboxSong.CODEC));
    public static final List<Entry<?>> DIMENSION_REGISTRIES = List.of(new Entry<DimensionOptions>(RegistryKeys.DIMENSION, DimensionOptions.CODEC));
    public static final List<Entry<?>> SYNCED_REGISTRIES = List.of(new Entry<Biome>(RegistryKeys.BIOME, Biome.NETWORK_CODEC), new Entry<MessageType>(RegistryKeys.MESSAGE_TYPE, MessageType.CODEC), new Entry<ArmorTrimPattern>(RegistryKeys.TRIM_PATTERN, ArmorTrimPattern.CODEC), new Entry<ArmorTrimMaterial>(RegistryKeys.TRIM_MATERIAL, ArmorTrimMaterial.CODEC), new Entry<WolfVariant>(RegistryKeys.WOLF_VARIANT, WolfVariant.CODEC, true), new Entry<PaintingVariant>(RegistryKeys.PAINTING_VARIANT, PaintingVariant.CODEC, true), new Entry<DimensionType>(RegistryKeys.DIMENSION_TYPE, DimensionType.CODEC), new Entry<DamageType>(RegistryKeys.DAMAGE_TYPE, DamageType.CODEC), new Entry<BannerPattern>(RegistryKeys.BANNER_PATTERN, BannerPattern.CODEC), new Entry<Enchantment>(RegistryKeys.ENCHANTMENT, Enchantment.CODEC), new Entry<JukeboxSong>(RegistryKeys.JUKEBOX_SONG, JukeboxSong.CODEC));

    public static DynamicRegistryManager.Immutable loadFromResource(ResourceManager resourceManager, DynamicRegistryManager registryManager, List<Entry<?>> entries) {
        return RegistryLoader.load((loader, infoGetter) -> loader.loadFromResource(resourceManager, infoGetter), registryManager, entries);
    }

    public static DynamicRegistryManager.Immutable loadFromNetwork(Map<RegistryKey<? extends Registry<?>>, List<SerializableRegistries.SerializedRegistryEntry>> data, ResourceFactory factory, DynamicRegistryManager registryManager, List<Entry<?>> entries) {
        return RegistryLoader.load((loader, infoGetter) -> loader.loadFromNetwork(data, factory, infoGetter), registryManager, entries);
    }

    private static DynamicRegistryManager.Immutable load(RegistryLoadable loadable, DynamicRegistryManager baseRegistryManager, List<Entry<?>> entries) {
        HashMap map = new HashMap();
        List<Loader<?>> list = entries.stream().map(entry -> entry.getLoader(Lifecycle.stable(), map)).collect(Collectors.toUnmodifiableList());
        RegistryOps.RegistryInfoGetter registryInfoGetter = RegistryLoader.createInfoGetter(baseRegistryManager, list);
        list.forEach(loader -> loadable.apply((Loader<?>)loader, registryInfoGetter));
        list.forEach(loader -> {
            MutableRegistry registry = loader.registry();
            try {
                registry.freeze();
            } catch (Exception exception) {
                map.put(registry.getKey(), exception);
            }
            if (loader.data.requiredNonEmpty && registry.size() == 0) {
                map.put(registry.getKey(), new IllegalStateException("Registry must be non-empty"));
            }
        });
        if (!map.isEmpty()) {
            RegistryLoader.writeLoadingError(map);
            throw new IllegalStateException("Failed to load registries due to above errors");
        }
        return new DynamicRegistryManager.ImmutableImpl(list.stream().map(Loader::registry).toList()).toImmutable();
    }

    private static RegistryOps.RegistryInfoGetter createInfoGetter(DynamicRegistryManager baseRegistryManager, List<Loader<?>> additionalRegistries) {
        final HashMap map = new HashMap();
        baseRegistryManager.streamAllRegistries().forEach(entry -> map.put(entry.key(), RegistryLoader.createInfo(entry.value())));
        additionalRegistries.forEach(loader -> map.put(loader.registry.getKey(), RegistryLoader.createInfo(loader.registry)));
        return new RegistryOps.RegistryInfoGetter(){

            @Override
            public <T> Optional<RegistryOps.RegistryInfo<T>> getRegistryInfo(RegistryKey<? extends Registry<? extends T>> registryRef) {
                return Optional.ofNullable((RegistryOps.RegistryInfo)map.get(registryRef));
            }
        };
    }

    private static <T> RegistryOps.RegistryInfo<T> createInfo(MutableRegistry<T> registry) {
        return new RegistryOps.RegistryInfo(registry.getReadOnlyWrapper(), registry.createMutableEntryLookup(), registry.getLifecycle());
    }

    private static <T> RegistryOps.RegistryInfo<T> createInfo(Registry<T> registry) {
        return new RegistryOps.RegistryInfo<T>(registry.getReadOnlyWrapper(), registry.getTagCreatingWrapper(), registry.getLifecycle());
    }

    private static void writeLoadingError(Map<RegistryKey<?>, Exception> exceptions) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        Map<Identifier, Map<Identifier, Exception>> map = exceptions.entrySet().stream().collect(Collectors.groupingBy(entry -> ((RegistryKey)entry.getKey()).getRegistry(), Collectors.toMap(entry -> ((RegistryKey)entry.getKey()).getValue(), Map.Entry::getValue)));
        map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            printWriter.printf("> Errors in registry %s:%n", entry.getKey());
            ((Map)entry.getValue()).entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(elementEntry -> {
                printWriter.printf(">> Errors in element %s:%n", elementEntry.getKey());
                ((Exception)elementEntry.getValue()).printStackTrace(printWriter);
            });
        });
        printWriter.flush();
        LOGGER.error("Registry loading errors:\n{}", (Object)stringWriter);
    }

    private static <E> void parseAndAdd(MutableRegistry<E> registry, Decoder<E> decoder, RegistryOps<JsonElement> ops, RegistryKey<E> key, Resource resource, RegistryEntryInfo entryInfo) throws IOException {
        try (BufferedReader reader = resource.getReader();){
            JsonElement jsonElement = JsonParser.parseReader(reader);
            DataResult<E> dataResult = decoder.parse(ops, jsonElement);
            E object = dataResult.getOrThrow();
            registry.add(key, object, entryInfo);
        }
    }

    static <E> void loadFromResource(ResourceManager resourceManager, RegistryOps.RegistryInfoGetter infoGetter, MutableRegistry<E> registry, Decoder<E> elementDecoder, Map<RegistryKey<?>, Exception> errors) {
        String string = RegistryKeys.getPath(registry.getKey());
        ResourceFinder resourceFinder = ResourceFinder.json(string);
        RegistryOps<JsonElement> registryOps = RegistryOps.of(JsonOps.INSTANCE, infoGetter);
        for (Map.Entry<Identifier, Resource> entry : resourceFinder.findResources(resourceManager).entrySet()) {
            Identifier identifier = entry.getKey();
            RegistryKey registryKey = RegistryKey.of(registry.getKey(), resourceFinder.toResourceId(identifier));
            Resource resource = entry.getValue();
            RegistryEntryInfo registryEntryInfo = RESOURCE_ENTRY_INFO_GETTER.apply(resource.getKnownPackInfo());
            try {
                RegistryLoader.parseAndAdd(registry, elementDecoder, registryOps, registryKey, resource, registryEntryInfo);
            } catch (Exception exception) {
                errors.put(registryKey, new IllegalStateException(String.format(Locale.ROOT, "Failed to parse %s from pack %s", identifier, resource.getPackId()), exception));
            }
        }
    }

    static <E> void loadFromNetwork(Map<RegistryKey<? extends Registry<?>>, List<SerializableRegistries.SerializedRegistryEntry>> data, ResourceFactory factory, RegistryOps.RegistryInfoGetter infoGetter, MutableRegistry<E> registry, Decoder<E> decoder, Map<RegistryKey<?>, Exception> loadingErrors) {
        List<SerializableRegistries.SerializedRegistryEntry> list = data.get(registry.getKey());
        if (list == null) {
            return;
        }
        RegistryOps<NbtElement> registryOps = RegistryOps.of(NbtOps.INSTANCE, infoGetter);
        RegistryOps<JsonElement> registryOps2 = RegistryOps.of(JsonOps.INSTANCE, infoGetter);
        String string = RegistryKeys.getPath(registry.getKey());
        ResourceFinder resourceFinder = ResourceFinder.json(string);
        for (SerializableRegistries.SerializedRegistryEntry serializedRegistryEntry : list) {
            RegistryKey registryKey = RegistryKey.of(registry.getKey(), serializedRegistryEntry.id());
            Optional<NbtElement> optional = serializedRegistryEntry.data();
            if (optional.isPresent()) {
                try {
                    DataResult<E> dataResult = decoder.parse(registryOps, optional.get());
                    E object = dataResult.getOrThrow();
                    registry.add(registryKey, object, EXPERIMENTAL_ENTRY_INFO);
                } catch (Exception exception) {
                    loadingErrors.put(registryKey, new IllegalStateException(String.format(Locale.ROOT, "Failed to parse value %s from server", optional.get()), exception));
                }
                continue;
            }
            Identifier identifier = resourceFinder.toResourcePath(serializedRegistryEntry.id());
            try {
                Resource resource = factory.getResourceOrThrow(identifier);
                RegistryLoader.parseAndAdd(registry, decoder, registryOps2, registryKey, resource, EXPERIMENTAL_ENTRY_INFO);
            } catch (Exception exception2) {
                loadingErrors.put(registryKey, new IllegalStateException("Failed to parse local data", exception2));
            }
        }
    }

    @FunctionalInterface
    static interface RegistryLoadable {
        public void apply(Loader<?> var1, RegistryOps.RegistryInfoGetter var2);
    }

    record Loader<T>(Entry<T> data, MutableRegistry<T> registry, Map<RegistryKey<?>, Exception> loadingErrors) {
        public void loadFromResource(ResourceManager resourceManager, RegistryOps.RegistryInfoGetter infoGetter) {
            RegistryLoader.loadFromResource(resourceManager, infoGetter, this.registry, this.data.elementCodec, this.loadingErrors);
        }

        public void loadFromNetwork(Map<RegistryKey<? extends Registry<?>>, List<SerializableRegistries.SerializedRegistryEntry>> data, ResourceFactory factory, RegistryOps.RegistryInfoGetter infoGetter) {
            RegistryLoader.loadFromNetwork(data, factory, infoGetter, this.registry, this.data.elementCodec, this.loadingErrors);
        }
    }

    public record Entry<T>(RegistryKey<? extends Registry<T>> key, Codec<T> elementCodec, boolean requiredNonEmpty) {
        Entry(RegistryKey<? extends Registry<T>> registryKey, Codec<T> codec) {
            this(registryKey, codec, false);
        }

        Loader<T> getLoader(Lifecycle lifecycle, Map<RegistryKey<?>, Exception> errors) {
            SimpleRegistry mutableRegistry = new SimpleRegistry(this.key, lifecycle);
            return new Loader(this, mutableRegistry, errors);
        }

        public void addToCloner(BiConsumer<RegistryKey<? extends Registry<T>>, Codec<T>> callback) {
            callback.accept(this.key, this.elementCodec);
        }
    }
}

