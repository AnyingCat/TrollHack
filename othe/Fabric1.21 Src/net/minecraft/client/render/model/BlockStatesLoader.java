/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.model;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.MultipartUnbakedModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.render.model.json.MultipartModelComponent;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class BlockStatesLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    static final int field_52259 = -1;
    private static final int field_52262 = 0;
    public static final ResourceFinder FINDER = ResourceFinder.json("blockstates");
    private static final Splitter COMMA_SPLITTER = Splitter.on(',');
    private static final Splitter EQUAL_SIGN_SPLITTER = Splitter.on('=').limit(2);
    private static final StateManager<Block, BlockState> ITEM_FRAME_STATE_MANAGER = new StateManager.Builder(Blocks.AIR).add(BooleanProperty.of("map")).build(Block::getDefaultState, BlockState::new);
    private static final Map<Identifier, StateManager<Block, BlockState>> STATIC_DEFINITIONS = Map.of(Identifier.ofVanilla("item_frame"), ITEM_FRAME_STATE_MANAGER, Identifier.ofVanilla("glow_item_frame"), ITEM_FRAME_STATE_MANAGER);
    private final Map<Identifier, List<SourceTrackedData>> blockStates;
    private final Profiler profiler;
    private final BlockColors blockColors;
    private final BiConsumer<ModelIdentifier, UnbakedModel> onLoad;
    private int lookupId = 1;
    private final Object2IntMap<BlockState> stateLookup = Util.make(new Object2IntOpenHashMap(), map -> map.defaultReturnValue(-1));
    private final BlockModel missingModel;
    private final ModelVariantMap.DeserializationContext context = new ModelVariantMap.DeserializationContext();

    public BlockStatesLoader(Map<Identifier, List<SourceTrackedData>> blockStates, Profiler profiler, UnbakedModel missingModel, BlockColors blockColors, BiConsumer<ModelIdentifier, UnbakedModel> onLoad) {
        this.blockStates = blockStates;
        this.profiler = profiler;
        this.blockColors = blockColors;
        this.onLoad = onLoad;
        ModelDefinition modelDefinition = new ModelDefinition(List.of(missingModel), List.of());
        this.missingModel = new BlockModel(missingModel, () -> modelDefinition);
    }

    public void load() {
        this.profiler.push("static_definitions");
        STATIC_DEFINITIONS.forEach(this::loadBlockStates);
        this.profiler.swap("blocks");
        for (Block block : Registries.BLOCK) {
            this.loadBlockStates(block.getRegistryEntry().registryKey().getValue(), block.getStateManager());
        }
        this.profiler.pop();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void loadBlockStates(Identifier id, StateManager<Block, BlockState> stateManager) {
        this.context.setStateFactory(stateManager);
        List<Property<?>> list = List.copyOf(this.blockColors.getProperties(stateManager.getOwner()));
        ImmutableList<BlockState> list2 = stateManager.getStates();
        HashMap<ModelIdentifier, BlockState> map = new HashMap<ModelIdentifier, BlockState>();
        list2.forEach(state -> map.put(BlockModels.getModelId(id, state), (BlockState)state));
        HashMap map2 = new HashMap();
        Identifier identifier = FINDER.toResourcePath(id);
        try {
            for (SourceTrackedData sourceTrackedData : this.blockStates.getOrDefault(identifier, List.of())) {
                MultipartUnbakedModel multipartUnbakedModel;
                ModelVariantMap modelVariantMap = sourceTrackedData.readVariantMap(id, this.context);
                IdentityHashMap map3 = new IdentityHashMap();
                if (modelVariantMap.hasMultipartModel()) {
                    multipartUnbakedModel = modelVariantMap.getMultipartModel();
                    list2.forEach(state -> map3.put(state, new BlockModel(multipartUnbakedModel, () -> ModelDefinition.create(state, multipartUnbakedModel, list))));
                } else {
                    multipartUnbakedModel = null;
                }
                modelVariantMap.getVariantMap().forEach((variant, model) -> {
                    try {
                        list2.stream().filter(BlockStatesLoader.toStatePredicate(stateManager, variant)).forEach(state -> {
                            BlockModel blockModel = map3.put(state, new BlockModel((UnbakedModel)model, () -> ModelDefinition.create(state, model, list)));
                            if (blockModel != null && blockModel.model != multipartUnbakedModel) {
                                map3.put(state, this.missingModel);
                                throw new RuntimeException("Overlapping definition with: " + (String)modelVariantMap.getVariantMap().entrySet().stream().filter(entry -> entry.getValue() == blockModel.model).findFirst().get().getKey());
                            }
                        });
                    } catch (Exception exception) {
                        LOGGER.warn("Exception loading blockstate definition: '{}' in resourcepack: '{}' for variant: '{}': {}", identifier, sourceTrackedData.source, variant, exception.getMessage());
                    }
                });
                map2.putAll(map3);
            }
        } catch (ModelLoaderException modelLoaderException) {
            LOGGER.warn("{}", (Object)modelLoaderException.getMessage());
        } catch (Exception exception) {
            LOGGER.warn("Exception loading blockstate definition: '{}'", (Object)identifier, (Object)exception);
        } finally {
            HashMap<ModelDefinition, Set> map4 = new HashMap<ModelDefinition, Set>();
            map.forEach((modelId, state) -> {
                BlockModel blockModel = (BlockModel)map2.get(state);
                if (blockModel == null) {
                    LOGGER.warn("Exception loading blockstate definition: '{}' missing model for variant: '{}'", (Object)identifier, modelId);
                    blockModel = this.missingModel;
                }
                this.onLoad.accept((ModelIdentifier)modelId, blockModel.model);
                try {
                    ModelDefinition modelDefinition = blockModel.key().get();
                    map4.computeIfAbsent(modelDefinition, definition -> Sets.newIdentityHashSet()).add(state);
                } catch (Exception exception) {
                    LOGGER.warn("Exception evaluating model definition: '{}'", modelId, (Object)exception);
                }
            });
            map4.forEach((definition, states) -> {
                Iterator iterator = states.iterator();
                while (iterator.hasNext()) {
                    BlockState blockState = (BlockState)iterator.next();
                    if (blockState.getRenderType() == BlockRenderType.MODEL) continue;
                    iterator.remove();
                    this.stateLookup.put(blockState, 0);
                }
                if (states.size() > 1) {
                    this.addStates((Iterable<BlockState>)states);
                }
            });
        }
    }

    private static Predicate<BlockState> toStatePredicate(StateManager<Block, BlockState> stateManager, String predicate) {
        HashMap map = new HashMap();
        for (String string : COMMA_SPLITTER.split(predicate)) {
            Iterator<String> iterator = EQUAL_SIGN_SPLITTER.split(string).iterator();
            if (!iterator.hasNext()) continue;
            String string2 = iterator.next();
            Property<?> property = stateManager.getProperty(string2);
            if (property != null && iterator.hasNext()) {
                String string3 = iterator.next();
                Object comparable = BlockStatesLoader.parseProperty(property, string3);
                if (comparable != null) {
                    map.put(property, comparable);
                    continue;
                }
                throw new RuntimeException("Unknown value: '" + string3 + "' for blockstate property: '" + string2 + "' " + String.valueOf(property.getValues()));
            }
            if (string2.isEmpty()) continue;
            throw new RuntimeException("Unknown blockstate property: '" + string2 + "'");
        }
        Block block = stateManager.getOwner();
        return state -> {
            if (state == null || !state.isOf(block)) {
                return false;
            }
            for (Map.Entry entry : map.entrySet()) {
                if (Objects.equals(state.get((Property)entry.getKey()), entry.getValue())) continue;
                return false;
            }
            return true;
        };
    }

    @Nullable
    static <T extends Comparable<T>> T parseProperty(Property<T> property, String value) {
        return (T)((Comparable)property.parse(value).orElse(null));
    }

    private void addStates(Iterable<BlockState> states) {
        int i = this.lookupId++;
        states.forEach(state -> this.stateLookup.put((BlockState)state, i));
    }

    public Object2IntMap<BlockState> getStateLookup() {
        return this.stateLookup;
    }

    @Environment(value=EnvType.CLIENT)
    record ModelDefinition(List<UnbakedModel> components, List<Object> values) {
        public static ModelDefinition create(BlockState state, MultipartUnbakedModel rawModel, Collection<Property<?>> properties) {
            StateManager<Block, BlockState> stateManager = state.getBlock().getStateManager();
            List<UnbakedModel> list = rawModel.getComponents().stream().filter(component -> component.getPredicate(stateManager).test(state)).map(MultipartModelComponent::getModel).collect(Collectors.toUnmodifiableList());
            List<Object> list2 = ModelDefinition.getStateValues(state, properties);
            return new ModelDefinition(list, list2);
        }

        public static ModelDefinition create(BlockState state, UnbakedModel rawModel, Collection<Property<?>> properties) {
            List<Object> list = ModelDefinition.getStateValues(state, properties);
            return new ModelDefinition(List.of(rawModel), list);
        }

        private static List<Object> getStateValues(BlockState state, Collection<Property<?>> properties) {
            return properties.stream().map(state::get).collect(Collectors.toUnmodifiableList());
        }
    }

    @Environment(value=EnvType.CLIENT)
    record BlockModel(UnbakedModel model, Supplier<ModelDefinition> key) {
    }

    @Environment(value=EnvType.CLIENT)
    public record SourceTrackedData(String source, JsonElement data) {
        ModelVariantMap readVariantMap(Identifier id, ModelVariantMap.DeserializationContext context) {
            try {
                return ModelVariantMap.fromJson(context, this.data);
            } catch (Exception exception) {
                throw new ModelLoaderException(String.format(Locale.ROOT, "Exception loading blockstate definition: '%s' in resourcepack: '%s': %s", id, this.source, exception.getMessage()));
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class ModelLoaderException
    extends RuntimeException {
        public ModelLoaderException(String message) {
            super(message);
        }
    }
}

