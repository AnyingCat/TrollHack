/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.search;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.search.IdentifierSearchProvider;
import net.minecraft.client.search.SearchProvider;
import net.minecraft.client.search.TextSearchProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class SearchManager {
    private static final Key RECIPE_OUTPUT = new Key();
    private static final Key ITEM_TOOLTIP = new Key();
    private static final Key ITEM_TAG = new Key();
    private CompletableFuture<SearchProvider<ItemStack>> itemTooltipReloadFuture = CompletableFuture.completedFuture(SearchProvider.empty());
    private CompletableFuture<SearchProvider<ItemStack>> itemTagReloadFuture = CompletableFuture.completedFuture(SearchProvider.empty());
    private CompletableFuture<SearchProvider<RecipeResultCollection>> recipeOutputReloadFuture = CompletableFuture.completedFuture(SearchProvider.empty());
    private final Map<Key, Runnable> reloaders = new IdentityHashMap<Key, Runnable>();

    private void addReloader(Key key, Runnable reloader) {
        reloader.run();
        this.reloaders.put(key, reloader);
    }

    public void refresh() {
        for (Runnable runnable : this.reloaders.values()) {
            runnable.run();
        }
    }

    private static Stream<String> collectItemTooltips(Stream<ItemStack> stacks, Item.TooltipContext context, TooltipType type) {
        return stacks.flatMap(stack -> stack.getTooltip(context, null, type).stream()).map(tooltip -> Formatting.strip(tooltip.getString()).trim()).filter(string -> !string.isEmpty());
    }

    public void addRecipeOutputReloader(ClientRecipeBook recipeBook, DynamicRegistryManager.Immutable registryManager) {
        this.addReloader(RECIPE_OUTPUT, () -> {
            List<RecipeResultCollection> list = recipeBook.getOrderedResults();
            Registry<Item> registry = registryManager.get(RegistryKeys.ITEM);
            Item.TooltipContext tooltipContext = Item.TooltipContext.create(registryManager);
            TooltipType.Default tooltipType = TooltipType.Default.BASIC;
            CompletableFuture<SearchProvider<RecipeResultCollection>> completableFuture = this.recipeOutputReloadFuture;
            this.recipeOutputReloadFuture = CompletableFuture.supplyAsync(() -> new TextSearchProvider<RecipeResultCollection>(resultCollection -> SearchManager.collectItemTooltips(resultCollection.getAllRecipes().stream().map(recipe -> recipe.value().getResult(registryManager)), tooltipContext, tooltipType), resultCollection -> resultCollection.getAllRecipes().stream().map(recipe -> registry.getId(recipe.value().getResult(registryManager).getItem())), list), Util.getMainWorkerExecutor());
            completableFuture.cancel(true);
        });
    }

    public SearchProvider<RecipeResultCollection> getRecipeOutputReloadFuture() {
        return this.recipeOutputReloadFuture.join();
    }

    public void addItemTagReloader(List<ItemStack> stacks) {
        this.addReloader(ITEM_TAG, () -> {
            CompletableFuture<SearchProvider<ItemStack>> completableFuture = this.itemTagReloadFuture;
            this.itemTagReloadFuture = CompletableFuture.supplyAsync(() -> new IdentifierSearchProvider<ItemStack>(stack -> stack.streamTags().map(TagKey::id), stacks), Util.getMainWorkerExecutor());
            completableFuture.cancel(true);
        });
    }

    public SearchProvider<ItemStack> getItemTagReloadFuture() {
        return this.itemTagReloadFuture.join();
    }

    public void addItemTooltipReloader(RegistryWrapper.WrapperLookup registryLookup, List<ItemStack> stacks) {
        this.addReloader(ITEM_TOOLTIP, () -> {
            Item.TooltipContext tooltipContext = Item.TooltipContext.create(registryLookup);
            TooltipType.Default tooltipType = TooltipType.Default.BASIC.withCreative();
            CompletableFuture<SearchProvider<ItemStack>> completableFuture = this.itemTooltipReloadFuture;
            this.itemTooltipReloadFuture = CompletableFuture.supplyAsync(() -> new TextSearchProvider<ItemStack>(stack -> SearchManager.collectItemTooltips(Stream.of(stack), tooltipContext, tooltipType), stack -> stack.getRegistryEntry().getKey().map(RegistryKey::getValue).stream(), stacks), Util.getMainWorkerExecutor());
            completableFuture.cancel(true);
        });
    }

    public SearchProvider<ItemStack> getItemTooltipReloadFuture() {
        return this.itemTooltipReloadFuture.join();
    }

    @Environment(value=EnvType.CLIENT)
    static class Key {
        Key() {
        }
    }
}

