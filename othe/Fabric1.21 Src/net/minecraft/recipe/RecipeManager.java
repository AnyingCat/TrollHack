/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * A recipe manager allows easier use of recipes, such as finding matches and
 * remainders. It is also integrated with a recipe loader, which loads recipes
 * from data packs' JSON files.
 */
public class RecipeManager
extends JsonDataLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final RegistryWrapper.WrapperLookup registryLookup;
    private Multimap<RecipeType<?>, RecipeEntry<?>> recipesByType = ImmutableMultimap.of();
    private Map<Identifier, RecipeEntry<?>> recipesById = ImmutableMap.of();
    /**
     * This isn't quite indicating an errored state; its value is only set to
     * {@code false} and is never {@code true}, and isn't used anywhere.
     */
    private boolean errored;

    public RecipeManager(RegistryWrapper.WrapperLookup registryLookup) {
        super(GSON, RegistryKeys.getPath(RegistryKeys.RECIPE));
        this.registryLookup = registryLookup;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler) {
        this.errored = false;
        ImmutableMultimap.Builder<RecipeType<?>, RecipeEntry<Recipe>> builder = ImmutableMultimap.builder();
        ImmutableMap.Builder<Identifier, RecipeEntry<Recipe>> builder2 = ImmutableMap.builder();
        RegistryOps<JsonElement> registryOps = this.registryLookup.getOps(JsonOps.INSTANCE);
        for (Map.Entry<Identifier, JsonElement> entry : map.entrySet()) {
            Identifier identifier = entry.getKey();
            try {
                Recipe recipe = (Recipe)Recipe.CODEC.parse(registryOps, entry.getValue()).getOrThrow(JsonParseException::new);
                RecipeEntry<Recipe> recipeEntry = new RecipeEntry<Recipe>(identifier, recipe);
                builder.put(recipe.getType(), recipeEntry);
                builder2.put(identifier, recipeEntry);
            } catch (JsonParseException | IllegalArgumentException runtimeException) {
                LOGGER.error("Parsing error loading recipe {}", (Object)identifier, (Object)runtimeException);
            }
        }
        this.recipesByType = builder.build();
        this.recipesById = builder2.build();
        LOGGER.info("Loaded {} recipes", (Object)this.recipesByType.size());
    }

    /**
     * {@return the {@link #errored} field} This is unused in vanilla and will only
     * return {@code false} without mods.
     */
    public boolean isErrored() {
        return this.errored;
    }

    /**
     * {@return a recipe of the given {@code type} that match the given
     * {@code inventory} and {@code world}}
     * 
     * <p>If there are multiple matching recipes, the result is arbitrary,
     * but this method will return the same result unless the recipes in this
     * manager are updated.
     * 
     * @param type the desired recipe type
     * @param world the input world
     */
    public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeEntry<T>> getFirstMatch(RecipeType<T> type, I input, World world) {
        return this.getFirstMatch(type, input, world, (RecipeEntry)null);
    }

    public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeEntry<T>> getFirstMatch(RecipeType<T> type, I input, World world, @Nullable Identifier id) {
        RecipeEntry<T> recipeEntry = id != null ? this.get(type, id) : null;
        return this.getFirstMatch(type, input, world, recipeEntry);
    }

    public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeEntry<T>> getFirstMatch(RecipeType<T> type, I input, World world, @Nullable RecipeEntry<T> recipe2) {
        if (input.isEmpty()) {
            return Optional.empty();
        }
        if (recipe2 != null && recipe2.value().matches(input, world)) {
            return Optional.of(recipe2);
        }
        return this.getAllOfType(type).stream().filter(recipe -> recipe.value().matches((RecipeInput)input, world)).findFirst();
    }

    /**
     * Creates a list of all recipes of the given {@code type}. The list has an
     * arbitrary order.
     * 
     * <p>This list does not update with this manager. Modifications to
     * the returned list do not affect this manager.
     * 
     * @return the created list of recipes of the given {@code type}
     * 
     * @param type the desired recipe type
     */
    public <I extends RecipeInput, T extends Recipe<I>> List<RecipeEntry<T>> listAllOfType(RecipeType<T> type) {
        return List.copyOf(this.getAllOfType(type));
    }

    /**
     * Creates a list of all recipes of the given {@code type} that match the
     * given {@code inventory} and {@code world}. The list is ordered by the
     * translation key of the output item stack of each recipe.
     * 
     * <p>This list does not update with this manager. Modifications to
     * the returned list do not affect this manager.
     * 
     * @return the created list of matching recipes
     * 
     * @param world the input world
     * @param type the desired recipe type
     */
    public <I extends RecipeInput, T extends Recipe<I>> List<RecipeEntry<T>> getAllMatches(RecipeType<T> type, I input, World world) {
        return this.getAllOfType(type).stream().filter(recipe -> recipe.value().matches((RecipeInput)input, world)).sorted(Comparator.comparing(entry -> entry.value().getResult(world.getRegistryManager()).getTranslationKey())).collect(Collectors.toList());
    }

    private <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeEntry<T>> getAllOfType(RecipeType<T> type) {
        return this.recipesByType.get(type);
    }

    /**
     * {@return the remainder of a recipe of the given {@code type} that match
     * the given {@code inventory} and {@code world}, or a shallow copy of the
     * {@code inventory}}
     * 
     * <p>This retrieves the {@linkplain Recipe#getRemainder(Inventory)
     * remainders} of {@link #getFirstMatch(RecipeType, Inventory, World)
     * getFirstMatch(type, inventory, world)} if the match exists.
     * 
     * @see Recipe#getRemainder(Inventory)
     * 
     * @param type the desired recipe type
     * @param world the input world
     */
    public <I extends RecipeInput, T extends Recipe<I>> DefaultedList<ItemStack> getRemainingStacks(RecipeType<T> type, I input, World world) {
        Optional<RecipeEntry<T>> optional = this.getFirstMatch(type, input, world);
        if (optional.isPresent()) {
            return optional.get().value().getRemainder(input);
        }
        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(input.getSize(), ItemStack.EMPTY);
        for (int i = 0; i < defaultedList.size(); ++i) {
            defaultedList.set(i, input.getStackInSlot(i));
        }
        return defaultedList;
    }

    /**
     * {@return a recipe with the given {@code id}, or empty if there is no such recipe}
     * 
     * @param id the ID of the desired recipe
     */
    public Optional<RecipeEntry<?>> get(Identifier id) {
        return Optional.ofNullable(this.recipesById.get(id));
    }

    /**
     * {@return a recipe with the given {@code id} and {@code type}, or empty if there is no such recipe}
     * 
     * @param type the type of the desired recipe
     * @param id the ID of the desired recipe
     */
    @Nullable
    private <T extends Recipe<?>> RecipeEntry<T> get(RecipeType<T> type, Identifier id) {
        RecipeEntry<?> recipeEntry = this.recipesById.get(id);
        if (recipeEntry != null && recipeEntry.value().getType().equals(type)) {
            return recipeEntry;
        }
        return null;
    }

    public Collection<RecipeEntry<?>> sortedValues() {
        return this.recipesByType.values();
    }

    /**
     * {@return all recipes in this manager}
     * 
     * <p>The returned set does not update with the manager. Modifications to the
     * returned set does not affect this manager.
     */
    public Collection<RecipeEntry<?>> values() {
        return this.recipesById.values();
    }

    /**
     * {@return a stream of IDs of recipes in this manager}
     * 
     * <p>The returned stream does not update after {@link #setRecipes(Iterable)}
     * call.
     * 
     * @apiNote This is used by the command sources to suggest recipe IDs for command
     * arguments.
     */
    public Stream<Identifier> keys() {
        return this.recipesById.keySet().stream();
    }

    /**
     * Reads a recipe from a JSON object.
     * 
     * @implNote Even though a recipe's {@linkplain Recipe#getSerializer() serializer}
     * is stored in a {@code type} field in the JSON format and referred so in this
     * method, its registry has key {@code minecraft:root/minecraft:recipe_serializer}
     * and is thus named.
     * 
     * @throws com.google.gson.JsonParseException if the recipe JSON is invalid
     * @return the read recipe
     * 
     * @param id the recipe's ID
     */
    @VisibleForTesting
    protected static RecipeEntry<?> deserialize(Identifier id, JsonObject json, RegistryWrapper.WrapperLookup registryLookup) {
        Recipe recipe = (Recipe)Recipe.CODEC.parse(registryLookup.getOps(JsonOps.INSTANCE), json).getOrThrow(JsonParseException::new);
        return new RecipeEntry<Recipe>(id, recipe);
    }

    /**
     * Sets the recipes for this recipe manager. Used by the client to set the server
     * side recipes.
     * 
     * @param recipes the recipes to set
     */
    public void setRecipes(Iterable<RecipeEntry<?>> recipes) {
        this.errored = false;
        ImmutableMultimap.Builder<RecipeType<?>, RecipeEntry<?>> builder = ImmutableMultimap.builder();
        ImmutableMap.Builder<Identifier, RecipeEntry<?>> builder2 = ImmutableMap.builder();
        for (RecipeEntry<?> recipeEntry : recipes) {
            RecipeType<?> recipeType = recipeEntry.value().getType();
            builder.put(recipeType, recipeEntry);
            builder2.put(recipeEntry.id(), recipeEntry);
        }
        this.recipesByType = builder.build();
        this.recipesById = builder2.build();
    }

    /**
     * Creates a cached match getter. This is optimized for getting matches of the same
     * recipe repeatedly, such as furnaces.
     */
    public static <I extends RecipeInput, T extends Recipe<I>> MatchGetter<I, T> createCachedMatchGetter(final RecipeType<T> type) {
        return new MatchGetter<I, T>(){
            @Nullable
            private Identifier id;

            @Override
            public Optional<RecipeEntry<T>> getFirstMatch(I input, World world) {
                RecipeManager recipeManager = world.getRecipeManager();
                Optional optional = recipeManager.getFirstMatch(type, input, world, this.id);
                if (optional.isPresent()) {
                    RecipeEntry recipeEntry = optional.get();
                    this.id = recipeEntry.id();
                    return Optional.of(recipeEntry);
                }
                return Optional.empty();
            }
        };
    }

    public static interface MatchGetter<I extends RecipeInput, T extends Recipe<I>> {
        public Optional<RecipeEntry<T>> getFirstMatch(I var1, World var2);
    }
}

