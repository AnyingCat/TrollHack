/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.slf4j.Logger;

public class KnowledgeBookItem
extends Item {
    private static final Logger LOGGER = LogUtils.getLogger();

    public KnowledgeBookItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        List list = itemStack.getOrDefault(DataComponentTypes.RECIPES, List.of());
        itemStack.decrementUnlessCreative(1, user);
        if (list.isEmpty()) {
            return TypedActionResult.fail(itemStack);
        }
        if (!world.isClient) {
            RecipeManager recipeManager = world.getServer().getRecipeManager();
            ArrayList list2 = new ArrayList(list.size());
            for (Identifier identifier : list) {
                Optional<RecipeEntry<?>> optional = recipeManager.get(identifier);
                if (optional.isPresent()) {
                    list2.add(optional.get());
                    continue;
                }
                LOGGER.error("Invalid recipe: {}", (Object)identifier);
                return TypedActionResult.fail(itemStack);
            }
            user.unlockRecipes(list2);
            user.incrementStat(Stats.USED.getOrCreateStat(this));
        }
        return TypedActionResult.success(itemStack, world.isClient());
    }
}

