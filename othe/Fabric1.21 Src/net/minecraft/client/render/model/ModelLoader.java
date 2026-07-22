/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.model;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BlockStatesLoader;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ItemModelGenerator;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ModelLoader {
    public static final SpriteIdentifier FIRE_0 = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("block/fire_0"));
    public static final SpriteIdentifier FIRE_1 = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("block/fire_1"));
    public static final SpriteIdentifier LAVA_FLOW = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("block/lava_flow"));
    public static final SpriteIdentifier WATER_FLOW = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("block/water_flow"));
    public static final SpriteIdentifier WATER_OVERLAY = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("block/water_overlay"));
    public static final SpriteIdentifier BANNER_BASE = new SpriteIdentifier(TexturedRenderLayers.BANNER_PATTERNS_ATLAS_TEXTURE, Identifier.ofVanilla("entity/banner_base"));
    public static final SpriteIdentifier SHIELD_BASE = new SpriteIdentifier(TexturedRenderLayers.SHIELD_PATTERNS_ATLAS_TEXTURE, Identifier.ofVanilla("entity/shield_base"));
    public static final SpriteIdentifier SHIELD_BASE_NO_PATTERN = new SpriteIdentifier(TexturedRenderLayers.SHIELD_PATTERNS_ATLAS_TEXTURE, Identifier.ofVanilla("entity/shield_base_nopattern"));
    public static final int field_32983 = 10;
    public static final List<Identifier> BLOCK_DESTRUCTION_STAGES = IntStream.range(0, 10).mapToObj(stage -> Identifier.ofVanilla("block/destroy_stage_" + stage)).collect(Collectors.toList());
    public static final List<Identifier> BLOCK_DESTRUCTION_STAGE_TEXTURES = BLOCK_DESTRUCTION_STAGES.stream().map(id -> id.withPath(path -> "textures/" + path + ".png")).collect(Collectors.toList());
    public static final List<RenderLayer> BLOCK_DESTRUCTION_RENDER_LAYERS = BLOCK_DESTRUCTION_STAGE_TEXTURES.stream().map(RenderLayer::getBlockBreaking).collect(Collectors.toList());
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String BUILTIN = "builtin/";
    private static final String BUILTIN_GENERATED = "builtin/generated";
    private static final String BUILTIN_ENTITY = "builtin/entity";
    private static final String MISSING = "missing";
    public static final Identifier MISSING_ID = Identifier.ofVanilla("builtin/missing");
    public static final ModelIdentifier MISSING_MODEL_ID = new ModelIdentifier(MISSING_ID, "missing");
    public static final ResourceFinder MODELS_FINDER = ResourceFinder.json("models");
    @VisibleForTesting
    public static final String MISSING_DEFINITION = ("{    'textures': {       'particle': '" + MissingSprite.getMissingSpriteId().getPath() + "',       'missingno': '" + MissingSprite.getMissingSpriteId().getPath() + "'    },    'elements': [         {  'from': [ 0, 0, 0 ],            'to': [ 16, 16, 16 ],            'faces': {                'down':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'down',  'texture': '#missingno' },                'up':    { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'up',    'texture': '#missingno' },                'north': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'north', 'texture': '#missingno' },                'south': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'south', 'texture': '#missingno' },                'west':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'west',  'texture': '#missingno' },                'east':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'east',  'texture': '#missingno' }            }        }    ]}").replace('\'', '\"');
    private static final Map<String, String> BUILTIN_MODEL_DEFINITIONS = Map.of("missing", MISSING_DEFINITION);
    public static final JsonUnbakedModel GENERATION_MARKER = Util.make(JsonUnbakedModel.deserialize("{\"gui_light\": \"front\"}"), model -> {
        model.id = "generation marker";
    });
    public static final JsonUnbakedModel BLOCK_ENTITY_MARKER = Util.make(JsonUnbakedModel.deserialize("{\"gui_light\": \"side\"}"), model -> {
        model.id = "block entity marker";
    });
    static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    private final Map<Identifier, JsonUnbakedModel> jsonUnbakedModels;
    private final Set<Identifier> modelsToLoad = new HashSet<Identifier>();
    private final Map<Identifier, UnbakedModel> unbakedModels = new HashMap<Identifier, UnbakedModel>();
    final Map<BakedModelCacheKey, BakedModel> bakedModelCache = new HashMap<BakedModelCacheKey, BakedModel>();
    private final Map<ModelIdentifier, UnbakedModel> modelsToBake = new HashMap<ModelIdentifier, UnbakedModel>();
    private final Map<ModelIdentifier, BakedModel> bakedModels = new HashMap<ModelIdentifier, BakedModel>();
    private final UnbakedModel missingModel;
    private final Object2IntMap<BlockState> stateLookup;

    public ModelLoader(BlockColors blockColors, Profiler profiler, Map<Identifier, JsonUnbakedModel> jsonUnbakedModels, Map<Identifier, List<BlockStatesLoader.SourceTrackedData>> blockStates) {
        this.jsonUnbakedModels = jsonUnbakedModels;
        profiler.push("missing_model");
        try {
            this.missingModel = this.loadModelFromJson(MISSING_ID);
            this.addModelToBake(MISSING_MODEL_ID, this.missingModel);
        } catch (IOException iOException) {
            LOGGER.error("Error loading missing model, should never happen :(", iOException);
            throw new RuntimeException(iOException);
        }
        BlockStatesLoader blockStatesLoader = new BlockStatesLoader(blockStates, profiler, this.missingModel, blockColors, this::add);
        blockStatesLoader.load();
        this.stateLookup = blockStatesLoader.getStateLookup();
        profiler.swap("items");
        for (Identifier identifier : Registries.ITEM.getIds()) {
            this.loadInventoryVariantItemModel(identifier);
        }
        profiler.swap("special");
        this.loadItemModel(ItemRenderer.TRIDENT_IN_HAND);
        this.loadItemModel(ItemRenderer.SPYGLASS_IN_HAND);
        this.modelsToBake.values().forEach(model -> model.setParents(this::getOrLoadModel));
        profiler.pop();
    }

    public void bake(SpriteGetter spliteGetter) {
        this.modelsToBake.forEach((id, model) -> {
            BakedModel bakedModel = null;
            try {
                bakedModel = new BakerImpl(spliteGetter, (ModelIdentifier)id).bake((UnbakedModel)model, (ModelBakeSettings)ModelRotation.X0_Y0);
            } catch (Exception exception) {
                LOGGER.warn("Unable to bake model: '{}': {}", id, (Object)exception);
            }
            if (bakedModel != null) {
                this.bakedModels.put((ModelIdentifier)id, bakedModel);
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    UnbakedModel getOrLoadModel(Identifier id) {
        if (this.unbakedModels.containsKey(id)) {
            return this.unbakedModels.get(id);
        }
        if (this.modelsToLoad.contains(id)) {
            throw new IllegalStateException("Circular reference while loading " + String.valueOf(id));
        }
        this.modelsToLoad.add(id);
        while (!this.modelsToLoad.isEmpty()) {
            Identifier identifier = this.modelsToLoad.iterator().next();
            try {
                if (this.unbakedModels.containsKey(identifier)) continue;
                JsonUnbakedModel unbakedModel = this.loadModelFromJson(identifier);
                this.unbakedModels.put(identifier, unbakedModel);
                this.modelsToLoad.addAll(unbakedModel.getModelDependencies());
            } catch (Exception exception) {
                LOGGER.warn("Unable to load model: '{}' referenced from: {}: {}", identifier, id, exception);
                this.unbakedModels.put(identifier, this.missingModel);
            } finally {
                this.modelsToLoad.remove(identifier);
            }
        }
        return this.unbakedModels.getOrDefault(id, this.missingModel);
    }

    private void loadInventoryVariantItemModel(Identifier id) {
        ModelIdentifier modelIdentifier = ModelIdentifier.ofInventoryVariant(id);
        Identifier identifier = id.withPrefixedPath("item/");
        UnbakedModel unbakedModel = this.getOrLoadModel(identifier);
        this.add(modelIdentifier, unbakedModel);
    }

    private void loadItemModel(ModelIdentifier id) {
        Identifier identifier = id.id().withPrefixedPath("item/");
        UnbakedModel unbakedModel = this.getOrLoadModel(identifier);
        this.add(id, unbakedModel);
    }

    private void add(ModelIdentifier id, UnbakedModel model) {
        for (Identifier identifier : model.getModelDependencies()) {
            this.getOrLoadModel(identifier);
        }
        this.addModelToBake(id, model);
    }

    private void addModelToBake(ModelIdentifier id, UnbakedModel model) {
        this.modelsToBake.put(id, model);
    }

    private JsonUnbakedModel loadModelFromJson(Identifier id) throws IOException {
        String string = id.getPath();
        if (BUILTIN_GENERATED.equals(string)) {
            return GENERATION_MARKER;
        }
        if (BUILTIN_ENTITY.equals(string)) {
            return BLOCK_ENTITY_MARKER;
        }
        if (string.startsWith(BUILTIN)) {
            String string2 = string.substring(BUILTIN.length());
            String string3 = BUILTIN_MODEL_DEFINITIONS.get(string2);
            if (string3 == null) {
                throw new FileNotFoundException(id.toString());
            }
            StringReader reader = new StringReader(string3);
            JsonUnbakedModel jsonUnbakedModel = JsonUnbakedModel.deserialize(reader);
            jsonUnbakedModel.id = id.toString();
            return jsonUnbakedModel;
        }
        Identifier identifier = MODELS_FINDER.toResourcePath(id);
        JsonUnbakedModel jsonUnbakedModel2 = this.jsonUnbakedModels.get(identifier);
        if (jsonUnbakedModel2 == null) {
            throw new FileNotFoundException(identifier.toString());
        }
        jsonUnbakedModel2.id = id.toString();
        return jsonUnbakedModel2;
    }

    public Map<ModelIdentifier, BakedModel> getBakedModelMap() {
        return this.bakedModels;
    }

    public Object2IntMap<BlockState> getStateLookup() {
        return this.stateLookup;
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface SpriteGetter {
        public Sprite get(ModelIdentifier var1, SpriteIdentifier var2);
    }

    @Environment(value=EnvType.CLIENT)
    class BakerImpl
    implements Baker {
        private final Function<SpriteIdentifier, Sprite> textureGetter = spriteId -> spriteGetter.get(modelIdentifier, (SpriteIdentifier)spriteId);

        BakerImpl(SpriteGetter spriteGetter, ModelIdentifier modelIdentifier) {
        }

        @Override
        public UnbakedModel getOrLoadModel(Identifier id) {
            return ModelLoader.this.getOrLoadModel(id);
        }

        @Override
        public BakedModel bake(Identifier id, ModelBakeSettings settings) {
            BakedModelCacheKey bakedModelCacheKey = new BakedModelCacheKey(id, settings.getRotation(), settings.isUvLocked());
            BakedModel bakedModel = ModelLoader.this.bakedModelCache.get(bakedModelCacheKey);
            if (bakedModel != null) {
                return bakedModel;
            }
            UnbakedModel unbakedModel = this.getOrLoadModel(id);
            BakedModel bakedModel2 = this.bake(unbakedModel, settings);
            ModelLoader.this.bakedModelCache.put(bakedModelCacheKey, bakedModel2);
            return bakedModel2;
        }

        @Nullable
        BakedModel bake(UnbakedModel model, ModelBakeSettings settings) {
            JsonUnbakedModel jsonUnbakedModel;
            if (model instanceof JsonUnbakedModel && (jsonUnbakedModel = (JsonUnbakedModel)model).getRootModel() == GENERATION_MARKER) {
                return ITEM_MODEL_GENERATOR.create(this.textureGetter, jsonUnbakedModel).bake(this, jsonUnbakedModel, this.textureGetter, settings, false);
            }
            return model.bake(this, this.textureGetter, settings);
        }
    }

    @Environment(value=EnvType.CLIENT)
    record BakedModelCacheKey(Identifier id, AffineTransformation transformation, boolean isUvLocked) {
    }
}

