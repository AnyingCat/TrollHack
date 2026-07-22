/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.font;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.BlankFont;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.FontFilterType;
import net.minecraft.client.font.FontLoader;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.DependencyTracker;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class FontManager
implements ResourceReloader,
AutoCloseable {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final String FONTS_JSON = "fonts.json";
    public static final Identifier MISSING_STORAGE_ID = Identifier.ofVanilla("missing");
    private static final ResourceFinder FINDER = ResourceFinder.json("font");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final FontStorage missingStorage;
    private final List<Font> fonts = new ArrayList<Font>();
    private final Map<Identifier, FontStorage> fontStorages = new HashMap<Identifier, FontStorage>();
    private final TextureManager textureManager;
    @Nullable
    private volatile FontStorage currentStorage;

    public FontManager(TextureManager manager) {
        this.textureManager = manager;
        this.missingStorage = Util.make(new FontStorage(manager, MISSING_STORAGE_ID), fontStorage -> fontStorage.setFonts(List.of(FontManager.createEmptyFont()), Set.of()));
    }

    private static Font.FontFilterPair createEmptyFont() {
        return new Font.FontFilterPair(new BlankFont(), FontFilterType.FilterMap.NO_FILTER);
    }

    @Override
    public CompletableFuture<Void> reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        prepareProfiler.startTick();
        prepareProfiler.endTick();
        return ((CompletableFuture)this.loadIndex(manager, prepareExecutor).thenCompose(synchronizer::whenPrepared)).thenAcceptAsync(index -> this.reload((ProviderIndex)index, applyProfiler), applyExecutor);
    }

    private CompletableFuture<ProviderIndex> loadIndex(ResourceManager resourceManager, Executor executor) {
        ArrayList<CompletableFuture<FontEntry>> list = new ArrayList<CompletableFuture<FontEntry>>();
        for (Map.Entry<Identifier, List<Resource>> entry : FINDER.findAllResources(resourceManager).entrySet()) {
            Identifier identifier = FINDER.toResourceId(entry.getKey());
            list.add(CompletableFuture.supplyAsync(() -> {
                List<Pair<FontKey, FontLoader.Provider>> list = FontManager.loadFontProviders((List)entry.getValue(), identifier);
                FontEntry fontEntry = new FontEntry(identifier);
                for (Pair<FontKey, FontLoader.Provider> pair : list) {
                    FontKey fontKey = pair.getFirst();
                    FontFilterType.FilterMap filterMap = pair.getSecond().filter();
                    pair.getSecond().definition().build().ifLeft(loadable -> {
                        CompletableFuture<Optional<Font>> completableFuture = this.load(fontKey, (FontLoader.Loadable)loadable, resourceManager, executor);
                        fontEntry.addBuilder(fontKey, filterMap, completableFuture);
                    }).ifRight(reference -> fontEntry.addReferenceBuilder(fontKey, filterMap, (FontLoader.Reference)reference));
                }
                return fontEntry;
            }, executor));
        }
        return Util.combineSafe(list).thenCompose(entries -> {
            List list = entries.stream().flatMap(FontEntry::getImmediateProviders).collect(Util.toArrayList());
            Font.FontFilterPair fontFilterPair = FontManager.createEmptyFont();
            list.add(CompletableFuture.completedFuture(Optional.of(fontFilterPair.provider())));
            return Util.combineSafe(list).thenCompose(providers -> {
                Map<Identifier, List<Font.FontFilterPair>> map = this.getRequiredFontProviders((List<FontEntry>)entries);
                CompletableFuture[] completableFutures = (CompletableFuture[])map.values().stream().map(dest -> CompletableFuture.runAsync(() -> this.insertFont((List<Font.FontFilterPair>)dest, fontFilterPair), executor)).toArray(CompletableFuture[]::new);
                return CompletableFuture.allOf(completableFutures).thenApply(ignored -> {
                    List<Font> list2 = providers.stream().flatMap(Optional::stream).toList();
                    return new ProviderIndex(map, list2);
                });
            });
        });
    }

    private CompletableFuture<Optional<Font>> load(FontKey key, FontLoader.Loadable loadable, ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Optional.of(loadable.load(resourceManager));
            } catch (Exception exception) {
                LOGGER.warn("Failed to load builder {}, rejecting", (Object)key, (Object)exception);
                return Optional.empty();
            }
        }, executor);
    }

    private Map<Identifier, List<Font.FontFilterPair>> getRequiredFontProviders(List<FontEntry> entries) {
        HashMap<Identifier, List<Font.FontFilterPair>> map = new HashMap<Identifier, List<Font.FontFilterPair>>();
        DependencyTracker<Identifier, FontEntry> dependencyTracker = new DependencyTracker<Identifier, FontEntry>();
        entries.forEach(entry -> dependencyTracker.add(entry.fontId, (FontEntry)entry));
        dependencyTracker.traverse((dependent, fontEntry) -> fontEntry.getRequiredFontProviders(map::get).ifPresent(fonts -> map.put((Identifier)dependent, (List<Font.FontFilterPair>)fonts)));
        return map;
    }

    private void insertFont(List<Font.FontFilterPair> fonts, Font.FontFilterPair font) {
        fonts.add(0, font);
        IntOpenHashSet intSet = new IntOpenHashSet();
        for (Font.FontFilterPair fontFilterPair : fonts) {
            intSet.addAll(fontFilterPair.provider().getProvidedGlyphs());
        }
        intSet.forEach(codePoint -> {
            Font.FontFilterPair fontFilterPair;
            if (codePoint == 32) {
                return;
            }
            Iterator iterator = Lists.reverse(fonts).iterator();
            while (iterator.hasNext() && (fontFilterPair = (Font.FontFilterPair)iterator.next()).provider().getGlyph(codePoint) == null) {
            }
        });
    }

    private static Set<FontFilterType> getActiveFilters(GameOptions options) {
        EnumSet<FontFilterType> set = EnumSet.noneOf(FontFilterType.class);
        if (options.getForceUnicodeFont().getValue().booleanValue()) {
            set.add(FontFilterType.UNIFORM);
        }
        if (options.getJapaneseGlyphVariants().getValue().booleanValue()) {
            set.add(FontFilterType.JAPANESE_VARIANTS);
        }
        return set;
    }

    private void reload(ProviderIndex index, Profiler profiler) {
        profiler.startTick();
        profiler.push("closing");
        this.currentStorage = null;
        this.fontStorages.values().forEach(FontStorage::close);
        this.fontStorages.clear();
        this.fonts.forEach(Font::close);
        this.fonts.clear();
        Set<FontFilterType> set = FontManager.getActiveFilters(MinecraftClient.getInstance().options);
        profiler.swap("reloading");
        index.fontSets().forEach((id, fonts) -> {
            FontStorage fontStorage = new FontStorage(this.textureManager, (Identifier)id);
            fontStorage.setFonts(Lists.reverse(fonts), set);
            this.fontStorages.put((Identifier)id, fontStorage);
        });
        this.fonts.addAll(index.allProviders);
        profiler.pop();
        profiler.endTick();
        if (!this.fontStorages.containsKey(MinecraftClient.DEFAULT_FONT_ID)) {
            throw new IllegalStateException("Default font failed to load");
        }
    }

    public void setActiveFilters(GameOptions options) {
        Set<FontFilterType> set = FontManager.getActiveFilters(options);
        for (FontStorage fontStorage : this.fontStorages.values()) {
            fontStorage.setActiveFilters(set);
        }
    }

    private static List<Pair<FontKey, FontLoader.Provider>> loadFontProviders(List<Resource> fontResources, Identifier id) {
        ArrayList<Pair<FontKey, FontLoader.Provider>> list = new ArrayList<Pair<FontKey, FontLoader.Provider>>();
        for (Resource resource : fontResources) {
            try {
                BufferedReader reader = resource.getReader();
                try {
                    JsonElement jsonElement = GSON.fromJson((Reader)reader, JsonElement.class);
                    Providers providers = (Providers)Providers.CODEC.parse(JsonOps.INSTANCE, jsonElement).getOrThrow(JsonParseException::new);
                    List<FontLoader.Provider> list2 = providers.providers;
                    for (int i = list2.size() - 1; i >= 0; --i) {
                        FontKey fontKey = new FontKey(id, resource.getPackId(), i);
                        list.add(Pair.of(fontKey, list2.get(i)));
                    }
                } finally {
                    if (reader == null) continue;
                    ((Reader)reader).close();
                }
            } catch (Exception exception) {
                LOGGER.warn("Unable to load font '{}' in {} in resourcepack: '{}'", id, FONTS_JSON, resource.getPackId(), exception);
            }
        }
        return list;
    }

    public TextRenderer createTextRenderer() {
        return new TextRenderer(this::getStorage, false);
    }

    public TextRenderer createAdvanceValidatingTextRenderer() {
        return new TextRenderer(this::getStorage, true);
    }

    private FontStorage getStorageInternal(Identifier id) {
        return this.fontStorages.getOrDefault(id, this.missingStorage);
    }

    private FontStorage getStorage(Identifier id) {
        FontStorage fontStorage2;
        FontStorage fontStorage = this.currentStorage;
        if (fontStorage != null && id.equals(fontStorage.getId())) {
            return fontStorage;
        }
        this.currentStorage = fontStorage2 = this.getStorageInternal(id);
        return fontStorage2;
    }

    @Override
    public void close() {
        this.fontStorages.values().forEach(FontStorage::close);
        this.fonts.forEach(Font::close);
        this.missingStorage.close();
    }

    @Environment(value=EnvType.CLIENT)
    record FontKey(Identifier fontId, String pack, int index) {
        @Override
        public String toString() {
            return "(" + String.valueOf(this.fontId) + ": builder #" + this.index + " from pack " + this.pack + ")";
        }
    }

    @Environment(value=EnvType.CLIENT)
    record ProviderIndex(Map<Identifier, List<Font.FontFilterPair>> fontSets, List<Font> allProviders) {
    }

    @Environment(value=EnvType.CLIENT)
    record Providers(List<FontLoader.Provider> providers) {
        public static final Codec<Providers> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)FontLoader.Provider.CODEC.listOf().fieldOf("providers")).forGetter(Providers::providers)).apply((Applicative<Providers, ?>)instance, Providers::new));
    }

    @Environment(value=EnvType.CLIENT)
    record FontEntry(Identifier fontId, List<Builder> builders, Set<Identifier> dependencies) implements DependencyTracker.Dependencies<Identifier>
    {
        public FontEntry(Identifier fontId) {
            this(fontId, new ArrayList<Builder>(), new HashSet<Identifier>());
        }

        public void addReferenceBuilder(FontKey key, FontFilterType.FilterMap filters, FontLoader.Reference reference) {
            this.builders.add(new Builder(key, filters, Either.right(reference.id())));
            this.dependencies.add(reference.id());
        }

        public void addBuilder(FontKey key, FontFilterType.FilterMap filters, CompletableFuture<Optional<Font>> fontFuture) {
            this.builders.add(new Builder(key, filters, Either.left(fontFuture)));
        }

        private Stream<CompletableFuture<Optional<Font>>> getImmediateProviders() {
            return this.builders.stream().flatMap(builder -> builder.result.left().stream());
        }

        public Optional<List<Font.FontFilterPair>> getRequiredFontProviders(Function<Identifier, List<Font.FontFilterPair>> fontRetriever) {
            ArrayList list = new ArrayList();
            for (Builder builder : this.builders) {
                Optional<List<Font.FontFilterPair>> optional = builder.build(fontRetriever);
                if (optional.isPresent()) {
                    list.addAll(optional.get());
                    continue;
                }
                return Optional.empty();
            }
            return Optional.of(list);
        }

        @Override
        public void forDependencies(Consumer<Identifier> callback) {
            this.dependencies.forEach(callback);
        }

        @Override
        public void forOptionalDependencies(Consumer<Identifier> callback) {
        }
    }

    @Environment(value=EnvType.CLIENT)
    record Builder(FontKey id, FontFilterType.FilterMap filter, Either<CompletableFuture<Optional<Font>>, Identifier> result) {
        public Optional<List<Font.FontFilterPair>> build(Function<Identifier, List<Font.FontFilterPair>> fontRetriever) {
            return this.result.map(future -> ((Optional)future.join()).map(font -> List.of(new Font.FontFilterPair((Font)font, this.filter))), referee -> {
                List list = (List)fontRetriever.apply((Identifier)referee);
                if (list == null) {
                    LOGGER.warn("Can't find font {} referenced by builder {}, either because it's missing, failed to load or is part of loading cycle", referee, (Object)this.id);
                    return Optional.empty();
                }
                return Optional.of(list.stream().map(this::applyFilter).toList());
            });
        }

        private Font.FontFilterPair applyFilter(Font.FontFilterPair font) {
            return new Font.FontFilterPair(font.provider(), this.filter.apply(font.filter()));
        }
    }
}

