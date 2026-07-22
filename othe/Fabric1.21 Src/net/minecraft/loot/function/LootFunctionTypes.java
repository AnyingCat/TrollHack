/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.AndLootFunction;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.CopyComponentsLootFunction;
import net.minecraft.loot.function.CopyNameLootFunction;
import net.minecraft.loot.function.CopyNbtLootFunction;
import net.minecraft.loot.function.CopyStateLootFunction;
import net.minecraft.loot.function.EnchantRandomlyLootFunction;
import net.minecraft.loot.function.EnchantWithLevelsLootFunction;
import net.minecraft.loot.function.EnchantedCountIncreaseLootFunction;
import net.minecraft.loot.function.ExplorationMapLootFunction;
import net.minecraft.loot.function.ExplosionDecayLootFunction;
import net.minecraft.loot.function.FillPlayerHeadLootFunction;
import net.minecraft.loot.function.FilteredLootFunction;
import net.minecraft.loot.function.FurnaceSmeltLootFunction;
import net.minecraft.loot.function.LimitCountLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.ModifyContentsLootFunction;
import net.minecraft.loot.function.ReferenceLootFunction;
import net.minecraft.loot.function.SetAttributesLootFunction;
import net.minecraft.loot.function.SetBannerPatternLootFunction;
import net.minecraft.loot.function.SetBookCoverLootFunction;
import net.minecraft.loot.function.SetComponentsLootFunction;
import net.minecraft.loot.function.SetContentsLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.function.SetCustomDataLootFunction;
import net.minecraft.loot.function.SetCustomModelDataLootFunction;
import net.minecraft.loot.function.SetDamageLootFunction;
import net.minecraft.loot.function.SetEnchantmentsLootFunction;
import net.minecraft.loot.function.SetFireworkExplosionLootFunction;
import net.minecraft.loot.function.SetFireworksLootFunction;
import net.minecraft.loot.function.SetInstrumentLootFunction;
import net.minecraft.loot.function.SetItemLootFunction;
import net.minecraft.loot.function.SetLootTableLootFunction;
import net.minecraft.loot.function.SetLoreLootFunction;
import net.minecraft.loot.function.SetNameLootFunction;
import net.minecraft.loot.function.SetOminousBottleAmplifierLootFunction;
import net.minecraft.loot.function.SetPotionLootFunction;
import net.minecraft.loot.function.SetStewEffectLootFunction;
import net.minecraft.loot.function.SetWritableBookPagesLootFunction;
import net.minecraft.loot.function.SetWrittenBookPagesLootFunction;
import net.minecraft.loot.function.ToggleTooltipsLootFunction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class LootFunctionTypes {
    public static final BiFunction<ItemStack, LootContext, ItemStack> NOOP = (stack, context) -> stack;
    public static final Codec<LootFunction> BASE_CODEC = Registries.LOOT_FUNCTION_TYPE.getCodec().dispatch("function", LootFunction::getType, LootFunctionType::codec);
    public static final Codec<LootFunction> CODEC = Codec.lazyInitialized(() -> Codec.withAlternative(BASE_CODEC, AndLootFunction.INLINE_CODEC));
    public static final Codec<RegistryEntry<LootFunction>> ENTRY_CODEC = RegistryElementCodec.of(RegistryKeys.ITEM_MODIFIER, CODEC);
    public static final LootFunctionType<SetCountLootFunction> SET_COUNT = LootFunctionTypes.register("set_count", SetCountLootFunction.CODEC);
    public static final LootFunctionType<SetItemLootFunction> SET_ITEM = LootFunctionTypes.register("set_item", SetItemLootFunction.CODEC);
    public static final LootFunctionType<EnchantWithLevelsLootFunction> ENCHANT_WITH_LEVELS = LootFunctionTypes.register("enchant_with_levels", EnchantWithLevelsLootFunction.CODEC);
    public static final LootFunctionType<EnchantRandomlyLootFunction> ENCHANT_RANDOMLY = LootFunctionTypes.register("enchant_randomly", EnchantRandomlyLootFunction.CODEC);
    public static final LootFunctionType<SetEnchantmentsLootFunction> SET_ENCHANTMENTS = LootFunctionTypes.register("set_enchantments", SetEnchantmentsLootFunction.CODEC);
    public static final LootFunctionType<SetCustomDataLootFunction> SET_CUSTOM_DATA = LootFunctionTypes.register("set_custom_data", SetCustomDataLootFunction.CODEC);
    public static final LootFunctionType<SetComponentsLootFunction> SET_COMPONENTS = LootFunctionTypes.register("set_components", SetComponentsLootFunction.CODEC);
    public static final LootFunctionType<FurnaceSmeltLootFunction> FURNACE_SMELT = LootFunctionTypes.register("furnace_smelt", FurnaceSmeltLootFunction.CODEC);
    public static final LootFunctionType<EnchantedCountIncreaseLootFunction> ENCHANTED_COUNT_INCREASE = LootFunctionTypes.register("enchanted_count_increase", EnchantedCountIncreaseLootFunction.CODEC);
    public static final LootFunctionType<SetDamageLootFunction> SET_DAMAGE = LootFunctionTypes.register("set_damage", SetDamageLootFunction.CODEC);
    public static final LootFunctionType<SetAttributesLootFunction> SET_ATTRIBUTES = LootFunctionTypes.register("set_attributes", SetAttributesLootFunction.CODEC);
    public static final LootFunctionType<SetNameLootFunction> SET_NAME = LootFunctionTypes.register("set_name", SetNameLootFunction.CODEC);
    public static final LootFunctionType<ExplorationMapLootFunction> EXPLORATION_MAP = LootFunctionTypes.register("exploration_map", ExplorationMapLootFunction.CODEC);
    public static final LootFunctionType<SetStewEffectLootFunction> SET_STEW_EFFECT = LootFunctionTypes.register("set_stew_effect", SetStewEffectLootFunction.CODEC);
    public static final LootFunctionType<CopyNameLootFunction> COPY_NAME = LootFunctionTypes.register("copy_name", CopyNameLootFunction.CODEC);
    public static final LootFunctionType<SetContentsLootFunction> SET_CONTENTS = LootFunctionTypes.register("set_contents", SetContentsLootFunction.CODEC);
    public static final LootFunctionType<ModifyContentsLootFunction> MODIFY_CONTENTS = LootFunctionTypes.register("modify_contents", ModifyContentsLootFunction.CODEC);
    public static final LootFunctionType<FilteredLootFunction> FILTERED = LootFunctionTypes.register("filtered", FilteredLootFunction.CODEC);
    public static final LootFunctionType<LimitCountLootFunction> LIMIT_COUNT = LootFunctionTypes.register("limit_count", LimitCountLootFunction.CODEC);
    public static final LootFunctionType<ApplyBonusLootFunction> APPLY_BONUS = LootFunctionTypes.register("apply_bonus", ApplyBonusLootFunction.CODEC);
    public static final LootFunctionType<SetLootTableLootFunction> SET_LOOT_TABLE = LootFunctionTypes.register("set_loot_table", SetLootTableLootFunction.CODEC);
    public static final LootFunctionType<ExplosionDecayLootFunction> EXPLOSION_DECAY = LootFunctionTypes.register("explosion_decay", ExplosionDecayLootFunction.CODEC);
    public static final LootFunctionType<SetLoreLootFunction> SET_LORE = LootFunctionTypes.register("set_lore", SetLoreLootFunction.CODEC);
    public static final LootFunctionType<FillPlayerHeadLootFunction> FILL_PLAYER_HEAD = LootFunctionTypes.register("fill_player_head", FillPlayerHeadLootFunction.CODEC);
    public static final LootFunctionType<CopyNbtLootFunction> COPY_CUSTOM_DATA = LootFunctionTypes.register("copy_custom_data", CopyNbtLootFunction.CODEC);
    public static final LootFunctionType<CopyStateLootFunction> COPY_STATE = LootFunctionTypes.register("copy_state", CopyStateLootFunction.CODEC);
    public static final LootFunctionType<SetBannerPatternLootFunction> SET_BANNER_PATTERN = LootFunctionTypes.register("set_banner_pattern", SetBannerPatternLootFunction.CODEC);
    public static final LootFunctionType<SetPotionLootFunction> SET_POTION = LootFunctionTypes.register("set_potion", SetPotionLootFunction.CODEC);
    public static final LootFunctionType<SetInstrumentLootFunction> SET_INSTRUMENT = LootFunctionTypes.register("set_instrument", SetInstrumentLootFunction.CODEC);
    public static final LootFunctionType<ReferenceLootFunction> REFERENCE = LootFunctionTypes.register("reference", ReferenceLootFunction.CODEC);
    public static final LootFunctionType<AndLootFunction> SEQUENCE = LootFunctionTypes.register("sequence", AndLootFunction.CODEC);
    public static final LootFunctionType<CopyComponentsLootFunction> COPY_COMPONENTS = LootFunctionTypes.register("copy_components", CopyComponentsLootFunction.CODEC);
    public static final LootFunctionType<SetFireworksLootFunction> SET_FIREWORKS = LootFunctionTypes.register("set_fireworks", SetFireworksLootFunction.CODEC);
    public static final LootFunctionType<SetFireworkExplosionLootFunction> SET_FIREWORK_EXPLOSION = LootFunctionTypes.register("set_firework_explosion", SetFireworkExplosionLootFunction.CODEC);
    public static final LootFunctionType<SetBookCoverLootFunction> SET_BOOK_COVER = LootFunctionTypes.register("set_book_cover", SetBookCoverLootFunction.CODEC);
    public static final LootFunctionType<SetWrittenBookPagesLootFunction> SET_WRITTEN_BOOK_PAGES = LootFunctionTypes.register("set_written_book_pages", SetWrittenBookPagesLootFunction.CODEC);
    public static final LootFunctionType<SetWritableBookPagesLootFunction> SET_WRITABLE_BOOK_PAGES = LootFunctionTypes.register("set_writable_book_pages", SetWritableBookPagesLootFunction.CODEC);
    public static final LootFunctionType<ToggleTooltipsLootFunction> TOGGLE_TOOLTIPS = LootFunctionTypes.register("toggle_tooltips", ToggleTooltipsLootFunction.CODEC);
    public static final LootFunctionType<SetOminousBottleAmplifierLootFunction> SET_OMINOUS_BOTTLE_AMPLIFIER = LootFunctionTypes.register("set_ominous_bottle_amplifier", SetOminousBottleAmplifierLootFunction.CODEC);
    public static final LootFunctionType<SetCustomModelDataLootFunction> SET_CUSTOM_MODEL_DATA = LootFunctionTypes.register("set_custom_model_data", SetCustomModelDataLootFunction.CODEC);

    private static <T extends LootFunction> LootFunctionType<T> register(String id, MapCodec<T> codec) {
        return Registry.register(Registries.LOOT_FUNCTION_TYPE, Identifier.ofVanilla(id), new LootFunctionType<T>(codec));
    }

    public static BiFunction<ItemStack, LootContext, ItemStack> join(List<? extends BiFunction<ItemStack, LootContext, ItemStack>> terms) {
        List<? extends BiFunction<ItemStack, LootContext, ItemStack>> list = List.copyOf(terms);
        return switch (list.size()) {
            case 0 -> NOOP;
            case 1 -> list.get(0);
            case 2 -> {
                BiFunction<ItemStack, LootContext, ItemStack> biFunction = list.get(0);
                BiFunction<ItemStack, LootContext, ItemStack> biFunction2 = list.get(1);
                yield (stack, context) -> (ItemStack)biFunction2.apply((ItemStack)biFunction.apply((ItemStack)stack, (LootContext)context), (LootContext)context);
            }
            default -> (stack, context) -> {
                for (BiFunction biFunction : list) {
                    stack = (ItemStack)biFunction.apply(stack, context);
                }
                return stack;
            };
        };
    }
}

