/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data.server.loottable.vanilla;

import java.util.function.BiConsumer;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.loottable.LootTableGenerator;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.loot.condition.LocationCheckLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.entry.LootTableEntry;
import net.minecraft.loot.function.EnchantWithLevelsLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.function.SetDamageLootFunction;
import net.minecraft.loot.function.SetPotionLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.potion.Potions;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.FishingHookPredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

public record VanillaFishingLootTableGenerator(RegistryWrapper.WrapperLookup registries) implements LootTableGenerator
{
    @Override
    public void accept(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> lootTableBiConsumer) {
        RegistryWrapper.Impl<Biome> impl = this.registries.getWrapperOrThrow(RegistryKeys.BIOME);
        lootTableBiConsumer.accept(LootTables.FISHING_GAMEPLAY, LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)((LeafEntry.Builder)LootTableEntry.builder(LootTables.FISHING_JUNK_GAMEPLAY).weight(10)).quality(-2)).with((LootPoolEntry.Builder<?>)((LootPoolEntry.Builder)((LeafEntry.Builder)LootTableEntry.builder(LootTables.FISHING_TREASURE_GAMEPLAY).weight(5)).quality(2)).conditionally(EntityPropertiesLootCondition.builder(LootContext.EntityTarget.THIS, EntityPredicate.Builder.create().typeSpecific(FishingHookPredicate.of(true))))).with((LootPoolEntry.Builder<?>)((LeafEntry.Builder)LootTableEntry.builder(LootTables.FISHING_FISH_GAMEPLAY).weight(85)).quality(-1))));
        lootTableBiConsumer.accept(LootTables.FISHING_FISH_GAMEPLAY, VanillaFishingLootTableGenerator.createFishTableBuilder());
        lootTableBiConsumer.accept(LootTables.FISHING_JUNK_GAMEPLAY, LootTable.builder().pool(LootPool.builder().with((LootPoolEntry.Builder<?>)ItemEntry.builder(Blocks.LILY_PAD).weight(17)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.LEATHER_BOOTS).weight(10)).apply(SetDamageLootFunction.builder(UniformLootNumberProvider.create(0.0f, 0.9f))))).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.LEATHER).weight(10)).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.BONE).weight(10)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.POTION).weight(10)).apply(SetPotionLootFunction.builder(Potions.WATER)))).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.STRING).weight(5)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.FISHING_ROD).weight(2)).apply(SetDamageLootFunction.builder(UniformLootNumberProvider.create(0.0f, 0.9f))))).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.BOWL).weight(10)).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.STICK).weight(5)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.INK_SAC).weight(1)).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(10.0f))))).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Blocks.TRIPWIRE_HOOK).weight(10)).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.ROTTEN_FLESH).weight(10)).with((LootPoolEntry.Builder<?>)((LeafEntry.Builder)ItemEntry.builder(Blocks.BAMBOO).conditionally(LocationCheckLootCondition.builder(LocationPredicate.Builder.create().biome(RegistryEntryList.of(impl.getOrThrow(BiomeKeys.JUNGLE), impl.getOrThrow(BiomeKeys.SPARSE_JUNGLE), impl.getOrThrow(BiomeKeys.BAMBOO_JUNGLE)))))).weight(10))));
        lootTableBiConsumer.accept(LootTables.FISHING_TREASURE_GAMEPLAY, LootTable.builder().pool(LootPool.builder().with(ItemEntry.builder(Items.NAME_TAG)).with(ItemEntry.builder(Items.SADDLE)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.BOW).apply(SetDamageLootFunction.builder(UniformLootNumberProvider.create(0.0f, 0.25f)))).apply(EnchantWithLevelsLootFunction.builder(this.registries, ConstantLootNumberProvider.create(30.0f))))).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(Items.FISHING_ROD).apply(SetDamageLootFunction.builder(UniformLootNumberProvider.create(0.0f, 0.25f)))).apply(EnchantWithLevelsLootFunction.builder(this.registries, ConstantLootNumberProvider.create(30.0f))))).with((LootPoolEntry.Builder<?>)((Object)ItemEntry.builder(Items.BOOK).apply(EnchantWithLevelsLootFunction.builder(this.registries, ConstantLootNumberProvider.create(30.0f))))).with(ItemEntry.builder(Items.NAUTILUS_SHELL))));
    }

    public static LootTable.Builder createFishTableBuilder() {
        return LootTable.builder().pool(LootPool.builder().with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.COD).weight(60)).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.SALMON).weight(25)).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.TROPICAL_FISH).weight(2)).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.PUFFERFISH).weight(13)));
    }
}

