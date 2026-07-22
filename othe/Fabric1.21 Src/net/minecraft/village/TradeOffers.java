/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.village;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.component.type.SuspiciousStewEffectsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.provider.EnchantmentProvider;
import net.minecraft.enchantment.provider.TradeRebalanceEnchantmentProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.DyeItem;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.item.map.MapState;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.Structure;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

public class TradeOffers {
    private static final int DEFAULT_MAX_USES = 12;
    private static final int COMMON_MAX_USES = 16;
    private static final int RARE_MAX_USES = 3;
    private static final int NOVICE_SELL_XP = 1;
    private static final int NOVICE_BUY_XP = 2;
    private static final int APPRENTICE_SELL_XP = 5;
    private static final int APPRENTICE_BUY_XP = 10;
    private static final int JOURNEYMAN_SELL_XP = 10;
    private static final int JOURNEYMAN_BUY_XP = 20;
    private static final int EXPERT_SELL_XP = 15;
    private static final int EXPERT_BUY_XP = 30;
    private static final int MASTER_TRADE_XP = 30;
    private static final float LOW_PRICE_MULTIPLIER = 0.05f;
    private static final float HIGH_PRICE_MULTIPLIER = 0.2f;
    public static final Map<VillagerProfession, Int2ObjectMap<Factory[]>> PROFESSION_TO_LEVELED_TRADE = Util.make(Maps.newHashMap(), map -> {
        map.put(VillagerProfession.FARMER, TradeOffers.copyToFastUtilMap(ImmutableMap.of(1, new Factory[]{new BuyItemFactory(Items.WHEAT, 20, 16, 2), new BuyItemFactory(Items.POTATO, 26, 16, 2), new BuyItemFactory(Items.CARROT, 22, 16, 2), new BuyItemFactory(Items.BEETROOT, 15, 16, 2), new SellItemFactory(Items.BREAD, 1, 6, 16, 1)}, 2, new Factory[]{new BuyItemFactory(Blocks.PUMPKIN, 6, 12, 10), new SellItemFactory(Items.PUMPKIN_PIE, 1, 4, 5), new SellItemFactory(Items.APPLE, 1, 4, 16, 5)}, 3, new Factory[]{new SellItemFactory(Items.COOKIE, 3, 18, 10), new BuyItemFactory(Blocks.MELON, 4, 12, 20)}, 4, new Factory[]{new SellItemFactory(Blocks.CAKE, 1, 1, 12, 15), new SellSuspiciousStewFactory(StatusEffects.NIGHT_VISION, 100, 15), new SellSuspiciousStewFactory(StatusEffects.JUMP_BOOST, 160, 15), new SellSuspiciousStewFactory(StatusEffects.WEAKNESS, 140, 15), new SellSuspiciousStewFactory(StatusEffects.BLINDNESS, 120, 15), new SellSuspiciousStewFactory(StatusEffects.POISON, 280, 15), new SellSuspiciousStewFactory(StatusEffects.SATURATION, 7, 15)}, 5, new Factory[]{new SellItemFactory(Items.GOLDEN_CARROT, 3, 3, 30), new SellItemFactory(Items.GLISTERING_MELON_SLICE, 4, 3, 30)})));
        map.put(VillagerProfession.FISHERMAN, TradeOffers.copyToFastUtilMap(ImmutableMap.of(1, new Factory[]{new BuyItemFactory(Items.STRING, 20, 16, 2), new BuyItemFactory(Items.COAL, 10, 16, 2), new ProcessItemFactory((ItemConvertible)Items.COD, 6, 1, Items.COOKED_COD, 6, 16, 1, 0.05f), new SellItemFactory(Items.COD_BUCKET, 3, 1, 16, 1)}, 2, new Factory[]{new BuyItemFactory(Items.COD, 15, 16, 10), new ProcessItemFactory((ItemConvertible)Items.SALMON, 6, 1, Items.COOKED_SALMON, 6, 16, 5, 0.05f), new SellItemFactory(Items.CAMPFIRE, 2, 1, 5)}, 3, new Factory[]{new BuyItemFactory(Items.SALMON, 13, 16, 20), new SellEnchantedToolFactory(Items.FISHING_ROD, 3, 3, 10, 0.2f)}, 4, new Factory[]{new BuyItemFactory(Items.TROPICAL_FISH, 6, 12, 30)}, 5, new Factory[]{new BuyItemFactory(Items.PUFFERFISH, 4, 12, 30), new TypeAwareBuyForOneEmeraldFactory(1, 12, 30, ImmutableMap.builder().put(VillagerType.PLAINS, Items.OAK_BOAT).put(VillagerType.TAIGA, Items.SPRUCE_BOAT).put(VillagerType.SNOW, Items.SPRUCE_BOAT).put(VillagerType.DESERT, Items.JUNGLE_BOAT).put(VillagerType.JUNGLE, Items.JUNGLE_BOAT).put(VillagerType.SAVANNA, Items.ACACIA_BOAT).put(VillagerType.SWAMP, Items.DARK_OAK_BOAT).build())})));
        map.put(VillagerProfession.SHEPHERD, TradeOffers.copyToFastUtilMap(ImmutableMap.of(1, new Factory[]{new BuyItemFactory(Blocks.WHITE_WOOL, 18, 16, 2), new BuyItemFactory(Blocks.BROWN_WOOL, 18, 16, 2), new BuyItemFactory(Blocks.BLACK_WOOL, 18, 16, 2), new BuyItemFactory(Blocks.GRAY_WOOL, 18, 16, 2), new SellItemFactory(Items.SHEARS, 2, 1, 1)}, 2, new Factory[]{new BuyItemFactory(Items.WHITE_DYE, 12, 16, 10), new BuyItemFactory(Items.GRAY_DYE, 12, 16, 10), new BuyItemFactory(Items.BLACK_DYE, 12, 16, 10), new BuyItemFactory(Items.LIGHT_BLUE_DYE, 12, 16, 10), new BuyItemFactory(Items.LIME_DYE, 12, 16, 10), new SellItemFactory(Blocks.WHITE_WOOL, 1, 1, 16, 5), new SellItemFactory(Blocks.ORANGE_WOOL, 1, 1, 16, 5), new SellItemFactory(Blocks.MAGENTA_WOOL, 1, 1, 16, 5), new SellItemFactory(Blocks.LIGHT_BLUE_WOOL, 1, 1, 16, 5), new SellItemFactory(Blocks.YELLOW_WOOL, 1, 1, 16, 5), new SellItemFactory(Blocks.LIME_WOOL, 1, 1, 16, 5), new SellItemFactory(Blocks.PINK_WOOL, 1, 1, 16, 5), new SellItemFactory(Blocks.GRAY_WOOL, 1, 1, 16, 5), new SellItemFactory(Blocks.LIGHT_GRAY_WOOL, 1, 1, 16, 5), new SellItemFactory(Blocks.CYAN_WOOL, 1, 1, 16, 5), new SellItemFactory(Blocks.PURPLE_WOOL, 1, 1, 16, 5), new SellItemFactory(Blocks.BLUE_WOOL, 1, 1, 16, 5), new SellItemFactory(Blocks.BROWN_WOOL, 1, 1, 16, 5), new SellItemFactory(Blocks.GREEN_WOOL, 1, 1, 16, 5), new SellItemFactory(Blocks.RED_WOOL, 1, 1, 16, 5), new SellItemFactory(Blocks.BLACK_WOOL, 1, 1, 16, 5), new SellItemFactory(Blocks.WHITE_CARPET, 1, 4, 16, 5), new SellItemFactory(Blocks.ORANGE_CARPET, 1, 4, 16, 5), new SellItemFactory(Blocks.MAGENTA_CARPET, 1, 4, 16, 5), new SellItemFactory(Blocks.LIGHT_BLUE_CARPET, 1, 4, 16, 5), new SellItemFactory(Blocks.YELLOW_CARPET, 1, 4, 16, 5), new SellItemFactory(Blocks.LIME_CARPET, 1, 4, 16, 5), new SellItemFactory(Blocks.PINK_CARPET, 1, 4, 16, 5), new SellItemFactory(Blocks.GRAY_CARPET, 1, 4, 16, 5), new SellItemFactory(Blocks.LIGHT_GRAY_CARPET, 1, 4, 16, 5), new SellItemFactory(Blocks.CYAN_CARPET, 1, 4, 16, 5), new SellItemFactory(Blocks.PURPLE_CARPET, 1, 4, 16, 5), new SellItemFactory(Blocks.BLUE_CARPET, 1, 4, 16, 5), new SellItemFactory(Blocks.BROWN_CARPET, 1, 4, 16, 5), new SellItemFactory(Blocks.GREEN_CARPET, 1, 4, 16, 5), new SellItemFactory(Blocks.RED_CARPET, 1, 4, 16, 5), new SellItemFactory(Blocks.BLACK_CARPET, 1, 4, 16, 5)}, 3, new Factory[]{new BuyItemFactory(Items.YELLOW_DYE, 12, 16, 20), new BuyItemFactory(Items.LIGHT_GRAY_DYE, 12, 16, 20), new BuyItemFactory(Items.ORANGE_DYE, 12, 16, 20), new BuyItemFactory(Items.RED_DYE, 12, 16, 20), new BuyItemFactory(Items.PINK_DYE, 12, 16, 20), new SellItemFactory(Blocks.WHITE_BED, 3, 1, 12, 10), new SellItemFactory(Blocks.YELLOW_BED, 3, 1, 12, 10), new SellItemFactory(Blocks.RED_BED, 3, 1, 12, 10), new SellItemFactory(Blocks.BLACK_BED, 3, 1, 12, 10), new SellItemFactory(Blocks.BLUE_BED, 3, 1, 12, 10), new SellItemFactory(Blocks.BROWN_BED, 3, 1, 12, 10), new SellItemFactory(Blocks.CYAN_BED, 3, 1, 12, 10), new SellItemFactory(Blocks.GRAY_BED, 3, 1, 12, 10), new SellItemFactory(Blocks.GREEN_BED, 3, 1, 12, 10), new SellItemFactory(Blocks.LIGHT_BLUE_BED, 3, 1, 12, 10), new SellItemFactory(Blocks.LIGHT_GRAY_BED, 3, 1, 12, 10), new SellItemFactory(Blocks.LIME_BED, 3, 1, 12, 10), new SellItemFactory(Blocks.MAGENTA_BED, 3, 1, 12, 10), new SellItemFactory(Blocks.ORANGE_BED, 3, 1, 12, 10), new SellItemFactory(Blocks.PINK_BED, 3, 1, 12, 10), new SellItemFactory(Blocks.PURPLE_BED, 3, 1, 12, 10)}, 4, new Factory[]{new BuyItemFactory(Items.BROWN_DYE, 12, 16, 30), new BuyItemFactory(Items.PURPLE_DYE, 12, 16, 30), new BuyItemFactory(Items.BLUE_DYE, 12, 16, 30), new BuyItemFactory(Items.GREEN_DYE, 12, 16, 30), new BuyItemFactory(Items.MAGENTA_DYE, 12, 16, 30), new BuyItemFactory(Items.CYAN_DYE, 12, 16, 30), new SellItemFactory(Items.WHITE_BANNER, 3, 1, 12, 15), new SellItemFactory(Items.BLUE_BANNER, 3, 1, 12, 15), new SellItemFactory(Items.LIGHT_BLUE_BANNER, 3, 1, 12, 15), new SellItemFactory(Items.RED_BANNER, 3, 1, 12, 15), new SellItemFactory(Items.PINK_BANNER, 3, 1, 12, 15), new SellItemFactory(Items.GREEN_BANNER, 3, 1, 12, 15), new SellItemFactory(Items.LIME_BANNER, 3, 1, 12, 15), new SellItemFactory(Items.GRAY_BANNER, 3, 1, 12, 15), new SellItemFactory(Items.BLACK_BANNER, 3, 1, 12, 15), new SellItemFactory(Items.PURPLE_BANNER, 3, 1, 12, 15), new SellItemFactory(Items.MAGENTA_BANNER, 3, 1, 12, 15), new SellItemFactory(Items.CYAN_BANNER, 3, 1, 12, 15), new SellItemFactory(Items.BROWN_BANNER, 3, 1, 12, 15), new SellItemFactory(Items.YELLOW_BANNER, 3, 1, 12, 15), new SellItemFactory(Items.ORANGE_BANNER, 3, 1, 12, 15), new SellItemFactory(Items.LIGHT_GRAY_BANNER, 3, 1, 12, 15)}, 5, new Factory[]{new SellItemFactory(Items.PAINTING, 2, 3, 30)})));
        map.put(VillagerProfession.FLETCHER, TradeOffers.copyToFastUtilMap(ImmutableMap.of(1, new Factory[]{new BuyItemFactory(Items.STICK, 32, 16, 2), new SellItemFactory(Items.ARROW, 1, 16, 1), new ProcessItemFactory((ItemConvertible)Blocks.GRAVEL, 10, 1, Items.FLINT, 10, 12, 1, 0.05f)}, 2, new Factory[]{new BuyItemFactory(Items.FLINT, 26, 12, 10), new SellItemFactory(Items.BOW, 2, 1, 5)}, 3, new Factory[]{new BuyItemFactory(Items.STRING, 14, 16, 20), new SellItemFactory(Items.CROSSBOW, 3, 1, 10)}, 4, new Factory[]{new BuyItemFactory(Items.FEATHER, 24, 16, 30), new SellEnchantedToolFactory(Items.BOW, 2, 3, 15)}, 5, new Factory[]{new BuyItemFactory(Items.TRIPWIRE_HOOK, 8, 12, 30), new SellEnchantedToolFactory(Items.CROSSBOW, 3, 3, 15), new SellPotionHoldingItemFactory(Items.ARROW, 5, Items.TIPPED_ARROW, 5, 2, 12, 30)})));
        map.put(VillagerProfession.LIBRARIAN, TradeOffers.copyToFastUtilMap(ImmutableMap.builder().put(1, new Factory[]{new BuyItemFactory(Items.PAPER, 24, 16, 2), new EnchantBookFactory(1, EnchantmentTags.TRADEABLE), new SellItemFactory(Blocks.BOOKSHELF, 9, 1, 12, 1)}).put(2, new Factory[]{new BuyItemFactory(Items.BOOK, 4, 12, 10), new EnchantBookFactory(5, EnchantmentTags.TRADEABLE), new SellItemFactory(Items.LANTERN, 1, 1, 5)}).put(3, new Factory[]{new BuyItemFactory(Items.INK_SAC, 5, 12, 20), new EnchantBookFactory(10, EnchantmentTags.TRADEABLE), new SellItemFactory(Items.GLASS, 1, 4, 10)}).put(4, new Factory[]{new BuyItemFactory(Items.WRITABLE_BOOK, 2, 12, 30), new EnchantBookFactory(15, EnchantmentTags.TRADEABLE), new SellItemFactory(Items.CLOCK, 5, 1, 15), new SellItemFactory(Items.COMPASS, 4, 1, 15)}).put(5, new Factory[]{new SellItemFactory(Items.NAME_TAG, 20, 1, 30)}).build()));
        map.put(VillagerProfession.CARTOGRAPHER, TradeOffers.copyToFastUtilMap(ImmutableMap.of(1, new Factory[]{new BuyItemFactory(Items.PAPER, 24, 16, 2), new SellItemFactory(Items.MAP, 7, 1, 1)}, 2, new Factory[]{new BuyItemFactory(Items.GLASS_PANE, 11, 16, 10), new SellMapFactory(13, StructureTags.ON_OCEAN_EXPLORER_MAPS, "filled_map.monument", MapDecorationTypes.MONUMENT, 12, 5)}, 3, new Factory[]{new BuyItemFactory(Items.COMPASS, 1, 12, 20), new SellMapFactory(14, StructureTags.ON_WOODLAND_EXPLORER_MAPS, "filled_map.mansion", MapDecorationTypes.MANSION, 12, 10), new SellMapFactory(12, StructureTags.ON_TRIAL_CHAMBERS_MAPS, "filled_map.trial_chambers", MapDecorationTypes.TRIAL_CHAMBERS, 12, 10)}, 4, new Factory[]{new SellItemFactory(Items.ITEM_FRAME, 7, 1, 15), new SellItemFactory(Items.WHITE_BANNER, 3, 1, 15), new SellItemFactory(Items.BLUE_BANNER, 3, 1, 15), new SellItemFactory(Items.LIGHT_BLUE_BANNER, 3, 1, 15), new SellItemFactory(Items.RED_BANNER, 3, 1, 15), new SellItemFactory(Items.PINK_BANNER, 3, 1, 15), new SellItemFactory(Items.GREEN_BANNER, 3, 1, 15), new SellItemFactory(Items.LIME_BANNER, 3, 1, 15), new SellItemFactory(Items.GRAY_BANNER, 3, 1, 15), new SellItemFactory(Items.BLACK_BANNER, 3, 1, 15), new SellItemFactory(Items.PURPLE_BANNER, 3, 1, 15), new SellItemFactory(Items.MAGENTA_BANNER, 3, 1, 15), new SellItemFactory(Items.CYAN_BANNER, 3, 1, 15), new SellItemFactory(Items.BROWN_BANNER, 3, 1, 15), new SellItemFactory(Items.YELLOW_BANNER, 3, 1, 15), new SellItemFactory(Items.ORANGE_BANNER, 3, 1, 15), new SellItemFactory(Items.LIGHT_GRAY_BANNER, 3, 1, 15)}, 5, new Factory[]{new SellItemFactory(Items.GLOBE_BANNER_PATTERN, 8, 1, 30)})));
        map.put(VillagerProfession.CLERIC, TradeOffers.copyToFastUtilMap(ImmutableMap.of(1, new Factory[]{new BuyItemFactory(Items.ROTTEN_FLESH, 32, 16, 2), new SellItemFactory(Items.REDSTONE, 1, 2, 1)}, 2, new Factory[]{new BuyItemFactory(Items.GOLD_INGOT, 3, 12, 10), new SellItemFactory(Items.LAPIS_LAZULI, 1, 1, 5)}, 3, new Factory[]{new BuyItemFactory(Items.RABBIT_FOOT, 2, 12, 20), new SellItemFactory(Blocks.GLOWSTONE, 4, 1, 12, 10)}, 4, new Factory[]{new BuyItemFactory(Items.TURTLE_SCUTE, 4, 12, 30), new BuyItemFactory(Items.GLASS_BOTTLE, 9, 12, 30), new SellItemFactory(Items.ENDER_PEARL, 5, 1, 15)}, 5, new Factory[]{new BuyItemFactory(Items.NETHER_WART, 22, 12, 30), new SellItemFactory(Items.EXPERIENCE_BOTTLE, 3, 1, 30)})));
        map.put(VillagerProfession.ARMORER, TradeOffers.copyToFastUtilMap(ImmutableMap.of(1, new Factory[]{new BuyItemFactory(Items.COAL, 15, 16, 2), new SellItemFactory(new ItemStack(Items.IRON_LEGGINGS), 7, 1, 12, 1, 0.2f), new SellItemFactory(new ItemStack(Items.IRON_BOOTS), 4, 1, 12, 1, 0.2f), new SellItemFactory(new ItemStack(Items.IRON_HELMET), 5, 1, 12, 1, 0.2f), new SellItemFactory(new ItemStack(Items.IRON_CHESTPLATE), 9, 1, 12, 1, 0.2f)}, 2, new Factory[]{new BuyItemFactory(Items.IRON_INGOT, 4, 12, 10), new SellItemFactory(new ItemStack(Items.BELL), 36, 1, 12, 5, 0.2f), new SellItemFactory(new ItemStack(Items.CHAINMAIL_BOOTS), 1, 1, 12, 5, 0.2f), new SellItemFactory(new ItemStack(Items.CHAINMAIL_LEGGINGS), 3, 1, 12, 5, 0.2f)}, 3, new Factory[]{new BuyItemFactory(Items.LAVA_BUCKET, 1, 12, 20), new BuyItemFactory(Items.DIAMOND, 1, 12, 20), new SellItemFactory(new ItemStack(Items.CHAINMAIL_HELMET), 1, 1, 12, 10, 0.2f), new SellItemFactory(new ItemStack(Items.CHAINMAIL_CHESTPLATE), 4, 1, 12, 10, 0.2f), new SellItemFactory(new ItemStack(Items.SHIELD), 5, 1, 12, 10, 0.2f)}, 4, new Factory[]{new SellEnchantedToolFactory(Items.DIAMOND_LEGGINGS, 14, 3, 15, 0.2f), new SellEnchantedToolFactory(Items.DIAMOND_BOOTS, 8, 3, 15, 0.2f)}, 5, new Factory[]{new SellEnchantedToolFactory(Items.DIAMOND_HELMET, 8, 3, 30, 0.2f), new SellEnchantedToolFactory(Items.DIAMOND_CHESTPLATE, 16, 3, 30, 0.2f)})));
        map.put(VillagerProfession.WEAPONSMITH, TradeOffers.copyToFastUtilMap(ImmutableMap.of(1, new Factory[]{new BuyItemFactory(Items.COAL, 15, 16, 2), new SellItemFactory(new ItemStack(Items.IRON_AXE), 3, 1, 12, 1, 0.2f), new SellEnchantedToolFactory(Items.IRON_SWORD, 2, 3, 1)}, 2, new Factory[]{new BuyItemFactory(Items.IRON_INGOT, 4, 12, 10), new SellItemFactory(new ItemStack(Items.BELL), 36, 1, 12, 5, 0.2f)}, 3, new Factory[]{new BuyItemFactory(Items.FLINT, 24, 12, 20)}, 4, new Factory[]{new BuyItemFactory(Items.DIAMOND, 1, 12, 30), new SellEnchantedToolFactory(Items.DIAMOND_AXE, 12, 3, 15, 0.2f)}, 5, new Factory[]{new SellEnchantedToolFactory(Items.DIAMOND_SWORD, 8, 3, 30, 0.2f)})));
        map.put(VillagerProfession.TOOLSMITH, TradeOffers.copyToFastUtilMap(ImmutableMap.of(1, new Factory[]{new BuyItemFactory(Items.COAL, 15, 16, 2), new SellItemFactory(new ItemStack(Items.STONE_AXE), 1, 1, 12, 1, 0.2f), new SellItemFactory(new ItemStack(Items.STONE_SHOVEL), 1, 1, 12, 1, 0.2f), new SellItemFactory(new ItemStack(Items.STONE_PICKAXE), 1, 1, 12, 1, 0.2f), new SellItemFactory(new ItemStack(Items.STONE_HOE), 1, 1, 12, 1, 0.2f)}, 2, new Factory[]{new BuyItemFactory(Items.IRON_INGOT, 4, 12, 10), new SellItemFactory(new ItemStack(Items.BELL), 36, 1, 12, 5, 0.2f)}, 3, new Factory[]{new BuyItemFactory(Items.FLINT, 30, 12, 20), new SellEnchantedToolFactory(Items.IRON_AXE, 1, 3, 10, 0.2f), new SellEnchantedToolFactory(Items.IRON_SHOVEL, 2, 3, 10, 0.2f), new SellEnchantedToolFactory(Items.IRON_PICKAXE, 3, 3, 10, 0.2f), new SellItemFactory(new ItemStack(Items.DIAMOND_HOE), 4, 1, 3, 10, 0.2f)}, 4, new Factory[]{new BuyItemFactory(Items.DIAMOND, 1, 12, 30), new SellEnchantedToolFactory(Items.DIAMOND_AXE, 12, 3, 15, 0.2f), new SellEnchantedToolFactory(Items.DIAMOND_SHOVEL, 5, 3, 15, 0.2f)}, 5, new Factory[]{new SellEnchantedToolFactory(Items.DIAMOND_PICKAXE, 13, 3, 30, 0.2f)})));
        map.put(VillagerProfession.BUTCHER, TradeOffers.copyToFastUtilMap(ImmutableMap.of(1, new Factory[]{new BuyItemFactory(Items.CHICKEN, 14, 16, 2), new BuyItemFactory(Items.PORKCHOP, 7, 16, 2), new BuyItemFactory(Items.RABBIT, 4, 16, 2), new SellItemFactory(Items.RABBIT_STEW, 1, 1, 1)}, 2, new Factory[]{new BuyItemFactory(Items.COAL, 15, 16, 2), new SellItemFactory(Items.COOKED_PORKCHOP, 1, 5, 16, 5), new SellItemFactory(Items.COOKED_CHICKEN, 1, 8, 16, 5)}, 3, new Factory[]{new BuyItemFactory(Items.MUTTON, 7, 16, 20), new BuyItemFactory(Items.BEEF, 10, 16, 20)}, 4, new Factory[]{new BuyItemFactory(Items.DRIED_KELP_BLOCK, 10, 12, 30)}, 5, new Factory[]{new BuyItemFactory(Items.SWEET_BERRIES, 10, 12, 30)})));
        map.put(VillagerProfession.LEATHERWORKER, TradeOffers.copyToFastUtilMap(ImmutableMap.of(1, new Factory[]{new BuyItemFactory(Items.LEATHER, 6, 16, 2), new SellDyedArmorFactory(Items.LEATHER_LEGGINGS, 3), new SellDyedArmorFactory(Items.LEATHER_CHESTPLATE, 7)}, 2, new Factory[]{new BuyItemFactory(Items.FLINT, 26, 12, 10), new SellDyedArmorFactory(Items.LEATHER_HELMET, 5, 12, 5), new SellDyedArmorFactory(Items.LEATHER_BOOTS, 4, 12, 5)}, 3, new Factory[]{new BuyItemFactory(Items.RABBIT_HIDE, 9, 12, 20), new SellDyedArmorFactory(Items.LEATHER_CHESTPLATE, 7)}, 4, new Factory[]{new BuyItemFactory(Items.TURTLE_SCUTE, 4, 12, 30), new SellDyedArmorFactory(Items.LEATHER_HORSE_ARMOR, 6, 12, 15)}, 5, new Factory[]{new SellItemFactory(new ItemStack(Items.SADDLE), 6, 1, 12, 30, 0.2f), new SellDyedArmorFactory(Items.LEATHER_HELMET, 5, 12, 30)})));
        map.put(VillagerProfession.MASON, TradeOffers.copyToFastUtilMap(ImmutableMap.of(1, new Factory[]{new BuyItemFactory(Items.CLAY_BALL, 10, 16, 2), new SellItemFactory(Items.BRICK, 1, 10, 16, 1)}, 2, new Factory[]{new BuyItemFactory(Blocks.STONE, 20, 16, 10), new SellItemFactory(Blocks.CHISELED_STONE_BRICKS, 1, 4, 16, 5)}, 3, new Factory[]{new BuyItemFactory(Blocks.GRANITE, 16, 16, 20), new BuyItemFactory(Blocks.ANDESITE, 16, 16, 20), new BuyItemFactory(Blocks.DIORITE, 16, 16, 20), new SellItemFactory(Blocks.DRIPSTONE_BLOCK, 1, 4, 16, 10), new SellItemFactory(Blocks.POLISHED_ANDESITE, 1, 4, 16, 10), new SellItemFactory(Blocks.POLISHED_DIORITE, 1, 4, 16, 10), new SellItemFactory(Blocks.POLISHED_GRANITE, 1, 4, 16, 10)}, 4, new Factory[]{new BuyItemFactory(Items.QUARTZ, 12, 12, 30), new SellItemFactory(Blocks.ORANGE_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.WHITE_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.BLUE_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.LIGHT_BLUE_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.GRAY_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.LIGHT_GRAY_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.BLACK_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.RED_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.PINK_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.MAGENTA_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.LIME_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.GREEN_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.CYAN_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.PURPLE_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.YELLOW_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.BROWN_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.ORANGE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.WHITE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.BLUE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.GRAY_GLAZED_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.BLACK_GLAZED_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.RED_GLAZED_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.PINK_GLAZED_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.MAGENTA_GLAZED_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.LIME_GLAZED_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.GREEN_GLAZED_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.CYAN_GLAZED_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.PURPLE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.YELLOW_GLAZED_TERRACOTTA, 1, 1, 12, 15), new SellItemFactory(Blocks.BROWN_GLAZED_TERRACOTTA, 1, 1, 12, 15)}, 5, new Factory[]{new SellItemFactory(Blocks.QUARTZ_PILLAR, 1, 1, 12, 30), new SellItemFactory(Blocks.QUARTZ_BLOCK, 1, 1, 12, 30)})));
    });
    public static final Int2ObjectMap<Factory[]> WANDERING_TRADER_TRADES = TradeOffers.copyToFastUtilMap(ImmutableMap.of(1, new Factory[]{new SellItemFactory(Items.SEA_PICKLE, 2, 1, 5, 1), new SellItemFactory(Items.SLIME_BALL, 4, 1, 5, 1), new SellItemFactory(Items.GLOWSTONE, 2, 1, 5, 1), new SellItemFactory(Items.NAUTILUS_SHELL, 5, 1, 5, 1), new SellItemFactory(Items.FERN, 1, 1, 12, 1), new SellItemFactory(Items.SUGAR_CANE, 1, 1, 8, 1), new SellItemFactory(Items.PUMPKIN, 1, 1, 4, 1), new SellItemFactory(Items.KELP, 3, 1, 12, 1), new SellItemFactory(Items.CACTUS, 3, 1, 8, 1), new SellItemFactory(Items.DANDELION, 1, 1, 12, 1), new SellItemFactory(Items.POPPY, 1, 1, 12, 1), new SellItemFactory(Items.BLUE_ORCHID, 1, 1, 8, 1), new SellItemFactory(Items.ALLIUM, 1, 1, 12, 1), new SellItemFactory(Items.AZURE_BLUET, 1, 1, 12, 1), new SellItemFactory(Items.RED_TULIP, 1, 1, 12, 1), new SellItemFactory(Items.ORANGE_TULIP, 1, 1, 12, 1), new SellItemFactory(Items.WHITE_TULIP, 1, 1, 12, 1), new SellItemFactory(Items.PINK_TULIP, 1, 1, 12, 1), new SellItemFactory(Items.OXEYE_DAISY, 1, 1, 12, 1), new SellItemFactory(Items.CORNFLOWER, 1, 1, 12, 1), new SellItemFactory(Items.LILY_OF_THE_VALLEY, 1, 1, 7, 1), new SellItemFactory(Items.WHEAT_SEEDS, 1, 1, 12, 1), new SellItemFactory(Items.BEETROOT_SEEDS, 1, 1, 12, 1), new SellItemFactory(Items.PUMPKIN_SEEDS, 1, 1, 12, 1), new SellItemFactory(Items.MELON_SEEDS, 1, 1, 12, 1), new SellItemFactory(Items.ACACIA_SAPLING, 5, 1, 8, 1), new SellItemFactory(Items.BIRCH_SAPLING, 5, 1, 8, 1), new SellItemFactory(Items.DARK_OAK_SAPLING, 5, 1, 8, 1), new SellItemFactory(Items.JUNGLE_SAPLING, 5, 1, 8, 1), new SellItemFactory(Items.OAK_SAPLING, 5, 1, 8, 1), new SellItemFactory(Items.SPRUCE_SAPLING, 5, 1, 8, 1), new SellItemFactory(Items.CHERRY_SAPLING, 5, 1, 8, 1), new SellItemFactory(Items.MANGROVE_PROPAGULE, 5, 1, 8, 1), new SellItemFactory(Items.RED_DYE, 1, 3, 12, 1), new SellItemFactory(Items.WHITE_DYE, 1, 3, 12, 1), new SellItemFactory(Items.BLUE_DYE, 1, 3, 12, 1), new SellItemFactory(Items.PINK_DYE, 1, 3, 12, 1), new SellItemFactory(Items.BLACK_DYE, 1, 3, 12, 1), new SellItemFactory(Items.GREEN_DYE, 1, 3, 12, 1), new SellItemFactory(Items.LIGHT_GRAY_DYE, 1, 3, 12, 1), new SellItemFactory(Items.MAGENTA_DYE, 1, 3, 12, 1), new SellItemFactory(Items.YELLOW_DYE, 1, 3, 12, 1), new SellItemFactory(Items.GRAY_DYE, 1, 3, 12, 1), new SellItemFactory(Items.PURPLE_DYE, 1, 3, 12, 1), new SellItemFactory(Items.LIGHT_BLUE_DYE, 1, 3, 12, 1), new SellItemFactory(Items.LIME_DYE, 1, 3, 12, 1), new SellItemFactory(Items.ORANGE_DYE, 1, 3, 12, 1), new SellItemFactory(Items.BROWN_DYE, 1, 3, 12, 1), new SellItemFactory(Items.CYAN_DYE, 1, 3, 12, 1), new SellItemFactory(Items.BRAIN_CORAL_BLOCK, 3, 1, 8, 1), new SellItemFactory(Items.BUBBLE_CORAL_BLOCK, 3, 1, 8, 1), new SellItemFactory(Items.FIRE_CORAL_BLOCK, 3, 1, 8, 1), new SellItemFactory(Items.HORN_CORAL_BLOCK, 3, 1, 8, 1), new SellItemFactory(Items.TUBE_CORAL_BLOCK, 3, 1, 8, 1), new SellItemFactory(Items.VINE, 1, 1, 12, 1), new SellItemFactory(Items.BROWN_MUSHROOM, 1, 1, 12, 1), new SellItemFactory(Items.RED_MUSHROOM, 1, 1, 12, 1), new SellItemFactory(Items.LILY_PAD, 1, 2, 5, 1), new SellItemFactory(Items.SMALL_DRIPLEAF, 1, 2, 5, 1), new SellItemFactory(Items.SAND, 1, 8, 8, 1), new SellItemFactory(Items.RED_SAND, 1, 4, 6, 1), new SellItemFactory(Items.POINTED_DRIPSTONE, 1, 2, 5, 1), new SellItemFactory(Items.ROOTED_DIRT, 1, 2, 5, 1), new SellItemFactory(Items.MOSS_BLOCK, 1, 2, 5, 1)}, 2, new Factory[]{new SellItemFactory(Items.TROPICAL_FISH_BUCKET, 5, 1, 4, 1), new SellItemFactory(Items.PUFFERFISH_BUCKET, 5, 1, 4, 1), new SellItemFactory(Items.PACKED_ICE, 3, 1, 6, 1), new SellItemFactory(Items.BLUE_ICE, 6, 1, 6, 1), new SellItemFactory(Items.GUNPOWDER, 1, 1, 8, 1), new SellItemFactory(Items.PODZOL, 3, 3, 6, 1)}));
    private static final SellMapFactory SELL_DESERT_VILLAGE_MAP_TRADE = new SellMapFactory(8, StructureTags.ON_DESERT_VILLAGE_MAPS, "filled_map.village_desert", MapDecorationTypes.VILLAGE_DESERT, 12, 5);
    private static final SellMapFactory SELL_SAVANNA_VILLAGE_MAP_TRADE = new SellMapFactory(8, StructureTags.ON_SAVANNA_VILLAGE_MAPS, "filled_map.village_savanna", MapDecorationTypes.VILLAGE_SAVANNA, 12, 5);
    private static final SellMapFactory SELL_PLAINS_VILLAGE_MAP_TRADE = new SellMapFactory(8, StructureTags.ON_PLAINS_VILLAGE_MAPS, "filled_map.village_plains", MapDecorationTypes.VILLAGE_PLAINS, 12, 5);
    private static final SellMapFactory SELL_TAIGA_VILLAGE_MAP_TRADE = new SellMapFactory(8, StructureTags.ON_TAIGA_VILLAGE_MAPS, "filled_map.village_taiga", MapDecorationTypes.VILLAGE_TAIGA, 12, 5);
    private static final SellMapFactory SELL_SNOWY_VILLAGE_MAP_TRADE = new SellMapFactory(8, StructureTags.ON_SNOWY_VILLAGE_MAPS, "filled_map.village_snowy", MapDecorationTypes.VILLAGE_SNOWY, 12, 5);
    private static final SellMapFactory SELL_JUNGLE_TEMPLE_MAP_TRADE = new SellMapFactory(8, StructureTags.ON_JUNGLE_EXPLORER_MAPS, "filled_map.explorer_jungle", MapDecorationTypes.JUNGLE_TEMPLE, 12, 5);
    private static final SellMapFactory SELL_SWAMP_HUT_MAP_TRADE = new SellMapFactory(8, StructureTags.ON_SWAMP_EXPLORER_MAPS, "filled_map.explorer_swamp", MapDecorationTypes.SWAMP_HUT, 12, 5);
    public static final Map<VillagerProfession, Int2ObjectMap<Factory[]>> REBALANCED_PROFESSION_TO_LEVELED_TRADE = Map.of(VillagerProfession.LIBRARIAN, TradeOffers.copyToFastUtilMap(ImmutableMap.builder().put(1, new Factory[]{new BuyItemFactory(Items.PAPER, 24, 16, 2), TradeOffers.createLibrarianTradeFactory(1), new SellItemFactory(Blocks.BOOKSHELF, 9, 1, 12, 1)}).put(2, new Factory[]{new BuyItemFactory(Items.BOOK, 4, 12, 10), TradeOffers.createLibrarianTradeFactory(5), new SellItemFactory(Items.LANTERN, 1, 1, 5)}).put(3, new Factory[]{new BuyItemFactory(Items.INK_SAC, 5, 12, 20), TradeOffers.createLibrarianTradeFactory(10), new SellItemFactory(Items.GLASS, 1, 4, 10)}).put(4, new Factory[]{new BuyItemFactory(Items.WRITABLE_BOOK, 2, 12, 30), new SellItemFactory(Items.CLOCK, 5, 1, 15), new SellItemFactory(Items.COMPASS, 4, 1, 15)}).put(5, new Factory[]{TradeOffers.createMasterLibrarianTradeFactory(), new SellItemFactory(Items.NAME_TAG, 20, 1, 30)}).build()), VillagerProfession.ARMORER, TradeOffers.copyToFastUtilMap(ImmutableMap.builder().put(1, new Factory[]{new BuyItemFactory(Items.COAL, 15, 12, 2), new BuyItemFactory(Items.IRON_INGOT, 5, 12, 2)}).put(2, new Factory[]{TypedWrapperFactory.of(new SellItemFactory(Items.IRON_BOOTS, 4, 1, 12, 5, 0.05f), VillagerType.DESERT, VillagerType.PLAINS, VillagerType.SAVANNA, VillagerType.SNOW, VillagerType.TAIGA), TypedWrapperFactory.of(new SellItemFactory(Items.CHAINMAIL_BOOTS, 4, 1, 12, 5, 0.05f), VillagerType.JUNGLE, VillagerType.SWAMP), TypedWrapperFactory.of(new SellItemFactory(Items.IRON_HELMET, 5, 1, 12, 5, 0.05f), VillagerType.DESERT, VillagerType.PLAINS, VillagerType.SAVANNA, VillagerType.SNOW, VillagerType.TAIGA), TypedWrapperFactory.of(new SellItemFactory(Items.CHAINMAIL_HELMET, 5, 1, 12, 5, 0.05f), VillagerType.JUNGLE, VillagerType.SWAMP), TypedWrapperFactory.of(new SellItemFactory(Items.IRON_LEGGINGS, 7, 1, 12, 5, 0.05f), VillagerType.DESERT, VillagerType.PLAINS, VillagerType.SAVANNA, VillagerType.SNOW, VillagerType.TAIGA), TypedWrapperFactory.of(new SellItemFactory(Items.CHAINMAIL_LEGGINGS, 7, 1, 12, 5, 0.05f), VillagerType.JUNGLE, VillagerType.SWAMP), TypedWrapperFactory.of(new SellItemFactory(Items.IRON_CHESTPLATE, 9, 1, 12, 5, 0.05f), VillagerType.DESERT, VillagerType.PLAINS, VillagerType.SAVANNA, VillagerType.SNOW, VillagerType.TAIGA), TypedWrapperFactory.of(new SellItemFactory(Items.CHAINMAIL_CHESTPLATE, 9, 1, 12, 5, 0.05f), VillagerType.JUNGLE, VillagerType.SWAMP)}).put(3, new Factory[]{new BuyItemFactory(Items.LAVA_BUCKET, 1, 12, 20), new SellItemFactory(Items.SHIELD, 5, 1, 12, 10, 0.05f), new SellItemFactory(Items.BELL, 36, 1, 12, 10, 0.2f)}).put(4, new Factory[]{TypedWrapperFactory.of(new SellItemFactory(Items.IRON_BOOTS, 8, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.DESERT_ARMORER_BOOTS_4), VillagerType.DESERT), TypedWrapperFactory.of(new SellItemFactory(Items.IRON_HELMET, 9, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.DESERT_ARMORER_HELMET_4), VillagerType.DESERT), TypedWrapperFactory.of(new SellItemFactory(Items.IRON_LEGGINGS, 11, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.DESERT_ARMORER_LEGGINGS_4), VillagerType.DESERT), TypedWrapperFactory.of(new SellItemFactory(Items.IRON_CHESTPLATE, 13, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.DESERT_ARMORER_CHESTPLATE_4), VillagerType.DESERT), TypedWrapperFactory.of(new SellItemFactory(Items.IRON_BOOTS, 8, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.PLAINS_ARMORER_BOOTS_4), VillagerType.PLAINS), TypedWrapperFactory.of(new SellItemFactory(Items.IRON_HELMET, 9, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.PLAINS_ARMORER_HELMET_4), VillagerType.PLAINS), TypedWrapperFactory.of(new SellItemFactory(Items.IRON_LEGGINGS, 11, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.PLAINS_ARMORER_LEGGINGS_4), VillagerType.PLAINS), TypedWrapperFactory.of(new SellItemFactory(Items.IRON_CHESTPLATE, 13, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.PLAINS_ARMORER_CHESTPLATE_4), VillagerType.PLAINS), TypedWrapperFactory.of(new SellItemFactory(Items.IRON_BOOTS, 2, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.SAVANNA_ARMORER_BOOTS_4), VillagerType.SAVANNA), TypedWrapperFactory.of(new SellItemFactory(Items.IRON_HELMET, 3, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.SAVANNA_ARMORER_HELMET_4), VillagerType.SAVANNA), TypedWrapperFactory.of(new SellItemFactory(Items.IRON_LEGGINGS, 5, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.SAVANNA_ARMORER_LEGGINGS_4), VillagerType.SAVANNA), TypedWrapperFactory.of(new SellItemFactory(Items.IRON_CHESTPLATE, 7, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.SAVANNA_ARMORER_CHESTPLATE_4), VillagerType.SAVANNA), TypedWrapperFactory.of(new SellItemFactory(Items.IRON_BOOTS, 8, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.SNOW_ARMORER_BOOTS_4), VillagerType.SNOW), TypedWrapperFactory.of(new SellItemFactory(Items.IRON_HELMET, 9, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.SNOW_ARMORER_HELMET_4), VillagerType.SNOW), TypedWrapperFactory.of(new SellItemFactory(Items.CHAINMAIL_BOOTS, 8, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.JUNGLE_ARMORER_BOOTS_4), VillagerType.JUNGLE), TypedWrapperFactory.of(new SellItemFactory(Items.CHAINMAIL_HELMET, 9, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.JUNGLE_ARMORER_HELMET_4), VillagerType.JUNGLE), TypedWrapperFactory.of(new SellItemFactory(Items.CHAINMAIL_LEGGINGS, 11, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.JUNGLE_ARMORER_LEGGINGS_4), VillagerType.JUNGLE), TypedWrapperFactory.of(new SellItemFactory(Items.CHAINMAIL_CHESTPLATE, 13, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.JUNGLE_ARMORER_CHESTPLATE_4), VillagerType.JUNGLE), TypedWrapperFactory.of(new SellItemFactory(Items.CHAINMAIL_BOOTS, 8, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.SWAMP_ARMORER_BOOTS_4), VillagerType.SWAMP), TypedWrapperFactory.of(new SellItemFactory(Items.CHAINMAIL_HELMET, 9, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.SWAMP_ARMORER_HELMET_4), VillagerType.SWAMP), TypedWrapperFactory.of(new SellItemFactory(Items.CHAINMAIL_LEGGINGS, 11, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.SWAMP_ARMORER_LEGGINGS_4), VillagerType.SWAMP), TypedWrapperFactory.of(new SellItemFactory(Items.CHAINMAIL_CHESTPLATE, 13, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.SWAMP_ARMORER_CHESTPLATE_4), VillagerType.SWAMP), TypedWrapperFactory.of(new ProcessItemFactory((ItemConvertible)Items.DIAMOND_BOOTS, 1, 4, Items.DIAMOND_LEGGINGS, 1, 3, 15, 0.05f), VillagerType.TAIGA), TypedWrapperFactory.of(new ProcessItemFactory((ItemConvertible)Items.DIAMOND_LEGGINGS, 1, 4, Items.DIAMOND_CHESTPLATE, 1, 3, 15, 0.05f), VillagerType.TAIGA), TypedWrapperFactory.of(new ProcessItemFactory((ItemConvertible)Items.DIAMOND_HELMET, 1, 4, Items.DIAMOND_BOOTS, 1, 3, 15, 0.05f), VillagerType.TAIGA), TypedWrapperFactory.of(new ProcessItemFactory((ItemConvertible)Items.DIAMOND_CHESTPLATE, 1, 2, Items.DIAMOND_HELMET, 1, 3, 15, 0.05f), VillagerType.TAIGA)}).put(5, new Factory[]{TypedWrapperFactory.of(new ProcessItemFactory(Items.DIAMOND, 4, 16, Items.DIAMOND_CHESTPLATE, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.DESERT_ARMORER_CHESTPLATE_5), VillagerType.DESERT), TypedWrapperFactory.of(new ProcessItemFactory(Items.DIAMOND, 3, 16, Items.DIAMOND_LEGGINGS, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.DESERT_ARMORER_LEGGINGS_5), VillagerType.DESERT), TypedWrapperFactory.of(new ProcessItemFactory(Items.DIAMOND, 3, 16, Items.DIAMOND_LEGGINGS, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.PLAINS_ARMORER_LEGGINGS_5), VillagerType.PLAINS), TypedWrapperFactory.of(new ProcessItemFactory(Items.DIAMOND, 2, 12, Items.DIAMOND_BOOTS, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.PLAINS_ARMORER_BOOTS_5), VillagerType.PLAINS), TypedWrapperFactory.of(new ProcessItemFactory(Items.DIAMOND, 2, 6, Items.DIAMOND_HELMET, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.SAVANNA_ARMORER_HELMET_5), VillagerType.SAVANNA), TypedWrapperFactory.of(new ProcessItemFactory(Items.DIAMOND, 3, 8, Items.DIAMOND_CHESTPLATE, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.SAVANNA_ARMORER_CHESTPLATE_5), VillagerType.SAVANNA), TypedWrapperFactory.of(new ProcessItemFactory(Items.DIAMOND, 2, 12, Items.DIAMOND_BOOTS, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.SNOW_ARMORER_BOOTS_5), VillagerType.SNOW), TypedWrapperFactory.of(new ProcessItemFactory(Items.DIAMOND, 3, 12, Items.DIAMOND_HELMET, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.SNOW_ARMORER_HELMET_5), VillagerType.SNOW), TypedWrapperFactory.of(new SellItemFactory(Items.CHAINMAIL_HELMET, 9, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.JUNGLE_ARMORER_HELMET_5), VillagerType.JUNGLE), TypedWrapperFactory.of(new SellItemFactory(Items.CHAINMAIL_BOOTS, 8, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.JUNGLE_ARMORER_BOOTS_5), VillagerType.JUNGLE), TypedWrapperFactory.of(new SellItemFactory(Items.CHAINMAIL_HELMET, 9, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.SWAMP_ARMORER_HELMET_5), VillagerType.SWAMP), TypedWrapperFactory.of(new SellItemFactory(Items.CHAINMAIL_BOOTS, 8, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.SWAMP_ARMORER_BOOTS_5), VillagerType.SWAMP), TypedWrapperFactory.of(new ProcessItemFactory(Items.DIAMOND, 4, 18, Items.DIAMOND_CHESTPLATE, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TAIGA_ARMORER_CHESTPLATE_5), VillagerType.TAIGA), TypedWrapperFactory.of(new ProcessItemFactory(Items.DIAMOND, 3, 18, Items.DIAMOND_LEGGINGS, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TAIGA_ARMORER_LEGGINGS_5), VillagerType.TAIGA), TypedWrapperFactory.of(new BuyItemFactory(Items.DIAMOND_BLOCK, 1, 12, 30, 42), VillagerType.TAIGA), TypedWrapperFactory.of(new BuyItemFactory(Items.IRON_BLOCK, 1, 12, 30, 4), VillagerType.DESERT, VillagerType.JUNGLE, VillagerType.PLAINS, VillagerType.SAVANNA, VillagerType.SNOW, VillagerType.SWAMP)}).build()), VillagerProfession.CARTOGRAPHER, TradeOffers.copyToFastUtilMap(ImmutableMap.of(1, new Factory[]{new BuyItemFactory(Items.PAPER, 24, 16, 2), new SellItemFactory(Items.MAP, 7, 1, 1)}, 2, new Factory[]{new BuyItemFactory(Items.GLASS_PANE, 11, 16, 10), new TypedWrapperFactory(ImmutableMap.builder().put(VillagerType.DESERT, SELL_SAVANNA_VILLAGE_MAP_TRADE).put(VillagerType.SAVANNA, SELL_PLAINS_VILLAGE_MAP_TRADE).put(VillagerType.PLAINS, SELL_TAIGA_VILLAGE_MAP_TRADE).put(VillagerType.TAIGA, SELL_SNOWY_VILLAGE_MAP_TRADE).put(VillagerType.SNOW, SELL_PLAINS_VILLAGE_MAP_TRADE).put(VillagerType.JUNGLE, SELL_SAVANNA_VILLAGE_MAP_TRADE).put(VillagerType.SWAMP, SELL_SNOWY_VILLAGE_MAP_TRADE).build()), new TypedWrapperFactory(ImmutableMap.builder().put(VillagerType.DESERT, SELL_PLAINS_VILLAGE_MAP_TRADE).put(VillagerType.SAVANNA, SELL_DESERT_VILLAGE_MAP_TRADE).put(VillagerType.PLAINS, SELL_SAVANNA_VILLAGE_MAP_TRADE).put(VillagerType.TAIGA, SELL_PLAINS_VILLAGE_MAP_TRADE).put(VillagerType.SNOW, SELL_TAIGA_VILLAGE_MAP_TRADE).put(VillagerType.JUNGLE, SELL_DESERT_VILLAGE_MAP_TRADE).put(VillagerType.SWAMP, SELL_TAIGA_VILLAGE_MAP_TRADE).build()), new TypedWrapperFactory(ImmutableMap.builder().put(VillagerType.DESERT, SELL_JUNGLE_TEMPLE_MAP_TRADE).put(VillagerType.SAVANNA, SELL_JUNGLE_TEMPLE_MAP_TRADE).put(VillagerType.PLAINS, (SellMapFactory)((Object)new EmptyFactory())).put(VillagerType.TAIGA, SELL_SWAMP_HUT_MAP_TRADE).put(VillagerType.SNOW, SELL_SWAMP_HUT_MAP_TRADE).put(VillagerType.JUNGLE, SELL_SWAMP_HUT_MAP_TRADE).put(VillagerType.SWAMP, SELL_JUNGLE_TEMPLE_MAP_TRADE).build())}, 3, new Factory[]{new BuyItemFactory(Items.COMPASS, 1, 12, 20), new SellMapFactory(13, StructureTags.ON_OCEAN_EXPLORER_MAPS, "filled_map.monument", MapDecorationTypes.MONUMENT, 12, 10), new SellMapFactory(12, StructureTags.ON_TRIAL_CHAMBERS_MAPS, "filled_map.trial_chambers", MapDecorationTypes.TRIAL_CHAMBERS, 12, 10)}, 4, new Factory[]{new SellItemFactory(Items.ITEM_FRAME, 7, 1, 15), new SellItemFactory(Items.WHITE_BANNER, 3, 1, 15), new SellItemFactory(Items.BLUE_BANNER, 3, 1, 15), new SellItemFactory(Items.LIGHT_BLUE_BANNER, 3, 1, 15), new SellItemFactory(Items.RED_BANNER, 3, 1, 15), new SellItemFactory(Items.PINK_BANNER, 3, 1, 15), new SellItemFactory(Items.GREEN_BANNER, 3, 1, 15), new SellItemFactory(Items.LIME_BANNER, 3, 1, 15), new SellItemFactory(Items.GRAY_BANNER, 3, 1, 15), new SellItemFactory(Items.BLACK_BANNER, 3, 1, 15), new SellItemFactory(Items.PURPLE_BANNER, 3, 1, 15), new SellItemFactory(Items.MAGENTA_BANNER, 3, 1, 15), new SellItemFactory(Items.CYAN_BANNER, 3, 1, 15), new SellItemFactory(Items.BROWN_BANNER, 3, 1, 15), new SellItemFactory(Items.YELLOW_BANNER, 3, 1, 15), new SellItemFactory(Items.ORANGE_BANNER, 3, 1, 15), new SellItemFactory(Items.LIGHT_GRAY_BANNER, 3, 1, 15)}, 5, new Factory[]{new SellItemFactory(Items.GLOBE_BANNER_PATTERN, 8, 1, 30), new SellMapFactory(14, StructureTags.ON_WOODLAND_EXPLORER_MAPS, "filled_map.mansion", MapDecorationTypes.MANSION, 1, 30)})));
    public static final List<Pair<Factory[], Integer>> REBALANCED_WANDERING_TRADER_TRADES = ((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)ImmutableList.builder().add(Pair.of(new Factory[]{new BuyItemFactory(TradeOffers.createPotion(Potions.WATER), 1, 1, 1), new BuyItemFactory(Items.WATER_BUCKET, 1, 1, 1, 2), new BuyItemFactory(Items.MILK_BUCKET, 1, 1, 1, 2), new BuyItemFactory(Items.FERMENTED_SPIDER_EYE, 1, 1, 1, 3), new BuyItemFactory(Items.BAKED_POTATO, 4, 1, 1), new BuyItemFactory(Items.HAY_BLOCK, 1, 1, 1)}, 2))).add(Pair.of(new Factory[]{new SellItemFactory(Items.PACKED_ICE, 1, 1, 6, 1), new SellItemFactory(Items.BLUE_ICE, 6, 1, 6, 1), new SellItemFactory(Items.GUNPOWDER, 1, 4, 2, 1), new SellItemFactory(Items.PODZOL, 3, 3, 6, 1), new SellItemFactory(Blocks.ACACIA_LOG, 1, 8, 4, 1), new SellItemFactory(Blocks.BIRCH_LOG, 1, 8, 4, 1), new SellItemFactory(Blocks.DARK_OAK_LOG, 1, 8, 4, 1), new SellItemFactory(Blocks.JUNGLE_LOG, 1, 8, 4, 1), new SellItemFactory(Blocks.OAK_LOG, 1, 8, 4, 1), new SellItemFactory(Blocks.SPRUCE_LOG, 1, 8, 4, 1), new SellItemFactory(Blocks.CHERRY_LOG, 1, 8, 4, 1), new SellEnchantedToolFactory(Items.IRON_PICKAXE, 1, 1, 1, 0.2f), new SellItemFactory(TradeOffers.createPotionStack(Potions.LONG_INVISIBILITY), 5, 1, 1, 1)}, 2))).add(Pair.of(new Factory[]{new SellItemFactory(Items.TROPICAL_FISH_BUCKET, 3, 1, 4, 1), new SellItemFactory(Items.PUFFERFISH_BUCKET, 3, 1, 4, 1), new SellItemFactory(Items.SEA_PICKLE, 2, 1, 5, 1), new SellItemFactory(Items.SLIME_BALL, 4, 1, 5, 1), new SellItemFactory(Items.GLOWSTONE, 2, 1, 5, 1), new SellItemFactory(Items.NAUTILUS_SHELL, 5, 1, 5, 1), new SellItemFactory(Items.FERN, 1, 1, 12, 1), new SellItemFactory(Items.SUGAR_CANE, 1, 1, 8, 1), new SellItemFactory(Items.PUMPKIN, 1, 1, 4, 1), new SellItemFactory(Items.KELP, 3, 1, 12, 1), new SellItemFactory(Items.CACTUS, 3, 1, 8, 1), new SellItemFactory(Items.DANDELION, 1, 1, 12, 1), new SellItemFactory(Items.POPPY, 1, 1, 12, 1), new SellItemFactory(Items.BLUE_ORCHID, 1, 1, 8, 1), new SellItemFactory(Items.ALLIUM, 1, 1, 12, 1), new SellItemFactory(Items.AZURE_BLUET, 1, 1, 12, 1), new SellItemFactory(Items.RED_TULIP, 1, 1, 12, 1), new SellItemFactory(Items.ORANGE_TULIP, 1, 1, 12, 1), new SellItemFactory(Items.WHITE_TULIP, 1, 1, 12, 1), new SellItemFactory(Items.PINK_TULIP, 1, 1, 12, 1), new SellItemFactory(Items.OXEYE_DAISY, 1, 1, 12, 1), new SellItemFactory(Items.CORNFLOWER, 1, 1, 12, 1), new SellItemFactory(Items.LILY_OF_THE_VALLEY, 1, 1, 7, 1), new SellItemFactory(Items.WHEAT_SEEDS, 1, 1, 12, 1), new SellItemFactory(Items.BEETROOT_SEEDS, 1, 1, 12, 1), new SellItemFactory(Items.PUMPKIN_SEEDS, 1, 1, 12, 1), new SellItemFactory(Items.MELON_SEEDS, 1, 1, 12, 1), new SellItemFactory(Items.ACACIA_SAPLING, 5, 1, 8, 1), new SellItemFactory(Items.BIRCH_SAPLING, 5, 1, 8, 1), new SellItemFactory(Items.DARK_OAK_SAPLING, 5, 1, 8, 1), new SellItemFactory(Items.JUNGLE_SAPLING, 5, 1, 8, 1), new SellItemFactory(Items.OAK_SAPLING, 5, 1, 8, 1), new SellItemFactory(Items.SPRUCE_SAPLING, 5, 1, 8, 1), new SellItemFactory(Items.CHERRY_SAPLING, 5, 1, 8, 1), new SellItemFactory(Items.MANGROVE_PROPAGULE, 5, 1, 8, 1), new SellItemFactory(Items.RED_DYE, 1, 3, 12, 1), new SellItemFactory(Items.WHITE_DYE, 1, 3, 12, 1), new SellItemFactory(Items.BLUE_DYE, 1, 3, 12, 1), new SellItemFactory(Items.PINK_DYE, 1, 3, 12, 1), new SellItemFactory(Items.BLACK_DYE, 1, 3, 12, 1), new SellItemFactory(Items.GREEN_DYE, 1, 3, 12, 1), new SellItemFactory(Items.LIGHT_GRAY_DYE, 1, 3, 12, 1), new SellItemFactory(Items.MAGENTA_DYE, 1, 3, 12, 1), new SellItemFactory(Items.YELLOW_DYE, 1, 3, 12, 1), new SellItemFactory(Items.GRAY_DYE, 1, 3, 12, 1), new SellItemFactory(Items.PURPLE_DYE, 1, 3, 12, 1), new SellItemFactory(Items.LIGHT_BLUE_DYE, 1, 3, 12, 1), new SellItemFactory(Items.LIME_DYE, 1, 3, 12, 1), new SellItemFactory(Items.ORANGE_DYE, 1, 3, 12, 1), new SellItemFactory(Items.BROWN_DYE, 1, 3, 12, 1), new SellItemFactory(Items.CYAN_DYE, 1, 3, 12, 1), new SellItemFactory(Items.BRAIN_CORAL_BLOCK, 3, 1, 8, 1), new SellItemFactory(Items.BUBBLE_CORAL_BLOCK, 3, 1, 8, 1), new SellItemFactory(Items.FIRE_CORAL_BLOCK, 3, 1, 8, 1), new SellItemFactory(Items.HORN_CORAL_BLOCK, 3, 1, 8, 1), new SellItemFactory(Items.TUBE_CORAL_BLOCK, 3, 1, 8, 1), new SellItemFactory(Items.VINE, 1, 3, 4, 1), new SellItemFactory(Items.BROWN_MUSHROOM, 1, 3, 4, 1), new SellItemFactory(Items.RED_MUSHROOM, 1, 3, 4, 1), new SellItemFactory(Items.LILY_PAD, 1, 5, 2, 1), new SellItemFactory(Items.SMALL_DRIPLEAF, 1, 2, 5, 1), new SellItemFactory(Items.SAND, 1, 8, 8, 1), new SellItemFactory(Items.RED_SAND, 1, 4, 6, 1), new SellItemFactory(Items.POINTED_DRIPSTONE, 1, 2, 5, 1), new SellItemFactory(Items.ROOTED_DIRT, 1, 2, 5, 1), new SellItemFactory(Items.MOSS_BLOCK, 1, 2, 5, 1)}, 5))).build();

    private static Factory createLibrarianTradeFactory(int experience) {
        return new TypedWrapperFactory(ImmutableMap.builder().put(VillagerType.DESERT, new EnchantBookFactory(experience, EnchantmentTags.DESERT_COMMON_TRADE)).put(VillagerType.JUNGLE, new EnchantBookFactory(experience, EnchantmentTags.JUNGLE_COMMON_TRADE)).put(VillagerType.PLAINS, new EnchantBookFactory(experience, EnchantmentTags.PLAINS_COMMON_TRADE)).put(VillagerType.SAVANNA, new EnchantBookFactory(experience, EnchantmentTags.SAVANNA_COMMON_TRADE)).put(VillagerType.SNOW, new EnchantBookFactory(experience, EnchantmentTags.SNOW_COMMON_TRADE)).put(VillagerType.SWAMP, new EnchantBookFactory(experience, EnchantmentTags.SWAMP_COMMON_TRADE)).put(VillagerType.TAIGA, new EnchantBookFactory(experience, EnchantmentTags.TAIGA_COMMON_TRADE)).build());
    }

    private static Factory createMasterLibrarianTradeFactory() {
        return new TypedWrapperFactory(ImmutableMap.builder().put(VillagerType.DESERT, new EnchantBookFactory(30, 3, 3, EnchantmentTags.DESERT_SPECIAL_TRADE)).put(VillagerType.JUNGLE, new EnchantBookFactory(30, 2, 2, EnchantmentTags.JUNGLE_SPECIAL_TRADE)).put(VillagerType.PLAINS, new EnchantBookFactory(30, 3, 3, EnchantmentTags.PLAINS_SPECIAL_TRADE)).put(VillagerType.SAVANNA, new EnchantBookFactory(30, 3, 3, EnchantmentTags.SAVANNA_SPECIAL_TRADE)).put(VillagerType.SNOW, new EnchantBookFactory(30, EnchantmentTags.SNOW_SPECIAL_TRADE)).put(VillagerType.SWAMP, new EnchantBookFactory(30, EnchantmentTags.SWAMP_SPECIAL_TRADE)).put(VillagerType.TAIGA, new EnchantBookFactory(30, 2, 2, EnchantmentTags.TAIGA_SPECIAL_TRADE)).build());
    }

    private static Int2ObjectMap<Factory[]> copyToFastUtilMap(ImmutableMap<Integer, Factory[]> map) {
        return new Int2ObjectOpenHashMap<Factory[]>(map);
    }

    private static TradedItem createPotion(RegistryEntry<Potion> potion) {
        return new TradedItem(Items.POTION).withComponents(builder -> builder.add(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(potion)));
    }

    private static ItemStack createPotionStack(RegistryEntry<Potion> potion) {
        return PotionContentsComponent.createStack(Items.POTION, potion);
    }

    public record TypedWrapperFactory(Map<VillagerType, Factory> typeToFactory) implements Factory
    {
        public static TypedWrapperFactory of(Factory factory, VillagerType ... types) {
            return new TypedWrapperFactory(Arrays.stream(types).collect(Collectors.toMap(type -> type, type -> factory)));
        }

        @Override
        @Nullable
        public TradeOffer create(Entity entity, Random random) {
            if (entity instanceof VillagerDataContainer) {
                VillagerDataContainer villagerDataContainer = (VillagerDataContainer)((Object)entity);
                VillagerType villagerType = villagerDataContainer.getVillagerData().getType();
                Factory factory = this.typeToFactory.get(villagerType);
                if (factory == null) {
                    return null;
                }
                return factory.create(entity, random);
            }
            return null;
        }
    }

    public static class EnchantBookFactory
    implements Factory {
        private final int experience;
        private final TagKey<Enchantment> possibleEnchantments;
        private final int minLevel;
        private final int maxLevel;

        public EnchantBookFactory(int experience, TagKey<Enchantment> possibleEnchantments) {
            this(experience, 0, Integer.MAX_VALUE, possibleEnchantments);
        }

        public EnchantBookFactory(int experience, int minLevel, int maxLevel, TagKey<Enchantment> possibleEnchantments) {
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.experience = experience;
            this.possibleEnchantments = possibleEnchantments;
        }

        @Override
        public TradeOffer create(Entity entity, Random random) {
            int l;
            ItemStack itemStack;
            Optional<RegistryEntry<Enchantment>> optional = entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getRandomEntry(this.possibleEnchantments, random);
            if (!optional.isEmpty()) {
                RegistryEntry<Enchantment> registryEntry = optional.get();
                Enchantment enchantment = registryEntry.value();
                int i = Math.max(enchantment.getMinLevel(), this.minLevel);
                int j = Math.min(enchantment.getMaxLevel(), this.maxLevel);
                int k = MathHelper.nextInt(random, i, j);
                itemStack = EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(registryEntry, k));
                l = 2 + random.nextInt(5 + k * 10) + 3 * k;
                if (registryEntry.isIn(EnchantmentTags.DOUBLE_TRADE_PRICE)) {
                    l *= 2;
                }
                if (l > 64) {
                    l = 64;
                }
            } else {
                l = 1;
                itemStack = new ItemStack(Items.BOOK);
            }
            return new TradeOffer(new TradedItem(Items.EMERALD, l), Optional.of(new TradedItem(Items.BOOK)), itemStack, 12, this.experience, 0.2f);
        }
    }

    public static interface Factory {
        @Nullable
        public TradeOffer create(Entity var1, Random var2);
    }

    public static class BuyItemFactory
    implements Factory {
        private final TradedItem stack;
        private final int maxUses;
        private final int experience;
        private final int price;
        private final float multiplier;

        public BuyItemFactory(ItemConvertible item, int count, int maxUses, int experience) {
            this(item, count, maxUses, experience, 1);
        }

        public BuyItemFactory(ItemConvertible item, int count, int maxUses, int experience, int price) {
            this(new TradedItem(item.asItem(), count), maxUses, experience, price);
        }

        public BuyItemFactory(TradedItem stack, int maxUses, int experience, int price) {
            this.stack = stack;
            this.maxUses = maxUses;
            this.experience = experience;
            this.price = price;
            this.multiplier = 0.05f;
        }

        @Override
        public TradeOffer create(Entity entity, Random random) {
            return new TradeOffer(this.stack, new ItemStack(Items.EMERALD, this.price), this.maxUses, this.experience, this.multiplier);
        }
    }

    public static class SellItemFactory
    implements Factory {
        private final ItemStack sell;
        private final int price;
        private final int maxUses;
        private final int experience;
        private final float multiplier;
        private final Optional<RegistryKey<EnchantmentProvider>> enchantmentProviderKey;

        public SellItemFactory(Block block, int price, int count, int maxUses, int experience) {
            this(new ItemStack(block), price, count, maxUses, experience);
        }

        public SellItemFactory(Item item, int price, int count, int experience) {
            this(new ItemStack(item), price, count, 12, experience);
        }

        public SellItemFactory(Item item, int price, int count, int maxUses, int experience) {
            this(new ItemStack(item), price, count, maxUses, experience);
        }

        public SellItemFactory(ItemStack stack, int price, int count, int maxUses, int experience) {
            this(stack, price, count, maxUses, experience, 0.05f);
        }

        public SellItemFactory(Item item, int price, int count, int maxUses, int experience, float multiplier) {
            this(new ItemStack(item), price, count, maxUses, experience, multiplier);
        }

        public SellItemFactory(Item item, int price, int count, int maxUses, int experience, float multiplier, RegistryKey<EnchantmentProvider> enchantmentProviderKey) {
            this(new ItemStack(item), price, count, maxUses, experience, multiplier, Optional.of(enchantmentProviderKey));
        }

        public SellItemFactory(ItemStack stack, int price, int count, int maxUses, int experience, float multiplier) {
            this(stack, price, count, maxUses, experience, multiplier, Optional.empty());
        }

        public SellItemFactory(ItemStack sell, int price, int count, int maxUses, int experience, float multiplier, Optional<RegistryKey<EnchantmentProvider>> enchantmentProviderKey) {
            this.sell = sell;
            this.price = price;
            this.sell.setCount(count);
            this.maxUses = maxUses;
            this.experience = experience;
            this.multiplier = multiplier;
            this.enchantmentProviderKey = enchantmentProviderKey;
        }

        @Override
        public TradeOffer create(Entity entity, Random random) {
            ItemStack itemStack = this.sell.copy();
            World world = entity.getWorld();
            this.enchantmentProviderKey.ifPresent(key -> EnchantmentHelper.applyEnchantmentProvider(itemStack, world.getRegistryManager(), key, world.getLocalDifficulty(entity.getBlockPos()), random));
            return new TradeOffer(new TradedItem(Items.EMERALD, this.price), itemStack, this.maxUses, this.experience, this.multiplier);
        }
    }

    public static class SellSuspiciousStewFactory
    implements Factory {
        private final SuspiciousStewEffectsComponent stewEffects;
        private final int experience;
        private final float multiplier;

        public SellSuspiciousStewFactory(RegistryEntry<StatusEffect> effect, int duration, int experience) {
            this(new SuspiciousStewEffectsComponent(List.of(new SuspiciousStewEffectsComponent.StewEffect(effect, duration))), experience, 0.05f);
        }

        public SellSuspiciousStewFactory(SuspiciousStewEffectsComponent stewEffects, int experience, float multiplier) {
            this.stewEffects = stewEffects;
            this.experience = experience;
            this.multiplier = multiplier;
        }

        @Override
        @Nullable
        public TradeOffer create(Entity entity, Random random) {
            ItemStack itemStack = new ItemStack(Items.SUSPICIOUS_STEW, 1);
            itemStack.set(DataComponentTypes.SUSPICIOUS_STEW_EFFECTS, this.stewEffects);
            return new TradeOffer(new TradedItem(Items.EMERALD), itemStack, 12, this.experience, this.multiplier);
        }
    }

    public static class ProcessItemFactory
    implements Factory {
        private final TradedItem toBeProcessed;
        private final int price;
        private final ItemStack processed;
        private final int maxUses;
        private final int experience;
        private final float multiplier;
        private final Optional<RegistryKey<EnchantmentProvider>> enchantmentProviderKey;

        public ProcessItemFactory(ItemConvertible item, int count, int price, Item processed, int processedCount, int maxUses, int experience, float multiplier) {
            this(item, count, price, new ItemStack(processed), processedCount, maxUses, experience, multiplier);
        }

        private ProcessItemFactory(ItemConvertible item, int count, int price, ItemStack processed, int processedCount, int maxUses, int experience, float multiplier) {
            this(new TradedItem(item, count), price, processed.copyWithCount(processedCount), maxUses, experience, multiplier, Optional.empty());
        }

        ProcessItemFactory(ItemConvertible item, int count, int price, ItemConvertible processed, int processedCount, int maxUses, int experience, float multiplier, RegistryKey<EnchantmentProvider> enchantmentProviderKey) {
            this(new TradedItem(item, count), price, new ItemStack(processed, processedCount), maxUses, experience, multiplier, Optional.of(enchantmentProviderKey));
        }

        public ProcessItemFactory(TradedItem toBeProcessed, int count, ItemStack processed, int maxUses, int processedCount, float multiplier, Optional<RegistryKey<EnchantmentProvider>> enchantmentProviderKey) {
            this.toBeProcessed = toBeProcessed;
            this.price = count;
            this.processed = processed;
            this.maxUses = maxUses;
            this.experience = processedCount;
            this.multiplier = multiplier;
            this.enchantmentProviderKey = enchantmentProviderKey;
        }

        @Override
        @Nullable
        public TradeOffer create(Entity entity, Random random) {
            ItemStack itemStack = this.processed.copy();
            World world = entity.getWorld();
            this.enchantmentProviderKey.ifPresent(key -> EnchantmentHelper.applyEnchantmentProvider(itemStack, world.getRegistryManager(), key, world.getLocalDifficulty(entity.getBlockPos()), random));
            return new TradeOffer(new TradedItem(Items.EMERALD, this.price), Optional.of(this.toBeProcessed), itemStack, 0, this.maxUses, this.experience, this.multiplier);
        }
    }

    public static class SellEnchantedToolFactory
    implements Factory {
        private final ItemStack tool;
        private final int basePrice;
        private final int maxUses;
        private final int experience;
        private final float multiplier;

        public SellEnchantedToolFactory(Item item, int basePrice, int maxUses, int experience) {
            this(item, basePrice, maxUses, experience, 0.05f);
        }

        public SellEnchantedToolFactory(Item item, int basePrice, int maxUses, int experience, float multiplier) {
            this.tool = new ItemStack(item);
            this.basePrice = basePrice;
            this.maxUses = maxUses;
            this.experience = experience;
            this.multiplier = multiplier;
        }

        @Override
        public TradeOffer create(Entity entity, Random random) {
            int i = 5 + random.nextInt(15);
            DynamicRegistryManager dynamicRegistryManager = entity.getWorld().getRegistryManager();
            Optional<RegistryEntryList.Named<Enchantment>> optional = dynamicRegistryManager.get(RegistryKeys.ENCHANTMENT).getEntryList(EnchantmentTags.ON_TRADED_EQUIPMENT);
            ItemStack itemStack = EnchantmentHelper.enchant(random, new ItemStack(this.tool.getItem()), i, dynamicRegistryManager, optional);
            int j = Math.min(this.basePrice + i, 64);
            TradedItem tradedItem = new TradedItem(Items.EMERALD, j);
            return new TradeOffer(tradedItem, itemStack, this.maxUses, this.experience, this.multiplier);
        }
    }

    public static class TypeAwareBuyForOneEmeraldFactory
    implements Factory {
        private final Map<VillagerType, Item> map;
        private final int count;
        private final int maxUses;
        private final int experience;

        public TypeAwareBuyForOneEmeraldFactory(int count, int maxUses, int experience, Map<VillagerType, Item> map) {
            Registries.VILLAGER_TYPE.stream().filter(villagerType -> !map.containsKey(villagerType)).findAny().ifPresent(villagerType -> {
                throw new IllegalStateException("Missing trade for villager type: " + String.valueOf(Registries.VILLAGER_TYPE.getId((VillagerType)villagerType)));
            });
            this.map = map;
            this.count = count;
            this.maxUses = maxUses;
            this.experience = experience;
        }

        @Override
        @Nullable
        public TradeOffer create(Entity entity, Random random) {
            if (entity instanceof VillagerDataContainer) {
                VillagerDataContainer villagerDataContainer = (VillagerDataContainer)((Object)entity);
                TradedItem tradedItem = new TradedItem(this.map.get(villagerDataContainer.getVillagerData().getType()), this.count);
                return new TradeOffer(tradedItem, new ItemStack(Items.EMERALD), this.maxUses, this.experience, 0.05f);
            }
            return null;
        }
    }

    public static class SellPotionHoldingItemFactory
    implements Factory {
        private final ItemStack sell;
        private final int sellCount;
        private final int price;
        private final int maxUses;
        private final int experience;
        private final Item secondBuy;
        private final int secondCount;
        private final float priceMultiplier;

        public SellPotionHoldingItemFactory(Item arrow, int secondCount, Item tippedArrow, int sellCount, int price, int maxUses, int experience) {
            this.sell = new ItemStack(tippedArrow);
            this.price = price;
            this.maxUses = maxUses;
            this.experience = experience;
            this.secondBuy = arrow;
            this.secondCount = secondCount;
            this.sellCount = sellCount;
            this.priceMultiplier = 0.05f;
        }

        @Override
        public TradeOffer create(Entity entity, Random random) {
            TradedItem tradedItem = new TradedItem(Items.EMERALD, this.price);
            List list = Registries.POTION.streamEntries().filter(entry -> !((Potion)entry.value()).getEffects().isEmpty() && entity.getWorld().getBrewingRecipeRegistry().isBrewable((RegistryEntry<Potion>)entry)).collect(Collectors.toList());
            RegistryEntry registryEntry = (RegistryEntry)Util.getRandom(list, random);
            ItemStack itemStack = new ItemStack(this.sell.getItem(), this.sellCount);
            itemStack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(registryEntry));
            return new TradeOffer(tradedItem, Optional.of(new TradedItem(this.secondBuy, this.secondCount)), itemStack, this.maxUses, this.experience, this.priceMultiplier);
        }
    }

    public static class SellMapFactory
    implements Factory {
        private final int price;
        private final TagKey<Structure> structure;
        private final String nameKey;
        private final RegistryEntry<MapDecorationType> decoration;
        private final int maxUses;
        private final int experience;

        public SellMapFactory(int price, TagKey<Structure> structure, String nameKey, RegistryEntry<MapDecorationType> decoration, int maxUses, int experience) {
            this.price = price;
            this.structure = structure;
            this.nameKey = nameKey;
            this.decoration = decoration;
            this.maxUses = maxUses;
            this.experience = experience;
        }

        @Override
        @Nullable
        public TradeOffer create(Entity entity, Random random) {
            if (!(entity.getWorld() instanceof ServerWorld)) {
                return null;
            }
            ServerWorld serverWorld = (ServerWorld)entity.getWorld();
            BlockPos blockPos = serverWorld.locateStructure(this.structure, entity.getBlockPos(), 100, true);
            if (blockPos != null) {
                ItemStack itemStack = FilledMapItem.createMap(serverWorld, blockPos.getX(), blockPos.getZ(), (byte)2, true, true);
                FilledMapItem.fillExplorationMap(serverWorld, itemStack);
                MapState.addDecorationsNbt(itemStack, blockPos, "+", this.decoration);
                itemStack.set(DataComponentTypes.ITEM_NAME, Text.translatable(this.nameKey));
                return new TradeOffer(new TradedItem(Items.EMERALD, this.price), Optional.of(new TradedItem(Items.COMPASS)), itemStack, this.maxUses, this.experience, 0.2f);
            }
            return null;
        }
    }

    public static class SellDyedArmorFactory
    implements Factory {
        private final Item sell;
        private final int price;
        private final int maxUses;
        private final int experience;

        public SellDyedArmorFactory(Item item, int price) {
            this(item, price, 12, 1);
        }

        public SellDyedArmorFactory(Item item, int price, int maxUses, int experience) {
            this.sell = item;
            this.price = price;
            this.maxUses = maxUses;
            this.experience = experience;
        }

        @Override
        public TradeOffer create(Entity entity, Random random) {
            TradedItem tradedItem = new TradedItem(Items.EMERALD, this.price);
            ItemStack itemStack = new ItemStack(this.sell);
            if (itemStack.isIn(ItemTags.DYEABLE)) {
                ArrayList<DyeItem> list = Lists.newArrayList();
                list.add(SellDyedArmorFactory.getDye(random));
                if (random.nextFloat() > 0.7f) {
                    list.add(SellDyedArmorFactory.getDye(random));
                }
                if (random.nextFloat() > 0.8f) {
                    list.add(SellDyedArmorFactory.getDye(random));
                }
                itemStack = DyedColorComponent.setColor(itemStack, list);
            }
            return new TradeOffer(tradedItem, itemStack, this.maxUses, this.experience, 0.2f);
        }

        private static DyeItem getDye(Random random) {
            return DyeItem.byColor(DyeColor.byId(random.nextInt(16)));
        }
    }

    static class EmptyFactory
    implements Factory {
        EmptyFactory() {
        }

        @Override
        public TradeOffer create(Entity entity, Random random) {
            return null;
        }
    }
}

